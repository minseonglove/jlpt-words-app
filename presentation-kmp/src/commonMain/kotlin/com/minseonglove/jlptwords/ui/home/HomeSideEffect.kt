package com.minseonglove.jlptwords.ui.home

import com.minseonglove.jlptwords.entity.JLPTLevel

sealed interface HomeSideEffect {
    data class NavigateToLevelSelection(
        val level: JLPTLevel,
    ) : HomeSideEffect

    data class NavigateToWordDetail(
        val kanji: String,
        val pronunciation: String,
    ) : HomeSideEffect

    data class NavigateToStudy(
        val sessionId: Int,
        val sessionIndex: Int,
    ) : HomeSideEffect

    data object NavigateToSetting : HomeSideEffect

    data object FinishApp : HomeSideEffect
}
