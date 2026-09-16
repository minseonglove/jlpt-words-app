package com.minseonglove.jlptwords.ui.selection.session

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.StudyStatus
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 세션 선택 화면이 스택 맨 위에 올리는 '현재 세션'이 홈 '계속 학습하기'와 같은 기준을 쓰는지 고정한다.
 */
class CurrentSessionIndexTest {
    private fun sessions(count: Int): List<StudySession> =
        (1..count).map { id ->
            StudySession(
                id = id,
                level = JLPTLevel.N1,
                type = SessionType.NORMAL,
                startNumber = (id - 1) * 50 + 1,
                endNumber = id * 50,
                wordsSize = 50,
            )
        }

    private fun record(
        sessionId: Int,
        createAt: Long,
    ) = StudyRecord(
        id = sessionId,
        sessionId = sessionId,
        completionTimeSeconds = 600,
        accuracy = 80,
        createAt = createAt,
    )

    private fun studyStatus(
        sessionId: Int,
    ) = StudyStatus(
        sessionId = sessionId,
        sessionPosition = sessionId - 1,
        level = JLPTLevel.N1,
        words = emptyList(),
        totalKnownWordIds = emptySet(),
        currentKnownWordIds = emptySet(),
        currentPage = 0,
        totalElapsedTimeSeconds = 0,
        currentElapsedTimeSeconds = 0,
        totalWordSize = 50,
        totalAppearanceCount = 50,
        roundProgresses = emptyList(),
    )

    @Test
    fun `앞 세션을 건너뛰었어도 가장 최근 완료한 세션의 다음을 가리킨다`() {
        // 1장을 건너뛰고 2·3·4장을 완료 → 홈과 같이 5장(index 4)
        val index =
            currentSessionIndexOf(
                sessions = sessions(count = 10),
                latestRecords = listOf(record(2, 100), record(3, 200), record(4, 300)),
                studyStatus = null,
            )

        assertEquals(4, index)
    }

    @Test
    fun `진행 중인 세션이 있으면 그 세션을 가리킨다`() {
        val index =
            currentSessionIndexOf(
                sessions = sessions(count = 10),
                latestRecords = listOf(record(4, 300)),
                studyStatus = studyStatus(sessionId = 2),
            )

        assertEquals(1, index)
    }

    @Test
    fun `진행 중인 세션이 다른 급수면 완료 기록 기준으로 정한다`() {
        val index =
            currentSessionIndexOf(
                sessions = sessions(count = 10),
                latestRecords = listOf(record(4, 300)),
                studyStatus = studyStatus(sessionId = 99),
            )

        assertEquals(4, index)
    }

    @Test
    fun `기록이 없으면 첫 세션을 가리킨다`() {
        val index =
            currentSessionIndexOf(
                sessions = sessions(count = 10),
                latestRecords = emptyList(),
                studyStatus = null,
            )

        assertEquals(0, index)
    }

    @Test
    fun `마지막 세션을 완료했으면 마지막 세션에 머무른다`() {
        val index =
            currentSessionIndexOf(
                sessions = sessions(count = 3),
                latestRecords = listOf(record(3, 300)),
                studyStatus = null,
            )

        assertEquals(2, index)
    }

    @Test
    fun `세션이 없으면 0 이다`() {
        val index =
            currentSessionIndexOf(
                sessions = emptyList(),
                latestRecords = listOf(record(1, 100)),
                studyStatus = null,
            )

        assertEquals(0, index)
    }

    // ── 챕터 매핑 ──

    @Test
    fun `현재 세션이 속한 챕터는 그 세션을 맨 위에 올린다`() {
        val index =
            chapterCurrentSessionIndex(
                currentSessionIndex = 26,
                chapterStartIndex = 24,
                chapterSize = 24,
            )

        assertEquals(2, index)
    }

    @Test
    fun `지나간 챕터는 마지막 장을 맨 위에 올린다`() {
        val index =
            chapterCurrentSessionIndex(
                currentSessionIndex = 26,
                chapterStartIndex = 0,
                chapterSize = 24,
            )

        assertEquals(23, index)
    }

    @Test
    fun `아직 오지 않은 챕터는 첫 장을 맨 위에 올린다`() {
        val index =
            chapterCurrentSessionIndex(
                currentSessionIndex = 26,
                chapterStartIndex = 48,
                chapterSize = 24,
            )

        assertEquals(0, index)
    }

    @Test
    fun `챕터의 마지막 장을 학습하면 그 챕터는 계속 마지막 장을 보여준다`() {
        // 30장(1챕터 24 + 2챕터 6) 급수에서 1챕터의 마지막인 24장을 완료한 상태
        val currentSessionIndex =
            currentSessionIndexOf(
                sessions = sessions(count = 30),
                latestRecords = listOf(record(24, 300)),
                studyStatus = null,
            )

        // 1챕터는 다음 챕터로 넘어간 뒤에도 자기 마지막 장(24장)을 맨 위에 유지한다
        assertEquals(
            23,
            chapterCurrentSessionIndex(currentSessionIndex, chapterStartIndex = 0, chapterSize = 24),
        )
        // 2챕터는 홈과 같은 다음 세션(25장)을 맨 위에 올린다
        assertEquals(
            0,
            chapterCurrentSessionIndex(currentSessionIndex, chapterStartIndex = 24, chapterSize = 6),
        )
    }

    @Test
    fun `챕터의 마지막 장을 학습 중이어도 그 챕터는 마지막 장을 보여준다`() {
        val currentSessionIndex =
            currentSessionIndexOf(
                sessions = sessions(count = 30),
                latestRecords = emptyList(),
                studyStatus = studyStatus(sessionId = 24),
            )

        assertEquals(
            23,
            chapterCurrentSessionIndex(currentSessionIndex, chapterStartIndex = 0, chapterSize = 24),
        )
    }

    @Test
    fun `급수가 챕터 경계에서 끝나도 마지막 챕터는 마지막 장에 머무른다`() {
        // 48장(24 + 24) 급수에서 마지막 48장을 완료한 상태
        val currentSessionIndex =
            currentSessionIndexOf(
                sessions = sessions(count = 48),
                latestRecords = listOf(record(48, 300)),
                studyStatus = null,
            )

        assertEquals(47, currentSessionIndex)
        assertEquals(
            23,
            chapterCurrentSessionIndex(currentSessionIndex, chapterStartIndex = 24, chapterSize = 24),
        )
    }

    @Test
    fun `급수의 마지막 세션을 완료해도 첫 챕터가 1장으로 되돌아가지 않는다`() {
        // 30장(24 + 6)짜리 급수에서 마지막 30장을 완료한 상태
        val currentSessionIndex =
            currentSessionIndexOf(
                sessions = sessions(count = 30),
                latestRecords = listOf(record(30, 300)),
                studyStatus = null,
            )

        assertEquals(29, currentSessionIndex)
        // 1챕터는 24장, 2챕터는 마지막 30장을 맨 위에 올린다
        assertEquals(
            23,
            chapterCurrentSessionIndex(currentSessionIndex, chapterStartIndex = 0, chapterSize = 24),
        )
        assertEquals(
            5,
            chapterCurrentSessionIndex(currentSessionIndex, chapterStartIndex = 24, chapterSize = 6),
        )
    }
}
