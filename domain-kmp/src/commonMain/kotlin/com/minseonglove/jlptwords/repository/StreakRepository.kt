package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.StreakInfo

interface StreakRepository {
    suspend fun addStreak(sessionId: Int)

    suspend fun getStreakInfo(): StreakInfo
}
