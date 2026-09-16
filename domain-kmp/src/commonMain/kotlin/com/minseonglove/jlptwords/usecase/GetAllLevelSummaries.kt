package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.LevelSummary
import com.minseonglove.jlptwords.repository.LevelRepository

class GetAllLevelSummaries(
    private val repository: LevelRepository,
) {
    suspend operator fun invoke(): List<LevelSummary> {
        return repository.getLevelSummaries()
    }
}
