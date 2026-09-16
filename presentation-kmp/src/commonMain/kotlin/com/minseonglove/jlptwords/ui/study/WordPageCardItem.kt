package com.minseonglove.jlptwords.ui.study

import com.minseonglove.jlptwords.entity.Example
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WordPageCardItem(
    val id: Int,
    val kanji: String,
    val pronunciation: String,
    val meaning: String,
    val partOfSpeech: String = "",
    val appearanceCount: Int = 0,
    val correctCount: Int = 0,
    val examples: ImmutableList<Example> = persistentListOf(),
) {
    val accuracy: Int? =
        if (appearanceCount == 0) {
            null
        } else {
            (correctCount * 100) / appearanceCount
        }
}
