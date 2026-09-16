package com.minseonglove.jlptwords.ui.worddetail

sealed interface WordDetailIntent {
    data class Initialize(
        val kanji: String,
        val pronunciation: String,
    ) : WordDetailIntent

    data object PlayTTS : WordDetailIntent

    data class PlayExampleTTS(
        val index: Int,
        val text: String,
    ) : WordDetailIntent

    data object StopTTS : WordDetailIntent

    data class ChangeTTSPlayingState(
        val utteranceId: String,
        val isPlaying: Boolean,
    ) : WordDetailIntent

    data class ClickExampleWord(
        val kanji: String,
        val pronunciation: String,
    ) : WordDetailIntent

    data object ClickBack : WordDetailIntent

    data object DismissTTSWarningDialog : WordDetailIntent

    data object OpenInstallLanguagePack : WordDetailIntent
}
