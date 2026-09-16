package com.minseonglove.jlptwords.ad

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.presentation.kmp.GADNativeAd

@OptIn(ExperimentalForeignApi::class)
actual class PlatformNativeAd(
    val nativeAd: GADNativeAd,
) : PlatformAd {
    actual val headline: String?
        get() = nativeAd.headline

    actual val callToAction: String?
        get() = nativeAd.callToAction
}
