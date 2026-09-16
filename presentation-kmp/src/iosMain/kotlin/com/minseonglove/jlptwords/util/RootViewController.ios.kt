package com.minseonglove.jlptwords.util

import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene

/**
 * 전면 광고 등 UIKit 표시에 쓸 루트 뷰 컨트롤러.
 *
 * UIApplication.keyWindow 는 iOS 13 멀티신 도입과 함께 deprecated 되었고 신 기반 앱에서는
 * nil 을 돌려줄 수 있어, 활성 신의 key window 에서 직접 찾는다.
 */
internal fun currentRootViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes
    val windowScenes = scenes.filterIsInstance<UIWindowScene>()
    val scene =
        windowScenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?: windowScenes.firstOrNull()
    return scene?.keyWindow?.rootViewController
}
