package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.ExampleDao
import com.minseonglove.jlptwords.db.dao.SearchedWordRow
import com.minseonglove.jlptwords.db.dao.WordDao
import com.minseonglove.jlptwords.db.dao.WordWithStats
import com.minseonglove.jlptwords.db.entity.WordEntity
import com.minseonglove.jlptwords.db.util.buildRubySegments
import com.minseonglove.jlptwords.db.util.decodeExampleTokens
import com.minseonglove.jlptwords.db.util.decodeFurigana
import com.minseonglove.jlptwords.entity.Example
import com.minseonglove.jlptwords.entity.ExampleToken
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.service.FirestoreService
import kotlinx.collections.immutable.toImmutableList

class WordDataSource(
    private val wordDao: WordDao,
    private val exampleDao: ExampleDao,
    private val firestoreService: FirestoreService,
) {
    suspend fun getWordCount(): Int {
        return wordDao.getWordCount()
    }

    suspend fun getWordCountByLevel(
        levelCode: Int,
    ): Int {
        return wordDao.getWordCountByLevel(levelCode)
    }

    suspend fun getWordCountMissingPartOfSpeech(
        levelCode: Int,
    ): Int {
        return wordDao.getWordCountMissingPartOfSpeech(levelCode)
    }

    suspend fun getWordsByLevel(
        levelCode: Int,
    ): List<WordEntity> {
        return wordDao.getWordsByLevel(levelCode)
    }

    /**
     * 범위 내 단어를 통계와 함께 로드한다(예문은 붙이지 않음).
     * RANDOM/LOW_ACCURACY 처럼 일부만 선별하는 세션은 선별을 먼저 끝낸 뒤
     * [attachExamples]로 살아남은 단어에만 예문을 붙여 불필요한 예문 I/O를 피한다.
     */
    suspend fun getWordsByRange(
        levelCode: Int,
        startOffset: Int,
        wordsSize: Int,
    ): List<Word> {
        return wordDao.getWordsByLevel(levelCode, startOffset, wordsSize).map { it.toWord() }
    }

    /**
     * (표기, 발음)으로 단어 1건을 전체 예문(예문 토큰의 JLPT 여부 포함)과 함께 조회한다. 없으면 null.
     * 동형이의어(같은 표기, 다른 발음·뜻)를 구분하기 위해 발음까지 받는다.
     * 예문은 단어가 속한 레벨의 것만 붙인다.
     */
    suspend fun getWord(
        kanji: String,
        pronunciation: String,
    ): Word? {
        val word = wordDao.getWordByKanjiAndPronunciation(kanji, pronunciation.escapeLikePattern())?.toWord() ?: return null
        val levelCode = wordDao.getMinLevelCodeByWordId(word.id) ?: return word
        return attachExamples(listOf(word), levelCode).firstOrNull()
    }

    /** 단어가 속한 JLPT 레벨 중 가장 높은 급수(N1쪽). 단어가 있으면 매핑도 항상 함께 생성되므로 non-null. */
    suspend fun getJlptLevel(wordId: Int): JLPTLevel =
        wordDao.getMinLevelCodeByWordId(wordId)?.let { JLPTLevel.fromCode(it) }
            ?: error("단어 id=$wordId 에 해당하는 JLPT 레벨 매핑이 없습니다.")

    /**
     * (표기, 발음) 단어에 [levelCode] 급수의 예문만 붙여 조회한다. 없으면 null.
     * [getWord] 와 달리 예문 귀속 급수를 호출부가 지정한다(홈 '오늘의 예문' 재조회용).
     */
    suspend fun getWordWithExamplesForLevel(
        kanji: String,
        pronunciation: String,
        levelCode: Int,
    ): Word? {
        val word = wordDao.getWordByKanjiAndPronunciation(kanji, pronunciation.escapeLikePattern())?.toWord() ?: return null
        return attachExamples(listOf(word), levelCode).firstOrNull()
    }

    /** 해당 급수에서 예문이 있는 단어 중 1건을 무작위로 조회한다(예문은 붙이지 않음). */
    suspend fun getRandomWordHavingExamples(
        levelCode: Int,
    ): Word? = wordDao.getRandomWordHavingExamples(levelCode)?.toWord()

    /** 한 번이라도 학습(노출)된 단어 수(전 급수 합산). */
    suspend fun getStudiedWordCount(): Int = wordDao.getStudiedWordCount()

    /** 검색어(표기·발음·뜻 부분일치)에 매칭되는 모든 단어를 급수·통계와 함께 조회한다. 빈 검색어면 전체 단어. */
    suspend fun searchWords(query: String): List<SearchedWord> = wordDao.searchWords(query.escapeLikePattern()).map { it.toSearchedWord() }

    /** LIKE 패턴 메타문자(%, _)와 이스케이프 문자(\)를 리터럴로 취급하도록 이스케이프한다. */
    private fun String.escapeLikePattern(): String =
        replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")

    /** 주어진 단어들에만 해당 레벨의 예문(+토큰)을 붙여 반환한다. 입력 순서는 보존된다. */
    suspend fun attachExamples(
        words: List<Word>,
        levelCode: Int,
    ): List<Word> {
        if (words.isEmpty()) return words
        val sentences = exampleDao.loadExamplesForKanjis(levelCode, words.map { it.kanji })
        if (sentences.isEmpty()) return words

        // 각 예문의 토큰을 역직렬화하고, 표시용 reading(요미)·JLPT 기출 여부를 표기 집합으로 한 번에 조회한다.
        val tokensBySentenceId = sentences.associate { it.id to decodeExampleTokens(it.tokens) }
        val surfaces =
            tokensBySentenceId.values
                .flatten()
                .map { it.surface }
                .distinct()
        val readingBySurface =
            surfaces
                .chunked(900)
                .flatMap { exampleDao.getReadingsForSurfaces(it) }
                .associate { it.surface to it.reading }
        val jlptSurfaces =
            surfaces
                .chunked(900)
                .flatMap { wordDao.getExistingKanjis(it) }
                .toHashSet()

        val examplesByKanji =
            sentences.groupBy { it.sourceWordKanji }.mapValues { (_, list) ->
                list.sortedBy { it.exampleOrder }.map { s ->
                    Example(
                        japanese = s.sentenceJp,
                        korean = s.sentenceKo,
                        furigana = buildRubySegments(s.sentenceJp, decodeFurigana(s.furigana)),
                        tokens =
                            tokensBySentenceId
                                .getValue(s.id)
                                .map { t ->
                                    ExampleToken(
                                        surface = t.surface,
                                        reading = readingBySurface[t.surface].orEmpty(),
                                        meaning = t.meaning,
                                        isJlptWord = t.surface in jlptSurfaces,
                                    )
                                }.toImmutableList(),
                    )
                }
            }
        return words.map { it.copy(examples = examplesByKanji[it.kanji] ?: emptyList()) }
    }

    private fun WordWithStats.toWord(): Word =
        Word(
            id = id,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            partOfSpeech = partOfSpeech,
            appearanceCount = appearanceCount,
            correctCount = correctCount,
        )

    private fun SearchedWordRow.toSearchedWord(): SearchedWord =
        SearchedWord(
            id = id,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            jlptLevel = JLPTLevel.fromCode(levelCode),
            appearanceCount = appearanceCount,
            correctCount = correctCount,
        )

    suspend fun increaseAppearanceCount(
        wordId: Int,
    ) {
        wordDao.increaseAppearanceCount(wordId)
    }

    suspend fun increaseCorrectAndAppearanceCount(
        wordId: Int,
    ) {
        wordDao.increaseCorrectAndAppearanceCount(wordId)
    }

    suspend fun getContentUpdateDate(level: JLPTLevel) = firestoreService.getContentUpdateDate(level)

    suspend fun getAllContentUpdateDates() = firestoreService.getAllContentUpdateDates()

    suspend fun getReadingsUpdateDate() = firestoreService.getReadingsUpdateDate()

    suspend fun getExampleCountByLevel(levelCode: Int) = exampleDao.getExampleCountByLevel(levelCode)

    suspend fun getVocabularyCount() = exampleDao.getVocabularyCount()
}
