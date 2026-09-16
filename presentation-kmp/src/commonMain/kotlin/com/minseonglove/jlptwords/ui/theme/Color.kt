package com.minseonglove.jlptwords.ui.theme

import androidx.compose.ui.graphics.Color

// ===== 레거시 디자인 시스템 =====
// 개편 전 컬러. 신규 mollu 컬러는 MolluColor.kt 참고. 전면 개편 시 mollu 토큰으로 이전 예정.

val PrimaryGradientStart = Color(0xFF667eea)
val PrimaryGradientEnd = Color(0xFF764ba2)

val SecondaryGradientStart = Color(0xFF5CB85C)
val SecondaryGradientEnd = Color(0xFF4CAF50)
val SecondaryContainerLight = Color(0xFFf8fff8)

val AccentGradientStart = Color(0xFFff9800)
val AccentGradientEnd = Color(0xFFff6f00)
val AccentContainerLight = Color(0xFFfff8f0)

val HighLightGradientStart = Color(0xFFff9a56)
val HighLightGradientEnd = Color(0xFFffad56)

val PassGradientStart = Color(0xFFFF6B6B)
val PassGradientEnd = Color(0xFFEE5A52)

val BackgroundLight = Color(0xFFf5f5f5)
val SurfaceLight = Color(0xFFffffff)
val BorderLight = Color(0xFFe0e0e0)

val TextPrimary = Color(0xFF333333)
val TextSecondary = Color(0xFF666666)
val TextDisabled = Color(0xFF999999)

val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1e1e1e)
val BorderDark = Color(0xFF333333)
val TextPrimaryDark = Color(0xFFe0e0e0)
val TextSecondaryDark = Color(0xFFb0b0b0)

val SecondaryContainerDark = Color(0xFF2e5233)
val TertiaryContainerDark = Color(0xFF4a3728)
val SurfaceVariantDark = Color(0xFF2a2a2a)

val ErrorLight = Color(0xFFd32f2f)
val ErrorContainerLight = Color(0xFFffebee)

val ErrorDark = Color(0xFFf48fb1)
val ErrorContainerDark = Color(0xFF6d1b1b)

// 커스텀
val StatusBadgeBackground =
    SurfaceLight.copy(
        alpha = 0.2f,
    )
val ProgressFill =
    SurfaceLight.copy(
        alpha = 0.9f,
    )
val ProgressBarBackground =
    SurfaceLight.copy(
        alpha = 0.2f,
    )
val DividerColorLight =
    SurfaceLight.copy(
        alpha = 0.2f,
    )

val surfaceVariantBorderLight = Color(0xfff0f0f0)
val surfaceVariantTextLight = Color(0xff666666)
val surfaceVariantBackgroundLight = Color(0xfff8f9fa)
val surfaceProgressBackgroundLight = Color(0xffe2e8f0)
val surfaceVariantDividerLight = Color(0xffe9ecef)
val surfaceVariantBorderDark = Color(0xff404040)
val surfaceVariantTextDark = Color(0xffcccccc)
val surfaceVariantBackgroundDark = Color(0xff404040)
val surfaceProgressBackgroundDark = Color(0xff404040)
val surfaceVariantDividerDark = Color(0xff404040)
val surfaceCardBackgroundGradientStartLight = Color(0xfff8fafc)
val surfaceCardBackgroundGradientEndLight = Color(0xfff1f5f9)
val surfaceCardBackgroundGradientStartDark = Color(0xff3a3a3a)
val surfaceCardBackgroundGradientEndDark = Color(0xff333333)
val surfaceCardTextLight = Color(0xff4a5568)
val surfaceCardTextDark = Color(0xff888888)

// AdZero Dialog specific colors
val disabledGradientStart = Color(0xffe0e0e0)
val disabledGradientEnd = Color(0xffbdbdbd)
val disabledGradientStartDark = Color(0xff404040)
val disabledGradientEndDark = Color(0xff2a2a2a)
