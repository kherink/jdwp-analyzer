package com.karelherink.jdwpanalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.awt.Cursor

/** Two panes with a draggable divider; [vertical] stacks them top/bottom instead of left/right. */
@Composable
fun SplitPane(
    initialFraction: Float,
    vertical: Boolean = false,
    modifier: Modifier = Modifier,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
) {
    var fraction by remember { mutableFloatStateOf(initialFraction) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val total = with(LocalDensity.current) { (if (vertical) maxHeight else maxWidth).toPx() }
        val drag = rememberDraggableState { delta -> fraction = (fraction + delta / total).coerceIn(0.1f, 0.9f) }
        val divider = Modifier
            .background(MaterialTheme.colorScheme.outlineVariant)
            .pointerHoverIcon(PointerIcon(Cursor(if (vertical) Cursor.N_RESIZE_CURSOR else Cursor.E_RESIZE_CURSOR)))
            .draggable(drag, if (vertical) Orientation.Vertical else Orientation.Horizontal)
        if (vertical) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().weight(fraction)) { first() }
                Box(divider.fillMaxWidth().height(4.dp))
                Box(Modifier.fillMaxWidth().weight(1f - fraction)) { second() }
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxHeight().weight(fraction)) { first() }
                Box(divider.fillMaxHeight().width(4.dp))
                Box(Modifier.fillMaxHeight().weight(1f - fraction)) { second() }
            }
        }
    }
}
