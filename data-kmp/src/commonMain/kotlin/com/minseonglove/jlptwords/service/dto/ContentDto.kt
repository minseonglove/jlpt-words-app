package com.minseonglove.jlptwords.service.dto

import com.minseonglove.jlptwords.entity.JLPTLevel

/** examples_{level} 청크에서 평탄화한 예문 1건 */
data class ExampleSentenceDto(
    val sourceWordKanji: String,
    val japanese: String,
    val korean: String,
    val order: Int,
    val tokens: List<ExampleTokenDto>,
    val furigana: List<ExampleRubyDto> = emptyList(),
)

data class ExampleTokenDto(
    val surface: String,
    val meaning: String,
)

data class ExampleRubyDto(
    val text: String,
    val reading: String,
)

/** content_update_date/{level} */
data class ContentUpdateDate(
    val wordsDate: Long,
    val examplesDate: Long,
)

/** content_update_date 컬렉션 전체 (급수·요미·한자 날짜를 1회 조회로 받는다) */
data class AllContentUpdateDates(
    val byLevel: Map<JLPTLevel, ContentUpdateDate>,
    val readingsDate: Long,
    val kanjiDate: Long,
) {
    fun forLevel(level: JLPTLevel): ContentUpdateDate = byLevel[level] ?: ContentUpdateDate(0L, 0L)

    companion object {
        /** 조회 실패(오프라인 등) 시 사용. 모든 날짜가 0 이라 전 항목이 스킵된다. */
        val EMPTY = AllContentUpdateDates(byLevel = emptyMap(), readingsDate = 0L, kanjiDate = 0L)
    }
}
