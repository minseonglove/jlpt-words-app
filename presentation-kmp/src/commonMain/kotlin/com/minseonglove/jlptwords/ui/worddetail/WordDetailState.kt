package com.minseonglove.jlptwords.ui.worddetail

import com.minseonglove.jlptwords.entity.Example
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.KanjiInfo
import com.minseonglove.jlptwords.tts.TTSUtteranceId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WordDetailState(
    val isLoading: Boolean = true,
    val kanji: String = "",
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val meaning: String = "",
    val jlptLevel: JLPTLevel = JLPTLevel.N5,
    val kanjiInfos: ImmutableList<KanjiInfo> = persistentListOf(),
    val examples: ImmutableList<Example> = persistentListOf(),
    val playingUtteranceId: String? = null,
    val isTTSWarningDialogVisible: Boolean = false,
) {
    val isWordTTSPlaying: Boolean = playingUtteranceId == TTSUtteranceId.WORD

    fun isExampleTTSPlaying(index: Int): Boolean = playingUtteranceId == TTSUtteranceId.example(index)
}
