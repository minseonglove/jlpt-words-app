package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import com.minseonglove.jlptwords.ad.PlatformNativeAd

@Composable
expect fun NativeAdBinder(
    nativeAd: PlatformNativeAd,
)
