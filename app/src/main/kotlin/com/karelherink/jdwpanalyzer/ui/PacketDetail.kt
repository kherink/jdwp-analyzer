package com.karelherink.jdwpanalyzer.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.karelherink.jdwpanalyzer.decode.DecodedNode
import com.karelherink.jdwpanalyzer.decode.DecodedPacket
import com.karelherink.jdwpanalyzer.decode.EntityRef
import com.karelherink.jdwpanalyzer.model.CapturedPacket
import com.karelherink.jdwpanalyzer.model.Exchange
import com.karelherink.jdwpanalyzer.model.Registry
import com.karelherink.jdwpanalyzer.protocol.Direction
import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.protocol.JdwpPacket
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.text.SimpleDateFormat
import java.util.Date

private enum class Part { COMMAND, REPLY }

private class TreeRow(val part: Part, val node: DecodedNode, val depth: Int, val path: String)

private sealed interface DetailItem {
    data class Header(val part: Part, val title: String, val packet: CapturedPacket?, val decoded: DecodedPacket?) : DetailItem
    data class Node(val row: TreeRow) : DetailItem
    data class Empty(val text: String) : DetailItem
}

@Composable
fun PacketDetail(exchange: Exchange, registry: Registry, revision: Long, onInspect: (EntityRef) -> Unit) {
    val colors = LocalStatusColors.current
    var showBytes by remember(exchange.index) { mutableStateOf(false) }
    var selection by remember(exchange.index) { mutableStateOf<Pair<Part, DecodedNode>?>(null) }
    val expanded = remember(exchange.index) { mutableStateMapOf<String, Boolean>() }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    exchange.name + (exchange.eventKinds?.let { "  ($it)" } ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (exchange.isEvent) colors.event else colors.command,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FilterChip(!showBytes, { showBytes = false }, label = { Text("Decoded") })
                FilterChip(showBytes, { showBytes = true }, label = { Text("Bytes") })
                TextButton(onClick = { copy(describeExchange(exchange)) }) { Text("Copy") }
            }
            Text(
                buildString {
                    append("Packet ID ${exchange.packetId} · ")
                    append(if (exchange.direction == Direction.DEBUGGER_TO_VM) "debugger → VM" else "VM → debugger")
                    exchange.command?.let { append(" · command set ${exchange.commandSet}, command ${exchange.commandId}") }
                    exchange.command?.takeIf { it.timeMillis > 0 }?.let { append(" · sent ${time(it.timeMillis)}") }
                    exchange.latencyMillis?.takeIf { exchange.command!!.timeMillis > 0 }?.let { append(" · reply after $it ms") }
                    if (exchange.isError) append(" · error ${JdwpConstants.error(exchange.errorCode)} (${exchange.errorCode})")
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        HorizontalDivider()
        val selectedRange = selection
        if (showBytes) {
            ByteView(exchange, selectedRange?.first, selectedRange?.second)
        } else {
            val items = remember(exchange, expanded.toMap()) { detailItems(exchange, expanded) }
            val listState = rememberLazyListState()
            Box(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize(), state = listState) {
                    items(items) { item ->
                        when (item) {
                            is DetailItem.Header -> SectionHeader(item)
                            is DetailItem.Empty -> Text(item.text, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = Mono, color = colors.muted)
                            is DetailItem.Node -> TreeNodeRow(
                                row = item.row,
                                registry = registry,
                                revision = revision,
                                selected = selection?.let { it.first == item.row.part && it.second === item.row.node } == true,
                                expanded = isExpanded(item.row, expanded),
                                onToggle = { expanded[item.row.path] = !isExpanded(item.row, expanded) },
                                onSelect = {
                                    selection = item.row.part to item.row.node
                                    item.row.node.ref?.let(onInspect)
                                },
                                onInspect = onInspect,
                            )
                        }
                    }
                }
                VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(Alignment.CenterEnd).fillMaxHeight())
            }
        }
    }
}

private fun isExpanded(row: TreeRow, overrides: Map<String, Boolean>): Boolean =
    overrides[row.path] ?: (row.node.children.size <= 64)

private fun detailItems(exchange: Exchange, expanded: Map<String, Boolean>): List<DetailItem> = buildList {
    fun addTree(part: Part, decoded: DecodedPacket?) {
        val fields = decoded?.fields.orEmpty()
        if (fields.isEmpty()) {
            add(DetailItem.Empty("(no data)"))
            return
        }
        fun visit(node: DecodedNode, depth: Int, path: String) {
            val row = TreeRow(part, node, depth, path)
            add(DetailItem.Node(row))
            if (node.children.isNotEmpty() && isExpanded(row, expanded)) {
                node.children.forEachIndexed { i, c -> visit(c, depth + 1, "$path/$i") }
            }
        }
        fields.forEachIndexed { i, f -> visit(f, 0, "${part.name}/$i") }
    }
    if (exchange.command != null) {
        add(DetailItem.Header(Part.COMMAND, if (exchange.isEvent) "Event" else "Command", exchange.command, exchange.decodedCommand))
        addTree(Part.COMMAND, exchange.decodedCommand)
    }
    when {
        exchange.reply != null -> {
            add(DetailItem.Header(Part.REPLY, "Reply", exchange.reply, exchange.decodedReply))
            addTree(Part.REPLY, exchange.decodedReply)
        }
        !exchange.isEvent -> {
            add(DetailItem.Header(Part.REPLY, "Reply", null, null))
            add(DetailItem.Empty("Waiting for the reply…"))
        }
    }
}

@Composable
private fun SectionHeader(item: DetailItem.Header) {
    val colors = LocalStatusColors.current
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        item.packet?.let { Text("${it.packet.dataSize} data bytes", style = MaterialTheme.typography.labelSmall, color = colors.muted) }
        item.decoded?.problem?.let {
            Text("  ⚠ $it", style = MaterialTheme.typography.labelSmall, color = colors.problem, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TreeNodeRow(
    row: TreeRow,
    registry: Registry,
    revision: Long,
    selected: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
    onInspect: (EntityRef) -> Unit,
) {
    val colors = LocalStatusColors.current
    val node = row.node
    val description = remember(node, revision) { describeNode(node, registry) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onSelect)
            .padding(start = 12.dp + (row.depth * 18).dp, end = 12.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(16.dp).clickable(enabled = node.children.isNotEmpty(), onClick = onToggle)) {
            if (node.children.isNotEmpty()) Text(if (expanded) "▾" else "▸", style = Mono, color = colors.muted)
        }
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = if (node.problem) colors.problem else colors.muted)) { append(node.name) }
                node.text?.let {
                    withStyle(SpanStyle(color = colors.muted)) { append(": ") }
                    withStyle(SpanStyle(color = if (node.problem) colors.problem else Color.Unspecified)) { append(it) }
                }
                if (node.children.isNotEmpty() && node.text == null) {
                    withStyle(SpanStyle(color = colors.muted)) { append("  (${node.children.size})") }
                }
            },
            style = Mono,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        val ref = node.ref
        if (description != null || ref != null) {
            Text(
                "  " + (description ?: "inspect"),
                style = Mono.copy(fontWeight = if (description != null) FontWeight.Medium else FontWeight.Normal),
                color = colors.link,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (ref != null) {
                    Modifier.pointerHoverIcon(PointerIcon.Hand).clickable { onSelect(); onInspect(ref) }
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
private fun ByteView(exchange: Exchange, part: Part?, node: DecodedNode?) {
    val colors = LocalStatusColors.current
    val listState = rememberLazyListState()
    val lines = remember(exchange) {
        buildList {
            exchange.command?.let { add("Command" to it); }
            exchange.reply?.let { add("Reply" to it) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), state = listState) {
            lines.forEach { (title, captured) ->
                val isReply = title == "Reply"
                val highlight = node?.takeIf { (part == Part.REPLY) == isReply }
                    ?.let { (JdwpPacket.HEADER_SIZE + it.offset) until (JdwpPacket.HEADER_SIZE + it.offset + it.size) }
                item {
                    Text(
                        "$title — ${captured.packet.bytes.size} bytes (11-byte header + ${captured.packet.dataSize} data)",
                        Modifier.padding(top = 10.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                val bytes = captured.packet.bytes
                items((bytes.size + 15) / 16) { line ->
                    Text(hexLine(bytes, line * 16, highlight, colors.highlight, colors.muted), style = Mono)
                }
            }
        }
        VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(Alignment.CenterEnd).fillMaxHeight())
    }
}

private fun hexLine(bytes: ByteArray, start: Int, highlight: IntRange?, highlightColor: Color, muted: Color): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = muted)) { append("%06x  ".format(start)) }
        for (i in start until start + 16) {
            if (i < bytes.size) {
                val header = i < JdwpPacket.HEADER_SIZE
                val style = SpanStyle(
                    background = if (highlight != null && i in highlight) highlightColor else Color.Transparent,
                    color = if (header) muted else Color.Unspecified,
                )
                withStyle(style) { append("%02x".format(bytes[i])) }
                append(if (i == start + 7) "  " else " ")
            } else {
                append(if (i == start + 7) "    " else "   ")
            }
        }
        append(" ")
        for (i in start until minOf(start + 16, bytes.size)) {
            val c = bytes[i].toInt() and 0xff
            val style = SpanStyle(background = if (highlight != null && i in highlight) highlightColor else Color.Transparent)
            withStyle(style) { append(if (c in 0x20..0x7e) c.toChar() else '·') }
        }
    }

private fun time(millis: Long): String = SimpleDateFormat("HH:mm:ss.SSS").format(Date(millis))

private fun describeExchange(e: Exchange): String = buildString {
    appendLine("${e.name} (packet ID ${e.packetId})")
    e.decodedCommand?.let { appendLine("-- ${if (e.isEvent) "event" else "command"} --"); append(it.root) }
    e.decodedReply?.let { appendLine("-- reply --"); append(it.root) }
}

fun copy(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
