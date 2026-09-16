package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.repository.StudyRecordRepository

class GetStudyRecordsByLevel(
    private val repository: StudyRecordRepository,
) {
    suspend operator fun invoke(
        level: JLPTLevel,
    ): List<StudyRecord> {
        return repository.getStudyRecordsByLevel(level)
    }
}
