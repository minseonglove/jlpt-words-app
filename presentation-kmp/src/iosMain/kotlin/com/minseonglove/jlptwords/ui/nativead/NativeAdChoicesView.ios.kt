package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.presentation.kmp.GADAdChoicesView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAdChoicesView(modifier: Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    UIKitView(
        factory = {
            val adChoicesView = GADAdChoicesView()
            nativeAdView.adChoicesView = adChoicesView
            adChoicesView
        },
        modifier = modifier,
        properties =
            UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
    )
}
