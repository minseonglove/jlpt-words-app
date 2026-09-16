package com.minseonglove.jlptwords.entity

data class LevelSummary(
    val level: JLPTLevel,
    val wordCount: Int,
    val sessionCount: Int,
    val sessionProgress: Float,
)
