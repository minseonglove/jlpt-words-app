package com.minseonglove.jlptwords.ad

import com.google.android.gms.ads.nativead.NativeAd

actual class PlatformNativeAd(
    val nativeAd: NativeAd,
) : PlatformAd {
    actual val headline: String?
        get() = nativeAd.headline

    actual val callToAction: String?
        get() = nativeAd.callToAction
}
