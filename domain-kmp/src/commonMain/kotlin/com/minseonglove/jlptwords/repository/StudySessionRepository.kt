package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.Word

interface StudySessionRepository {
    suspend fun getWordsByStudySessionId(id: Int): List<Word>

    suspend fun getStudySessionsByLevel(
        level: JLPTLevel,
    ): List<StudySession>

    suspend fun getStudySessionCount(
        level: JLPTLevel,
    ): Int

    suspend fun getLevelBySessionId(
        id: Int,
    ): JLPTLevel
}
