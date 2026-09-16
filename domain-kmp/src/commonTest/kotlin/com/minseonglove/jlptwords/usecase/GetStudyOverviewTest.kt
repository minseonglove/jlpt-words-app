package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.OverviewValue
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StreakInfo
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.repository.HomeRepository
import com.minseonglove.jlptwords.repository.StreakRepository
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudySessionRepository
import com.minseonglove.jlptwords.repository.TimeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetStudyOverviewTest {
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

    private class FakeHomeRepository(
        private val studiedWordCount: OverviewValue,
    ) : HomeRepository {
        override suspend fun getTodayExample(level: JLPTLevel): DailyExample? = null

        override suspend fun getStudiedWordCount(): OverviewValue = studiedWordCount
    }

    private class FakeTimeRepository(
        private val nowMillis: Long,
    ) : TimeRepository {
        override suspend fun getRealCurrentTimeMillis(): Long? = nowMillis

        override fun getDeviceCurrentTimeMillis(): Long = nowMillis
    }

    private val todayMillis = 1_700_000_000_000L // 기준 '오늘'
    private val pastMillis = todayMillis - 10L * 24 * 60 * 60 * 1000 // 10일 전 → 어떤 시간대에서도 다른 날짜

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

    private fun build(
        streakInfo: StreakInfo = StreakInfo(streak = 0, isStudyToday = false, totalDays = 0),
        records: List<StudyRecord> = emptyList(),
        sessionsByLevel: Map<JLPTLevel, List<StudySession>> = emptyMap(),
        studiedWordCount: OverviewValue = OverviewValue(0, false),
    ) = GetStudyOverview(
        FakeStreakRepository(streakInfo),
        FakeStudyRecordRepository(records),
        FakeStudySessionRepository(sessionsByLevel),
        FakeHomeRepository(studiedWordCount),
        FakeTimeRepository(todayMillis),
    )

    @Test
    fun `학습 현황의 각 수치를 저장소 값으로 매핑하고 오늘 최초 완료 시 진행률을 변경으로 표시한다`() =
        runTest {
            val sessions = (10..13).map { session(it, JLPTLevel.N3) } // 4개
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = todayMillis),
                    record(sessionId = 11, seconds = 200, createAt = todayMillis),
                )
            val useCase =
                build(
                    streakInfo = StreakInfo(streak = 5, isStudyToday = true, totalDays = 20),
                    records = records,
                    sessionsByLevel = mapOf(JLPTLevel.N3 to sessions),
                    studiedWordCount = OverviewValue(42, isUpdatedToday = true),
                )

            val result = useCase(JLPTLevel.N3)

            assertEquals(OverviewValue(5, isUpdatedToday = true), result.streakDays)
            assertEquals(OverviewValue(20, isUpdatedToday = true), result.totalAttendanceDays)
            assertEquals(OverviewValue(300, isUpdatedToday = true), result.totalStudyTimeSeconds)
            assertEquals(OverviewValue(42, isUpdatedToday = true), result.studiedWordCount)
            // 완료 세션 2개 / 전체 4개 = 50%, 오늘 최초 완료라 변경됨
            assertEquals(OverviewValue(50, isUpdatedToday = true), result.totalProgressPercent)
        }

    @Test
    fun `오늘 학습 기록이 없으면 학습시간과 진행률은 오늘 변경되지 않은 것으로 표시한다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = pastMillis),
                    record(sessionId = 11, seconds = 200, createAt = pastMillis),
                )

            val result = build(records = records, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(OverviewValue(300, isUpdatedToday = false), result.totalStudyTimeSeconds)
            assertEquals(OverviewValue(100, isUpdatedToday = false), result.totalProgressPercent)
        }

    @Test
    fun `오늘 한 완료가 재학습이면 진행률은 불변이지만 학습시간은 오늘 변경된 것으로 본다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = pastMillis), // 세션10 최초 완료(과거)
                    record(sessionId = 10, seconds = 50, createAt = todayMillis), // 세션10 오늘 재학습
                    record(sessionId = 11, seconds = 100, createAt = pastMillis), // 세션11 과거만
                )

            val result = build(records = records, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            // 완료 세션 {10, 11} = 2 / 2 = 100%
            assertEquals(100, result.totalProgressPercent.value)
            // 어떤 세션도 '최초 완료'가 오늘이 아니므로 진행률은 변경 아님
            assertEquals(false, result.totalProgressPercent.isUpdatedToday)
            // 그러나 오늘 생성된 기록이 있으므로 학습시간은 변경됨
            assertEquals(true, result.totalStudyTimeSeconds.isUpdatedToday)
        }

    @Test
    fun `오늘 최초로 완료한 세션이 하나라도 있으면 진행률이 오늘 변경된 것으로 본다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val records =
                listOf(
                    record(sessionId = 10, seconds = 100, createAt = pastMillis), // 과거
                    record(sessionId = 11, seconds = 100, createAt = todayMillis), // 오늘 최초 완료
                )

            val result = build(records = records, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(true, result.totalProgressPercent.isUpdatedToday)
        }

    @Test
    fun `세션이 없으면 진행률은 0으로 나눠지지 않고 0이 된다`() =
        runTest {
            val records = listOf(record(sessionId = 99, seconds = 100, createAt = todayMillis))

            val result = build(records = records, sessionsByLevel = emptyMap())(JLPTLevel.N3)

            assertEquals(0, result.totalProgressPercent.value)
        }
}
