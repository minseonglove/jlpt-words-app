package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.StudyHelpRepository

class HasSeenStudyHelp(
    private val studyHelpRepository: StudyHelpRepository,
) {
    suspend operator fun invoke(): Boolean {
        return studyHelpRepository.hasSeenStudyHelp()
    }
}
