package com.minseonglove.jlptwords.db.util

import com.minseonglove.jlptwords.db.entity.WordEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WordLevelDiffTest {
    private fun word(
        id: Int = 0,
        kanji: String,
        pronunciation: String,
        meaning: String = "뜻",
        partOfSpeech: String = "명사",
    ) = WordEntity(
        id = id,
        kanji = kanji,
        pronunciation = pronunciation,
        meaning = meaning,
        partOfSpeech = partOfSpeech,
    )

    @Test
    fun `표기와 발음이 같으면 뜻·품사만 갱신한다`() {
        val result =
            WordLevelDiff.compute(
                previous = listOf(word(id = 1, kanji = "上", pronunciation = "うえ", meaning = "위", partOfSpeech = "")),
                current = listOf(word(kanji = "上", pronunciation = "うえ", meaning = "위", partOfSpeech = "명사")),
            )

        assertEquals(1, result.updated.size)
        assertEquals(1, result.updated.single().id, "id 가 유지되어야 통계가 보존된다")
        assertEquals("명사", result.updated.single().partOfSpeech)
        assertTrue(result.renamed.isEmpty() && result.deleted.isEmpty() && result.added.isEmpty())
    }

    @Test
    fun `바뀐 것이 없으면 아무 대상도 만들지 않는다`() {
        val result =
            WordLevelDiff.compute(
                previous = listOf(word(id = 1, kanji = "上", pronunciation = "うえ")),
                current = listOf(word(kanji = "上", pronunciation = "うえ")),
            )

        assertTrue(
            result.updated.isEmpty() &&
                result.renamed.isEmpty() &&
                result.deleted.isEmpty() &&
                result.added.isEmpty(),
        )
    }

    @Test
    fun `같은 표기의 동형이의어는 한쪽이 사라지지 않고 모두 유지된다`() {
        val result =
            WordLevelDiff.compute(
                previous =
                    listOf(
                        word(id = 1, kanji = "空", pronunciation = "そら", meaning = "하늘"),
                        word(id = 2, kanji = "空", pronunciation = "くう", meaning = "공"),
                    ),
                current =
                    listOf(
                        word(kanji = "空", pronunciation = "そら", meaning = "하늘"),
                        word(kanji = "空", pronunciation = "くう", meaning = "공"),
                    ),
            )

        assertTrue(
            result.updated.isEmpty() &&
                result.renamed.isEmpty() &&
                result.deleted.isEmpty() &&
                result.added.isEmpty(),
            "표기가 같아도 발음이 다르면 각각 짝이 맞아야 한다",
        )
    }

    @Test
    fun `로컬에 없는 동형이의어는 추가 대상이 된다`() {
        val result =
            WordLevelDiff.compute(
                previous = listOf(word(id = 1, kanji = "空", pronunciation = "くう")),
                current =
                    listOf(
                        word(kanji = "空", pronunciation = "くう"),
                        word(kanji = "空", pronunciation = "そら"),
                    ),
            )

        assertEquals(listOf("そら"), result.added.map { it.pronunciation })
        assertTrue(result.deleted.isEmpty() && result.renamed.isEmpty())
    }

    @Test
    fun `발음이 수정된 단어는 삭제·추가가 아니라 갱신 후보가 된다`() {
        val result =
            WordLevelDiff.compute(
                previous = listOf(word(id = 7, kanji = "他", pronunciation = "た", meaning = "타")),
                current = listOf(word(kanji = "他", pronunciation = "ほか", meaning = "다른")),
            )

        assertTrue(
            result.deleted.isEmpty() && result.added.isEmpty(),
            "삭제·추가로 처리하면 행이 새로 만들어져 학습 통계가 사라진다",
        )
        val rename = result.renamed.single()
        assertEquals(7, rename.previous.id)
        assertEquals("ほか", rename.current.pronunciation)
    }

    @Test
    fun `단어장에서 빠진 단어는 삭제 대상이 된다`() {
        val result =
            WordLevelDiff.compute(
                previous =
                    listOf(
                        word(id = 1, kanji = "上", pronunciation = "うえ"),
                        word(id = 2, kanji = "中", pronunciation = "なか"),
                    ),
                current = listOf(word(kanji = "上", pronunciation = "うえ")),
            )

        assertEquals(listOf(2), result.deleted.map { it.id })
        assertTrue(result.renamed.isEmpty() && result.added.isEmpty())
    }

    @Test
    fun `표기가 겹치는 삭제와 추가가 섞여도 짝지어진 만큼만 발음 수정으로 다룬다`() {
        val result =
            WordLevelDiff.compute(
                previous =
                    listOf(
                        word(id = 1, kanji = "空", pronunciation = "そら"),
                        word(id = 2, kanji = "空", pronunciation = "くう"),
                    ),
                current =
                    listOf(
                        word(kanji = "空", pronunciation = "そら"),
                        word(kanji = "空", pronunciation = "から"),
                        word(kanji = "空", pronunciation = "あき"),
                    ),
            )

        assertEquals(1, result.renamed.size, "짝이 없는 previous 1건만 발음 수정으로 매칭된다")
        assertEquals(
            2,
            result.renamed
                .single()
                .previous.id,
        )
        assertEquals(1, result.added.size, "남은 current 는 추가 대상이다")
        assertTrue(result.deleted.isEmpty())
    }
}
