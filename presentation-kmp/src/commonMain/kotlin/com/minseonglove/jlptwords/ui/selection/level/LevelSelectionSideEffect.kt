package com.minseonglove.jlptwords.ui.selection.level

import com.minseonglove.jlptwords.entity.JLPTLevel

sealed interface LevelSelectionSideEffect {
    data class NavigateToSessionSelectionScreen(
        val level: JLPTLevel,
    ) : LevelSelectionSideEffect

    data object NavigateToBack : LevelSelectionSideEffect
}
