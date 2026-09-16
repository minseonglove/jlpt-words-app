package com.minseonglove.jlptwords.ad

import com.minseonglove.jlptwords.util.currentRootViewController
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.presentation.kmp.GADRewardedAd

@OptIn(ExperimentalForeignApi::class)
actual class PlatformRewardedAd(
    private val rewardedAd: GADRewardedAd,
) : PlatformAd {
    // GADFullScreenContentDelegate 는 weak 프로퍼티다. 강한 참조로 붙잡지 않으면 대입 직후
    // 해제돼 노출·실패 콜백이 오지 않고, 광고 제거 다이얼로그가 로딩 상태에서 멈춘다.
    private var contentDelegate: FullScreenContentDelegate? = null

    actual fun show(onUserEarnedReward: () -> Unit) {
        currentRootViewController()?.let { viewController ->
            rewardedAd.presentFromRootViewController(
                rootViewController = viewController,
                userDidEarnRewardHandler = onUserEarnedReward,
            )
        }
    }

    actual fun setFullScreenContentCallback(callback: PlatformFullScreenContentCallback) {
        val delegate = FullScreenContentDelegate(callback)
        contentDelegate = delegate
        rewardedAd.fullScreenContentDelegate = delegate
    }
}
