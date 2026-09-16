package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.WordRepository

class IncreaseAppearanceCount(
    private val repository: WordRepository,
) {
    suspend operator fun invoke(wordId: Int) {
        repository.increaseAppearanceCount(wordId)
    }
}
