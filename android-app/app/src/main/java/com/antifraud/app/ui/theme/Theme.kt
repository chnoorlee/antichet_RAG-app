package com.antifraud.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    secondary = BlueSecondary,
    onSecondary = Color.White,
    background = SurfaceLight,
    surface = CardLight,
    error = RedPrimary,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueSecondary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003A6B),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color.Black,
    background = SurfaceDark,
    surface = CardDark,
    error = RedSecondary,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun AntifraudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
