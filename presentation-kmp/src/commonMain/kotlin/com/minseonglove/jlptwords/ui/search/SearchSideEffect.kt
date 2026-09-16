package com.minseonglove.jlptwords.ui.search

sealed interface SearchSideEffect {
    data class NavigateToWordDetail(
        val kanji: String,
        val pronunciation: String,
    ) : SearchSideEffect

    data object NavigateToBack : SearchSideEffect

    data object NavigateToSetting : SearchSideEffect
}
