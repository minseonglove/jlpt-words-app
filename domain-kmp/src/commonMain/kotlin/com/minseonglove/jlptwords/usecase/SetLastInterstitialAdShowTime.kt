package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.AdRepository

class SetLastInterstitialAdShowTime(
    private val adRepository: AdRepository,
) {
    suspend operator fun invoke(time: Long) {
        adRepository.setLastInterstitialAdShowTime(time)
    }
}
