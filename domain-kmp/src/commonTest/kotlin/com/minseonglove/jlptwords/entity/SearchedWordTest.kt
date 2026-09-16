package com.minseonglove.jlptwords.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchedWordTest {
    private fun word(
        kanji: String = "あ",
        appearanceCount: Int = 0,
        correctCount: Int = 0,
    ) = SearchedWord(
        id = 1,
        kanji = kanji,
        pronunciation = "あ",
        meaning = "",
        jlptLevel = JLPTLevel.N5,
        appearanceCount = appearanceCount,
        correctCount = correctCount,
    )

    @Test
    fun `노출 이력이 없으면 정답률은 null 이다`() {
        assertNull(word(appearanceCount = 0, correctCount = 0).accuracy)
    }

    @Test
    fun `정답률은 정답 수를 노출 수로 나눈 값이다`() {
        assertEquals(0.25f, word(appearanceCount = 4, correctCount = 1).accuracy)
    }

    @Test
    fun `노출은 있고 정답이 없으면 정답률은 0이다`() {
        assertEquals(0f, word(appearanceCount = 5, correctCount = 0).accuracy)
    }

    @Test
    fun `scriptType 은 표기 문자열로 판별된다`() {
        assertEquals(WordScriptType.KANJI, word(kanji = "漢字").scriptType)
        assertEquals(WordScriptType.KATAKANA, word(kanji = "テスト").scriptType)
        assertEquals(WordScriptType.HIRAGANA, word(kanji = "たべる").scriptType)
    }
}
