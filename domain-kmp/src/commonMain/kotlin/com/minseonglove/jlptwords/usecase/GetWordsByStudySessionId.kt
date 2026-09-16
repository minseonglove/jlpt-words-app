package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.repository.StudySessionRepository

class GetWordsByStudySessionId(
    private val repository: StudySessionRepository,
) {
    suspend operator fun invoke(
        id: Int,
    ): List<Word> {
        return repository.getWordsByStudySessionId(id)
    }
}
