package com.karelherink.jdwpanalyzer

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.karelherink.jdwpanalyzer.proxy.ProxyConfig
import com.karelherink.jdwpanalyzer.ui.AnalyzerTheme
import com.karelherink.jdwpanalyzer.ui.Session
import com.karelherink.jdwpanalyzer.ui.SessionScreen
import com.karelherink.jdwpanalyzer.ui.SessionSource
import com.karelherink.jdwpanalyzer.ui.SetupScreen
import java.io.File
import kotlin.system.exitProcess

private const val USAGE = """
Usage: jdwp-analyzer [<inPort> <outAddress> <reqDelay> <respDelay> [<log_dir>] | <log_dir>]

With no arguments, opens the setup screen.

inPort     = port to which the debugger will connect
outAddress = address as 'host:port' on which the remote VM is accepting debugger connections;
             a localhost address can just be a port
reqDelay   = milliseconds to wait after passing each packet to the VM
respDelay  = milliseconds to wait after passing each packet to the debugger
log_dir    = log directory to write (with a proxy) or to replay (on its own)
"""

/** Accepts the same arguments as the original Java tool, so existing scripts keep working. */
private fun parseArgs(args: Array<String>): SessionSource? = when {
    args.isEmpty() -> null
    args.size == 1 && args[0] in setOf("-h", "--help", "-help") -> {
        println(USAGE.trimIndent()); exitProcess(0)
    }
    args.size == 1 -> SessionSource.Replay(File(args[0]))
    args.size == 4 || args.size == 5 -> try {
        val (host, port) = ProxyConfig.parseAddress(args[1])
        SessionSource.Live(
            ProxyConfig(args[0].toInt(), host, port, args[2].toLong(), args[3].toLong()),
            args.getOrNull(4)?.let(::File),
        )
    } catch (_: NumberFormatException) {
        System.err.println(USAGE.trimIndent()); exitProcess(1)
    }
    else -> {
        System.err.println(USAGE.trimIndent()); exitProcess(1)
    }
}

fun main(args: Array<String>) {
    val initial = parseArgs(args)
    application {
        var source by remember { mutableStateOf(initial) }
        var error by remember { mutableStateOf<String?>(null) }
        Window(
            onCloseRequest = ::exitApplication,
            title = "JDWP Analyzer",
            state = rememberWindowState(width = 1440.dp, height = 900.dp),
        ) {
            AnalyzerTheme {
                val current = source
                if (current == null) {
                    SetupScreen(error) { source = it; error = null }
                } else {
                    key(current) {
                        val session = remember {
                            runCatching { Session(current).also { it.start() } }
                                .onFailure { error = it.message ?: it.toString(); source = null }
                                .getOrNull()
                        }
                        if (session != null) {
                            DisposableEffect(session) { onDispose { session.stop() } }
                            SessionScreen(session, onClose = { session.stop(); source = null })
                        }
                    }
                }
            }
        }
    }
}
