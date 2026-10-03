package com.karelherink.jdwpanalyzer.proxy

import com.karelherink.jdwpanalyzer.log.PacketLogWriter
import com.karelherink.jdwpanalyzer.model.Analyzer
import com.karelherink.jdwpanalyzer.protocol.Direction
import com.karelherink.jdwpanalyzer.protocol.JdwpPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * @property listenPort port the debugger attaches to; 0 picks a free one (see [ProxyState.Listening]).
 * @property vmHost host of the VM's JDWP socket.
 * @property vmPort port of the VM's JDWP socket.
 * @property requestDelayMillis pause after forwarding each packet from the debugger to the VM.
 * @property responseDelayMillis pause after forwarding each packet from the VM to the debugger.
 */
data class ProxyConfig(
    val listenPort: Int,
    val vmHost: String,
    val vmPort: Int,
    val requestDelayMillis: Long = 0,
    val responseDelayMillis: Long = 0,
) {
    companion object {
        /** Parses `host:port`, or just `port` for localhost. */
        fun parseAddress(address: String): Pair<String, Int> {
            val colon = address.lastIndexOf(':')
            return if (colon < 0) "localhost" to address.trim().toInt()
            else address.substring(0, colon).trim().ifEmpty { "localhost" } to address.substring(colon + 1).trim().toInt()
        }
    }
}

sealed interface ProxyState {
    data object Idle : ProxyState
    data class Listening(val port: Int) : ProxyState
    data class Connecting(val host: String, val port: Int) : ProxyState
    data class Connected(val listenPort: Int, val host: String, val port: Int) : ProxyState
    data class Closed(val reason: String, val failed: Boolean) : ProxyState
}

/**
 * Sits between a debugger and a VM: accepts one debugger connection, connects to the VM, relays the
 * JDWP handshake, then forwards every packet unchanged while handing a copy to the [analyzer].
 */
class JdwpProxy(
    private val config: ProxyConfig,
    private val analyzer: Analyzer,
    private val logWriter: PacketLogWriter? = null,
) {
    private val _state = MutableStateFlow<ProxyState>(ProxyState.Idle)
    val state: StateFlow<ProxyState> = _state.asStateFlow()

    @Volatile private var server: ServerSocket? = null
    @Volatile private var debugger: Socket? = null
    @Volatile private var vm: Socket? = null
    @Volatile private var stopped = false
    private val recordLock = Any()

    fun start() {
        thread(name = "jdwp-proxy", isDaemon = true) { run() }
    }

    fun stop() {
        stopped = true
        closeAll()
        logWriter?.close()
    }

    private fun run() {
        try {
            val server = ServerSocket().also { this.server = it }
            server.reuseAddress = true
            server.bind(InetSocketAddress(config.listenPort))
            val listenPort = server.localPort
            _state.value = ProxyState.Listening(listenPort)
            val debugger = server.accept().also { this.debugger = it }
            server.close()

            _state.value = ProxyState.Connecting(config.vmHost, config.vmPort)
            val vm = Socket(config.vmHost, config.vmPort).also { this.vm = it }
            debugger.tcpNoDelay = true
            vm.tcpNoDelay = true

            val fromDebugger = DataInputStream(BufferedInputStream(debugger.getInputStream()))
            val fromVm = DataInputStream(BufferedInputStream(vm.getInputStream()))
            val toDebugger = BufferedOutputStream(debugger.getOutputStream())
            val toVm = BufferedOutputStream(vm.getOutputStream())

            relayHandshake(fromDebugger, toVm, "debugger")
            relayHandshake(fromVm, toDebugger, "VM")
            _state.value = ProxyState.Connected(listenPort, config.vmHost, config.vmPort)

            val up = thread(name = "jdwp-debugger-to-vm", isDaemon = true) {
                pump(fromDebugger, toVm, Direction.DEBUGGER_TO_VM, config.requestDelayMillis)
            }
            val down = thread(name = "jdwp-vm-to-debugger", isDaemon = true) {
                pump(fromVm, toDebugger, Direction.VM_TO_DEBUGGER, config.responseDelayMillis)
            }
            up.join()
            down.join()
            if (_state.value !is ProxyState.Closed) {
                _state.value = ProxyState.Closed(if (stopped) "Stopped" else "Session ended", failed = false)
            }
        } catch (e: Exception) {
            _state.value = if (stopped) ProxyState.Closed("Stopped", failed = false)
            else ProxyState.Closed(e.message ?: e.toString(), failed = true)
            closeAll()
        } finally {
            logWriter?.close()
        }
    }

    private fun relayHandshake(from: DataInputStream, to: OutputStream, side: String) {
        val handshake = ByteArray(JdwpPacket.HANDSHAKE.size)
        from.readFully(handshake)
        if (!handshake.contentEquals(JdwpPacket.HANDSHAKE)) {
            throw IOException("Unexpected handshake from the $side: ${String(handshake, Charsets.ISO_8859_1)}")
        }
        to.write(handshake)
        to.flush()
    }

    private fun pump(from: DataInputStream, to: OutputStream, direction: Direction, delayMillis: Long) {
        try {
            while (true) {
                val packet = JdwpPacket.read(from) ?: break
                // Record before forwarding: otherwise the VM's reply could reach the analyzer before its command.
                synchronized(recordLock) {
                    runCatching { logWriter?.write(packet) }
                    analyzer.onPacket(packet, direction)
                }
                packet.writeTo(to)
                to.flush()
                if (delayMillis > 0) Thread.sleep(delayMillis)
            }
        } catch (_: IOException) {
            // The other side went away; closing both sockets below ends the other pump too.
        } catch (_: InterruptedException) {
        } finally {
            closeAll()
        }
    }

    private fun closeAll() {
        listOf(server, debugger, vm).forEach { runCatching { it?.close() } }
    }
}
