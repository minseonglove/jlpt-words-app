package com.minseonglove.jlptwords.ui.search

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.WordScriptType
import com.minseonglove.jlptwords.entity.WordSortType

sealed interface SearchIntent {
    data object Initialize : SearchIntent

    data class ClickWord(
        val kanji: String,
        val pronunciation: String,
    ) : SearchIntent

    data object ClickBack : SearchIntent

    data object OpenFilterPanel : SearchIntent

    data object OpenSortPanel : SearchIntent

    data object ToggleSortDirection : SearchIntent

    data class ToggleDraftLevel(
        val level: JLPTLevel,
    ) : SearchIntent

    data class ToggleDraftScriptType(
        val scriptType: WordScriptType,
    ) : SearchIntent

    data object SelectAllDraftLevels : SearchIntent

    data object SelectAllDraftScriptTypes : SearchIntent

    data object ResetDraftFilter : SearchIntent

    data object ApplyFilter : SearchIntent

    data class SelectDraftSortType(
        val sortType: WordSortType,
    ) : SearchIntent

    data object ApplySort : SearchIntent

    data object ShowAdZeroDialog : SearchIntent

    data object DismissAdZeroDialog : SearchIntent

    data object ClickSettings : SearchIntent
}
