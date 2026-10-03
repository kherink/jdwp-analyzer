package com.karelherink.jdwpanalyzer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/** Colors for packet status, matching the original tool: blue commands, green replies, red errors. */
@Immutable
data class StatusColors(
    val command: Color,
    val ok: Color,
    val error: Color,
    val event: Color,
    val pending: Color,
    val problem: Color,
    val link: Color,
    val highlight: Color,
    val muted: Color,
)

private val LightStatus = StatusColors(
    command = Color(0xFF1F4FD1),
    ok = Color(0xFF16803C),
    error = Color(0xFFC62828),
    event = Color(0xFF7B3FC4),
    pending = Color(0xFF8A8F98),
    problem = Color(0xFFB45F06),
    link = Color(0xFF0B63C5),
    highlight = Color(0x33FFC107),
    muted = Color(0xFF6B7280),
)

private val DarkStatus = StatusColors(
    command = Color(0xFF8AB4FF),
    ok = Color(0xFF6FD08C),
    error = Color(0xFFFF7B72),
    event = Color(0xFFC7A2FF),
    pending = Color(0xFF8B949E),
    problem = Color(0xFFF0A657),
    link = Color(0xFF79C0FF),
    highlight = Color(0x40FFC107),
    muted = Color(0xFF9AA4B2),
)

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

val Mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)

@Composable
fun AnalyzerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) {
        darkColorScheme(primary = Color(0xFF8AB4FF), secondary = Color(0xFFC7A2FF))
    } else {
        lightColorScheme(primary = Color(0xFF1F4FD1), secondary = Color(0xFF7B3FC4))
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalStatusColors provides if (dark) DarkStatus else LightStatus) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
