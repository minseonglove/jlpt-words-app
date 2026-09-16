package com.minseonglove.jlptwords.ad

expect class PlatformNativeAd : PlatformAd {
    val headline: String?
    val callToAction: String?
}
