package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.minseonglove.jlptwords.ad.PlatformNativeAd

@Composable
expect fun NativeAdImage(
    modifier: Modifier = Modifier,
    nativeAd: PlatformNativeAd,
)
