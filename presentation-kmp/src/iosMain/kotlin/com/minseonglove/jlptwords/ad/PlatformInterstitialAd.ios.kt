package com.minseonglove.jlptwords.ad

import com.minseonglove.jlptwords.util.currentRootViewController
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.presentation.kmp.GADInterstitialAd

@OptIn(ExperimentalForeignApi::class)
actual class PlatformInterstitialAd(
    private val interstitialAd: GADInterstitialAd,
) : PlatformAd {
    // GADFullScreenContentDelegate 는 weak 프로퍼티다. 강한 참조로 붙잡지 않으면 대입 직후
    // 해제돼 노출·실패·닫힘 콜백이 오지 않고, 학습 화면이 다음 라운드로 넘어가지 못한다.
    private var contentDelegate: FullScreenContentDelegate? = null

    actual fun show() {
        currentRootViewController()?.let { viewController ->
            interstitialAd.presentFromRootViewController(viewController)
        }
    }

    actual fun setFullScreenContentCallback(callback: PlatformFullScreenContentCallback) {
        val delegate = FullScreenContentDelegate(callback)
        contentDelegate = delegate
        interstitialAd.fullScreenContentDelegate = delegate
    }
}
