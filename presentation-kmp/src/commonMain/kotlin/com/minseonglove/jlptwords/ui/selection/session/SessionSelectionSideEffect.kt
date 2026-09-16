package com.minseonglove.jlptwords.ui.selection.session

sealed interface SessionSelectionSideEffect {
    data class NavigationToStudyScreen(
        val sessionId: Int,
        val sessionIndex: Int,
    ) : SessionSelectionSideEffect

    data object NavigateToBack : SessionSelectionSideEffect

    data object NavigateToSetting : SessionSelectionSideEffect
}
