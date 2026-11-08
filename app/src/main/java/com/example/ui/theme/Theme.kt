package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RoyalIndigoLight,
    onPrimary = MetallicSilverLight,
    primaryContainer = RoyalIndigo,
    onPrimaryContainer = MetallicSilverLight,
    secondary = CharcoalSlate,
    onSecondary = MetallicSilverLight,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = MetallicSilverLight,
    tertiary = VioletAccent,
    onTertiary = MetallicSilverLight,
    background = ObsidianBlack,
    onBackground = MetallicSilverLight,
    surface = DarkSurface,
    onSurface = MetallicSilverLight,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = MetallicSilverMuted,
    outline = CharcoalBorder,
    outlineVariant = CharcoalDark,
    error = RubyExpense,
    onError = MetallicSilverLight
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalIndigo,
    onPrimary = MetallicSilverLight,
    primaryContainer = RoyalIndigoLight,
    onPrimaryContainer = MetallicSilverLight,
    secondary = CharcoalSlate,
    onSecondary = MetallicSilverLight,
    secondaryContainer = MetallicSilverLight,
    onSecondaryContainer = ObsidianBlack,
    tertiary = VioletAccent,
    onTertiary = MetallicSilverLight,
    background = MetallicSilverLight,
    onBackground = ObsidianBlack,
    surface = androidx.compose.ui.graphics.Color(0xFFF3F4F6),
    onSurface = ObsidianBlack,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    onSurfaceVariant = CharcoalSlate,
    outline = MetallicSilverMuted,
    outlineVariant = MetallicSilver,
    error = RubyExpense,
    onError = MetallicSilverLight
)

@Composable
fun AetherPayTheme(
    darkTheme: Boolean = true, // Default to OLED dark mode as specified
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ObsidianBlack.toArgb()
                window.navigationBarColor = ObsidianBlack.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AetherTypography,
        content = content
    )
}

// Retain alias for any boilerplate caller
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = AetherPayTheme(darkTheme = darkTheme, content = content)
