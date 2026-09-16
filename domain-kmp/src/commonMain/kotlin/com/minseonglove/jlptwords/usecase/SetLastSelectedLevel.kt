package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.LevelRepository

class SetLastSelectedLevel(
    private val levelRepository: LevelRepository,
) {
    suspend operator fun invoke(level: JLPTLevel) {
        levelRepository.setLastSelectedLevel(level)
    }
}
