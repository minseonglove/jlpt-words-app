package com.minseonglove.jlptwords.ui.nativead

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.NativeAdView as GoogleNativeAdView

internal val LocalNativeAdView = staticCompositionLocalOf<NativeAdView?> { null }

@Composable
actual fun NativeAdView(
    modifier: Modifier,
    content: @Composable (() -> Unit),
) {
    val localContext = LocalContext.current
    val nativeAdView = remember { GoogleNativeAdView(localContext).apply { id = View.generateViewId() } }

    AndroidView(
        factory = {
            nativeAdView.apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                addView(
                    ComposeView(context).apply {
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                        setContent {
                            CompositionLocalProvider(LocalNativeAdView provides nativeAdView) {
                                content.invoke()
                            }
                        }
                    },
                )
            }
        },
        modifier = modifier,
    )
}
