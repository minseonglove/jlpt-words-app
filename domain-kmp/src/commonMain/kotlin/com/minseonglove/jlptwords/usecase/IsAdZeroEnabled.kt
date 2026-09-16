package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.AdRepository
import com.minseonglove.jlptwords.util.runCatchingCancellable

class IsAdZeroEnabled(
    private val adRepository: AdRepository,
) {
    suspend operator fun invoke(): Boolean {
        return runCatchingCancellable {
            adRepository.isAdZeroEnabled()
        }.getOrDefault(false)
    }
}
