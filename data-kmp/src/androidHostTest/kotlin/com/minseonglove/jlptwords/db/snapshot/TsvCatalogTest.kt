package com.minseonglove.jlptwords.db.snapshot

import com.minseonglove.jlptwords.service.dto.ExampleRubyDto
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** TsvCatalog.parseFurigana 단위 테스트 — 마커 경계 분할·루비 귀속·인접 무루비 병합 규칙 검증. */
class TsvCatalogTest {
    private fun tsvFile(vararg lines: String): File =
        File.createTempFile("furigana", ".tsv").apply {
            deleteOnExit()
            writeText(lines.joinToString("\n"))
        }

    @Test
    fun `마커 경계로 분할되고 루비가 직전 한자 런에 귀속된다`() {
        val file = tsvFile("n5\tご飯\t0\tご飯[はん]を*食[た]べ*ます。")

        val result = TsvCatalog.parseFurigana(file)

        assertEquals(
            listOf(
                ExampleRubyDto("ご", ""),
                ExampleRubyDto("飯", "はん"),
                ExampleRubyDto("を", ""),
                ExampleRubyDto("食", "た"),
                ExampleRubyDto("べ", ""),
                ExampleRubyDto("ます。", ""),
            ),
            result[Triple("n5", "ご飯", 0)],
        )
    }

    @Test
    fun `마커가 없으면 인접한 무루비 텍스트가 하나의 세그먼트로 병합된다`() {
        val file = tsvFile("n5\ttest\t2\t食べます")

        val result = TsvCatalog.parseFurigana(file)

        assertEquals(
            listOf(ExampleRubyDto("食べます", "")),
            result[Triple("n5", "test", 2)],
        )
    }

    @Test
    fun `여러 행은 (급수, 표기, meaning_index) 키로 각각 매핑된다`() {
        val file =
            tsvFile(
                "n5\tもしもし\t0\t*もしもし*、田中[たなか]さんですか。",
                "n5\t一つ\t0\tリンゴを*一[ひと]つ*ください。",
            )

        val result = TsvCatalog.parseFurigana(file)

        assertEquals(2, result.size)
        assertEquals(
            listOf(
                ExampleRubyDto("もしもし", ""),
                ExampleRubyDto("、", ""),
                ExampleRubyDto("田中", "たなか"),
                ExampleRubyDto("さんですか。", ""),
            ),
            result[Triple("n5", "もしもし", 0)],
        )
        assertEquals(
            listOf(
                ExampleRubyDto("リンゴを", ""),
                ExampleRubyDto("一", "ひと"),
                ExampleRubyDto("つ", ""),
                ExampleRubyDto("ください。", ""),
            ),
            result[Triple("n5", "一つ", 0)],
        )
    }
}
