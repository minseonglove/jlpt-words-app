package com.minseonglove.jlptwords.entity

data class Word(
    val id: Int,
    val kanji: String,
    val pronunciation: String,
    val meaning: String,
    val partOfSpeech: String = "",
    val appearanceCount: Int = 0,
    val correctCount: Int = 0,
    val examples: List<Example> = emptyList(),
)
