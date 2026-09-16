package com.minseonglove.jlptwords.entity

data class StudySession(
    val id: Int,
    val level: JLPTLevel,
    val type: SessionType,
    val startNumber: Int,
    val endNumber: Int,
    val wordsSize: Int,
)
