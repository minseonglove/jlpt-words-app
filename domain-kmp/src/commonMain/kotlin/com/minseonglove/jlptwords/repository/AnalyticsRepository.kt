package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.JLPTLevel

interface AnalyticsRepository {
    fun logStudySessionCompleted(
        level: JLPTLevel,
        sessionNumber: Int,
        accuracy: Int,
        elapsedTimeSeconds: Int,
    )
}
