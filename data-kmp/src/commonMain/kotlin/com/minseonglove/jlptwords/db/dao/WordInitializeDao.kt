package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.minseonglove.jlptwords.db.entity.StudySessionEntity
import com.minseonglove.jlptwords.db.entity.WordEntity
import com.minseonglove.jlptwords.db.entity.WordLevelMappingEntity
import com.minseonglove.jlptwords.db.entity.WordStatisticEntity
import com.minseonglove.jlptwords.db.util.StudySessionGenerator
import com.minseonglove.jlptwords.entity.JLPTLevel

@Dao
interface WordInitializeDao {
    // REPLACE 금지: words 의 (kanji, pronunciation) unique 충돌 시 REPLACE 는 기존 행을 삭제해
    // CASCADE 로 다른 레벨의 매핑·통계까지 파괴한다. IGNORE 후 기존 행을 찾아 매핑만 추가한다.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<WordEntity>): List<Long>

    @Query("SELECT id FROM words WHERE kanji = :kanji AND pronunciation = :pronunciation")
    suspend fun getWordIdByKanjiAndPronunciation(
        kanji: String,
        pronunciation: String,
    ): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWordStatistics(stats: List<WordStatisticEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>)

    @Update
    suspend fun updateSessions(sessions: List<StudySessionEntity>): Int

    @Query("DELETE FROM study_sessions WHERE id IN (:ids)")
    suspend fun deleteSessionsByIds(ids: List<Int>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMappings(mappings: List<WordLevelMappingEntity>)

    @Update
    suspend fun updateWordEntities(words: List<WordEntity>): Int

    @Query("DELETE FROM word_level_mapping WHERE level_code = :levelCode AND word_id IN (:wordIds)")
    suspend fun deleteMappingsByLevel(
        levelCode: Int,
        wordIds: List<Int>,
    )

    // 다른 레벨 매핑이 남아 있는 단어(미래의 다중 레벨 단어)는 행을 보존한다.
    @Query("DELETE FROM words WHERE id IN (:wordIds) AND id NOT IN (SELECT word_id FROM word_level_mapping)")
    suspend fun deleteWordsWithoutMapping(wordIds: List<Int>)

    /**
     * 단어들을 IGNORE 로 insert 하고, (kanji, pronunciation) 충돌로 무시된 항목은
     * 기존 행의 id 를 조회해 입력 순서대로 단어 id 목록을 돌려준다.
     */
    private suspend fun insertWordsResolvingConflicts(words: List<WordEntity>): List<Int> {
        val rowIds = insertWords(words)
        return words.mapIndexed { index, word ->
            val rowId = rowIds[index]
            if (rowId != -1L) {
                rowId.toInt()
            } else {
                getWordIdByKanjiAndPronunciation(word.kanji, word.pronunciation)
                    ?: error("단어 '${word.kanji}(${word.pronunciation})' insert 실패: 충돌 행도 찾을 수 없습니다.")
            }
        }
    }

    @Query("DELETE FROM study_sessions WHERE level_code = :levelCode")
    suspend fun deleteSessionsByLevel(levelCode: Int)

    @Query("SELECT * FROM study_sessions WHERE level_code = :levelCode ORDER BY id")
    suspend fun getStudySessionsByLevel(levelCode: Int): List<StudySessionEntity>

    @Transaction
    suspend fun initWord(
        words: List<WordEntity>,
        level: JLPTLevel,
    ) {
        // Thread safety: @Transaction requires sequential execution on single thread
        val wordIds = insertWordsWithStatistics(words)

        val sessions =
            StudySessionGenerator.generateStudySessions(
                level = level,
                totalWordCount = words.size,
            )
        insertSessions(sessions)

        insertLevelMappings(wordIds, level)
    }

    /** 단어를 넣고 각 행의 학습 통계를 함께 만든다. 돌려주는 id 는 기존 행과 충돌한 것도 포함한다. */
    private suspend fun insertWordsWithStatistics(words: List<WordEntity>): List<Int> {
        val wordIds = insertWordsResolvingConflicts(words)
        insertWordStatistics(
            wordIds.map { wordId ->
                WordStatisticEntity(
                    wordId = wordId,
                )
            },
        )
        return wordIds
    }

    private suspend fun insertLevelMappings(
        wordIds: List<Int>,
        level: JLPTLevel,
    ) {
        insertMappings(
            wordIds.map { wordId ->
                WordLevelMappingEntity(
                    wordId = wordId,
                    levelCode = level.code,
                )
            },
        )
    }

    @Transaction
    suspend fun updateWords(
        level: JLPTLevel,
        totalWordCount: Int,
        updatedWords: List<WordEntity>,
        deletedWords: List<WordEntity>,
        addedWords: List<WordEntity>,
    ) {
        // 1. 변경된 단어목록은 update
        if (updatedWords.isNotEmpty()) {
            updateWordEntities(updatedWords)
        }

        // 2. 제거된 단어목록은 이 레벨 매핑을 끊고, 다른 레벨 매핑이 없는 행만 삭제
        if (deletedWords.isNotEmpty()) {
            val deletedIds = deletedWords.map { it.id }
            deleteMappingsByLevel(level.code, deletedIds)
            deleteWordsWithoutMapping(deletedIds)
        }

        // 3. 추가된 단어목록은 Insert (기존 행과 충돌하면 매핑만 추가)
        if (addedWords.isNotEmpty()) {
            insertLevelMappings(insertWordsWithStatistics(addedWords), level)
        }

        // 단어 수가 바뀌었으면 세션 구성도 달라진다.
        if (deletedWords.size != addedWords.size) {
            val plan =
                planSessionSync(
                    current =
                        StudySessionGenerator.generateStudySessions(
                            level = level,
                            totalWordCount = totalWordCount,
                        ),
                    previous = getStudySessionsByLevel(levelCode = level.code),
                )

            if (plan.deleteIds.isNotEmpty()) {
                deleteSessionsByIds(plan.deleteIds)
            }
            if (plan.toInsert.isNotEmpty()) {
                insertSessions(plan.toInsert)
            }
            if (plan.toUpdate.isNotEmpty()) {
                updateSessions(plan.toUpdate)
            }
        }
    }
}

/** 기존 세션 목록을 새 구성으로 맞추기 위한 변경 지시. */
internal data class SessionSyncPlan(
    val deleteIds: List<Int>,
    val toInsert: List<StudySessionEntity>,
    val toUpdate: List<StudySessionEntity>,
)

/**
 * 세션 목록을 [current] 구성으로 맞추는 변경 지시를 만든다.
 *
 * 세션은 id 순서가 곧 학습 순서라 앞에서부터 짝지어 비교한다. 겹치는 구간은 내용이 달라진
 * 것만 기존 id 를 유지한 채 갱신해, 학습 기록이 매달린 행을 지웠다 다시 만들지 않는다.
 * 남거나 모자라는 꼬리만 삭제하거나 새로 넣는다.
 */
internal fun planSessionSync(
    current: List<StudySessionEntity>,
    previous: List<StudySessionEntity>,
): SessionSyncPlan {
    val common = minOf(current.size, previous.size)
    val toUpdate =
        (0 until common).mapNotNull { index ->
            val curr = current[index]
            val prev = previous[index]
            val isSame =
                curr.sessionTypeCode == prev.sessionTypeCode &&
                    curr.startNumber == prev.startNumber &&
                    curr.endNumber == prev.endNumber &&
                    curr.wordsSize == prev.wordsSize
            if (isSame) null else curr.copy(id = prev.id, levelCode = prev.levelCode)
        }

    return SessionSyncPlan(
        deleteIds = previous.drop(common).map { it.id },
        toInsert = current.drop(common),
        toUpdate = toUpdate,
    )
}
