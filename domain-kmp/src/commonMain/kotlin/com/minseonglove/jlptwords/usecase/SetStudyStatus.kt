package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.StudyStatus
import com.minseonglove.jlptwords.repository.StudyStatusRepository

class SetStudyStatus(
    private val repository: StudyStatusRepository,
) {
    suspend operator fun invoke(studyStatus: StudyStatus?) {
        repository.setStudyStatus(studyStatus)
    }
}
