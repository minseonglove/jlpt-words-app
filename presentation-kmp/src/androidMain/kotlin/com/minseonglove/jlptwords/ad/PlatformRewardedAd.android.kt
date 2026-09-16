package com.minseonglove.jlptwords.ad

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.minseonglove.jlptwords.util.ActivityProvider

actual class PlatformRewardedAd(
    private val rewardedAd: RewardedAd,
) : PlatformAd {
    actual fun show(
        onUserEarnedReward: () -> Unit,
    ) {
        val activity = ActivityProvider.currentActivity
        activity?.let {
            rewardedAd.show(
                it,
            ) {
                onUserEarnedReward()
            }
        }
    }

    actual fun setFullScreenContentCallback(callback: PlatformFullScreenContentCallback) {
        rewardedAd.fullScreenContentCallback =
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
