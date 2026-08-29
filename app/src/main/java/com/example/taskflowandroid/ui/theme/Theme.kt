package com.example.taskflowandroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MedNavyDark,
    onPrimary = Color(0xFF00315F),
    secondary = MedTealDark,
    onSecondary = Color(0xFF003731),
    background = MedDarkBackground,
    surface = MedDarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = MedNavy,
    onPrimary = Color.White,
    primaryContainer = MedBlueLight,
    onPrimaryContainer = MedNavy,
    secondary = MedTeal,
    onSecondary = Color(0xFF003731),
    secondaryContainer = MedTealLight,
    onSecondaryContainer = Color(0xFF003731),
    background = MedBackground,
    onBackground = MedText,
    surface = MedSurface,
    onSurface = MedText
)

@Composable
fun TaskFlowAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
