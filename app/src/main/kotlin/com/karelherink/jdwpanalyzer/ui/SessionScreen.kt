package com.karelherink.jdwpanalyzer.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.karelherink.jdwpanalyzer.decode.EntityRef
import com.karelherink.jdwpanalyzer.model.Exchange
import com.karelherink.jdwpanalyzer.model.Registry
import com.karelherink.jdwpanalyzer.protocol.Direction
import com.karelherink.jdwpanalyzer.protocol.JdwpConstants
import com.karelherink.jdwpanalyzer.proxy.ProxyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private class Snapshot(val all: List<Exchange>, val shown: List<Exchange>, val revision: Long)

private data class Filter(
    val text: String = "",
    val events: Boolean = true,
    val errorsOnly: Boolean = false,
    val problemsOnly: Boolean = false,
)

@Composable
fun SessionScreen(session: Session, onClose: () -> Unit) {
    val state by session.state.collectAsState()
    var filter by remember { mutableStateOf(Filter()) }
    var follow by remember { mutableStateOf(!session.isReplay) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val inspectorHistory = remember { mutableStateListOf<EntityRef>() }
    var message by remember { mutableStateOf<String?>(null) }
    val registry = session.analyzer.registry

    // Poll rather than react to every packet: a busy session produces thousands of packets per second.
    val snapshot by produceState(Snapshot(emptyList(), emptyList(), -1), filter) {
        var lastVersion = -1L
        var lastRevision = -1L
        var first = true
        while (true) {
            val version = session.analyzer.version.value
            val revision = registry.revision
            if (first || version != lastVersion || revision != lastRevision) {
                first = false
                lastVersion = version
                lastRevision = revision
                value = withContext(Dispatchers.Default) {
                    val all = session.analyzer.exchanges()
                    Snapshot(all, all.filter { matches(it, filter, registry) }, revision)
                }
            }
            delay(150)
        }
    }
    val selected = selectedIndex?.let { snapshot.all.getOrNull(it) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Toolbar(
                session = session,
                state = state,
                all = snapshot.all,
                shown = snapshot.shown.size,
                filter = filter,
                onFilter = { filter = it },
                follow = follow,
                onFollow = { follow = it },
                onSave = {
                    chooseFile(directoriesOnly = false, save = true)?.let { target ->
                        message = runCatching { "Saved to ${session.saveLog(target).path}" }
                            .getOrElse { "Couldn't save: ${it.message}" }
                    }
                },
                onClose = onClose,
            )
            message?.let {
                Text(
                    it,
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            HorizontalDivider()
            SplitPane(
                initialFraction = 0.52f,
                first = {
                    PacketTable(
                        exchanges = snapshot.shown,
                        revision = snapshot.revision,
                        registry = registry,
                        isReplay = session.isReplay,
                        startTime = snapshot.all.firstOrNull()?.firstTime ?: 0,
                        selected = selectedIndex,
                        follow = follow,
                        onSelect = { selectedIndex = it; follow = false },
                    )
                },
                second = {
                    SplitPane(
                        initialFraction = 0.68f,
                        vertical = true,
                        first = {
                            if (selected == null) {
                                Placeholder("Select a packet to see it decoded.")
                            } else {
                                PacketDetail(selected, registry, snapshot.revision) { ref ->
                                    if (inspectorHistory.lastOrNull() != ref) inspectorHistory.add(ref)
                                }
                            }
                        },
                        second = {
                            Inspector(
                                ref = inspectorHistory.lastOrNull(),
                                registry = registry,
                                revision = snapshot.revision,
                                canGoBack = inspectorHistory.size > 1,
                                onBack = { inspectorHistory.removeAt(inspectorHistory.lastIndex) },
                                onOpen = { inspectorHistory.add(it) },
                            )
                        },
                    )
                },
            )
        }
    }
}

private fun matches(e: Exchange, f: Filter, registry: Registry): Boolean {
    if (!f.events && e.isEvent) return false
    if (f.errorsOnly && !e.isError) return false
    if (f.problemsOnly && !e.hasProblem) return false
    if (f.text.isBlank()) return true
    val needle = f.text.trim()
    return e.name.contains(needle, ignoreCase = true) ||
        e.packetId.toString() == needle ||
        summarize(e, registry).contains(needle, ignoreCase = true)
}

@Composable
fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = LocalStatusColors.current.muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Toolbar(
    session: Session,
    state: ProxyState,
    all: List<Exchange>,
    shown: Int,
    filter: Filter,
    onFilter: (Filter) -> Unit,
    follow: Boolean,
    onFollow: (Boolean) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalStatusColors.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val (label, color) = when (state) {
                ProxyState.Idle -> "Starting…" to colors.pending
                is ProxyState.Listening -> "Waiting for a debugger on port ${state.port}" to colors.pending
                is ProxyState.Connecting ->
                    (if (session.isReplay) "Reading ${state.host}…" else "Connecting to ${state.host}:${state.port}…") to colors.pending
                is ProxyState.Connected -> "Connected: debugger ⇄ :${state.listenPort} ⇄ ${state.host}:${state.port}" to colors.ok
                is ProxyState.Closed -> state.reason to if (state.failed) colors.error else colors.muted
            }
            Box(Modifier.width(10.dp).padding(vertical = 1.dp)) {
                Text("●", color = color)
            }
            Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            session.logFile?.let { Text("Logging to ${it.path}", style = MaterialTheme.typography.bodySmall, color = colors.muted) }
            OutlinedButton(onClick = onSave) { Text("Save log…") }
            OutlinedButton(onClick = onClose) {
                Text(if (state is ProxyState.Closed) "New session" else if (session.isReplay) "Close" else "Stop")
            }
        }
        Row(
            Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = filter.text,
                onValueChange = { onFilter(filter.copy(text = it)) },
                placeholder = { Text("Filter by command, ID, class, thread…") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(340.dp),
            )
            FilterChip(filter.events, { onFilter(filter.copy(events = !filter.events)) }, label = { Text("Events") })
            FilterChip(filter.errorsOnly, { onFilter(filter.copy(errorsOnly = !filter.errorsOnly)) }, label = { Text("Errors only") })
            FilterChip(filter.problemsOnly, { onFilter(filter.copy(problemsOnly = !filter.problemsOnly)) }, label = { Text("Decode problems") })
            FilterChip(follow, { onFollow(!follow) }, label = { Text("Follow") })
            Box(Modifier.weight(1f))
            val events = all.count { it.isEvent }
            val errors = all.count { it.isError }
            val problems = all.count { it.hasProblem }
            val pending = all.count { it.isPending }
            Text(
                buildString {
                    append("${all.size - events} commands · $events events")
                    if (errors > 0) append(" · $errors errors")
                    if (pending > 0) append(" · $pending awaiting reply")
                    if (problems > 0) append(" · $problems decode problems")
                    if (shown != all.size) append(" · showing $shown")
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
    }
}

private val columns: List<Pair<String, Dp>> = listOf(
    "#" to 56.dp,
    "Time" to 72.dp,
    "" to 20.dp,
    "Command" to 250.dp,
    "Reply" to 120.dp,
    "ms" to 44.dp,
)

@Composable
private fun PacketTable(
    exchanges: List<Exchange>,
    revision: Long,
    registry: Registry,
    isReplay: Boolean,
    startTime: Long,
    selected: Int?,
    follow: Boolean,
    onSelect: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    val focus = remember { FocusRequester() }
    LaunchedEffect(exchanges.size, follow) {
        if (follow && exchanges.isNotEmpty()) listState.scrollToItem(exchanges.lastIndex)
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 6.dp)) {
            columns.forEach { (title, width) -> Text(title, Modifier.width(width), style = MaterialTheme.typography.labelMedium) }
            Text("Summary", style = MaterialTheme.typography.labelMedium)
        }
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focus)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown || exchanges.isEmpty()) return@onPreviewKeyEvent false
                        val position = exchanges.indexOfFirst { it.index == selected }
                        val next = when (event.key) {
                            Key.DirectionDown -> (position + 1).coerceAtMost(exchanges.lastIndex)
                            Key.DirectionUp -> (position - 1).coerceAtLeast(0)
                            else -> return@onPreviewKeyEvent false
                        }
                        onSelect(exchanges[next].index)
                        true
                    },
            ) {
                items(exchanges, key = { it.index }) { e ->
                    PacketRow(e, revision, registry, isReplay, startTime, e.index == selected) {
                        onSelect(e.index)
                        focus.requestFocus()
                    }
                }
            }
            VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(Alignment.CenterEnd).fillMaxHeight())
        }
    }
    LaunchedEffect(selected) {
        val position = exchanges.indexOfFirst { it.index == selected }
        if (position < 0) return@LaunchedEffect
        val visible = listState.layoutInfo.visibleItemsInfo
        if (visible.isNotEmpty() && (position < visible.first().index || position > visible.last().index)) {
            listState.scrollToItem(position)
        }
    }
}

@Composable
private fun PacketRow(
    e: Exchange,
    revision: Long,
    registry: Registry,
    isReplay: Boolean,
    startTime: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalStatusColors.current
    val summary = remember(e, revision) { summarize(e, registry) }
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Row(
        Modifier.fillMaxWidth().background(background).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val nameColor = when {
            e.isEvent -> colors.event
            e.command == null -> colors.problem
            else -> colors.command
        }
        Text(e.index.toString(), Modifier.width(columns[0].second), style = Mono, color = colors.muted)
        Text(
            if (isReplay) "" else "%.3f".format((e.firstTime - startTime) / 1000.0),
            Modifier.width(columns[1].second),
            style = Mono,
            color = colors.muted,
        )
        Text(
            if (e.direction == Direction.DEBUGGER_TO_VM) "→" else "←",
            Modifier.width(columns[2].second),
            style = Mono,
            color = nameColor,
        )
        Text(e.name, Modifier.width(columns[3].second), style = Mono, color = nameColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        val (reply, replyColor) = when {
            e.isEvent && e.reply == null -> "event" to colors.event
            e.reply == null -> "pending" to colors.pending
            e.isError -> JdwpConstants.error(e.errorCode) to colors.error
            else -> "OK" to colors.ok
        }
        Text(reply, Modifier.width(columns[4].second), style = Mono, color = replyColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            e.latencyMillis?.takeIf { !isReplay }?.toString() ?: "",
            Modifier.width(columns[5].second),
            style = Mono,
            color = colors.muted,
        )
        Text(
            (if (e.hasProblem) "⚠ " else "") + summary,
            style = Mono,
            color = if (e.hasProblem) colors.problem else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
