package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.AnalyticsRepository

class LogStudySessionCompleted(
    private val repository: AnalyticsRepository,
) {
    operator fun invoke(
        level: JLPTLevel,
        sessionNumber: Int,
        accuracy: Int,
        elapsedTimeSeconds: Int,
    ) {
        repository.logStudySessionCompleted(
            level = level,
            sessionNumber = sessionNumber,
            accuracy = accuracy,
            elapsedTimeSeconds = elapsedTimeSeconds,
        )
    }
}
