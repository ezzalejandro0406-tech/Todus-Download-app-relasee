package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Screenshots exact palette: Dark Obsidian Slate
val UdyatDarkBackground = Color(0xFF0C0E14)
val UdyatDarkSurface = Color(0xFF161922)
val UdyatDarkSurfaceVariant = Color(0xFF1F2330)
val UdyatDarkCardBorder = Color(0xFF282D3D)

// Neon Red Brand Palette
val NeonRed = Color(0xFFFF1744)
val NeonRedGlow = Color(0xFFFF2A55)
val NeonRedDark = Color(0xFF8A001F)
val NeonRedContainer = Color(0xFF330B14)

// Legacy alias compatibility
val AccentRed = NeonRed
val AccentRedContainer = NeonRedContainer
val DarkBackground = UdyatDarkBackground
val DarkSurface = UdyatDarkSurface
val DarkSurfaceVariant = UdyatDarkSurfaceVariant
val DarkCardBorder = UdyatDarkCardBorder

// Green banner palette (Screenshots 3, 5, 13)
val BannerGreenBg = Color(0xFF132A1E)
val BannerGreenBorder = Color(0xFF2E7D32)
val BannerGreenAccent = Color(0xFF4CAF50)

// 24 Swatches Grid from Settings Screenshot 11
// Index 0 is the default Radiant Neon Red requested by the user
val AccentSwatches = listOf(
    Color(0xFFFF1744), // 0 Radiant Neon Red (Default)
    Color(0xFFD32F2F), // 1 Crimson
    Color(0xFFFF9800), // 2 Orange
    Color(0xFFFFA000), // 3 Amber
    Color(0xFFFFD600), // 4 Yellow
    Color(0xFFCDDC39), // 5 Lime
    Color(0xFF4CAF50), // 6 Green
    Color(0xFF2E7D32), // 7 Dark Green
    Color(0xFF009688), // 8 Teal
    Color(0xFF00B4D8), // 9 Cyan-Teal
    Color(0xFF0077B6), // 10 Deep Teal
    Color(0xFF00E5FF), // 11 Bright Cyan
    Color(0xFF29B6F6), // 12 Sky Blue
    Color(0xFF2196F3), // 13 Dodger Blue
    Color(0xFF3D5AFE), // 14 Royal Blue
    Color(0xFF651FFF), // 15 Electric Purple
    Color(0xFF7C4DFF), // 16 Light Purple
    Color(0xFF9C27B0), // 17 Purple
    Color(0xFF673AB7), // 18 Deep Purple
    Color(0xFFE91E63), // 19 Pink
    Color(0xFFF06292), // 20 Rose
    Color(0xFFC2185B), // 21 Magenta
    Color(0xFF90A4AE), // 22 Steel Blue
    Color(0xFF78909C)  // 23 Slate Gray
)

val TextWhite = Color(0xFFF0F4FC)
val TextGray = Color(0xFF8E95A5)
val TextDarkGray = Color(0xFF5A6275)

val StatusGreen = Color(0xFF00C853)
val StatusAmber = Color(0xFFFFA000)
val StatusOrange = Color(0xFFFF9800)
val StatusCyan = Color(0xFF00E5FF)
val StatusRed = Color(0xFFFF1744)
