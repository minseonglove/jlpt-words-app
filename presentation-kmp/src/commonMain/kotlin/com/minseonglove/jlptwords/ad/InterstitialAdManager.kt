package com.minseonglove.jlptwords.ad

interface InterstitialAdManager : AdManager<PlatformInterstitialAd> {
    fun startRefreshAd()

    fun stopRefreshAd()
}
