package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAdCallToActionView(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")

    UIKitViewController(
        factory = {
            val viewController =
                ComposeUIViewController {
                    content()
                }
            nativeAdView.callToActionView = viewController.view
            viewController
        },
        modifier = modifier,
        properties =
            UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
    )
}
