package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.StreakRepository

class AddStreak(
    private val repository: StreakRepository,
) {
    suspend operator fun invoke(sessionId: Int) {
        repository.addStreak(sessionId)
    }
}
