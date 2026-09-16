package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.ContinueSessionResult
import com.minseonglove.jlptwords.entity.ContinueSessionStatus
import com.minseonglove.jlptwords.entity.ContinueStudySession
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.StudyStatus
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudySessionRepository
import com.minseonglove.jlptwords.repository.StudyStatusRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetContinueSessionTest {
    private class FakeStudyStatusRepository(
        private val status: StudyStatus?,
    ) : StudyStatusRepository {
        override suspend fun getStudyStatus(): StudyStatus? = status

        override suspend fun setStudyStatus(status: StudyStatus?) = Unit
    }

    private class FakeStudyRecordRepository(
        private val latest: StudyRecord?,
    ) : StudyRecordRepository {
        override suspend fun getAllStudyRecords(): List<StudyRecord> = emptyList()

        override suspend fun getLatestStudyRecord(): StudyRecord? = latest

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

        override suspend fun getLevelBySessionId(id: Int): JLPTLevel =
            sessionsByLevel.entries
                .firstOrNull { entry -> entry.value.any { it.id == id } }
                ?.key
                ?: error("session $id not registered in fake")
    }

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

    private fun studyStatus(
        sessionId: Int,
        level: JLPTLevel,
        knownCount: Int,
        totalWordSize: Int,
    ) = StudyStatus(
        sessionId = sessionId,
        sessionPosition = 0,
        level = level,
        words = emptyList(),
        totalKnownWordIds = (1..knownCount).toSet(),
        currentKnownWordIds = emptySet(),
        currentPage = 0,
        totalElapsedTimeSeconds = 0,
        currentElapsedTimeSeconds = 0,
        totalWordSize = totalWordSize,
        totalAppearanceCount = 0,
        roundProgresses = emptyList(),
    )

    private fun useCase(
        status: StudyStatus? = null,
        latest: StudyRecord? = null,
        sessionsByLevel: Map<JLPTLevel, List<StudySession>> = emptyMap(),
    ) = GetContinueSession(
        FakeStudyStatusRepository(status),
        FakeStudyRecordRepository(latest),
        FakeStudySessionRepository(sessionsByLevel),
    )

    @Test
    fun `진행 중인 세션이 있으면 그 세션을 진행률과 함께 IN_PROGRESS 로 반환한다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3), session(12, JLPTLevel.N3))
            // 진행률 = 아는 단어 3개 * 100 / 전체 12개 = 25
            val status = studyStatus(sessionId = 11, level = JLPTLevel.N3, knownCount = 3, totalWordSize = 12)

            val result = useCase(status = status, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(
                ContinueSessionResult.Ready(
                    ContinueStudySession(
                        session = sessions[1],
                        sessionIndex = 1,
                        status = ContinueSessionStatus.IN_PROGRESS,
                        progress = 25,
                    ),
                ),
                result,
            )
        }

    @Test
    fun `진행 중 세션의 전체 단어 수가 0이어도 진행률 계산이 0으로 나눠지지 않는다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3))
            val status = studyStatus(sessionId = 10, level = JLPTLevel.N3, knownCount = 0, totalWordSize = 0)

            val result = useCase(status = status, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            val ready = result as ContinueSessionResult.Ready
            assertEquals(0, ready.item.progress)
        }

    @Test
    fun `진행 중 세션도 학습 이력도 없으면 현재 급수의 첫 세션을 NOT_STARTED 로 반환한다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))

            val result = useCase(sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(
                ContinueSessionResult.Ready(
                    ContinueStudySession(sessions[0], sessionIndex = 0, ContinueSessionStatus.NOT_STARTED, progress = 0),
                ),
                result,
            )
        }

    @Test
    fun `가장 최근 완료한 세션의 다음 세션을 NOT_STARTED 로 반환한다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3), session(12, JLPTLevel.N3))
            val latest = StudyRecord(id = 1, sessionId = 11, completionTimeSeconds = 0, accuracy = 0, createAt = 0)

            val result = useCase(latest = latest, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(
                ContinueSessionResult.Ready(
                    ContinueStudySession(sessions[2], sessionIndex = 2, ContinueSessionStatus.NOT_STARTED, progress = 0),
                ),
                result,
            )
        }

    @Test
    fun `급수의 마지막 세션을 완료했으면 다음 급수의 첫 세션을 반환한다`() =
        runTest {
            val n3Sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val n2Sessions = listOf(session(20, JLPTLevel.N2), session(21, JLPTLevel.N2))
            // sessionId 11 = N3 의 마지막 세션
            val latest = StudyRecord(id = 1, sessionId = 11, completionTimeSeconds = 0, accuracy = 0, createAt = 0)

            val result =
                useCase(
                    latest = latest,
                    sessionsByLevel = mapOf(JLPTLevel.N3 to n3Sessions, JLPTLevel.N2 to n2Sessions),
                )(JLPTLevel.N3)

            assertEquals(
                ContinueSessionResult.Ready(
                    ContinueStudySession(n2Sessions[0], sessionIndex = 0, ContinueSessionStatus.NOT_STARTED, progress = 0),
                ),
                result,
            )
        }

    @Test
    fun `N1 의 마지막 세션까지 완료했으면 마지막 세션을 COMPLETED 로 반환한다`() =
        runTest {
            val n1Sessions = listOf(session(40, JLPTLevel.N1), session(41, JLPTLevel.N1))
            val latest = StudyRecord(id = 1, sessionId = 41, completionTimeSeconds = 0, accuracy = 0, createAt = 0)

            val result = useCase(latest = latest, sessionsByLevel = mapOf(JLPTLevel.N1 to n1Sessions))(JLPTLevel.N1)

            assertEquals(
                ContinueSessionResult.Ready(
                    ContinueStudySession(n1Sessions[1], sessionIndex = 1, ContinueSessionStatus.COMPLETED, progress = 100),
                ),
                result,
            )
        }

    @Test
    fun `대상 급수의 세션이 아직 생성되지 않았으면 RequiresInitialization 을 반환한다`() =
        runTest {
            val result = useCase(sessionsByLevel = emptyMap())(JLPTLevel.N3)

            assertEquals(ContinueSessionResult.RequiresInitialization(JLPTLevel.N3), result)
        }

    @Test
    fun `다음 급수의 세션이 아직 없으면 다음 급수에 대한 RequiresInitialization 을 반환한다`() =
        runTest {
            val n3Sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            val latest = StudyRecord(id = 1, sessionId = 11, completionTimeSeconds = 0, accuracy = 0, createAt = 0)

            // N2 세션 미등록 → 다음 급수 N2 초기화 필요
            val result = useCase(latest = latest, sessionsByLevel = mapOf(JLPTLevel.N3 to n3Sessions))(JLPTLevel.N3)

            assertEquals(ContinueSessionResult.RequiresInitialization(JLPTLevel.N2), result)
        }

    @Test
    fun `진행 중 세션 ID 가 세션 목록에 없으면 완료 기록 기반으로 폴백한다`() =
        runTest {
            val sessions = listOf(session(10, JLPTLevel.N3), session(11, JLPTLevel.N3))
            // 세션 목록에 없는 999 → 진행 중 블록을 건너뛰고, 완료 기록도 없으므로 현재 급수 첫 세션
            val status = studyStatus(sessionId = 999, level = JLPTLevel.N3, knownCount = 1, totalWordSize = 10)

            val result = useCase(status = status, sessionsByLevel = mapOf(JLPTLevel.N3 to sessions))(JLPTLevel.N3)

            assertEquals(
                ContinueSessionResult.Ready(
                    ContinueStudySession(sessions[0], sessionIndex = 0, ContinueSessionStatus.NOT_STARTED, progress = 0),
                ),
                result,
            )
        }
}
