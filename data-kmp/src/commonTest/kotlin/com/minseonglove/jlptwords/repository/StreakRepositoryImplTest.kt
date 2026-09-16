package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.StreakDataSource
import com.minseonglove.jlptwords.db.dao.StreakDao
import com.minseonglove.jlptwords.db.entity.StreakEntity
import com.minseonglove.jlptwords.entity.StreakInfo
import com.minseonglove.jlptwords.util.getCurrentDaysFromEpoch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StreakRepositoryImplTest {
    /** days 는 내림차순(최신 먼저)으로 도착한다고 가정(DAO 쿼리 계약). */
    private class FakeStreakDao(
        private val daysDesc: List<Int>,
    ) : StreakDao {
        override suspend fun addStreak(streak: StreakEntity) = Unit

        override suspend fun getAllStreakDaysDesc(): List<Int> = daysDesc

        override suspend fun getAllStreakDaysAsc(): List<Int> = daysDesc.sorted()
    }

    // 시스템 시계와 무관하게 결과가 고정되도록, '오늘' 기준 상대 일수로 학습일을 구성한다.
    private val today = getCurrentDaysFromEpoch()

    private fun repository(daysDesc: List<Int>) = StreakRepositoryImpl(StreakDataSource(FakeStreakDao(daysDesc)))

    @Test
    fun `오늘 포함 연속 학습이면 연속일수와 총 출석일이 모두 일치한다`() =
        runTest {
            val result = repository(listOf(today, today - 1, today - 2)).getStreakInfo()

            assertEquals(StreakInfo(streak = 3, isStudyToday = true, totalDays = 3), result)
        }

    @Test
    fun `어제까지 연속 학습했고 오늘은 안 했으면 연속은 유지되고 오늘 학습은 false 다`() =
        runTest {
            val result = repository(listOf(today - 1, today - 2)).getStreakInfo()

            assertEquals(StreakInfo(streak = 2, isStudyToday = false, totalDays = 2), result)
        }

    @Test
    fun `마지막 학습이 이틀 이상 전이면 연속일수는 0이 된다`() =
        runTest {
            val result = repository(listOf(today - 2, today - 3)).getStreakInfo()

            assertEquals(StreakInfo(streak = 0, isStudyToday = false, totalDays = 2), result)
        }

    @Test
    fun `중간에 하루라도 끊겼으면 최근 연속 구간까지만 연속일수로 센다`() =
        runTest {
            // 오늘, 어제는 연속이지만 그 앞(today-3)에서 끊김
            val result = repository(listOf(today, today - 1, today - 3, today - 4)).getStreakInfo()

            assertEquals(StreakInfo(streak = 2, isStudyToday = true, totalDays = 4), result)
        }

    @Test
    fun `학습 기록이 없으면 모든 값이 0이고 오늘 학습은 false 다`() =
        runTest {
            val result = repository(emptyList()).getStreakInfo()

            assertEquals(StreakInfo(streak = 0, isStudyToday = false, totalDays = 0), result)
        }
}
