package com.example.shuttleapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/** Maps the Shuttle roles onto Material 3 so stock components (fields, dialogs, menus) follow the theme. */
private fun ShuttleColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = surfaceQuiet,
        onPrimaryContainer = textPrimary,
        secondary = success,
        onSecondary = onSuccess,
        secondaryContainer = successSoft,
        onSecondaryContainer = textPrimary,
        tertiary = warning,
        onTertiary = onSuccess,
        tertiaryContainer = warningSoft,
        onTertiaryContainer = warningText,
        background = canvas,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceSubtle,
        onSurfaceVariant = textSecondary,
        surfaceTint = primary,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = surfaceSubtle,
        inverseSurface = textPrimary,
        inverseOnSurface = canvas,
        outline = border,
        outlineVariant = divider,
        error = error,
        onError = onError,
        errorContainer = errorSoft,
        onErrorContainer = error,
        scrim = scrim
    )
}

@Composable
fun ShuttleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkShuttleColors else LightShuttleColors
    val systemUiController = rememberSystemUiController()

    // The app bar band is dark in both themes, so status bar icons stay light.
    SideEffect {
        systemUiController.setStatusBarColor(color = colors.brandBand, darkIcons = false)
    }
    CompositionLocalProvider(LocalShuttleColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = Typography,
            content = content
        )
    }
}
