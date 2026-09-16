package com.minseonglove.jlptwords.ui.main

import com.minseonglove.jlptwords.entity.JLPTLevel

sealed interface MainSideEffect {
    data class NavigateToLevelSelection(
        val level: JLPTLevel,
    ) : MainSideEffect
}
