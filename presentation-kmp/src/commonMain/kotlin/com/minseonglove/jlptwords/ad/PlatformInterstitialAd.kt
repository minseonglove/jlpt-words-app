package com.minseonglove.jlptwords.ad

expect class PlatformInterstitialAd : PlatformAd {
    fun show()

    fun setFullScreenContentCallback(callback: PlatformFullScreenContentCallback)
}
