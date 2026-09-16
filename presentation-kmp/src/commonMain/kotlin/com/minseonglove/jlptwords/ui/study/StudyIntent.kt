package com.minseonglove.jlptwords.ui.study

sealed interface StudyIntent {
    data object Initialize : StudyIntent

    data object ToggleAnswerReveal : StudyIntent

    data object ShowExample : StudyIntent

    data class MoveNextWord(
        val isKnownWord: Boolean,
    ) : StudyIntent

    data object NavigateToBack : StudyIntent

    data class NavigateToWordDetail(
        val kanji: String,
        val pronunciation: String,
    ) : StudyIntent

    data object ShowHelpOverlay : StudyIntent

    data object HideHelpOverlay : StudyIntent

    data object IncreaseElapsedTime : StudyIntent

    data class ShuffleLeftWordsAndRestart(
        val isAdShown: Boolean,
    ) : StudyIntent

    data object SaveStudyStatus : StudyIntent

    data object StartTimerIfStudying : StudyIntent

    data object HandleBackPress : StudyIntent

    data class HideExitSessionBottomSheet(
        val isExit: Boolean,
    ) : StudyIntent

    data object ContinueNextRound : StudyIntent

    data object ShowInterstitialAd : StudyIntent

    data class CopyToClipBoard(
        val kanji: String,
    ) : StudyIntent

    data object ShowAdZeroDialog : StudyIntent

    data object DismissAdZeroDialog : StudyIntent

    data object OpenInstallLanguagePack : StudyIntent

    data class PlayTTS(
        val text: String,
        val utteranceId: String,
    ) : StudyIntent

    data object DismissTTSWarningDialog : StudyIntent

    data class ChangeTTSPlayingState(
        val utteranceId: String,
        val isPlaying: Boolean,
    ) : StudyIntent

    data object ClickSettings : StudyIntent
}
