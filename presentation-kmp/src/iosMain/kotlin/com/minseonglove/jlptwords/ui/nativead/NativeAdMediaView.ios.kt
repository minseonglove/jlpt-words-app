package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.presentation.kmp.GADMediaView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAdMediaView(modifier: Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    UIKitView(
        factory = {
            val mediaView = GADMediaView()
            nativeAdView.mediaView = mediaView
            mediaView
        },
        modifier = modifier,
        properties =
            UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
    )
}
