package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.StreakInfo
import com.minseonglove.jlptwords.repository.StreakRepository

class GetStreakInfo(
    private val repository: StreakRepository,
) {
    suspend operator fun invoke(): StreakInfo {
        return repository.getStreakInfo()
    }
}
