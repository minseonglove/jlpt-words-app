package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.repository.StudyRecordRepository

class GetStudyRecordsBySessionId(
    private val repository: StudyRecordRepository,
) {
    suspend operator fun invoke(
        sessionId: Int,
    ): List<StudyRecord> {
        return repository.getStudyRecordsBySessionId(sessionId)
    }
}
