package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.WordRepository

class IncreaseCorrectAndAppearanceCount(
    private val repository: WordRepository,
) {
    suspend operator fun invoke(wordId: Int) {
        repository.increaseCorrectAndAppearanceCount(wordId)
    }
}
