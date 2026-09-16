package com.minseonglove.jlptwords.db.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ExampleTokenCodecTest {
    private val fieldSep = Char(0x1F)
    private val recordSep = Char(0x1E)

    @Test
    fun `빈 토큰 목록은 빈 문자열로 인코딩된다`() {
        assertEquals("", encodeExampleTokens(emptyList()))
    }

    @Test
    fun `빈 문자열은 빈 토큰 목록으로 디코딩된다`() {
        assertEquals(emptyList(), decodeExampleTokens(""))
    }

    @Test
    fun `인코딩 후 디코딩하면 원래 토큰 목록과 순서가 보존된다`() {
        val tokens =
            listOf(
                DecodedToken(surface = "日本語", meaning = "일본어"),
                DecodedToken(surface = "を", meaning = "을"),
                DecodedToken(surface = "勉強", meaning = "공부"),
            )

        assertEquals(tokens, decodeExampleTokens(encodeExampleTokens(tokens)))
    }

    @Test
    fun `구분자가 없는 레코드는 표면형으로만 디코딩되고 뜻은 빈 문자열이다`() {
        val raw = "勉強"

        assertEquals(listOf(DecodedToken(surface = "勉強", meaning = "")), decodeExampleTokens(raw))
    }

    @Test
    fun `뜻이 빈 토큰도 왕복 변환된다`() {
        val tokens = listOf(DecodedToken(surface = "あ", meaning = ""))

        assertEquals(tokens, decodeExampleTokens(encodeExampleTokens(tokens)))
    }

    @Test
    fun `표면형이나 뜻에 구분자가 섞여 있으면 인코딩 시 방어적으로 제거한다`() {
        val tokens =
            listOf(
                DecodedToken(surface = "ab${fieldSep}cd", meaning = "x${recordSep}y"),
            )

        val decoded = decodeExampleTokens(encodeExampleTokens(tokens))

        assertEquals(listOf(DecodedToken(surface = "abcd", meaning = "xy")), decoded)
    }

    @Test
    fun `뜻에 첫 구분자 이후의 추가 구분자는 뜻 문자열에 포함된다`() {
        // 디코딩은 첫 FIELD_SEP 만 기준으로 surface 와 meaning 을 나눈다.
        val raw = "surface${fieldSep}mean${fieldSep}ing"

        assertEquals(
            listOf(DecodedToken(surface = "surface", meaning = "mean${fieldSep}ing")),
            decodeExampleTokens(raw),
        )
    }
}
