package com.minseonglove.jlptwords.entity

/** 단어학습 탭 진입 시 갈 곳. 마지막 선택 급수가 있으면 세션 선택, 없으면(첫 진입) 급수 선택. */
sealed interface StudyEntry {
    data class Sessions(
        val level: JLPTLevel,
    ) : StudyEntry

    data class NeedsLevelSelection(
        val defaultLevel: JLPTLevel,
    ) : StudyEntry
}
