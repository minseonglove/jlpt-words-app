package com.minseonglove.jlptwords.ui.selection.level

import com.minseonglove.jlptwords.entity.JLPTLevel

sealed interface LevelSelectionIntent {
    data object Initialize : LevelSelectionIntent

    data object NavigateToSessionSelectionScreen : LevelSelectionIntent

    data class ChangeSelectedLevel(
        val level: JLPTLevel,
    ) : LevelSelectionIntent

    data object NavigateToBack : LevelSelectionIntent
}
