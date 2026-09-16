package com.minseonglove.jlptwords.db.util

import com.minseonglove.jlptwords.db.entity.StudySessionEntity
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType

object StudySessionGenerator {
    /** 첫 세션은 학습 흐름에 익숙해지도록 짧게 잡는다. */
    private const val FIRST_SESSION_WORD_COUNT = 10

    /** NORMAL 세션이 한 번에 늘어나는 단어 수. 윈도우 안에서 이만큼씩 누적된다. */
    private const val NORMAL_STEP = 50

    /** 한 윈도우가 담는 NORMAL 세션 수. */
    private const val NORMALS_PER_WINDOW = 6

    /** 한 윈도우가 덮는 단어 수. */
    private const val WINDOW_SPAN = NORMAL_STEP * NORMALS_PER_WINDOW

    private const val RANDOM_MAX_SIZE = 300
    private const val RANDOM_RANGE_DIVISOR = 3
    private const val LOW_ACCURACY_SIZE = 100

    /**
     * 급수의 총 단어 수로 학습 세션 목록을 만든다.
     *
     * 윈도우 하나는 NORMAL 세션 여섯 개(1~50, 1~100, … 1~300)와 복습 세션으로 이뤄지고,
     * 다음 윈도우는 그 뒤 300개를 같은 방식으로 덮는다. 복습은 윈도우가 끝날 때 붙는다.
     */
    fun generateStudySessions(
        level: JLPTLevel,
        totalWordCount: Int,
    ): List<StudySessionEntity> {
        if (totalWordCount <= 0) return emptyList()

        val sessions = mutableListOf<StudySessionEntity>()
        sessions.add(
            createNormalSession(
                level = level,
                start = 1,
                end = minOf(FIRST_SESSION_WORD_COUNT, totalWordCount),
            ),
        )

        var windowStart = 1
        var previousWindowEnd = 0
        var windowIndex = 1

        while (windowStart <= totalWordCount) {
            val windowNormals = buildWindowNormals(level, windowStart, totalWordCount)
            if (windowNormals.isEmpty()) break
            sessions.addAll(windowNormals)

            val currentWindowEnd = windowNormals.last().endNumber
            // 첫 윈도우는 되돌아볼 범위가 없어 RANDOM 을 만들지 않는다.
            if (windowIndex > 1) {
                randomReviewSession(level, previousWindowEnd)?.let(sessions::add)
            }
            sessions.add(lowAccuracyReviewSession(level, currentWindowEnd))

            previousWindowEnd = currentWindowEnd
            windowStart = currentWindowEnd + 1
            windowIndex += 1
        }

        return sessions
    }

    /**
     * [windowStart] 부터 시작하는 윈도우의 NORMAL 세션들.
     *
     * 데이터가 모자라 마지막 증가분이 [NORMAL_STEP] 에 못 미치면 직전 세션과 합쳐,
     * 몇 단어만 늘어난 세션이 따로 생기지 않게 한다.
     */
    private fun buildWindowNormals(
        level: JLPTLevel,
        windowStart: Int,
        totalWordCount: Int,
    ): List<StudySessionEntity> {
        val normals = mutableListOf<StudySessionEntity>()

        for (step in 1..NORMALS_PER_WINDOW) {
            val end = minOf(windowStart + (NORMAL_STEP * step) - 1, totalWordCount)
            if (end < windowStart) break

            val isLast = end == totalWordCount
            val isTooSmallToStandAlone =
                normals.isNotEmpty() && end - normals.last().endNumber < NORMAL_STEP
            if (isLast && isTooSmallToStandAlone) {
                normals[normals.lastIndex] = createNormalSession(level, windowStart, end)
                break
            }

            normals.add(createNormalSession(level, windowStart, end))
            if (isLast) break
        }

        return absorbTailWindow(level, normals, windowStart, totalWordCount)
    }

    /**
     * 다음 윈도우가 [NORMAL_STEP] 미만 한 개짜리로만 남는 경우(총 1220개 등), 이번 윈도우의
     * 마지막 NORMAL 을 끝까지 늘려 그 꼬리를 흡수한다. 남기면 마지막 장이 몇 단어짜리가 된다.
     */
    private fun absorbTailWindow(
        level: JLPTLevel,
        normals: List<StudySessionEntity>,
        windowStart: Int,
        totalWordCount: Int,
    ): List<StudySessionEntity> {
        if (normals.size != NORMALS_PER_WINDOW) return normals

        val windowEnd = normals.last().endNumber
        val isFullWindow = windowEnd >= windowStart + WINDOW_SPAN - 1
        if (isFullWindow.not() || windowEnd >= totalWordCount) return normals

        val nextWindowStart = windowStart + WINDOW_SPAN
        if (nextWindowStart > totalWordCount) return normals

        val nextWindowSize = minOf(nextWindowStart + NORMAL_STEP - 1, totalWordCount) - nextWindowStart + 1
        if (nextWindowSize !in 1 until NORMAL_STEP) return normals

        return normals.dropLast(1) +
            createNormalSession(level, normals.last().startNumber, totalWordCount)
    }

    /** 이전 윈도우까지의 범위에서 일부를 무작위로 되짚는 복습 세션. 범위가 없으면 만들지 않는다. */
    private fun randomReviewSession(
        level: JLPTLevel,
        rangeEnd: Int,
    ): StudySessionEntity? {
        if (rangeEnd <= 0) return null
        val size = minOf(rangeEnd / RANDOM_RANGE_DIVISOR, RANDOM_MAX_SIZE)
        if (size <= 0) return null

        return StudySessionEntity(
            levelCode = level.code,
            sessionTypeCode = SessionType.RANDOM.code,
            startNumber = 1,
            endNumber = rangeEnd,
            wordsSize = size,
        )
    }

    /** 지금까지의 전체 범위에서 정답률이 낮은 단어를 모아 주는 복습 세션. */
    private fun lowAccuracyReviewSession(
        level: JLPTLevel,
        rangeEnd: Int,
    ): StudySessionEntity =
        StudySessionEntity(
            levelCode = level.code,
            sessionTypeCode = SessionType.LOW_ACCURACY.code,
            startNumber = 1,
            endNumber = rangeEnd,
            wordsSize = LOW_ACCURACY_SIZE,
        )

    private fun createNormalSession(
        level: JLPTLevel,
        start: Int,
        end: Int,
    ): StudySessionEntity =
        StudySessionEntity(
            levelCode = level.code,
            sessionTypeCode = SessionType.NORMAL.code,
            startNumber = start,
            endNumber = end,
            wordsSize = (end - start + 1).coerceAtLeast(0),
        )
}
