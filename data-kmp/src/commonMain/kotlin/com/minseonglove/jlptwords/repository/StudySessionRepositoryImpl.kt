package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.StudySessionDataSource
import com.minseonglove.jlptwords.datasource.WordDataSource
import com.minseonglove.jlptwords.db.entity.toStudySession
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class StudySessionRepositoryImpl(
    private val studySessionDataSource: StudySessionDataSource,
    private val wordDataSource: WordDataSource,
) : StudySessionRepository {
    override suspend fun getWordsByStudySessionId(id: Int): List<Word> =
        withContext(Dispatchers.IO) {
            val selectedSession = studySessionDataSource.getSession(id) ?: return@withContext emptyList()
            val startOffset = selectedSession.startNumber - 1 // 1-based to 0-based
            // 예문 없이 범위 단어만 먼저 로드 → 세션 타입별로 선별한 뒤,
            // 살아남은 단어에만 예문을 붙인다(RANDOM/LOW_ACCURACY 의 예문 과적재 방지).
            val rangeWords =
                wordDataSource.getWordsByRange(
                    levelCode = selectedSession.levelCode,
                    startOffset = startOffset,
                    wordsSize = selectedSession.endNumber - selectedSession.startNumber + 1,
                )
            val selectedWords =
                when (SessionType.fromCode(selectedSession.sessionTypeCode)) {
                    SessionType.NORMAL -> {
                        rangeWords
                    }

                    SessionType.RANDOM -> {
                        rangeWords.shuffled().take(selectedSession.wordsSize)
                    }

                    SessionType.LOW_ACCURACY -> {
                        getLowAccuracyWords(
                            words = rangeWords,
                            offset = selectedSession.wordsSize,
                        )
                    }
                }
            wordDataSource.attachExamples(selectedWords, selectedSession.levelCode)
        }

    private fun getLowAccuracyWords(
        words: List<Word>,
        offset: Int,
    ): List<Word> {
        val cmp =
            Comparator<Word> { a, b ->
                val aUnseen = a.appearanceCount == 0 && a.correctCount == 0
                val bUnseen = b.appearanceCount == 0 && b.correctCount == 0

                if (aUnseen && !bUnseen) {
                    // a는 한 번도 등장하지 않은 단어 → 리스트의 뒤로 보냄
                    return@Comparator 1
                }
                if (bUnseen && !aUnseen) {
                    // b는 한 번도 등장하지 않은 단어 → 리스트의 뒤로 보냄
                    return@Comparator -1
                }

                val left = a.correctCount.toLong() * b.appearanceCount.toLong()
                val right = b.correctCount.toLong() * a.appearanceCount.toLong()
                left.compareTo(right)
            }

        return words.sortedWith(cmp).take(offset)
    }

    override suspend fun getStudySessionsByLevel(
        level: JLPTLevel,
    ): List<StudySession> {
        return studySessionDataSource
            .getStudySessionsByLevel(
                level = level,
            ).map {
                it.toStudySession()
            }
    }

    override suspend fun getStudySessionCount(
        level: JLPTLevel,
    ): Int {
        return studySessionDataSource.getStudySessionCount(level)
    }

    override suspend fun getLevelBySessionId(id: Int): JLPTLevel {
        return studySessionDataSource.getLevelBySessionId(id)
    }
}
