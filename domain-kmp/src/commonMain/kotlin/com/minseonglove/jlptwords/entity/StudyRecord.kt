package com.minseonglove.jlptwords.entity

data class StudyRecord(
    val id: Int,
    val sessionId: Int,
    val completionTimeSeconds: Int,
    val accuracy: Int,
    val createAt: Long,
)
