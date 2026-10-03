package com.karelherink.jdwpanalyzer

import com.karelherink.jdwpanalyzer.decode.JdwpSpec
import com.karelherink.jdwpanalyzer.model.Analyzer
import com.karelherink.jdwpanalyzer.model.Exchange
import com.karelherink.jdwpanalyzer.protocol.JdwpPacket
import com.karelherink.jdwpanalyzer.proxy.JdwpProxy
import com.karelherink.jdwpanalyzer.proxy.ProxyConfig
import com.karelherink.jdwpanalyzer.proxy.ProxyState
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Sends the commands JDI never uses (older variants, HoldEvents, PopFrames, ...) straight to a real VM through
 * the proxy, so their layouts are checked against real replies too.
 */
class RawClientTest {

    @Test
    fun `commands JDI never sends decode cleanly`() {
        val debuggee = ProcessBuilder(
            File(System.getProperty("java.home"), "bin/java").path,
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:0",
            "-version",
        ).redirectErrorStream(true).start()
        val analyzer = Analyzer()
        var proxy: JdwpProxy? = null
        try {
            val banner = debuggee.inputReader().readLine()
            val vmPort = Regex("address: .*?(\\d+)$").find(banner)!!.groupValues[1].toInt()
            proxy = JdwpProxy(ProxyConfig(0, "127.0.0.1", vmPort), analyzer).also { it.start() }
            val port = waitFor { (proxy.state.value as? ProxyState.Listening)?.port }
            Client(port, analyzer).use { c -> exercise(c) }
        } finally {
            proxy?.stop()
            debuggee.destroyForcibly()
        }

        val exchanges = analyzer.exchanges()
        exchanges.filter { it.isError }.forEach { println("Error reply: ${it.name} → ${it.decodedReply?.fields?.first()?.text}") }
        val problems = exchanges.filter { it.hasProblem }
        problems.forEach { println("PROBLEM ${it.name}\n${it.decodedCommand?.root}\n--- reply ---\n${it.decodedReply?.root}") }
        assertTrue(problems.isEmpty())
        println("Commands: " + exchanges.mapNotNull { it.spec?.fullName }.distinct().joinToString())
    }

    private fun exercise(c: Client) {
        c.send(1, 7) // IDSizes
        c.send(1, 1) // Version
        c.send(1, 12) // Capabilities
        c.send(1, 3) // AllClasses
        val threadClass = c.send(1, 2) { string("Ljava/lang/Thread;") }
            .decodedReply!!.root.child("classes")!!.children[1].long("typeID")!!
        for (cmd in listOf(1, 4, 5, 8, 9, 13)) c.send(2, cmd) { id(threadClass) } // Signature, Fields, Methods, NestedTypes, Status, SignatureWithGeneric
        val methods = c.send(2, 5) { id(threadClass) }.decodedReply!!.root.child("methods")!!.children.drop(1)
        val run = methods.first { it.string("name") == "run" }.long("methodID")!!
        c.send(6, 2) { id(threadClass); id(run) } // VariableTable

        val main = c.send(1, 4).decodedReply!!.root.child("threads")!!.children[1].value as Long
        c.send(15, 1) { byte(1); byte(2); int(0) } // EventRequest.Set SINGLE_STEP without modifiers → error
        val frames = c.send(11, 6) { id(main); int(0); int(-1) }.decodedReply!!.root.child("frames")!!.children.drop(1)
        val frame = frames.firstOrNull()?.long("frameID") ?: 1L
        c.send(16, 3) { id(main); id(frame) } // ThisObject
        c.send(16, 4) { id(main); id(frame) } // PopFrames
        c.send(11, 14) { id(main); byte('V'.code) } // ForceEarlyReturn void
        c.send(11, 10) { id(main); id(0) } // Stop with a null throwable
        c.send(1, 15) // HoldEvents
        c.send(1, 16) // ReleaseEvents
        c.send(1, 14) { int(1); id(main); int(1) } // DisposeObjects
        c.send(1, 6) // Dispose
    }

    private class Body {
        val bytes = ByteArrayOutputStream()
        private val out = DataOutputStream(bytes)
        fun byte(v: Int) = out.writeByte(v)
        fun int(v: Int) = out.writeInt(v)
        fun id(v: Long) = out.writeLong(v)
        fun string(s: String) = s.toByteArray().let { out.writeInt(it.size); out.write(it) }
    }

    private class Client(port: Int, private val analyzer: Analyzer) : AutoCloseable {
        private val socket = Socket("127.0.0.1", port)
        private val out = DataOutputStream(socket.getOutputStream())
        private val input = DataInputStream(socket.getInputStream())
        private var nextId = 1

        init {
            out.write(JdwpPacket.HANDSHAKE)
            input.readFully(ByteArray(JdwpPacket.HANDSHAKE.size))
            // Drain replies and events; the analyzer sees them through the proxy.
            thread(isDaemon = true) { runCatching { while (JdwpPacket.read(input) != null) Unit } }
        }

        fun send(set: Int, cmd: Int, body: Body.() -> Unit = {}): Exchange {
            val id = nextId++
            JdwpPacket.command(id, set, cmd, Body().apply(body).bytes.toByteArray()).writeTo(out)
            out.flush()
            return waitFor {
                analyzer.exchanges().firstOrNull { it.command?.packet?.id == id && !it.isEvent && it.reply != null }
            }.also { assertTrue(it.spec == JdwpSpec.find(set, cmd)) }
        }

        override fun close() = socket.close()
    }

    companion object {
        fun <T : Any> waitFor(get: () -> T?): T {
            val deadline = System.currentTimeMillis() + 20_000
            while (System.currentTimeMillis() < deadline) {
                get()?.let { return it }
                Thread.sleep(10)
            }
            fail("Timed out")
        }
    }
}
