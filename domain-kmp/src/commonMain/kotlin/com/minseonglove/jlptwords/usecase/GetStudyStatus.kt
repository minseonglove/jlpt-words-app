package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.StudyStatus
import com.minseonglove.jlptwords.repository.StudyStatusRepository

class GetStudyStatus(
    private val repository: StudyStatusRepository,
) {
    suspend operator fun invoke(): StudyStatus? {
        return repository.getStudyStatus()
    }
}
