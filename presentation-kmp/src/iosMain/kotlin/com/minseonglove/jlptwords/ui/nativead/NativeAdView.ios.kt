package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.presentation.kmp.GADNativeAdView

@OptIn(ExperimentalForeignApi::class)
internal val LocalNativeAdView = staticCompositionLocalOf<GADNativeAdView?> { null }

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeAdView(
    modifier: Modifier,
    content: @Composable (() -> Unit),
) {
    val nativeAdView = remember { GADNativeAdView() }

    Box(modifier = modifier) {
        UIKitView(
            factory = {
                nativeAdView
            },
            modifier = Modifier.matchParentSize(),
            properties =
                UIKitInteropProperties(
                    isInteractive = true,
                    isNativeAccessibilityEnabled = true,
                ),
        )

        CompositionLocalProvider(LocalNativeAdView provides nativeAdView) {
            content.invoke()
        }
    }
}
