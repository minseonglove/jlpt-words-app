package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.StreakDao
import com.minseonglove.jlptwords.db.entity.StreakEntity

class StreakDataSource(
    private val streakDao: StreakDao,
) {
    suspend fun addStreak(
        streakEntity: StreakEntity,
    ) {
        streakDao.addStreak(streakEntity)
    }

    suspend fun getAllStreakDays(
        isDesc: Boolean = true,
    ): List<Int> {
        return if (isDesc) {
            streakDao.getAllStreakDaysDesc()
        } else {
            streakDao.getAllStreakDaysAsc()
        }
    }
}
