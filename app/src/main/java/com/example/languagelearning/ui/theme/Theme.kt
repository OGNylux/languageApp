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

private val CuteDarkColorScheme = darkColorScheme(
    primary = CutePrimaryNight,
    secondary = CuteSecondaryNight,
    tertiary = CuteTertiary,
    background = CuteBackgroundNight,
    surface = CuteSurfaceNight,
    onPrimary = CuteOnPrimaryNight,
    onSecondary = CuteOnBackgroundNight,
    onTertiary = CuteOnSurfaceNight,
    onBackground = CuteOnBackgroundNight,
    onSurface = CuteOnSurfaceNight,
    error = CuteError
)

private val CuteLightColorScheme = lightColorScheme(
    primary = CutePrimary,
    secondary = CuteSecondary,
    tertiary = CuteTertiary,
    background = CuteBackground,
    surface = CuteSurface,
    surfaceVariant = CuteSurfaceVariant,
    onPrimary = CuteOnPrimary,
    onSecondary = CuteOnBackground,
    onTertiary = CuteOnSurface,
    onBackground = CuteOnBackground,
    onSurface = CuteOnSurface,
    error = CuteError
)

@Composable
fun LanguageLearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CuteDarkColorScheme else CuteLightColorScheme

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