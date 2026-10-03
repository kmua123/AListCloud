package com.kmuaz.alistcloud.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FF), onPrimaryContainer = Color(0xFF163B80),
    secondary = Color(0xFF52709B), secondaryContainer = Color(0xFFE8F0FF), onSecondaryContainer = Color(0xFF2563EB), background = Color(0xFFF5F7FC),
    surface = Color.White, surfaceVariant = Color(0xFFEDF1F8),
    onSurface = Color(0xFF17233B), onSurfaceVariant = Color(0xFF69778D),
    outlineVariant = Color(0xFFE6EBF3)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CBDFF), primaryContainer = Color(0xFF203A66),
    background = Color(0xFF101722), surface = Color(0xFF172131),
    surfaceVariant = Color(0xFF253247), onSurface = Color(0xFFE5ECF8),
    onSurfaceVariant = Color(0xFFA8B7CC)
)
@Composable
fun AListCloudTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography, content = content)
}
