package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StreakInfo
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.repository.StreakRepository
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudySessionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetStudyStatisticTest {
    private class FakeStreakRepository(
        private val info: StreakInfo,
    ) : StreakRepository {
        override suspend fun addStreak(sessionId: Int) = Unit

        override suspend fun getStreakInfo(): StreakInfo = info
    }

    private class FakeStudyRecordRepository(
        private val records: List<StudyRecord>,
    ) : StudyRecordRepository {
        override suspend fun getAllStudyRecords(): List<StudyRecord> = records

        override suspend fun getLatestStudyRecord(): StudyRecord? = records.maxByOrNull { it.createAt }

        override suspend fun getStudyRecordsByLevel(level: JLPTLevel): List<StudyRecord> = emptyList()

        override suspend fun getStudyRecordsBySessionId(sessionId: Int): List<StudyRecord> = emptyList()

        override suspend fun addStudyRecord(
            sessionId: Int,
            completionTimeSeconds: Int,
            accuracy: Int,
            createAt: Long,
        ) = Unit
    }

    private class FakeStudySessionRepository(
        private val sessionsByLevel: Map<JLPTLevel, List<StudySession>>,
    ) : StudySessionRepository {
        override suspend fun getWordsByStudySessionId(id: Int): List<Word> = emptyList()

        override suspend fun getStudySessionsByLevel(level: JLPTLevel): List<StudySession> = sessionsByLevel[level] ?: emptyList()

        override suspend fun getStudySessionCount(level: JLPTLevel): Int = sessionsByLevel[level]?.size ?: 0

        override suspend fun getLevelBySessionId(id: Int): JLPTLevel = JLPTLevel.N3
    }

    private val streakInfo = StreakInfo(streak = 3, isStudyToday = true, totalDays = 7)

    // 어떤 시스템 시간대에서도 서로 다른 날짜가 되도록 10일 간격으로 벌린 epoch millis.
    private fun daysApart(index: Int): Long = index.toLong() * 10L * 24 * 60 * 60 * 1000

    private fun session(
        id: Int,
        level: JLPTLevel,
    ) = StudySession(
        id = id,
        level = level,
        type = SessionType.NORMAL,
        startNumber = 1,
        endNumber = 50,
        wordsSize = 50,
    )

    private fun record(
        sessionId: Int,
        seconds: Int,
        createAt: Long,
    ) = StudyRecord(
        id = 0,
        sessionId = sessionId,
        completionTimeSeconds = seconds,
        accuracy = 0,
        createAt = createAt,
    )

    private fun useCase(
        records: List<StudyRecord>,
        sessionsByLevel: Map<JLPTLevel, List<StudySession>>,
    ) = GetStudyStatistic(
        FakeStreakRepository(streakInfo),
        FakeStudyRecordRepository(records),
        FakeStudySessionRepository(sessionsByLevel),
    )

    @Test
    fun `전체 진행률은 해당 급수의 완료 세션 수를 전체 세션 수로 나눈 값이다`() =
        runTest {
            val sessions = (10..13).map { session(it, JLPTLevel.N3) } // 4개 세션
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = daysApart(0)),
                    record(sessionId = 11, seconds = 200, createAt = daysApart(1)),
                )

            val result = useCase(records, mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(0.5f, result.totalProgress)
            assertEquals(streakInfo, result.streakInfo)
        }

    @Test
    fun `세션이 없으면 진행률은 0으로 나눠지지 않고 0이 된다`() =
        runTest {
            val records = listOf(record(sessionId = 99, seconds = 100, createAt = daysApart(0)))

            val result = useCase(records, emptyMap())(JLPTLevel.N3)

            assertEquals(0f, result.totalProgress)
        }

    @Test
    fun `누적 학습시간과 평균 일일 학습시간은 서로 다른 학습일 수로 나눈 값이다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = daysApart(0)),
                    record(sessionId = 11, seconds = 300, createAt = daysApart(1)),
                )

            val result = useCase(records, mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(400, result.totalElapsedTimeSeconds)
            // 학습일 2일 → 400 / 2 = 200
            assertEquals(200, result.averageDailyElapsedTimeSeconds)
        }

    @Test
    fun `같은 날의 여러 기록은 하루로 계산한다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = daysApart(0)),
                    record(sessionId = 11, seconds = 100, createAt = daysApart(0)), // 같은 날
                )

            val result = useCase(records, mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            // 학습일 1일 → 200 / 1 = 200
            assertEquals(200, result.averageDailyElapsedTimeSeconds)
        }

    @Test
    fun `학습 기록이 없으면 누적 평균 학습시간 모두 0이고 0으로 나눠지지 않는다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3))

            val result = useCase(emptyList(), mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(0, result.totalElapsedTimeSeconds)
            assertEquals(0, result.averageDailyElapsedTimeSeconds)
            assertEquals(0f, result.totalProgress)
        }
}
