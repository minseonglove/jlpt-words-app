package com.minseonglove.jlptwords.ad

interface NativeAdManager : AdManager<PlatformNativeAd> {
    fun startRefreshAd()

    fun stopRefreshAd()
}
