package com.minseonglove.jlptwords.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun DarkStatusBarIcons() {
    val view = LocalView.current
    val window = ActivityProvider.currentActivity?.window
    DisposableEffect(window, view) {
        if (window == null) return@DisposableEffect onDispose { }
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        // 밝은 상태바 배경 = 어두운 아이콘.
        controller.isAppearanceLightStatusBars = true
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}
