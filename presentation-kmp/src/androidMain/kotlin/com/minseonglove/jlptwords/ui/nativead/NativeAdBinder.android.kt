package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.minseonglove.jlptwords.ad.PlatformNativeAd

@Composable
actual fun NativeAdBinder(
    nativeAd: PlatformNativeAd,
) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    // Re-bind when the ad instance changes
    LaunchedEffect(nativeAd) {
        nativeAdView.setNativeAd(nativeAd.nativeAd)
    }
}
