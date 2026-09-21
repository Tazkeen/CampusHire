package com.example.campushire.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val Blue = Color(0xFF0B5CFF)
val Navy = Color(0xFF071A4A)

private val LightColors = lightColorScheme(
    primary = Blue, onPrimary = Color.White,
    primaryContainer = Color(0xFFE6EEFF), onPrimaryContainer = Navy,
    background = Color(0xFFF5F7FB), onBackground = Color(0xFF111827),
    surface = Color.White, onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEAF0FF), onSurfaceVariant = Color(0xFF5B6475),
    outline = Color(0xFFD5DBE8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5B8CFF), onPrimary = Color.White,
    primaryContainer = Color(0xFF1E2C57), onPrimaryContainer = Color.White,
    background = Color(0xFF0E1220), onBackground = Color(0xFFEDEFF7),
    surface = Color(0xFF171B2C), onSurface = Color(0xFFEDEFF7),
    surfaceVariant = Color(0xFF232944), onSurfaceVariant = Color(0xFFA8B0C5),
    outline = Color(0xFF3A4260)
)

/**
 * App theme. Reacts to the user's Settings: dark mode, high contrast and text size.
 */
@Composable
fun CampusHireTheme(
    darkTheme: Boolean,
    highContrast: Boolean,
    fontScale: Float,
    content: @Composable () -> Unit
) {
    var scheme = if (darkTheme) DarkColors else LightColors
    if (highContrast) {
        scheme = if (darkTheme) {
            scheme.copy(onSurface = Color.White, onBackground = Color.White, onSurfaceVariant = Color.White, outline = Color.White)
        } else {
            scheme.copy(
                onSurface = Color.Black, onBackground = Color.Black, onSurfaceVariant = Color.Black,
                outline = Color.Black, primary = Color(0xFF0038A8)
            )
        }
    }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * fontScale)) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

