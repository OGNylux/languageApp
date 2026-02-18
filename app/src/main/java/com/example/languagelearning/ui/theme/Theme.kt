package com.example.languagelearning.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkColorScheme = darkColorScheme(
    primary = AppPrimaryNight,
    secondary = AppSecondaryNight,
    tertiary = AppTertiary,
    background = AppBackgroundNight,
    surface = AppSurfaceNight,
    onPrimary = AppOnPrimaryNight,
    onSecondary = AppOnBackgroundNight,
    onTertiary = AppOnSurfaceNight,
    onBackground = AppOnBackgroundNight,
    onSurface = AppOnSurfaceNight,
    error = AppError
)

private val AppLightColorScheme = lightColorScheme(
    primary = AppPrimary,
    secondary = AppSecondary,
    tertiary = AppTertiary,
    background = AppBackground,
    surface = AppSurface,
    surfaceVariant = AppSurfaceVariant,
    onPrimary = AppOnPrimary,
    onSecondary = AppOnBackground,
    onTertiary = AppOnSurface,
    onBackground = AppOnBackground,
    onSurface = AppOnSurface,
    error = AppError
)

@Composable
fun LanguageLearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}