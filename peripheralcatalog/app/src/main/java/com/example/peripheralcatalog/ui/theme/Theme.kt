package com.example.peripheralcatalog.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

private val LightBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF001B44),
    secondary = Color(0xFF1565C0),
    onSecondary = Color.White,
    background = Color.White,
    surface = Color(0xFFF8FAFF),
    onSurface = Color(0xFF0A0A0A),
    tertiary =
        Color(0xFF42A5F5)
)

@Composable
fun PeripheralCatalogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightBlueColorScheme,
        typography = Typography,
        content = content
    )
}
