package com.minseonglove.jlptwords.util

import androidx.compose.runtime.Composable

/**
 * 컴포지션에 남아 있는 동안 상태바 아이콘·텍스트를 어두운 색으로 그린다.
 *
 * 앱 기본값은 검정 상단바([com.minseonglove.jlptwords.ui.base.MolluTopBar])에 맞춘 밝은색이므로,
 * 상단바 없이 밝은 배경이 상태바까지 닿는 화면에서만 호출한다.
 */
@Composable
expect fun DarkStatusBarIcons()
