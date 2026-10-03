package com.karelherink.jdwpanalyzer.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.karelherink.jdwpanalyzer.decode.EntityRef
import com.karelherink.jdwpanalyzer.model.Registry

/** Shows everything learnt so far about a class, method, field, object, frame or event request. */
@Composable
fun Inspector(
    ref: EntityRef?,
    registry: Registry,
    revision: Long,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onOpen: (EntityRef) -> Unit,
) {
    val colors = LocalStatusColors.current
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val title = ref?.let { remember(it, revision) { registry.describe(it) } ?: kindOf(it) }
            Text(
                if (title != null) "Inspector · $title" else "Inspector",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (canGoBack) TextButton(onClick = onBack) { Text("← Back") }
        }
        if (ref == null) {
            Placeholder("Click an ID in a decoded packet to see what the debugger has learnt about it.")
            return@Column
        }
        val rows = remember(ref, revision) { registry.details(ref) }
        val listState = rememberLazyListState()
        Box(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize().padding(vertical = 4.dp), state = listState) {
                items(rows) { row ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
                        Text(row.label, Modifier.width(150.dp), style = Mono, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val target = row.ref
                        Text(
                            row.text,
                            style = Mono,
                            color = if (target != null) colors.link else MaterialTheme.colorScheme.onSurface,
                            modifier = if (target != null) {
                                Modifier.pointerHoverIcon(PointerIcon.Hand).clickable { onOpen(target) }
                            } else {
                                Modifier
                            },
                        )
                    }
                }
            }
            VerticalScrollbar(rememberScrollbarAdapter(listState), Modifier.align(Alignment.CenterEnd).fillMaxHeight())
        }
    }
}

private fun kindOf(ref: EntityRef): String = when (ref) {
    is EntityRef.Type -> "reference type #${ref.id}"
    is EntityRef.Method -> "method #${ref.methodId}"
    is EntityRef.Field -> "field #${ref.fieldId}"
    is EntityRef.Obj -> "${ref.kind.label} #${ref.id}"
    is EntityRef.Frame -> "frame #${ref.id}"
    is EntityRef.EventRequest -> "event request ${ref.id}"
}
