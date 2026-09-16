package com.minseonglove.jlptwords.db.util

import kotlin.test.Test
import kotlin.test.assertEquals

class FuriganaCodecTest {
    @Test
    fun `루비 세그먼트를 왕복 직렬화한다`() {
        val segments =
            listOf(
                DecodedRuby(text = "学校", reading = "がっこう"),
                DecodedRuby(text = "へ", reading = ""),
                DecodedRuby(text = "行", reading = "い"),
                DecodedRuby(text = "きます。", reading = ""),
            )
        assertEquals(segments, decodeFurigana(encodeFurigana(segments)))
    }

    @Test
    fun `빈 목록은 빈 문자열로 직렬화된다`() {
        assertEquals("", encodeFurigana(emptyList()))
        assertEquals(emptyList(), decodeFurigana(""))
    }
}
