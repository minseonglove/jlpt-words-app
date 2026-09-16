package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.StreakDataSource
import com.minseonglove.jlptwords.db.entity.StreakEntity
import com.minseonglove.jlptwords.entity.StreakInfo
import com.minseonglove.jlptwords.util.getCurrentDaysFromEpoch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StreakRepositoryImpl(
    private val streakDataSource: StreakDataSource,
) : StreakRepository {
    override suspend fun addStreak(sessionId: Int) {
        streakDataSource.addStreak(
            streakEntity =
                StreakEntity(
                    sessionId = sessionId,
                    days = getCurrentDaysFromEpoch(),
                ),
        )
    }

    override suspend fun getStreakInfo(): StreakInfo {
        val streakDays = streakDataSource.getAllStreakDays()
        val currentDaysFromEpoch = getCurrentDaysFromEpoch()
        val streak =
            getStreak(
                streakDays = streakDays,
                currentDaysFromEpoch = currentDaysFromEpoch,
            )

        return StreakInfo(
            streak = streak,
            isStudyToday = currentDaysFromEpoch == streakDays.firstOrNull(),
            // getAllStreakDays 는 DISTINCT 조회라 그대로 학습일 수가 된다.
            totalDays = streakDays.size,
        )
    }

    private suspend fun getStreak(
        streakDays: List<Int>,
        currentDaysFromEpoch: Int,
    ): Int =
        withContext(Dispatchers.Default) {
            val lastStudiedDay = streakDays.firstOrNull() ?: return@withContext 0

            val daysDifference = currentDaysFromEpoch - lastStudiedDay
            if (daysDifference > 1) {
                return@withContext 0
            }

            // 연속 출석하지 않는 포지션 찾기
            val breakIndex =
                streakDays
                    .windowed(2)
                    .indexOfFirst { (current, next) ->
                        current - next > 1
                    }.takeIf {
                        it != -1
                    }

            if (breakIndex != null) {
                breakIndex + 1
            } else {
                streakDays.size
            }
        }
}
