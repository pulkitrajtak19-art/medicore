package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SoftEmeraldAccent,
    onPrimary = Color.White,
    primaryContainer = SoftEmeraldContainer,
    onPrimaryContainer = SoftEmeraldDark,
    secondary = LightBluePrimary,
    onSecondary = Color.White,
    secondaryContainer = LightBlueContainer,
    onSecondaryContainer = LightBlueOnContainer,
    tertiary = SoftEmeraldDark,
    onTertiary = Color.White,
    background = WarmWhiteBackground,
    onBackground = TextDarkSlate,
    surface = WarmWhiteSurface,
    onSurface = TextDarkSlate,
    surfaceVariant = WarmWhiteSubtle,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    outlineVariant = SoftEmeraldBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftEmeraldAccent,
    onPrimary = Color(0xFF064E3B),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = LightBlueSoft,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFF6EE7B7),
    onTertiary = Color(0xFF064E3B),
    background = Color(0xFF12181F),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep clean brand styling
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
