package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
    accentColor: Color = AccentSwatches[0], // Default 0 is Neon Radiant Red
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = TextWhite,
        primaryContainer = UdyatDarkSurfaceVariant,
        onPrimaryContainer = TextWhite,
        secondary = accentColor,
        onSecondary = TextWhite,
        secondaryContainer = UdyatDarkSurfaceVariant,
        onSecondaryContainer = TextWhite,
        tertiary = StatusCyan,
        background = UdyatDarkBackground,
        onBackground = TextWhite,
        surface = UdyatDarkSurface,
        onSurface = TextWhite,
        surfaceVariant = UdyatDarkSurfaceVariant,
        onSurfaceVariant = TextGray,
        outline = UdyatDarkCardBorder,
        error = StatusRed
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = UdyatDarkBackground.toArgb()
                window.navigationBarColor = UdyatDarkBackground.toArgb()
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
