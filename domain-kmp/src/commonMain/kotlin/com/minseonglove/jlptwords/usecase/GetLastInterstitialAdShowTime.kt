package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.AdRepository

class GetLastInterstitialAdShowTime(
    private val adRepository: AdRepository,
) {
    suspend operator fun invoke(): Long {
        return adRepository.getLastInterstitialAdShowTime()
    }
}
