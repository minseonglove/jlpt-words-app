package com.minseonglove.jlptwords.ad

expect class PlatformRewardedAd : PlatformAd {
    fun show(
        onUserEarnedReward: () -> Unit,
    )

    fun setFullScreenContentCallback(callback: PlatformFullScreenContentCallback)
}
