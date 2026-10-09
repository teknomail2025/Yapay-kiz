package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CallColorScheme = darkColorScheme(
    primary = CallTealLight,
    onPrimary = Color.White,
    primaryContainer = CallTeal,
    onPrimaryContainer = Color.White,
    secondary = CallCyan,
    onSecondary = Color.Black,
    tertiary = CallAmber,
    onTertiary = Color.Black,
    background = CallDarkBackground,
    onBackground = TextWhite,
    surface = CallDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = CallDarkSurfaceVariant,
    onSurfaceVariant = TextMuted,
    error = CallRed,
    onError = Color.White,
    outline = CallDarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent call UI styling
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CallColorScheme,
        typography = Typography,
        content = content
    )
}
