package com.minseonglove.jlptwords.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ===== 현재 디자인 시스템 (Figma: mollu) =====
// Figma "mollu" 디자인 시스템의 타이포그래피 토큰. 레거시는 Type.kt 참고.
// 4단계 위계 · 9개 스타일. 굵기(Bold/Medium/Regular)는 사용처에서 fontWeight 로 지정한다.
// 자간은 피그마와 동일하게 em(폰트 크기 상대값) 기준으로 정의한다.

// TODO(디자인 개편): Noto Sans CJK KR(한/영/일 지원) 폰트를 composeResources 에 번들 후 연결.
// 현재는 메트릭(크기/행간/자간)만 피그마대로 정의하고 글꼴은 시스템 기본으로 대체한다.
val MolluFontFamily: FontFamily = FontFamily.Default

@Immutable
data class MolluTypography(
    val jpDisplay: TextStyle,
    val jpHead: TextStyle,
    val display: TextStyle,
    val head1: TextStyle,
    val head2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val caption1: TextStyle,
    val caption2: TextStyle,
)

fun molluTypography(fontFamily: FontFamily = MolluFontFamily): MolluTypography =
    MolluTypography(
        // JP Display · 64 / 76(1.19) / -0.03em
        jpDisplay =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 64.sp,
                lineHeight = 76.sp,
                letterSpacing = (-0.03).em,
            ),
        // JP Head · 32 / 40(1.25) / -0.02em
        jpHead =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.02).em,
            ),
        // Display · 32 / 40(1.25) / -0.02em
        display =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.02).em,
            ),
        // Head 1 · 24 / 32(1.375) / -0.02em
        head1 =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.02).em,
            ),
        // Head 2 · 20 / 28(1.4) / -0.012em
        head2 =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.012).em,
            ),
        // Body 1 · 16 / 24(1.5) / 0.01em
        body1 =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.01.em,
            ),
        // Body 2 · 14 / 20(1.43) / 0.01em
        body2 =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.01.em,
            ),
        // Caption 1 · 12 / 16(1.33) / 0.03em
        caption1 =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.03.em,
            ),
        // Caption 2 · 10 / 12(1.2) / 0.03em
        caption2 =
            TextStyle(
                fontFamily = fontFamily,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.03.em,
            ),
    )
