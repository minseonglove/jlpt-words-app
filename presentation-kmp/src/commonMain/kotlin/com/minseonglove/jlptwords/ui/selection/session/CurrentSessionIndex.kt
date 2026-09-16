package com.minseonglove.jlptwords.ui.selection.session

import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.StudyStatus

/**
 * 급수 안에서 이어서 학습할 세션의 위치(0-base).
 * 홈 '계속 학습하기'(GetContinueSession)와 같은 기준으로,
 * 진행 중인 세션 > 가장 최근에 완료한 세션의 다음 > 첫 세션 순으로 고른다.
 * 마지막 세션까지 완료했으면 마지막 세션에 머무른다.
 */
internal fun currentSessionIndexOf(
    sessions: List<StudySession>,
    latestRecords: Collection<StudyRecord>,
    studyStatus: StudyStatus?,
): Int {
    if (sessions.isEmpty()) return 0

    val inProgressSessionId = studyStatus?.sessionId
    if (inProgressSessionId != null) {
        val inProgressIndex = sessions.indexOfFirst { it.id == inProgressSessionId }
        if (inProgressIndex >= 0) return inProgressIndex
    }

    val latestRecord = latestRecords.maxByOrNull { it.createAt } ?: return 0
    val completedIndex = sessions.indexOfFirst { it.id == latestRecord.sessionId }
    if (completedIndex < 0) return 0
    return (completedIndex + 1).coerceAtMost(sessions.lastIndex)
}

/**
 * [chapterStartIndex] 에서 시작하는 [chapterSize] 장짜리 챕터가 스택 맨 위에 올릴 카드 위치.
 * 현재 세션이 지나간 챕터는 마지막 장을, 아직 오지 않은 챕터는 첫 장을 올린다.
 */
internal fun chapterCurrentSessionIndex(
    currentSessionIndex: Int,
    chapterStartIndex: Int,
    chapterSize: Int,
): Int = (currentSessionIndex - chapterStartIndex).coerceIn(0, (chapterSize - 1).coerceAtLeast(0))
