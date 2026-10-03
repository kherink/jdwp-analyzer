package com.karelherink.jdwpanalyzer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.karelherink.jdwpanalyzer.proxy.ProxyConfig
import java.io.File
import javax.swing.JFileChooser

@Composable
fun SetupScreen(error: String?, onStart: (SessionSource) -> Unit) {
    var listenPort by remember { mutableStateOf("5006") }
    var vmAddress by remember { mutableStateOf("localhost:5005") }
    var requestDelay by remember { mutableStateOf("0") }
    var responseDelay by remember { mutableStateOf("0") }
    var logDir by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).padding(32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("JDWP Analyzer", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Sits between a debugger and a JVM and decodes every Java Debug Wire Protocol packet that passes " +
                        "through, covering the whole protocol as of JDK 26.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                (formError ?: error)?.let {
                    Text(it, color = LocalStatusColors.current.error, style = MaterialTheme.typography.bodyMedium)
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Proxy a debug session", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Start the JVM with -agentlib:jdwp=transport=dt_socket,server=y,address=5005, then attach " +
                                "your debugger to the listen port below instead of to the JVM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(listenPort, { listenPort = it }, Modifier.weight(1f), label = { Text("Debugger attaches to port") }, singleLine = true)
                            OutlinedTextField(vmAddress, { vmAddress = it }, Modifier.weight(1f), label = { Text("JVM address (host:port)") }, singleLine = true)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(requestDelay, { requestDelay = it }, Modifier.weight(1f), label = { Text("Delay to JVM (ms)") }, singleLine = true)
                            OutlinedTextField(responseDelay, { responseDelay = it }, Modifier.weight(1f), label = { Text("Delay to debugger (ms)") }, singleLine = true)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(logDir, { logDir = it }, Modifier.weight(1f), label = { Text("Log directory (optional)") }, singleLine = true)
                            OutlinedButton(onClick = { chooseFile(directoriesOnly = true)?.let { logDir = it.path } }) { Text("Browse…") }
                        }
                        Button(onClick = {
                            formError = null
                            try {
                                val (host, port) = ProxyConfig.parseAddress(vmAddress)
                                val config = ProxyConfig(
                                    listenPort = listenPort.trim().toInt(),
                                    vmHost = host,
                                    vmPort = port,
                                    requestDelayMillis = requestDelay.trim().ifEmpty { "0" }.toLong(),
                                    responseDelayMillis = responseDelay.trim().ifEmpty { "0" }.toLong(),
                                )
                                onStart(SessionSource.Live(config, logDir.trim().takeIf { it.isNotEmpty() }?.let(::File)))
                            } catch (_: NumberFormatException) {
                                formError = "Ports and delays must be numbers."
                            }
                        }) { Text("Start proxy") }
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Replay a log", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Open a log directory (or its seq.log file) written by this tool or by the original Java version.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(onClick = { chooseFile(directoriesOnly = false)?.let { onStart(SessionSource.Replay(it)) } }) {
                            Text("Open log…")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp).width(1.dp))
            }
        }
    }
}

fun chooseFile(directoriesOnly: Boolean, save: Boolean = false): File? {
    val chooser = JFileChooser().apply {
        fileSelectionMode = if (directoriesOnly) JFileChooser.DIRECTORIES_ONLY else JFileChooser.FILES_AND_DIRECTORIES
    }
    val result = if (save) chooser.showSaveDialog(null) else chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}
