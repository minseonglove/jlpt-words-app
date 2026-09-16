package com.minseonglove.jlptwords.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ===== 레거시 디자인 시스템 =====
// 개편 전 테마(MaterialTheme 기반). 신규 mollu 테마는 MolluTheme.kt 참고.

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryGradientStart,
        onPrimary = SurfaceLight,
        primaryContainer = PrimaryGradientEnd,
        onPrimaryContainer = SurfaceLight,
        secondary = SecondaryGradientStart,
        onSecondary = SurfaceLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = TextPrimary,
        tertiary = AccentGradientStart,
        onTertiary = SurfaceLight,
        tertiaryContainer = AccentContainerLight,
        onTertiaryContainer = TextPrimary,
        background = BackgroundLight,
        onBackground = TextPrimary,
        surface = SurfaceLight,
        onSurface = TextPrimary,
        surfaceVariant = BackgroundLight,
        onSurfaceVariant = TextSecondary,
        outline = BorderLight,
        outlineVariant = BorderLight,
        error = ErrorLight,
        onError = SurfaceLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = ErrorLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryGradientStart,
        onPrimary = SurfaceLight,
        primaryContainer = PrimaryGradientStart,
        onPrimaryContainer = TextPrimaryDark,
        secondary = SecondaryGradientStart,
        onSecondary = TextPrimaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = SecondaryGradientStart,
        tertiary = AccentGradientEnd,
        onTertiary = TextPrimaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = AccentGradientStart,
        background = BackgroundDark,
        onBackground = TextPrimaryDark,
        surface = SurfaceDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = TextSecondaryDark,
        outline = BorderDark,
        outlineVariant = BorderDark,
        error = ErrorDark,
        onError = BackgroundDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = ErrorDark,
    )

private val LocalColors =
    staticCompositionLocalOf {
        JLPTwordsLocalColors(
            surfaceVariantBorder = Color.Unspecified,
            surfaceVariantText = Color.Unspecified,
            surfaceVariantBackground = Color.Unspecified,
            surfaceVariantProgressBackground = Color.Unspecified,
            surfaceVariantDivider = Color.Unspecified,
            surfaceCardBackgroundGradientStart = Color.Unspecified,
            surfaceCardBackgroundGradientEnd = Color.Unspecified,
            surfaceCardText = Color.Unspecified,
            disabledGradientStart = Color.Unspecified,
            disabledGradientEnd = Color.Unspecified,
        )
    }

val MaterialTheme.localColors: JLPTwordsLocalColors
    @Composable get() = LocalColors.current

@Composable
fun JLPTWordsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val localColors =
        if (darkTheme) {
            JLPTwordsLocalColors(
                surfaceVariantBorder = surfaceVariantBorderDark,
                surfaceVariantText = surfaceVariantTextDark,
                surfaceVariantBackground = surfaceVariantBackgroundDark,
                surfaceVariantProgressBackground = surfaceProgressBackgroundDark,
                surfaceVariantDivider = surfaceVariantDividerDark,
                surfaceCardBackgroundGradientStart = surfaceCardBackgroundGradientStartDark,
                surfaceCardBackgroundGradientEnd = surfaceCardBackgroundGradientEndDark,
                surfaceCardText = surfaceCardTextDark,
                disabledGradientStart = disabledGradientStartDark,
                disabledGradientEnd = disabledGradientEndDark,
            )
        } else {
            JLPTwordsLocalColors(
                surfaceVariantBorder = surfaceVariantBorderLight,
                surfaceVariantText = surfaceVariantTextLight,
                surfaceVariantBackground = surfaceVariantBackgroundLight,
                surfaceVariantProgressBackground = surfaceProgressBackgroundLight,
                surfaceVariantDivider = surfaceVariantDividerLight,
                surfaceCardBackgroundGradientStart = surfaceCardBackgroundGradientStartLight,
                surfaceCardBackgroundGradientEnd = surfaceCardBackgroundGradientEndLight,
                surfaceCardText = surfaceCardTextLight,
                disabledGradientStart = disabledGradientStart,
                disabledGradientEnd = disabledGradientEnd,
            )
        }
    CompositionLocalProvider(LocalColors provides localColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
