package com.minseonglove.jlptwords.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ===== 현재 디자인 시스템 (Figma: mollu) =====
// Figma "mollu" 디자인 시스템의 컬러 토큰. 레거시 컬러는 Color.kt 참고.
// 출처: Figma Style_Guide (file Xj16gvUSIsKukLzON3LF66, node 2076:13825)
// 전면 개편 시 화면별로 아래 토큰(또는 MolluTheme.colorScheme)으로 이전한다.

// ---- 원시(Primitive) 팔레트 ----
val MolluWhite = Color(0xFFFFFFFF)
val MolluBlack = Color(0xFF3F3B36) // 00_Black · 검정
val MolluRed = Color(0xFFD40000) // 99_Red · 하이라이팅
val MolluBlue = Color(0xFF00498D) // 99_Blue · 하이라이팅
val MolluLightGrey = Color(0xFFADA6A6) // 02_Light_Grey · 라인/비활성

// ---- 시맨틱(Semantic) · 역할 기반 ----
val MolluBackgroundNormal = Color(0xFFFFFFFF) // Background/Normal/Normal
val MolluBackgroundAlternative = Color(0xFFF7F7F8) // Background/Normal/Alternative
val MolluLabelStrong = Color(0xFF000000) // Label/Strong
val MolluLabelAlternative = Color(0x9C37383C) // Label/Alternative · #37383C 61%
val MolluLineNormal = Color(0x3870737C) // Line/Normal/Normal · #70737C 22%
val MolluLineStrong = Color(0x8570737C) // Line/Normal/_Strong · #70737C 52%
val MolluFillStrong = Color(0x2970737C) // Fill/Strong · #70737C 16%

// mollu 시맨틱 컬러 묶음. MolluTheme 를 통해 제공/조회한다.
// mollu 는 단일(라이트) 팔레트만 정의되어 있어 다크 스킴은 두지 않는다(피그마에 없음).
@Immutable
data class MolluColorScheme(
    val backgroundNormal: Color,
    val backgroundAlternative: Color,
    val labelStrong: Color,
    val labelAlternative: Color,
    val lineNormal: Color,
    val lineStrong: Color,
    val fillStrong: Color,
    val highlightRed: Color,
    val highlightBlue: Color,
    val black: Color,
    val white: Color,
    val sub: Color,
)

fun molluColorScheme(): MolluColorScheme =
    MolluColorScheme(
        backgroundNormal = MolluBackgroundNormal,
        backgroundAlternative = MolluBackgroundAlternative,
        labelStrong = MolluLabelStrong,
        labelAlternative = MolluLabelAlternative,
        lineNormal = MolluLineNormal,
        lineStrong = MolluLineStrong,
        fillStrong = MolluFillStrong,
        highlightRed = MolluRed,
        highlightBlue = MolluBlue,
        black = MolluBlack,
        white = MolluWhite,
        sub = MolluLightGrey,
    )
