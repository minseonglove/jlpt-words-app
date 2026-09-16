package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.StudyRecordRepository

class AddStudyRecord(
    private val repository: StudyRecordRepository,
) {
    suspend operator fun invoke(
        sessionId: Int,
        completionTimeSeconds: Int,
        accuracy: Int,
        createAt: Long,
    ) {
        repository.addStudyRecord(
            sessionId = sessionId,
            completionTimeSeconds = completionTimeSeconds,
            accuracy = accuracy,
            createAt = createAt,
        )
    }
}
