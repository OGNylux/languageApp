package com.example.languagelearning.ui.theme

import androidx.compose.ui.graphics.Color

// App cooler teal-green used for both light and dark themes
val AppGreen = Color(0xFF00BFA5)      // cooler teal-green
val AppGreenDark = Color(0xFF00A086)  // slightly darker accent
val AppGreenLight = Color(0xFF66EFD6) // lighter accent

// Light theme palette (off-white + teal-green)
val AppPrimary = AppGreen
val AppPrimaryDark = AppGreenDark
val AppSecondary = AppGreenLight
val AppSecondaryDark = AppGreenDark
val AppTertiary = AppGreenLight
val AppBackground = Color(0xFFF6F7F4)     // professional off-white
val AppSurface = Color(0xFFFFFFFF)        // white surface
val AppSurfaceVariant = Color(0xFFF0F2EE) // slightly darker off-white
val AppOnPrimary = Color(0xFFFFFFFF)      // white text on primary
val AppOnBackground = Color(0xFF071014)   // very dark gray for text on off-white
val AppOnSurface = Color(0xFF071014)      // very dark gray for surface text
val AppError = Color(0xFFEF4444)          // red error

// Dark mode palette (off-black + same teal-green)
val AppPrimaryNight = AppGreen
val AppSecondaryNight = AppGreenLight
val AppBackgroundNight = Color(0xFF0B0F10) // near black / off-black
val AppSurfaceNight = Color(0xFF0F1314)    // slightly lighter surface
val AppOnPrimaryNight = Color(0xFFFFFFFF)  // white text on teal for good contrast
val AppOnBackgroundNight = Color(0xFFE9F7F0) // pale, slightly greenish text on dark background
val AppOnSurfaceNight = Color(0xFFE9F7F0)  // pale text on surfaces
