package com.minseonglove.jlptwords.ui.worddetail

sealed interface WordDetailSideEffect {
    /** 예문 속 JLPT 기출 단어(파란색)를 눌렀을 때, 해당 단어의 상세로 이동. */
    data class NavigateToWordDetail(
        val kanji: String,
        val pronunciation: String,
    ) : WordDetailSideEffect

    /** 예문 속 단어가 현재 보고 있는 단어와 같을 때, 이동 대신 스크롤을 최상단으로 올림. */
    data object ScrollToTop : WordDetailSideEffect

    data object NavigateToBack : WordDetailSideEffect

    /** TTS 언어팩 미설치 시 설치 화면으로 이동. */
    data object OpenInstallLanguagePack : WordDetailSideEffect

    /** 기기 볼륨이 0이라 발음이 들리지 않는 상태 안내. */
    data object ShowVolumeMutedMessage : WordDetailSideEffect
}
