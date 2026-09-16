package com.minseonglove.jlptwords.db.util

import com.minseonglove.jlptwords.db.entity.WordEntity

/**
 * 한 급수의 로컬 단어와 원격 단어를 비교해 갱신·삭제·추가 대상을 가른다.
 *
 * 단어의 정체성은 (표기, 발음)이다 — 같은 표기라도 발음이 다르면 별개 단어로 공존한다.
 * 그래서 표기만으로 짝을 지으면 동형이의어 중 하나가 조용히 사라지므로 두 단계로 매칭한다.
 */
object WordLevelDiff {
    /** 발음이 수정된 것으로 판정된 짝. 실제 갱신 전에 다른 급수와의 (표기, 발음) 충돌 검사가 필요하다. */
    data class Rename(
        val previous: WordEntity,
        val current: WordEntity,
    )

    data class Result(
        /** id 를 유지한 채 뜻·품사만 갱신할 단어. */
        val updated: List<WordEntity>,
        val renamed: List<Rename>,
        val deleted: List<WordEntity>,
        val added: List<WordEntity>,
    )

    fun compute(
        previous: List<WordEntity>,
        current: List<WordEntity>,
    ): Result {
        val currentByKey = current.associateBy { it.key() }
        val previousKeys = previous.mapTo(HashSet()) { it.key() }

        val updated = mutableListOf<WordEntity>()
        val unmatchedPrevious = mutableListOf<WordEntity>()
        previous.forEach { previousWord ->
            val currentWord = currentByKey[previousWord.key()]
            when {
                currentWord == null -> unmatchedPrevious += previousWord
                previousWord.meaning != currentWord.meaning ||
                    previousWord.partOfSpeech != currentWord.partOfSpeech ->
                    updated +=
                        previousWord.copy(
                            meaning = currentWord.meaning,
                            partOfSpeech = currentWord.partOfSpeech,
                        )
            }
        }

        val unmatchedCurrentByKanji =
            current
                .filterNot { it.key() in previousKeys }
                .groupByTo(HashMap<String, MutableList<WordEntity>>()) { it.kanji }

        // 짝을 못 찾은 것끼리 표기로 재매칭한다. 삭제·추가로 처리하면 행이 새로 만들어져
        // 학습 통계가 CASCADE 로 사라지므로, 발음 수정은 id 를 유지하는 갱신으로 다룬다.
        val renamed = mutableListOf<Rename>()
        val deleted = mutableListOf<WordEntity>()
        unmatchedPrevious.forEach { previousWord ->
            val currentWord = unmatchedCurrentByKanji[previousWord.kanji]?.removeFirstOrNull()
            if (currentWord == null) {
                deleted += previousWord
            } else {
                renamed += Rename(previous = previousWord, current = currentWord)
            }
        }

        return Result(
            updated = updated,
            renamed = renamed,
            deleted = deleted,
            added = unmatchedCurrentByKanji.values.flatten(),
        )
    }

    private fun WordEntity.key(): Pair<String, String> = kanji to pronunciation
}
