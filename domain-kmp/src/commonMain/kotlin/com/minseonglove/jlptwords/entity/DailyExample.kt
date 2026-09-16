package com.minseonglove.jlptwords.entity

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 홈 화면 '오늘의 예문'. 하루(자정 기준) 동안 동일한 예문이 유지된다.
 * [japanese] 는 표제어를 `*단어*` 마커로 감싼 원문이다(표시 시 마커 부분만 강조).
 */
data class DailyExample(
    val kanji: String,
    val pronunciation: String,
    val japanese: String,
    val korean: String,
    val furigana: ImmutableList<RubySegment> = persistentListOf(),
)
