package com.minseonglove.jlptwords.db.util

import com.minseonglove.jlptwords.entity.RubySegment
import kotlin.test.Test
import kotlin.test.assertEquals

class RubyHighlightTest {
    @Test
    fun `마커 구간의 세그먼트에 하이라이트가 붙는다`() {
        val segments =
            listOf(
                DecodedRuby("授業", "じゅぎょう"),
                DecodedRuby("は", ""),
                DecodedRuby("九時", "くじ"),
                DecodedRuby("に", ""),
                DecodedRuby("始", "はじ"),
                DecodedRuby("まり", ""),
                DecodedRuby("ます。", ""),
            )
        val result = buildRubySegments("授業は九時に*始まり*ます。", segments)
        assertEquals(
            listOf(
                RubySegment("授業", "じゅぎょう", false),
                RubySegment("は", "", false),
                RubySegment("九時", "くじ", false),
                RubySegment("に", "", false),
                RubySegment("始", "はじ", true),
                RubySegment("まり", "", true),
                RubySegment("ます。", "", false),
            ),
            result,
        )
    }

    @Test
    fun `세그먼트 연결이 문장과 다르면 빈 목록으로 폴백한다`() {
        val result = buildRubySegments("水を飲む。", listOf(DecodedRuby("水", "みず")))
        assertEquals(emptyList(), result)
    }

    @Test
    fun `마커가 세그먼트 중간에 오면 빈 목록으로 폴백한다`() {
        val result =
            buildRubySegments(
                "*一*つ。",
                listOf(DecodedRuby("一つ。", "")),
            )
        assertEquals(emptyList(), result)
    }

    @Test
    fun `빈 루비 입력은 빈 목록을 반환한다`() {
        assertEquals(emptyList(), buildRubySegments("水を飲む。", emptyList()))
    }
}
