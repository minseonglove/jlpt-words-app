package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.entity.WordScriptType
import com.minseonglove.jlptwords.entity.WordSortType
import com.minseonglove.jlptwords.repository.WordRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchWordsTest {
    private class FakeWordRepository(
        private val words: List<SearchedWord>,
    ) : WordRepository {
        /** searchWords 에 마지막으로 전달된 검색어. */
        var lastQuery: String? = null
            private set

        override suspend fun increaseAppearanceCount(wordId: Int) = Unit

        override suspend fun increaseCorrectAndAppearanceCount(wordId: Int) = Unit

        override suspend fun getWord(
            kanji: String,
            pronunciation: String,
        ): Word? = null

        override suspend fun getJlptLevel(wordId: Int): JLPTLevel = JLPTLevel.N5

        override suspend fun searchWords(query: String): List<SearchedWord> {
            lastQuery = query
            return words
        }
    }

    private val allLevels = JLPTLevel.entries.toSet()
    private val allScripts = WordScriptType.entries.toSet()

    private fun word(
        id: Int,
        kanji: String = "あ",
        pronunciation: String = "あ",
        level: JLPTLevel = JLPTLevel.N5,
        appearanceCount: Int = 0,
        correctCount: Int = 0,
    ) = SearchedWord(
        id = id,
        kanji = kanji,
        pronunciation = pronunciation,
        meaning = "",
        jlptLevel = level,
        appearanceCount = appearanceCount,
        correctCount = correctCount,
    )

    @Test
    fun `급수와 표기 필터에 맞지 않는 단어는 제외한다`() =
        runTest {
            val n5Hiragana = word(1, kanji = "あう", pronunciation = "あう", level = JLPTLevel.N5)
            val n1Kanji = word(2, kanji = "漢字", pronunciation = "かんじ", level = JLPTLevel.N1)
            val useCase = SearchWords(FakeWordRepository(listOf(n5Hiragana, n1Kanji)))

            val result =
                useCase(
                    query = "",
                    levels = setOf(JLPTLevel.N5),
                    scriptTypes = setOf(WordScriptType.HIRAGANA),
                    sortType = WordSortType.WORD_NUMBER,
                    isAscending = true,
                )

            assertEquals(listOf(n5Hiragana), result)
        }

    @Test
    fun `단어 번호 정렬은 오름차순이면 저장소 순서를 유지하고 내림차순이면 역순으로 만든다`() =
        runTest {
            val w1 = word(1)
            val w2 = word(2)
            val w3 = word(3)
            val useCase = SearchWords(FakeWordRepository(listOf(w1, w2, w3)))

            assertEquals(
                listOf(w1, w2, w3),
                useCase("", allLevels, allScripts, WordSortType.WORD_NUMBER, isAscending = true),
            )
            assertEquals(
                listOf(w3, w2, w1),
                useCase("", allLevels, allScripts, WordSortType.WORD_NUMBER, isAscending = false),
            )
        }

    @Test
    fun `사전순 정렬은 발음 기준으로 오름 내림차순을 적용한다`() =
        runTest {
            val a = word(1, pronunciation = "あ")
            val ka = word(2, pronunciation = "か")
            val sa = word(3, pronunciation = "さ")
            // 일부러 뒤섞어 입력
            val useCase = SearchWords(FakeWordRepository(listOf(sa, a, ka)))

            assertEquals(
                listOf(a, ka, sa),
                useCase("", allLevels, allScripts, WordSortType.DICTIONARY, isAscending = true),
            )
            assertEquals(
                listOf(sa, ka, a),
                useCase("", allLevels, allScripts, WordSortType.DICTIONARY, isAscending = false),
            )
        }

    @Test
    fun `정답률 정렬은 학습한 단어를 정답률 순으로 두고 미학습 단어를 방향과 무관하게 항상 뒤에 둔다`() =
        runTest {
            val low = word(1, appearanceCount = 10, correctCount = 2) // 0.2
            val high = word(2, appearanceCount = 10, correctCount = 8) // 0.8
            val unstudied = word(3) // 정답률 null
            val useCase = SearchWords(FakeWordRepository(listOf(unstudied, high, low)))

            assertEquals(
                listOf(low, high, unstudied),
                useCase("", allLevels, allScripts, WordSortType.ACCURACY, isAscending = true),
            )
            assertEquals(
                listOf(high, low, unstudied),
                useCase("", allLevels, allScripts, WordSortType.ACCURACY, isAscending = false),
            )
        }

    @Test
    fun `검색어의 앞뒤 공백을 제거해 저장소에 전달한다`() =
        runTest {
            val repository = FakeWordRepository(emptyList())

            SearchWords(repository)("  単語  ", allLevels, allScripts, WordSortType.WORD_NUMBER, isAscending = true)

            assertEquals("単語", repository.lastQuery)
        }
}
