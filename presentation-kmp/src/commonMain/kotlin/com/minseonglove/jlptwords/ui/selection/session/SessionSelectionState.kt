package com.minseonglove.jlptwords.ui.selection.session

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class SessionSelectionState(
    val level: JLPTLevel? = null,
    val chapters: ImmutableList<SessionChapter> = persistentListOf(),
    val expandedChapters: PersistentSet<Int> = persistentSetOf(),
    val pendingExpandChapter: Int? = null,
    val adZeroRemainingHours: Int = 0,
    val lastStudyStatus: StudyStatus? = null,
    val sessionWarning: SessionWarning? = null,
    val isLoading: Boolean = true,
    val isAdZeroDialogShown: Boolean = false,
    val isRetryScreenVisible: Boolean = false,
)

/** 진행 중인 세션을 두고 다른 세션을 선택했을 때 띄우는 경고 다이얼로그의 표시 값. */
data class SessionWarning(
    val selectedSessionId: Int,
    val level: JLPTLevel,
    val chapterNumber: Int,
    val sessionNumber: Int,
    val progressPercent: Int,
)
