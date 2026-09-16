package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.minseonglove.jlptwords.ad.PlatformNativeAd
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAdImage(
    modifier: Modifier,
    nativeAd: PlatformNativeAd,
) {
    val uiImage = nativeAd.nativeAd.icon?.image

    uiImage?.let { image ->
        UIKitView(
            factory = {
                UIImageView().apply {
                    this.image = image
                    contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                }
            },
            modifier = modifier,
            properties =
                UIKitInteropProperties(
                    isInteractive = false,
                    isNativeAccessibilityEnabled = true,
                ),
        )
    }
}
