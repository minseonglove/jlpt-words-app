package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.LevelRepository
import kotlinx.coroutines.flow.Flow

class ObserveLastSelectedLevel(
    private val levelRepository: LevelRepository,
) {
    operator fun invoke(): Flow<JLPTLevel?> = levelRepository.observeLastSelectedLevel()
}
