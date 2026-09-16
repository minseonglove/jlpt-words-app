package com.minseonglove.jlptwords.ui.adzero

sealed interface AdZeroIntent {
    data object Initialize : AdZeroIntent

    data object ShowRewardedAd : AdZeroIntent

    data object RequestDismiss : AdZeroIntent

    data object ShowLoadFailedMessage : AdZeroIntent

    data object AddAdZeroTime : AdZeroIntent

    data object EnableAdZeroButton : AdZeroIntent
}
