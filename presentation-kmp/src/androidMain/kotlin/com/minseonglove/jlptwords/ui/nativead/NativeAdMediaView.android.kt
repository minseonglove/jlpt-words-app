package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView

@Composable
actual fun NativeAdMediaView(modifier: Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    AndroidView(
        factory = { MediaView(localContext) },
        update = { view -> nativeAdView.mediaView = view },
        modifier = modifier,
    )
}
