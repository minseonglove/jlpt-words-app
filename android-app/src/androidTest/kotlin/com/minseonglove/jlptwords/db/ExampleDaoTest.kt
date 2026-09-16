package com.minseonglove.jlptwords.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.minseonglove.jlptwords.db.dao.ExampleDao
import com.minseonglove.jlptwords.db.entity.ExampleSentenceEntity
import com.minseonglove.jlptwords.db.entity.VocabularyWordEntity
import com.minseonglove.jlptwords.db.entity.WordEntity
import com.minseonglove.jlptwords.db.util.DecodedToken
import com.minseonglove.jlptwords.db.util.decodeExampleTokens
import com.minseonglove.jlptwords.db.util.encodeExampleTokens
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExampleDaoTest {
    private lateinit var db: JLPTWordsDatabase
    private lateinit var dao: ExampleDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db =
            Room
                .inMemoryDatabaseBuilder<JLPTWordsDatabase>(context)
                .setDriver(BundledSQLiteDriver())
                .build()
        dao = db.exampleDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun 예문을_토큰_직렬화로_저장하고_조회_역직렬화한다() =
        runTest {
            // 단어 본체 1건
            db.wordInitializeDao().insertWords(
                listOf(WordEntity(id = 0, kanji = "吸う", pronunciation = "すう", meaning = "마시다")),
            )
            // 요미가나 사전 (표시용 reading)
            dao.insertVocabularyWordsIgnore(
                listOf(
                    VocabularyWordEntity(id = 0, surface = "空気", reading = "くうき"),
                    VocabularyWordEntity(id = 0, surface = "吸う", reading = "すう"),
                ),
            )
            // 예문 — 토큰은 tokens 컬럼에 직렬화
            dao.insertExampleSentences(
                listOf(
                    ExampleSentenceEntity(
                        id = 0,
                        sourceWordKanji = "吸う",
                        levelCode = 5,
                        sentenceJp = "空気を吸う。",
                        sentenceKo = "공기를 마시다.",
                        exampleOrder = 0,
                        tokens =
                            encodeExampleTokens(
                                listOf(
                                    DecodedToken(surface = "空気", meaning = "공기"),
                                    DecodedToken(surface = "吸う", meaning = "들이마시다"),
                                ),
                            ),
                        furigana = "",
                    ),
                ),
            )

            val sentences = dao.loadExamplesForKanjis(levelCode = 5, kanjis = listOf("吸う"))
            assertEquals(1, sentences.size)
            assertEquals("空気を吸う。", sentences.first().sentenceJp)

            val tokens = decodeExampleTokens(sentences.first().tokens)
            assertEquals(2, tokens.size)
            assertEquals("空気", tokens[0].surface)
            assertEquals("공기", tokens[0].meaning)
            assertEquals("吸う", tokens[1].surface)

            val readings = dao.getReadingsForSurfaces(listOf("空気", "吸う")).associate { it.surface to it.reading }
            assertEquals("くうき", readings["空気"])
            assertEquals(1, dao.getExampleCountByLevel(5))
        }

    @Test
    fun 레벨별_예문_삭제는_해당_레벨만_지운다() =
        runTest {
            db.wordInitializeDao().insertWords(
                listOf(
                    WordEntity(id = 0, kanji = "A", pronunciation = "a", meaning = "에이"),
                    WordEntity(id = 0, kanji = "B", pronunciation = "b", meaning = "비"),
                ),
            )
            dao.insertExampleSentences(
                listOf(
                    ExampleSentenceEntity(0, "A", 5, "A文", "A문", 0, "", ""),
                    ExampleSentenceEntity(0, "B", 1, "B文", "B문", 0, "", ""),
                ),
            )
            dao.deleteExampleSentencesByLevel(5)
            assertEquals(0, dao.getExampleCountByLevel(5))
            assertEquals(1, dao.getExampleCountByLevel(1))
        }
}
