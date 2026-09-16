package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyEntry
import com.minseonglove.jlptwords.repository.LevelRepository

class ResolveStudyEntry(
    private val levelRepository: LevelRepository,
) {
    suspend operator fun invoke(): StudyEntry {
        val level = levelRepository.getLastSelectedLevel()
        return if (level == null) {
            StudyEntry.NeedsLevelSelection(JLPTLevel.N3)
        } else {
            StudyEntry.Sessions(level)
        }
    }
}
