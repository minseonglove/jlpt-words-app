package com.minseonglove.jlptwords.db.util

import com.minseonglove.jlptwords.db.entity.StudySessionEntity
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StudySessionGeneratorTest {
    private val level = JLPTLevel.N5

    private fun normal(
        start: Int,
        end: Int,
    ) = StudySessionEntity(
        levelCode = level.code,
        sessionTypeCode = SessionType.NORMAL.code,
        startNumber = start,
        endNumber = end,
        wordsSize = end - start + 1,
    )

    private fun lowAccuracy(end: Int) =
        StudySessionEntity(
            levelCode = level.code,
            sessionTypeCode = SessionType.LOW_ACCURACY.code,
            startNumber = 1,
            endNumber = end,
            wordsSize = 100,
        )

    private fun random(
        rangeEnd: Int,
        size: Int,
    ) = StudySessionEntity(
        levelCode = level.code,
        sessionTypeCode = SessionType.RANDOM.code,
        startNumber = 1,
        endNumber = rangeEnd,
        wordsSize = size,
    )

    private fun generate(total: Int) = StudySessionGenerator.generateStudySessions(level, total)

    @Test
    fun `단어 수가 0 이하이면 빈 목록을 반환한다`() {
        assertEquals(emptyList(), generate(0))
        assertEquals(emptyList(), generate(-5))
    }

    @Test
    fun `첫 세션은 단어 수와 무관하게 항상 1~10 NORMAL 세션이다`() {
        assertEquals(normal(1, 10), generate(50).first())
        assertEquals(normal(1, 10), generate(300).first())
        assertEquals(normal(1, 10), generate(2000).first())
    }

    @Test
    fun `정확히 한 윈도우(300개)이면 50 단위 누적 NORMAL 6개와 LOW_ACCURACY 1개를 만들고 RANDOM 은 없다`() {
        val expected =
            listOf(
                normal(1, 10),
                normal(1, 50),
                normal(1, 100),
                normal(1, 150),
                normal(1, 200),
                normal(1, 250),
                normal(1, 300),
                lowAccuracy(300),
            )

        assertEquals(expected, generate(300))
        // 첫 윈도우에는 RANDOM 세션이 없다.
        assertTrue(generate(300).none { it.sessionTypeCode == SessionType.RANDOM.code })
    }

    @Test
    fun `윈도우 내 마지막 증가분이 50 미만이면 직전 NORMAL 과 병합한다`() {
        // 총 130개: 1~50 다음 증가분(1~150 의도 → 130)이 30개라 직전(1~100)을 1~130 으로 병합.
        val expected =
            listOf(
                normal(1, 10),
                normal(1, 50),
                normal(1, 130),
                lowAccuracy(130),
            )

        assertEquals(expected, generate(130))
    }

    @Test
    fun `다음 윈도우가 50 미만 한 개뿐이면 직전 윈도우의 마지막 NORMAL 을 끝까지 확장한다`() {
        // 총 320개: 첫 윈도우 1~300 후 다음 윈도우가 301~320(20개)뿐이라 마지막 NORMAL 을 1~320 으로 흡수.
        val expected =
            listOf(
                normal(1, 10),
                normal(1, 50),
                normal(1, 100),
                normal(1, 150),
                normal(1, 200),
                normal(1, 250),
                normal(1, 320),
                lowAccuracy(320),
            )

        assertEquals(expected, generate(320))
    }

    @Test
    fun `두 윈도우(600개)이면 두 번째 윈도우부터 이전 윈도우 범위 기준 RANDOM 세션이 추가된다`() {
        val expected =
            listOf(
                // 윈도우 1
                normal(1, 10),
                normal(1, 50),
                normal(1, 100),
                normal(1, 150),
                normal(1, 200),
                normal(1, 250),
                normal(1, 300),
                lowAccuracy(300),
                // 윈도우 2 (windowStart = 301)
                normal(301, 350),
                normal(301, 400),
                normal(301, 450),
                normal(301, 500),
                normal(301, 550),
                normal(301, 600),
                // RANDOM 은 이전 윈도우 범위(1~300)에서 min(300/3, 300)=100 개
                random(rangeEnd = 300, size = 100),
                lowAccuracy(600),
            )

        assertEquals(expected, generate(600))
    }

    @Test
    fun `모든 세션은 요청한 급수 코드로 생성된다`() {
        val sessions = StudySessionGenerator.generateStudySessions(JLPTLevel.N1, 600)

        assertTrue(sessions.isNotEmpty())
        assertTrue(sessions.all { it.levelCode == JLPTLevel.N1.code })
    }
}
