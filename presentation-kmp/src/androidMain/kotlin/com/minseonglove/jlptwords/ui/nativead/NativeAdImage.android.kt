package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap
import com.minseonglove.jlptwords.ad.PlatformNativeAd

@Composable
actual fun NativeAdImage(
    modifier: Modifier,
    nativeAd: PlatformNativeAd,
) {
    val bitmap =
        nativeAd.nativeAd.icon
            ?.drawable
            ?.toBitmap()

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}
