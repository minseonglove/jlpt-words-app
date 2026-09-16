package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.KanjiInfo
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.SyncMode
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.repository.KanjiInfoRepository
import com.minseonglove.jlptwords.repository.WordRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetWordDetailTest {
    private class FakeWordRepository(
        private val word: Word?,
        private val jlptLevel: JLPTLevel = JLPTLevel.N5,
    ) : WordRepository {
        override suspend fun increaseAppearanceCount(wordId: Int) = Unit

        override suspend fun increaseCorrectAndAppearanceCount(wordId: Int) = Unit

        override suspend fun getWord(
            kanji: String,
            pronunciation: String,
        ): Word? = word

        override suspend fun getJlptLevel(wordId: Int): JLPTLevel = jlptLevel

        override suspend fun searchWords(query: String): List<SearchedWord> = emptyList()
    }

    private class FakeKanjiInfoRepository(
        private val infos: List<KanjiInfo>,
    ) : KanjiInfoRepository {
        /** getKanjiInfos 에 전달된 한자 목록. 호출되지 않았으면 null. */
        var requestedKanjis: List<String>? = null
            private set

        override suspend fun syncIfNeeded(
            remoteDate: Long?,
            onSyncStart: suspend (SyncMode) -> Unit,
        ): Boolean = false

        override suspend fun getKanjiInfos(kanjis: List<String>): List<KanjiInfo> {
            requestedKanjis = kanjis
            return infos
        }
    }

    @Test
    fun `표제어에 해당하는 단어가 없으면 null 을 반환하고 사전을 조회하지 않는다`() =
        runTest {
            val kanjiRepo = FakeKanjiInfoRepository(emptyList())
            val useCase = GetWordDetail(FakeWordRepository(null), kanjiRepo)

            assertNull(useCase("肝要", "かんよう"))
            assertNull(kanjiRepo.requestedKanjis)
        }

    @Test
    fun `단어 표기에서 추출한 한자로 사전 정보를 조회해 조립한다`() =
        runTest {
            val word = Word(id = 1, kanji = "肝要", pronunciation = "かんよう", meaning = "긴요함")
            val kanjiInfos =
                listOf(
                    KanjiInfo("肝", "간 간", listOf("かん"), listOf("きも")),
                    KanjiInfo("要", "요긴할 요", listOf("よう"), listOf("いる", "かなめ")),
                )
            val kanjiRepo = FakeKanjiInfoRepository(kanjiInfos)
            val useCase = GetWordDetail(FakeWordRepository(word, JLPTLevel.N1), kanjiRepo)

            val detail = useCase("肝要", "かんよう")

            assertEquals(word, detail?.word)
            assertEquals(kanjiInfos, detail?.kanjiInfos)
            assertEquals(JLPTLevel.N1, detail?.jlptLevel)
            assertEquals(listOf("肝", "要"), kanjiRepo.requestedKanjis)
        }

    @Test
    fun `한자가 없는 단어는 빈 한자 목록으로 조회한다`() =
        runTest {
            val word = Word(id = 2, kanji = "あう", pronunciation = "あう", meaning = "만나다")
            val kanjiRepo = FakeKanjiInfoRepository(emptyList())
            val useCase = GetWordDetail(FakeWordRepository(word), kanjiRepo)

            val detail = useCase("あう", "あう")

            assertEquals(emptyList(), kanjiRepo.requestedKanjis)
            assertEquals(emptyList(), detail?.kanjiInfos)
        }
}
