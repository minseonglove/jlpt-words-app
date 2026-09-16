package com.minseonglove.jlptwords.ui.selection.session

sealed interface SessionSelectionIntent {
    data object Initialize : SessionSelectionIntent

    data object Retry : SessionSelectionIntent

    data class SelectSession(
        val sessionId: Int,
    ) : SessionSelectionIntent

    data class NavigationToStudyScreen(
        val sessionId: Int,
    ) : SessionSelectionIntent

    data object ShowAdZeroDialog : SessionSelectionIntent

    data object DismissAdZeroDialog : SessionSelectionIntent

    data object DismissSessionWarningDialog : SessionSelectionIntent

    data object NavigateBack : SessionSelectionIntent

    data object ClickSettings : SessionSelectionIntent

    data class ToggleChapter(
        val chapterNumber: Int,
    ) : SessionSelectionIntent

    data object CollapseAnimationCompleted : SessionSelectionIntent
}
