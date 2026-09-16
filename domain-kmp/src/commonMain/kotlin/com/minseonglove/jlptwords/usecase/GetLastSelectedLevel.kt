package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.LevelRepository

class GetLastSelectedLevel(
    private val levelRepository: LevelRepository,
) {
    suspend operator fun invoke(): JLPTLevel? {
        return levelRepository.getLastSelectedLevel()
    }
}
