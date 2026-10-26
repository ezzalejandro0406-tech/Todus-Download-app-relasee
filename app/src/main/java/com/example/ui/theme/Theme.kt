package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeonRedDarkColorScheme = darkColorScheme(
    primary = NeonRedPrimary,
    onPrimary = TextPrimary,
    primaryContainer = NeonRedContainer,
    onPrimaryContainer = NeonRedOnContainer,
    secondary = NeonRedSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = NeonRedDarkSurfaceVariant,
    onSecondaryContainer = NeonRedTertiary,
    tertiary = NeonCyanAccent,
    background = NeonRedDarkBackground,
    onBackground = TextPrimary,
    surface = NeonRedDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = NeonRedDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NeonRedCardBorder,
    error = NeonRedBright
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Radiant Neon Red Dark is the signature identity
    dynamicColor: Boolean = false, // Keep the custom radiant neon red identity
    content: @Composable () -> Unit
) {
    val colorScheme = NeonRedDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NeonRedDarkBackground.toArgb()
                window.navigationBarColor = NeonRedDarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
