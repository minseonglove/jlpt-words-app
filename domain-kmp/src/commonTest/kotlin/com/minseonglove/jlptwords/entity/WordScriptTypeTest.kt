package com.minseonglove.jlptwords.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class WordScriptTypeTest {
    @Test
    fun `한자가 하나라도 있으면 KANJI 로 판별한다`() {
        assertEquals(WordScriptType.KANJI, WordScriptType.from("漢字"))
        // 한자 + 히라가나 혼합도 한자 우선
        assertEquals(WordScriptType.KANJI, WordScriptType.from("食べる"))
    }

    @Test
    fun `한자가 없고 가타카나가 있으면 KATAKANA 로 판별한다`() {
        assertEquals(WordScriptType.KATAKANA, WordScriptType.from("テスト"))
        // 장음 부호(ー)도 가타카나 범위
        assertEquals(WordScriptType.KATAKANA, WordScriptType.from("ラーメン"))
    }

    @Test
    fun `한자 가타카나 혼합이면 한자 우선으로 KANJI 로 판별한다`() {
        assertEquals(WordScriptType.KANJI, WordScriptType.from("テスト漢"))
    }

    @Test
    fun `한자도 가타카나도 없으면 HIRAGANA 로 판별한다`() {
        assertEquals(WordScriptType.HIRAGANA, WordScriptType.from("たべる"))
    }

    @Test
    fun `빈 문자열은 HIRAGANA 로 판별한다`() {
        assertEquals(WordScriptType.HIRAGANA, WordScriptType.from(""))
    }
}
