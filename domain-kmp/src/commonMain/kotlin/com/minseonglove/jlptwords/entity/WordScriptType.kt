package com.minseonglove.jlptwords.entity

import com.minseonglove.jlptwords.util.isKanji

/**
 * 단어 표기 형태. 단어 검색 화면의 표기 필터에 사용한다.
 * 한자 포함 -> 가타카나 포함 -> 히라가나 순으로 판별한다(혼합 표기는 앞선 분류 우선).
 */
enum class WordScriptType {
    HIRAGANA,
    KATAKANA,
    KANJI,
    ;

    companion object {
        private val KATAKANA_RANGE = 0x30A0..0x30FF

        fun from(text: String): WordScriptType =
            when {
                text.any { isKanji(it) } -> KANJI
                text.any { it.code in KATAKANA_RANGE } -> KATAKANA
                else -> HIRAGANA
            }
    }
}
