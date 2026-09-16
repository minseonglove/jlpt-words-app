package com.minseonglove.jlptwords.ui.exit

import com.minseonglove.jlptwords.ad.PlatformNativeAd

sealed interface ExitAdState {
    data object AdZero : ExitAdState

    data object Loading : ExitAdState

    data class AdReady(
        val nativeAd: PlatformNativeAd,
    ) : ExitAdState
}
