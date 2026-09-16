package com.minseonglove.jlptwords.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// ===== 현재 디자인 시스템 (Figma: mollu) =====
// mollu 디자인 시스템 테마 골격. Material(MaterialTheme)을 따르지 않는 독립 테마로,
// CompositionLocal 로 mollu 컬러/타이포를 제공한다. 레거시 테마는 Theme.kt(JLPTWordsTheme) 참고.
//
// 사용(전면 개편 시):
//   MolluTheme { /* content */ }            // Provider 로 감싼다
//   MolluTheme.colorScheme.labelStrong      // 컬러 토큰 조회
//   MolluTheme.typography.head1             // 타이포 토큰 조회
//
// 주의:
//  - 아직 어떤 화면에도 적용되어 있지 않은 스캐폴드다. 동작/디자인은 레거시 그대로다.
//  - Material3 컴포넌트(Button 등)는 여전히 MaterialTheme 를 참조하므로, 필요 시
//    JLPTWordsTheme(레거시) 안쪽에 MolluTheme 를 중첩해 토큰만 별도로 끌어다 쓸 수 있다.

val LocalMolluColorScheme =
    staticCompositionLocalOf { molluColorScheme() }

val LocalMolluTypography =
    staticCompositionLocalOf { molluTypography() }

@Composable
fun MolluTheme(
    colorScheme: MolluColorScheme = MolluTheme.colorScheme,
    typography: MolluTypography = MolluTheme.typography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMolluColorScheme provides colorScheme,
        LocalMolluTypography provides typography,
        content = content,
    )
}

// MaterialTheme 와 동일하게, 동명의 object 로 토큰 접근자를 제공한다.
object MolluTheme {
    val colorScheme: MolluColorScheme
        @Composable get() = LocalMolluColorScheme.current

    val typography: MolluTypography
        @Composable get() = LocalMolluTypography.current
}
