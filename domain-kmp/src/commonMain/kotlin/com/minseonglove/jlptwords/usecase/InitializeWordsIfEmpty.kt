package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.WordsInitializeProgress
import com.minseonglove.jlptwords.repository.WordInitializeRepository
import kotlinx.coroutines.flow.Flow

class InitializeWordsIfEmpty(
    private val repository: WordInitializeRepository,
) {
    suspend operator fun invoke(
        level: JLPTLevel,
    ): Flow<WordsInitializeProgress> {
        return repository.initializeWordsIfEmpty(level)
    }
}
