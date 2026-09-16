package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.AdRepository

class AddAdZeroTime(
    private val adRepository: AdRepository,
) {
    suspend operator fun invoke(time: Long): Long {
        return adRepository.addAdZeroTime(time)
    }
}
