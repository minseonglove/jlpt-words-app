package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.minseonglove.jlptwords.db.entity.ExampleSentenceEntity
import com.minseonglove.jlptwords.db.entity.VocabularyWordEntity
import com.minseonglove.jlptwords.db.util.DecodedRuby
import com.minseonglove.jlptwords.db.util.DecodedToken
import com.minseonglove.jlptwords.db.util.encodeExampleTokens
import com.minseonglove.jlptwords.db.util.encodeFurigana
import com.minseonglove.jlptwords.service.dto.ExampleSentenceDto

/** vocabulary_words surface→reading 조회 결과 (예문 토큰 표시용 후리가나) */
data class VocabReadingRow(
    val surface: String,
    val reading: String,
)

@Dao
interface ExampleDao {
    // ---- 요미가나 전역 사전 ----
    @Query("SELECT COUNT(*) FROM vocabulary_words")
    suspend fun getVocabularyCount(): Int

    // ---- 예문 (레벨별) ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExampleSentences(sentences: List<ExampleSentenceEntity>): List<Long>

    @Query("DELETE FROM example_sentences WHERE level_code = :levelCode")
    suspend fun deleteExampleSentencesByLevel(levelCode: Int)

    @Query("SELECT COUNT(*) FROM example_sentences WHERE level_code = :levelCode")
    suspend fun getExampleCountByLevel(levelCode: Int): Int

    @Query("SELECT COUNT(*) FROM example_sentences WHERE level_code = :levelCode AND furigana = ''")
    suspend fun getExampleCountWithoutFuriganaByLevel(levelCode: Int): Int

    // ---- 조회 (단어별 예문) ----
    // 동형이의어(같은 표기, 다른 단어)가 레벨별로 공존하므로, 예문은 (표기 + 레벨)로 귀속시킨다.
    // 토큰은 example_sentences.tokens 컬럼에 직렬화돼 있어 별도 조인이 필요 없다.
    @Query(
        """
        SELECT * FROM example_sentences
        WHERE level_code = :levelCode AND source_word_kanji IN (:kanjis)
        ORDER BY source_word_kanji, example_order
        """,
    )
    suspend fun getExampleSentencesByKanji(
        levelCode: Int,
        kanjis: List<String>,
    ): List<ExampleSentenceEntity>

    /** 예문 토큰 표시용 후리가나. 화면에 로드된 예문의 토큰 surface 집합으로만 조회한다. */
    @Query("SELECT surface, reading FROM vocabulary_words WHERE surface IN (:surfaces)")
    suspend fun getReadingsForSurfaces(surfaces: List<String>): List<VocabReadingRow>

    /** 표기로 예문 문장들을 로드한다(IN 절 900 분할). 토큰은 각 행의 tokens 컬럼에 들어 있다. */
    @Transaction
    suspend fun loadExamplesForKanjis(
        levelCode: Int,
        kanjis: List<String>,
    ): List<ExampleSentenceEntity> {
        if (kanjis.isEmpty()) return emptyList()
        return kanjis.chunked(900).flatMap { getExampleSentencesByKanji(levelCode, it) }
    }

    // ---- 동기화 ----
    @Query("SELECT * FROM vocabulary_words")
    suspend fun getAllVocabularyWords(): List<VocabularyWordEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVocabularyWordsIgnore(words: List<VocabularyWordEntity>)

    @Query("DELETE FROM vocabulary_words WHERE surface IN (:surfaces)")
    suspend fun deleteVocabularyBySurfaces(surfaces: List<String>)

    @Query("UPDATE vocabulary_words SET reading = :reading WHERE surface = :surface")
    suspend fun updateReadingBySurface(
        surface: String,
        reading: String,
    )

    /**
     * 레벨 예문 교체. 예문 1개 = 1행(토큰은 tokens 컬럼에 직렬화). 토큰당 행을 만들던 word_in_example 제거.
     * reading 은 표시 시 vocabulary_words 에서 조회하므로 여기서 surface→id 매핑이 필요 없다.
     */
    @Transaction
    suspend fun replaceLevelExamples(
        levelCode: Int,
        sentences: List<ExampleSentenceDto>,
    ) {
        deleteExampleSentencesByLevel(levelCode)
        if (sentences.isEmpty()) return
        insertExampleSentences(
            sentences.map { dto ->
                ExampleSentenceEntity(
                    id = 0,
                    sourceWordKanji = dto.sourceWordKanji,
                    levelCode = levelCode,
                    sentenceJp = dto.japanese,
                    sentenceKo = dto.korean,
                    exampleOrder = dto.order,
                    tokens = encodeExampleTokens(dto.tokens.map { DecodedToken(it.surface, it.meaning) }),
                    furigana = encodeFurigana(dto.furigana.map { DecodedRuby(it.text, it.reading) }),
                )
            },
        )
    }

    @Transaction
    suspend fun replaceReadings(readings: Map<String, String>) {
        val existing = getAllVocabularyWords().associateBy { it.surface }
        // 1. 제거된 surface만 삭제
        val toDelete = existing.keys.filterNot { readings.containsKey(it) }
        if (toDelete.isNotEmpty()) deleteVocabularyBySurfaces(toDelete)
        // 2. 신규 surface만 삽입 (기존 id 보존 — REPLACE 금지)
        val toInsert =
            readings
                .filterKeys { !existing.containsKey(it) }
                .map { (surface, reading) -> VocabularyWordEntity(id = 0, surface = surface, reading = reading) }
        if (toInsert.isNotEmpty()) insertVocabularyWordsIgnore(toInsert)
        // 3. reading이 바뀐 기존 surface만 업데이트 (id 유지)
        for ((surface, reading) in readings) {
            val prev = existing[surface]
            if (prev != null && prev.reading != reading) updateReadingBySurface(surface, reading)
        }
    }
}
