package com.minseonglove.jlptwords.entity

/**
 * 단어 검색 결과 항목. 목록 표시에 필요한 최소 정보와 필터·정렬용 부가 정보를 가진다.
 * [jlptLevel] 은 단어가 속한 레벨 중 가장 높은 급수(N1쪽)다.
 */
data class SearchedWord(
    val id: Int,
    val kanji: String,
    val pronunciation: String,
    val meaning: String,
    val jlptLevel: JLPTLevel,
    val appearanceCount: Int = 0,
    val correctCount: Int = 0,
) {
    /** 표기 형태(히라가나/가타카나/한자단어). 표기 필터에 사용한다. */
    val scriptType: WordScriptType
        get() = WordScriptType.from(kanji)

    /** 정답률(0.0~1.0). 학습 이력이 없으면 null. */
    val accuracy: Float?
        get() = if (appearanceCount > 0) correctCount.toFloat() / appearanceCount else null
}
