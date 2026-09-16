package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import com.minseonglove.jlptwords.db.entity.WordEntity

data class WordWithStats(
    val id: Int,
    val kanji: String,
    val pronunciation: String,
    val meaning: String,
    val partOfSpeech: String,
    val appearanceCount: Int,
    val correctCount: Int,
)

/** 단어의 정체성인 (표기, 발음) 쌍. */
data class WordKey(
    val kanji: String,
    val pronunciation: String,
)

/** 단어 검색 결과 행. 급수(단어가 속한 레벨 중 가장 높은 급수)와 통계를 함께 가진다. */
data class SearchedWordRow(
    val id: Int,
    val kanji: String,
    val pronunciation: String,
    val meaning: String,
    val levelCode: Int,
    val appearanceCount: Int,
    val correctCount: Int,
)

@Dao
interface WordDao {
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query(
        """
        SELECT COUNT(*) FROM words words
        INNER JOIN word_level_mapping wlm ON words.id = wlm.word_id
        WHERE wlm.level_code = :levelCode
        """,
    )
    suspend fun getWordCountByLevel(levelCode: Int): Int

    /**
     * 품사가 비어 있는 단어 수. 원본 단어장은 모든 행에 품사를 갖고 있으므로,
     * 0 이 아니면 스키마만 갱신되고 값이 채워지지 않은 상태(v1→v2 마이그레이션 직후)다.
     */
    @Query(
        """
        SELECT COUNT(*) FROM words words
        INNER JOIN word_level_mapping wlm ON words.id = wlm.word_id
        WHERE wlm.level_code = :levelCode AND words.part_of_speech = ''
        """,
    )
    suspend fun getWordCountMissingPartOfSpeech(levelCode: Int): Int

    @Query(
        """
        SELECT words.* FROM words words
        INNER JOIN word_level_mapping wlm ON words.id = wlm.word_id
        WHERE wlm.level_code = :levelCode
        ORDER BY words.id
        """,
    )
    suspend fun getWordsByLevel(levelCode: Int): List<WordEntity>

    @Query(
        """
    SELECT
      w.id,
      w.kanji,
      w.pronunciation,
      w.meaning,
      w.part_of_speech       AS partOfSpeech,
      ws.appearance_count    AS appearanceCount,
      ws.correct_count       AS correctCount
    FROM words AS w
    INNER JOIN word_level_mapping AS wlm
      ON w.id = wlm.word_id
     AND wlm.level_code = :levelCode
    LEFT JOIN words_statistics AS ws
      ON ws.word_id = w.id
    ORDER BY w.id
    LIMIT :wordsSize OFFSET :startOffset
    """,
    )
    suspend fun getWordsByLevel(
        levelCode: Int,
        startOffset: Int,
        wordsSize: Int,
    ): List<WordWithStats>

    /**
     * (표기, 발음)으로 단어 1건을 조회한다. 동형이의어(같은 표기, 다른 발음)를 구분하기 위함.
     * 발음은 `・` 병기(예: いく・ゆく)를 지원한다 — 전달된 발음이 병기 전체와 일치하거나
     * 병기 항목 중 하나와 일치하면 매칭된다(예문 토큰의 단일 요미로도 진입 가능하도록).
     *
     * [pronunciation] 의 LIKE 메타문자는 호출부([com.minseonglove.jlptwords.datasource.WordDataSource])에서
     * 이스케이프되어 리터럴로 취급된다.
     */
    @Query(
        """
    SELECT
      w.id,
      w.kanji,
      w.pronunciation,
      w.meaning,
      w.part_of_speech       AS partOfSpeech,
      COALESCE(ws.appearance_count, 0)    AS appearanceCount,
      COALESCE(ws.correct_count, 0)       AS correctCount
    FROM words AS w
    LEFT JOIN words_statistics AS ws
      ON ws.word_id = w.id
    WHERE w.kanji = :kanji
      AND ('・' || w.pronunciation || '・') LIKE ('%・' || :pronunciation || '・%') ESCAPE '\'
    LIMIT 1
    """,
    )
    suspend fun getWordByKanjiAndPronunciation(
        kanji: String,
        pronunciation: String,
    ): WordWithStats?

    /**
     * 검색어(표기·발음·뜻 부분일치)에 매칭되는 모든 단어를 급수·통계와 함께 조회한다.
     * 빈 검색어면 전체 단어가 매칭된다. 여러 레벨에 속한 단어는 가장 높은 급수(MIN level_code)로 묶는다.
     *
     * [query] 의 LIKE 메타문자(%, _)는 호출부([com.minseonglove.jlptwords.datasource.WordDataSource])에서
     * '\' 로 이스케이프되어 리터럴로 취급된다.
     */
    @Query(
        """
    SELECT
      w.id,
      w.kanji,
      w.pronunciation,
      w.meaning,
      MIN(wlm.level_code)                 AS levelCode,
      COALESCE(ws.appearance_count, 0)    AS appearanceCount,
      COALESCE(ws.correct_count, 0)       AS correctCount
    FROM words AS w
    INNER JOIN word_level_mapping AS wlm
      ON w.id = wlm.word_id
    LEFT JOIN words_statistics AS ws
      ON ws.word_id = w.id
    WHERE w.kanji LIKE '%' || :query || '%' ESCAPE '\'
       OR w.pronunciation LIKE '%' || :query || '%' ESCAPE '\'
       OR w.meaning LIKE '%' || :query || '%' ESCAPE '\'
    GROUP BY w.id
    ORDER BY w.id
    """,
    )
    suspend fun searchWords(query: String): List<SearchedWordRow>

    @Query("SELECT kanji FROM words")
    suspend fun getAllKanji(): List<String>

    /**
     * 이 급수에 속하지 않은 단어들의 (표기, 발음).
     * 발음 수정 갱신이 다른 급수의 행과 unique 충돌을 일으키는지 미리 거르는 데 쓴다.
     */
    @Query(
        """
    SELECT kanji, pronunciation FROM words
    WHERE id NOT IN (SELECT word_id FROM word_level_mapping WHERE level_code = :levelCode)
    """,
    )
    suspend fun getWordKeysOutsideLevel(levelCode: Int): List<WordKey>

    /** 주어진 표기 중 words 에 등재된 것만 반환(예문 토큰의 JLPT 기출 여부 판정용). */
    @Query("SELECT kanji FROM words WHERE kanji IN (:kanjis)")
    suspend fun getExistingKanjis(kanjis: List<String>): List<String>

    @Query("SELECT MIN(level_code) FROM word_level_mapping WHERE word_id = :wordId")
    suspend fun getMinLevelCodeByWordId(wordId: Int): Int?

    // UPDATE 만 두면 통계 행이 없는 단어(적재 경로 변경·수동 복구 등)의 학습 결과가 조용히 사라진다.
    // UPSERT 로 없으면 만들고 있으면 누적한다.
    @Query(
        """
        INSERT INTO words_statistics (word_id, appearance_count, correct_count)
        VALUES (:wordId, 1, 0)
        ON CONFLICT(word_id) DO UPDATE SET
          appearance_count = appearance_count + 1
        """,
    )
    suspend fun increaseAppearanceCount(wordId: Int)

    @Query(
        """
        INSERT INTO words_statistics (word_id, appearance_count, correct_count)
        VALUES (:wordId, 1, 1)
        ON CONFLICT(word_id) DO UPDATE SET
          appearance_count = appearance_count + 1,
          correct_count = correct_count + 1
        """,
    )
    suspend fun increaseCorrectAndAppearanceCount(wordId: Int)

    /** 한 번이라도 학습(노출)된 단어 수. 전 급수 합산(words 테이블은 급수 공용). */
    @Query("SELECT COUNT(*) FROM words_statistics WHERE appearance_count > 0")
    suspend fun getStudiedWordCount(): Int

    /** 해당 레벨에서 예문이 있는 단어 중 1건을 무작위로 조회한다. 홈 '오늘의 예문' 추첨용. */
    @Query(
        """
    SELECT
      w.id,
      w.kanji,
      w.pronunciation,
      w.meaning,
      w.part_of_speech       AS partOfSpeech,
      COALESCE(ws.appearance_count, 0)    AS appearanceCount,
      COALESCE(ws.correct_count, 0)       AS correctCount
    FROM words AS w
    INNER JOIN word_level_mapping AS wlm
      ON w.id = wlm.word_id
     AND wlm.level_code = :levelCode
    LEFT JOIN words_statistics AS ws
      ON ws.word_id = w.id
    WHERE w.kanji IN (
      SELECT DISTINCT source_word_kanji FROM example_sentences WHERE level_code = :levelCode
    )
    ORDER BY RANDOM()
    LIMIT 1
    """,
    )
    suspend fun getRandomWordHavingExamples(levelCode: Int): WordWithStats?
}
