package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.ExampleDao
import com.minseonglove.jlptwords.db.dao.WordDao
import com.minseonglove.jlptwords.db.dao.WordInitializeDao
import com.minseonglove.jlptwords.db.dao.WordKey
import com.minseonglove.jlptwords.db.entity.WordEntity
import com.minseonglove.jlptwords.db.util.WordLevelDiff
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.proto.ExampleChunk
import com.minseonglove.jlptwords.proto.ReadingsBlob
import com.minseonglove.jlptwords.service.FirestoreService
import com.minseonglove.jlptwords.service.dto.ExampleRubyDto
import com.minseonglove.jlptwords.service.dto.ExampleSentenceDto
import com.minseonglove.jlptwords.service.dto.ExampleTokenDto
import com.minseonglove.jlptwords.util.inflateRaw

class WordInitializeDataSource(
    private val wordInitializeDao: WordInitializeDao,
    private val wordDao: WordDao,
    private val exampleDao: ExampleDao,
    private val firestoreService: FirestoreService,
) {
    suspend fun initializeWords(level: JLPTLevel) {
        val words = firestoreService.getWords(level)
        wordInitializeDao.initWord(words = words.toEntities(), level = level)
    }

    suspend fun updateWords(level: JLPTLevel) {
        val currentWords = firestoreService.getWords(level)
        val diff =
            WordLevelDiff.compute(
                previous = wordDao.getWordsByLevel(level.code),
                current = currentWords.toEntities(),
            )
        // 발음 수정은 id 를 유지하는 갱신으로 반영해 학습 통계를 보존한다. 다만 바뀐 발음이
        // 다른 급수의 행과 같아지면 (표기, 발음) unique 에 걸려 트랜잭션 전체가 실패하므로,
        // 그 짝만 삭제·추가로 넘긴다(적재 시 기존 행에 매핑만 추가되고 통계는 그쪽을 따른다).
        val (safeRenames, conflictingRenames) =
            if (diff.renamed.isEmpty()) {
                emptyList<WordLevelDiff.Rename>() to emptyList()
            } else {
                val takenKeys = wordDao.getWordKeysOutsideLevel(level.code).toHashSet()
                diff.renamed.partition { rename ->
                    WordKey(rename.current.kanji, rename.current.pronunciation) !in takenKeys
                }
            }

        wordInitializeDao.updateWords(
            level = level,
            totalWordCount = currentWords.size,
            updatedWords =
                diff.updated +
                    safeRenames.map { (previous, current) ->
                        previous.copy(
                            pronunciation = current.pronunciation,
                            meaning = current.meaning,
                            partOfSpeech = current.partOfSpeech,
                        )
                    },
            deletedWords = diff.deleted + conflictingRenames.map { it.previous },
            addedWords = diff.added + conflictingRenames.map { it.current },
        )
    }

    /**
     * 요미 사전 전역 교체 (readings/all — ReadingsBlob 직렬화 + raw deflate 압축 bytes 필드).
     * blob 이 없으면 예외를 던진다 — 빈 사전으로 교체해 기존 요미를 지우지 않기 위함.
     */
    suspend fun syncReadings() {
        val blob = firestoreService.getReadingsBlob() ?: error("readings/all 문서에 data 필드가 없습니다")
        val readings =
            ReadingsBlob.ADAPTER
                .decode(inflateRaw(blob))
                .entries
                .associate { it.surface to it.reading }
        exampleDao.replaceReadings(readings)
    }

    /** words 전체 표제어(kanji) 집합. 예문 표제어 필터용. 일괄 동기화 시 1회 만들어 재사용한다. */
    suspend fun loadKnownKanji(): Set<String> = wordDao.getAllKanji().toHashSet()

    /**
     * 레벨 예문 청크 다운로드 + 디코드 (적재는 [insertExamples]).
     * 청크는 Wire(ExampleChunk) 직렬화 후 raw deflate 로 압축된 bytes 필드다.
     * 예문이 전체 페이로드의 대부분이라, 단어 다운로드와 네트워크 구간을 겹칠 수 있도록 적재와 분리했다.
     */
    suspend fun fetchExamples(level: JLPTLevel): List<ExampleSentenceDto> =
        firestoreService.getExampleChunkBlobs(level).flatMap { blob ->
            ExampleChunk.ADAPTER.decode(inflateRaw(blob)).words.flatMap { word ->
                word.examples.mapIndexed { order, example ->
                    ExampleSentenceDto(
                        sourceWordKanji = word.kanji,
                        japanese = example.japanese,
                        korean = example.korean,
                        order = order,
                        tokens = example.tokens.map { ExampleTokenDto(surface = it.surface, meaning = it.meaning) },
                        furigana = example.furigana.map { ExampleRubyDto(text = it.text, reading = it.reading) },
                    )
                }
            }
        }

    /**
     * 레벨 예문 교체 적재 (examples_{level}). 예문은 (표기, 레벨)로 단어에 귀속된다.
     *
     * words 에 없는 표제어(예문·단어장 표기 불일치: 반복기호 々·이체자 등)의 예문은 표시 시 단어와 못 이어지므로
     * 적재 전에 words 에 실재하는 표제어의 예문만 남긴다.
     *
     * [knownKanji] 는 호출자가 1회 만들어 주입한다(급수마다 words 전체를 재조회하지 않도록).
     *
     * @return 제외된 표제어 종류 수. 원본 데이터에 고아 예문은 없으므로, 0 이 아니면
     * 로컬 단어가 낡아 필터가 과하게 걸린 것이다 — 호출부는 이 적재를 완결로 취급하면 안 된다.
     */
    suspend fun insertExamples(
        level: JLPTLevel,
        dtos: List<ExampleSentenceDto>,
        knownKanji: Set<String>,
    ): Int {
        val (valid, dropped) = dtos.partition { it.sourceWordKanji in knownKanji }
        val droppedKanji = dropped.map { it.sourceWordKanji }.distinct()
        if (droppedKanji.isNotEmpty()) {
            println("insertExamples($level): words 미존재 표제어 ${droppedKanji.size}건 예문 제외 → $droppedKanji")
        }
        exampleDao.replaceLevelExamples(level.code, valid)
        return droppedKanji.size
    }

    /** 레벨 예문 다운로드 + 적재 (레벨 단독 동기화 경로용). 반환값은 [insertExamples] 와 같다. */
    suspend fun syncExamples(
        level: JLPTLevel,
        knownKanji: Set<String>,
    ): Int = insertExamples(level, fetchExamples(level), knownKanji)

    private fun List<Word>.toEntities(): List<WordEntity> = map { it.toEntity() }

    private fun Word.toEntity(): WordEntity =
        WordEntity(
            id = 0,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            partOfSpeech = partOfSpeech,
        )
}
