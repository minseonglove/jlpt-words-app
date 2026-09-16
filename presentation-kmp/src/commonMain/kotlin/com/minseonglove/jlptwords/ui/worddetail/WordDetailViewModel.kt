package com.minseonglove.jlptwords.ui.worddetail

import com.minseonglove.jlptwords.tts.TTSCallback
import com.minseonglove.jlptwords.tts.TTSManager
import com.minseonglove.jlptwords.tts.TTSUtteranceId
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.GetWordDetail
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Syntax

class WordDetailViewModel(
    private val getWordDetail: GetWordDetail,
    private val ttsManager: TTSManager,
) : BaseViewModel<WordDetailState, WordDetailSideEffect>() {
    override val container: Container<WordDetailState, WordDetailSideEffect> =
        container(
            initialState = WordDetailState(),
        )

    private var isInitialized = false

    private val ttsCallback =
        object : TTSCallback {
            override fun onInit(status: Int) = Unit

            override fun onStart(utteranceId: String) {
                onChangeTTSPlayingState(utteranceId, isPlaying = true)
            }

            override fun onDone(utteranceId: String) {
                onChangeTTSPlayingState(utteranceId, isPlaying = false)
            }

            override fun onError(
                utteranceId: String,
                errorCode: Int,
            ) {
                onChangeTTSPlayingState(utteranceId, isPlaying = false)
            }
        }

    fun onCreate(
        kanji: String,
        pronunciation: String,
    ) {
        WordDetailIntent.Initialize(kanji, pronunciation).post()
    }

    fun onTTSClick() {
        WordDetailIntent.PlayTTS.post()
    }

    fun onExampleTTSClick(
        index: Int,
        text: String,
    ) {
        WordDetailIntent.PlayExampleTTS(index, text).post()
    }

    fun onStopTTS() {
        WordDetailIntent.StopTTS.post()
    }

    fun onExampleWordClick(
        kanji: String,
        pronunciation: String,
    ) {
        WordDetailIntent.ClickExampleWord(kanji, pronunciation).post()
    }

    fun onBackClick() {
        WordDetailIntent.ClickBack.post()
    }

    fun onDismissTTSWarningDialog() {
        WordDetailIntent.DismissTTSWarningDialog.post()
    }

    fun onOpenInstallLanguagePack() {
        WordDetailIntent.OpenInstallLanguagePack.post()
    }

    private fun onChangeTTSPlayingState(
        utteranceId: String,
        isPlaying: Boolean,
    ) {
        WordDetailIntent.ChangeTTSPlayingState(utteranceId, isPlaying).post()
    }

    private fun WordDetailIntent.post() {
        intent {
            when (this@post) {
                is WordDetailIntent.Initialize ->
                    initialize(kanji = kanji, pronunciation = pronunciation)

                WordDetailIntent.PlayTTS ->
                    playTTS(text = state.pronunciation, utteranceId = TTSUtteranceId.WORD)

                is WordDetailIntent.PlayExampleTTS ->
                    playTTS(text = text, utteranceId = TTSUtteranceId.example(index))

                WordDetailIntent.StopTTS -> {
                    ttsManager.stop()
                    reduce {
                        state.copy(playingUtteranceId = null)
                    }
                }

                is WordDetailIntent.ChangeTTSPlayingState -> {
                    reduce {
                        when {
                            isPlaying -> state.copy(playingUtteranceId = utteranceId)
                            // 다음 발화가 이미 시작된 뒤 도착한 종료 콜백은 흘려보낸다.
                            state.playingUtteranceId == utteranceId -> state.copy(playingUtteranceId = null)
                            else -> state
                        }
                    }
                }

                is WordDetailIntent.ClickExampleWord -> {
                    if (kanji == state.kanji && pronunciation == state.pronunciation) {
                        postSideEffect(WordDetailSideEffect.ScrollToTop)
                    } else {
                        postSideEffect(WordDetailSideEffect.NavigateToWordDetail(kanji, pronunciation))
                    }
                }

                WordDetailIntent.ClickBack -> {
                    postSideEffect(WordDetailSideEffect.NavigateToBack)
                }

                WordDetailIntent.DismissTTSWarningDialog -> {
                    reduce {
                        state.copy(isTTSWarningDialogVisible = false)
                    }
                }

                WordDetailIntent.OpenInstallLanguagePack -> {
                    postSideEffect(WordDetailSideEffect.OpenInstallLanguagePack)
                    reduce {
                        state.copy(isTTSWarningDialogVisible = false)
                    }
                }
            }
        }
    }

    private suspend fun Syntax<WordDetailState, WordDetailSideEffect>.initialize(
        kanji: String,
        pronunciation: String,
    ) {
        // 표제어는 이 화면 인스턴스에서 고정이지만, 백스택에서 돌아오면 화면이 다시
        // 컴포지션에 들어와 Initialize 가 한 번 더 온다. TTS 엔진이 중복 생성되고
        // 상세를 다시 조회하게 되므로 최초 1회만 수행한다.
        if (isInitialized) return
        isInitialized = true

        ttsManager.init(ttsCallback)
        val detail = getWordDetail(kanji, pronunciation)
        if (detail != null) {
            reduce {
                state.copy(
                    isLoading = false,
                    kanji = detail.word.kanji,
                    pronunciation = detail.word.pronunciation,
                    partOfSpeech = detail.word.partOfSpeech,
                    meaning = detail.word.meaning,
                    jlptLevel = detail.jlptLevel,
                    kanjiInfos = detail.kanjiInfos.toPersistentList(),
                    examples = detail.word.examples.toPersistentList(),
                )
            }
        } else {
            reduce {
                state.copy(isLoading = false)
            }
        }
    }

    private suspend fun Syntax<WordDetailState, WordDetailSideEffect>.playTTS(
        text: String,
        utteranceId: String,
    ) {
        if (ttsManager.isAvailable().not()) {
            reduce {
                state.copy(isTTSWarningDialogVisible = true)
            }
            return
        }
        ttsManager.speak(text = text, utteranceId = utteranceId)
        // 볼륨 확인은 발화를 시작시킨 뒤에 한다. iOS 는 오디오 세션이 활성화된 뒤라야
        // outputVolume 이 현재 값을 돌려준다.
        if (ttsManager.isMuted()) {
            postSideEffect(WordDetailSideEffect.ShowVolumeMutedMessage)
        }
    }

    override fun onCleared() {
        ttsManager.release()
        super.onCleared()
    }
}
