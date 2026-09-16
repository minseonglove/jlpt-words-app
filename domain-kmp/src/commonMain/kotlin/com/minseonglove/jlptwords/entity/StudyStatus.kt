package com.minseonglove.jlptwords.entity

data class StudyStatus(
    val sessionId: Int,
    val sessionPosition: Int,
    val level: JLPTLevel,
    val words: List<Word>,
    val totalKnownWordIds: Set<Int>,
    val currentKnownWordIds: Set<Int>,
    val currentPage: Int,
    val totalElapsedTimeSeconds: Int,
    val currentElapsedTimeSeconds: Int,
    val totalWordSize: Int,
    val totalAppearanceCount: Int,
    val roundProgresses: List<Int>,
)
