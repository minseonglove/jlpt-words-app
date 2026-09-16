package com.minseonglove.jlptwords.util

import kotlin.test.Test
import kotlin.test.assertEquals

class KanjiExtractorTest {
    @Test
    fun `한자로만 이루어진 단어에서 한자를 순서대로 추출한다`() {
        assertEquals(listOf("肝", "要"), extractKanji("肝要"))
    }

    @Test
    fun `오쿠리가나가 섞인 단어에서 한자만 추출한다`() {
        assertEquals(listOf("諦"), extractKanji("諦める"))
    }

    @Test
    fun `한자와 히라가나가 섞인 단어에서 한자만 추출한다`() {
        assertEquals(listOf("勉", "強"), extractKanji("勉強する"))
    }

    @Test
    fun `히라가나로만 이루어진 단어는 빈 리스트를 반환한다`() {
        assertEquals(emptyList(), extractKanji("あきらめる"))
    }

    @Test
    fun `가타카나로만 이루어진 단어는 빈 리스트를 반환한다`() {
        assertEquals(emptyList(), extractKanji("コーヒー"))
    }

    @Test
    fun `중복된 한자는 첫 등장 순서를 유지하며 한 번만 반환한다`() {
        assertEquals(listOf("各"), extractKanji("各各"))
    }

    @Test
    fun `빈 문자열은 빈 리스트를 반환한다`() {
        assertEquals(emptyList(), extractKanji(""))
    }

    @Test
    fun `반복 부호 々는 한자가 아니므로 제외하고 한자만 추출한다`() {
        assertEquals(listOf("人"), extractKanji("人々"))
    }

    @Test
    fun `전각 숫자가 섞여도 한자만 추출한다`() {
        assertEquals(listOf("代"), extractKanji("２０代"))
    }
}
