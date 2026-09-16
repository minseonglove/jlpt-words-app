package com.minseonglove.jlptwords.ui.study

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.tts.TTSUtteranceId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class StudyState(
    val sessionId: Int = 0,
    val sessionIndex: Int = 0,
    val level: JLPTLevel = JLPTLevel.N1,
    val words: PersistentList<WordPageCardItem> = persistentListOf(),
    val totalKnownWordIds: PersistentSet<Int> = persistentSetOf(),
    val currentKnownWordIds: PersistentSet<Int> = persistentSetOf(),
    val currentPage: Int = 0,
    val isAnswerRevealed: Boolean = false,
    val isExampleRevealed: Boolean = false,
    val totalElapsedTimeSeconds: Int = 0,
    val currentElapsedTimeSeconds: Int = 0,
    val totalWordSize: Int = 0,
    val totalAppearanceCount: Int = 0,
    val totalAccuracy: Int = 0,
    val isRoundCompletionVisible: Boolean = false,
    val isSessionCompletionVisible: Boolean = false,
    val isExitSessionBottomSheetVisible: Boolean = false,
    val isHelpOverlayVisible: Boolean = false,
    val isAdZeroDialogShown: Boolean = false,
    val isSwipeEnabled: Boolean = true,
    val isNextRoundLoading: Boolean = false,
    val isTTSWarningDialogVisible: Boolean = false,
    val playingUtteranceId: String? = null,
    val adZeroRemainingHours: Int = 0,
    val roundProgresses: PersistentList<Int> = persistentListOf(),
    val sessionRecords: PersistentList<StudyRecord> = persistentListOf(),
) {
    val isStudySectionVisible: Boolean = isRoundCompletionVisible.not() && isSessionCompletionVisible.not()

    // 뜻을 공개하면 예문도 함께 보이지만, 예문만 따로 열어둔 경우엔 뜻을 다시 가려도 예문은 남는다.
    val isExampleVisible: Boolean = isAnswerRevealed || isExampleRevealed
    val totalProgress: Float = totalKnownWordIds.size / totalWordSize.toFloat().coerceAtLeast(1f)
    val leftWordsSize: Int = totalWordSize - totalKnownWordIds.size

    val isWordTTSPlaying: Boolean = playingUtteranceId == TTSUtteranceId.WORD
    val isExampleTTSPlaying: Boolean = playingUtteranceId == TTSUtteranceId.EXAMPLE
}
