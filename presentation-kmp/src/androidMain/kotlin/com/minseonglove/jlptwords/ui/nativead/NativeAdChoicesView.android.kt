package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.AdChoicesView

@Composable
actual fun NativeAdChoicesView(modifier: Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    AndroidView(
        factory = {
            AdChoicesView(localContext).apply {
                minimumWidth = 15
                minimumHeight = 15
            }
        },
        update = { view -> nativeAdView.adChoicesView = view },
        modifier = modifier,
    )
}
