package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.StudyHelpRepository

class SetStudyHelpSeen(
    private val studyHelpRepository: StudyHelpRepository,
) {
    suspend operator fun invoke() {
        studyHelpRepository.setStudyHelpSeen()
    }
}
