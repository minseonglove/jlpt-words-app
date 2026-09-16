package com.minseonglove.jlptwords.ui.nativead

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun NativeAdIconView(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.iconView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}
