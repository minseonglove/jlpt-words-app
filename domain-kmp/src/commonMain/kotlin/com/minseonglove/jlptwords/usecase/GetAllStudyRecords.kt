package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.repository.StudyRecordRepository

class GetAllStudyRecords(
    private val repository: StudyRecordRepository,
) {
    suspend operator fun invoke(): List<StudyRecord> {
        return repository.getAllStudyRecords()
    }
}
