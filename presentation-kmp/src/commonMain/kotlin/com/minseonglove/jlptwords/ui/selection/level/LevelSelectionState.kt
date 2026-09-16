package com.minseonglove.jlptwords.ui.selection.level

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.LevelSummary
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class LevelSelectionState(
    val lastSelectedLevel: JLPTLevel = JLPTLevel.N3,
    val selectedLevel: JLPTLevel = JLPTLevel.N3,
    val levelSummaries: PersistentList<LevelSummary> = persistentListOf(),
    val isLoading: Boolean = true,
)
