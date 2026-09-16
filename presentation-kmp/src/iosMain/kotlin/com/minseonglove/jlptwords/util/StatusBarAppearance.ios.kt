package com.minseonglove.jlptwords.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * iOS 의 상태바 스타일은 Compose 가 올라간 UIViewController 가 아니라 그 상위 컨트롤러가 정하고,
 * Compose 쪽에서는 직접 바꿀 수 없다. 앱 진입점의 루트 컨트롤러가 [StatusBarAppearance.listener] 로
 * 자신을 등록해 스타일 변경을 대신 수행한다.
 */
interface StatusBarStyleListener {
    fun onDarkIconsChanged(darkIcons: Boolean)
}

object StatusBarAppearance {
    var listener: StatusBarStyleListener? = null
}

@Composable
actual fun DarkStatusBarIcons() {
    DisposableEffect(Unit) {
        StatusBarAppearance.listener?.onDarkIconsChanged(darkIcons = true)
        onDispose { StatusBarAppearance.listener?.onDarkIconsChanged(darkIcons = false) }
    }
}
