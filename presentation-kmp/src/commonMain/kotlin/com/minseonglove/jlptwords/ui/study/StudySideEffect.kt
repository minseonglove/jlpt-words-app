package com.minseonglove.jlptwords.ui.study

import com.minseonglove.jlptwords.ad.PlatformInterstitialAd

sealed interface StudySideEffect {
    data object NavigateToBack : StudySideEffect

    data class NavigateToWordDetail(
        val kanji: String,
        val pronunciation: String,
    ) : StudySideEffect

    data class ShowInterstitialAd(
        val interstitialAd: PlatformInterstitialAd,
    ) : StudySideEffect

    data object ShowCopyFailureMessage : StudySideEffect

    /** OS 가 복사 안내를 띄우지 않는 환경에서만 발행된다. [isSystemClipboardFeedbackShown] 참고. */
    data object ShowCopySuccessMessage : StudySideEffect

    /** 기기 볼륨이 0이라 발음이 들리지 않는 상태 안내. */
    data object ShowVolumeMutedMessage : StudySideEffect

    data object OpenInstallLanguagePack : StudySideEffect

    data object NavigateToSetting : StudySideEffect
}
