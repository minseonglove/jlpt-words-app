package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.LevelSummary
import kotlinx.coroutines.flow.Flow

interface LevelRepository {
    suspend fun getLevelSummaries(): List<LevelSummary>

    suspend fun getLastSelectedLevel(): JLPTLevel?

    fun observeLastSelectedLevel(): Flow<JLPTLevel?>

    suspend fun setLastSelectedLevel(
        level: JLPTLevel,
    )
}
