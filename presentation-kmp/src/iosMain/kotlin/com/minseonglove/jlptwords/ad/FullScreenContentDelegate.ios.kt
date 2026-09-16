package com.minseonglove.jlptwords.ad

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.darwin.NSObject
import swiftPMImport.jlpt.words.presentation.kmp.GADFullScreenContentDelegateProtocol
import swiftPMImport.jlpt.words.presentation.kmp.GADFullScreenPresentingAdProtocol

@OptIn(ExperimentalForeignApi::class)
class FullScreenContentDelegate(
    private val callback: PlatformFullScreenContentCallback,
) : NSObject(),
    GADFullScreenContentDelegateProtocol {
    override fun adDidRecordImpression(ad: GADFullScreenPresentingAdProtocol) {
        // 필요시 구현
    }

    override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
        callback.onAdDismissedFullScreenContent()
    }

    override fun adWillDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
        // 필요시 구현
    }

    override fun adWillPresentFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
        callback.onAdShowedFullScreenContent()
    }

    override fun ad(
        ad: GADFullScreenPresentingAdProtocol,
        didFailToPresentFullScreenContentWithError: NSError,
    ) {
        callback.onAdFailedToShowFullScreenContent()
    }
}
