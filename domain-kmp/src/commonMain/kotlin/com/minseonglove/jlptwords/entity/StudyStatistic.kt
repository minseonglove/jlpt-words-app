package com.minseonglove.jlptwords.entity

data class StudyStatistic(
    val streakInfo: StreakInfo,
    val totalProgress: Float,
    val totalElapsedTimeSeconds: Int,
    val averageDailyElapsedTimeSeconds: Int,
    val weeklyElapsedTimeSeconds: Int,
)
