package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.StudySessionRepository

class GetLevelByStudySessionId(
    private val repository: StudySessionRepository,
) {
    suspend operator fun invoke(id: Int): JLPTLevel {
        return repository.getLevelBySessionId(id)
    }
}
