package com.minseonglove.jlptwords.ad

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.minseonglove.jlptwords.util.ActivityProvider

actual class PlatformInterstitialAd(
    private val interstitialAd: InterstitialAd,
) : PlatformAd {
    actual fun show() {
        val activity = ActivityProvider.currentActivity
        activity?.let {
            interstitialAd.show(it)
        }
    }

    actual fun setFullScreenContentCallback(callback: PlatformFullScreenContentCallback) {
        interstitialAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    callback.onAdFailedToShowFullScreenContent()
                }

                override fun onAdShowedFullScreenContent() {
                    callback.onAdShowedFullScreenContent()
                }

                override fun onAdDismissedFullScreenContent() {
                    callback.onAdDismissedFullScreenContent()
                }
            }
    }
}
