package com.minseonglove.jlptwords.ui.search

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.WordScriptType
import com.minseonglove.jlptwords.entity.WordSortType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentSet

/** 검색 결과 목록 위에 인라인으로 띄우는 패널 종류. 패널이 열려 있는 동안 목록은 흐리게 표시된다. */
enum class SearchPanel {
    NONE,
    FILTER,
    SORT,
}

/** 급수 필터 '전체선택' 상태. State 기본값과 전체선택/초기화 리듀서가 공유한다. */
internal val ALL_JLPT_LEVELS: PersistentSet<JLPTLevel> = JLPTLevel.entries.toPersistentSet()

/** 표기 필터 '전체선택' 상태. State 기본값과 전체선택/초기화 리듀서가 공유한다. */
internal val ALL_WORD_SCRIPT_TYPES: PersistentSet<WordScriptType> = WordScriptType.entries.toPersistentSet()

data class SearchState(
    val isLoading: Boolean = true,
    val query: String = "",
    val words: ImmutableList<SearchedWord> = persistentListOf(),
    /** 적용된 급수 필터. 기본 전체 선택. */
    val selectedLevels: PersistentSet<JLPTLevel> = ALL_JLPT_LEVELS,
    /** 적용된 표기 필터. 기본 전체 선택. */
    val selectedScriptTypes: PersistentSet<WordScriptType> = ALL_WORD_SCRIPT_TYPES,
    val sortType: WordSortType = WordSortType.WORD_NUMBER,
    val isAscending: Boolean = true,
    val activePanel: SearchPanel = SearchPanel.NONE,
    /** 필터 패널에서 편집 중인 값. '필터적용' 시점에 selected* 로 반영된다. */
    val draftLevels: PersistentSet<JLPTLevel> = ALL_JLPT_LEVELS,
    val draftScriptTypes: PersistentSet<WordScriptType> = ALL_WORD_SCRIPT_TYPES,
    /** 정렬 패널에서 편집 중인 값. '정렬적용' 시점에 sortType 으로 반영된다. */
    val draftSortType: WordSortType = WordSortType.WORD_NUMBER,
    val adZeroRemainingHours: Int = 0,
    val isAdZeroDialogShown: Boolean = false,
)
