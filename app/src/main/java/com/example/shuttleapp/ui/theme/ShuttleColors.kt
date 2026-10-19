package com.example.shuttleapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic color roles. Read them with `ShuttleTheme.colors`; never use raw palette values in UI code. */
@Immutable
data class ShuttleColors(
    val isDark: Boolean,
    // Backgrounds
    val canvas: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceQuiet: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    // Lines
    val border: Color,
    val divider: Color,
    // Brand
    val primary: Color,
    val onPrimary: Color,
    val brandBand: Color,
    val brandBandDeep: Color,
    val onBrandBand: Color,
    val onBrandBandMuted: Color,
    val brandBandLine: Color,
    val brandAccent: Color,
    // Status
    val success: Color,
    val onSuccess: Color,
    val successText: Color,
    val successSoft: Color,
    val warning: Color,
    val warningText: Color,
    val warningSoft: Color,
    val error: Color,
    val onError: Color,
    val errorSoft: Color,
    // Overlays
    val scrim: Color,
    val shadow: Color,
    // Illustrations
    val illoBuilding: Color,
    val illoRoad: Color,
    val illoRoadMarking: Color,
    val busBody: Color,
    val busTrim: Color,
    val busGlass: Color,
    val busLight: Color,
    val busShadow: Color,
    val highlight: Color
)

val LightShuttleColors = ShuttleColors(
    isDark = false,
    canvas = WarmWhite,
    surface = SurfaceWhite,
    surfaceSubtle = SurfaceSubtle,
    surfaceQuiet = MistBlue,
    textPrimary = Midnight,
    textSecondary = TextSecondary,
    textDisabled = DisabledText,
    border = BorderColor,
    divider = DividerColor,
    primary = Midnight,
    onPrimary = PureWhite,
    brandBand = Midnight,
    brandBandDeep = PureBlack,
    onBrandBand = PureWhite,
    onBrandBandMuted = PureWhite.copy(alpha = 0.75f),
    brandBandLine = PureWhite.copy(alpha = 0.06f),
    brandAccent = BrandBlue,
    success = Mint,
    onSuccess = Midnight,
    successText = MintDark,
    successSoft = MintSoft,
    warning = Warning,
    warningText = WarningText,
    warningSoft = WarningSoft,
    error = ErrorRed,
    onError = PureWhite,
    errorSoft = ErrorSoft,
    scrim = Midnight.copy(alpha = 0.56f),
    shadow = PureBlack,
    illoBuilding = MistBlue.copy(alpha = 0.7f),
    illoRoad = BrandBlue,
    illoRoadMarking = PureWhite.copy(alpha = 0.7f),
    busBody = Midnight,
    busTrim = Navy,
    busGlass = MistBlue,
    busLight = WarningSoft,
    busShadow = PureBlack.copy(alpha = 0.14f),
    highlight = PureWhite
)

val DarkShuttleColors = ShuttleColors(
    isDark = true,
    canvas = NightCanvas,
    surface = NightSurface,
    surfaceSubtle = NightSurfaceSubtle,
    surfaceQuiet = NightSurfaceQuiet,
    textPrimary = NightTextPrimary,
    textSecondary = NightTextSecondary,
    textDisabled = NightTextDisabled,
    border = NightBorder,
    divider = NightDivider,
    primary = NightPrimary,
    onPrimary = Midnight,
    brandBand = NightBand,
    brandBandDeep = PureBlack,
    onBrandBand = NightTextPrimary,
    onBrandBandMuted = NightTextPrimary.copy(alpha = 0.75f),
    brandBandLine = PureWhite.copy(alpha = 0.06f),
    brandAccent = NightBrandAccent,
    success = Mint,
    onSuccess = Midnight,
    successText = Mint,
    successSoft = NightMintSoft,
    warning = NightWarning,
    warningText = NightWarningText,
    warningSoft = NightWarningSoft,
    error = NightError,
    onError = NightOnError,
    errorSoft = NightErrorSoft,
    scrim = PureBlack.copy(alpha = 0.64f),
    shadow = PureBlack,
    illoBuilding = NightSurfaceQuiet.copy(alpha = 0.8f),
    illoRoad = NightBusTrim,
    illoRoadMarking = PureWhite.copy(alpha = 0.5f),
    busBody = NightBusBody,
    busTrim = NightBusTrim,
    busGlass = NightBusGlass,
    busLight = NightWarningText,
    busShadow = PureBlack.copy(alpha = 0.35f),
    highlight = PureWhite
)

val LocalShuttleColors = staticCompositionLocalOf { LightShuttleColors }

/** Entry point for the app's design tokens inside a [ShuttleAppTheme]. */
object ShuttleTheme {
    val colors: ShuttleColors
        @Composable
        @ReadOnlyComposable
        get() = LocalShuttleColors.current
}
