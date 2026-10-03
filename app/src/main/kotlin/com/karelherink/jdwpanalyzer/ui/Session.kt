package com.karelherink.jdwpanalyzer.ui

import com.karelherink.jdwpanalyzer.log.PacketLog
import com.karelherink.jdwpanalyzer.log.PacketLogWriter
import com.karelherink.jdwpanalyzer.model.Analyzer
import com.karelherink.jdwpanalyzer.proxy.JdwpProxy
import com.karelherink.jdwpanalyzer.proxy.ProxyConfig
import com.karelherink.jdwpanalyzer.proxy.ProxyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.concurrent.thread

/** Where a session's packets come from. */
sealed interface SessionSource {
    /** Proxy a live debugger ↔ VM connection, optionally logging it to [logDir]. */
    data class Live(val config: ProxyConfig, val logDir: File?) : SessionSource

    /** Replay a packet log written by this tool or the original Java one. */
    data class Replay(val path: File) : SessionSource
}

/** One analysis session: a proxy or a log replay feeding an [Analyzer]. */
class Session(val source: SessionSource) {
    val analyzer = Analyzer()
    val isReplay: Boolean get() = source is SessionSource.Replay

    private val logWriter: PacketLogWriter? = (source as? SessionSource.Live)?.logDir?.let { PacketLogWriter(it) }
    val logFile: File? get() = logWriter?.location

    private val proxy: JdwpProxy? = (source as? SessionSource.Live)?.let { JdwpProxy(it.config, analyzer, logWriter) }

    private val replayState = MutableStateFlow<ProxyState>(ProxyState.Idle)

    /** For replays, [ProxyState.Closed] once the whole log has been read. */
    val state: StateFlow<ProxyState> get() = proxy?.state ?: replayState

    fun start() {
        when (source) {
            is SessionSource.Live -> proxy!!.start()
            is SessionSource.Replay -> thread(name = "jdwp-replay", isDaemon = true) {
                replayState.value = ProxyState.Connecting(source.path.path, 0)
                try {
                    val packets = PacketLog.read(source.path)
                    packets.forEach { analyzer.onPacket(it, direction = null, timeMillis = 0) }
                    replayState.value = ProxyState.Closed("Replayed ${packets.size} packets from ${PacketLog.resolve(source.path).path}", failed = false)
                } catch (e: Exception) {
                    replayState.value = ProxyState.Closed(e.message ?: e.toString(), failed = true)
                }
            }
        }
    }

    fun stop() {
        proxy?.stop()
        logWriter?.close()
    }

    /** Saves every packet seen so far, in the original `seq.log` format. */
    fun saveLog(target: File): File {
        val file = if (target.isDirectory) File(target, PacketLog.FILE_NAME) else target
        PacketLog.write(file, analyzer.packets().map { it.packet })
        return file
    }
}
