package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.repository.StudySessionRepository

class GetStudySessionsByLevel(
    private val repository: StudySessionRepository,
) {
    suspend operator fun invoke(
        level: JLPTLevel,
    ): List<StudySession> {
        return repository.getStudySessionsByLevel(level)
    }
}
