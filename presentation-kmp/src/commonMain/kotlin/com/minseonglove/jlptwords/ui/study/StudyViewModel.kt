package com.minseonglove.jlptwords.ui.study

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.minseonglove.jlptwords.ad.InterstitialAdManager
import com.minseonglove.jlptwords.ad.PlatformFullScreenContentCallback
import com.minseonglove.jlptwords.entity.StudyStatus
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.navigation.StudyRoute
import com.minseonglove.jlptwords.navigation.dto.StudyDTO
import com.minseonglove.jlptwords.navigation.type.StudyNavType
import com.minseonglove.jlptwords.tts.TTSCallback
import com.minseonglove.jlptwords.tts.TTSManager
import com.minseonglove.jlptwords.tts.TTSUtteranceId
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.AddStreak
import com.minseonglove.jlptwords.usecase.AddStudyRecord
import com.minseonglove.jlptwords.usecase.CopyToClipboard
import com.minseonglove.jlptwords.usecase.GetAdZeroRemainingHours
import com.minseonglove.jlptwords.usecase.GetLastInterstitialAdShowTime
import com.minseonglove.jlptwords.usecase.GetLevelByStudySessionId
import com.minseonglove.jlptwords.usecase.GetStudyRecordsBySessionId
import com.minseonglove.jlptwords.usecase.GetStudyStatus
import com.minseonglove.jlptwords.usecase.GetWordsByStudySessionId
import com.minseonglove.jlptwords.usecase.HasSeenStudyHelp
import com.minseonglove.jlptwords.usecase.IncreaseAppearanceCount
import com.minseonglove.jlptwords.usecase.IncreaseCorrectAndAppearanceCount
import com.minseonglove.jlptwords.usecase.IsAdZeroEnabled
import com.minseonglove.jlptwords.usecase.LogStudySessionCompleted
import com.minseonglove.jlptwords.usecase.SetLastInterstitialAdShowTime
import com.minseonglove.jlptwords.usecase.SetStudyHelpSeen
import com.minseonglove.jlptwords.usecase.SetStudyStatus
import com.minseonglove.jlptwords.util.TimeProvider
import com.minseonglove.jlptwords.util.isSystemClipboardFeedbackShown
import com.minseonglove.jlptwords.util.runCatchingCancellable
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Syntax
import kotlin.reflect.typeOf

class StudyViewModel(
    savedStateHandle: SavedStateHandle,
    private val getWordsByStudySessionId: GetWordsByStudySessionId,
    private val increaseAppearanceCount: IncreaseAppearanceCount,
    private val increaseCorrectAndAppearanceCount: IncreaseCorrectAndAppearanceCount,
    private val addStreak: AddStreak,
    private val setStudyStatus: SetStudyStatus,
    private val getStudyStatus: GetStudyStatus,
    private val addStudyRecord: AddStudyRecord,
    private val getStudyRecordsBySessionId: GetStudyRecordsBySessionId,
    private val logStudySessionCompleted: LogStudySessionCompleted,
    private val interstitialAdManager: InterstitialAdManager,
    private val getLastInterstitialAdShowTime: GetLastInterstitialAdShowTime,
    private val setLastInterstitialAdShowTime: SetLastInterstitialAdShowTime,
    private val copyToClipboard: CopyToClipboard,
    private val getLevelByStudySessionId: GetLevelByStudySessionId,
    private val isAdZeroEnabled: IsAdZeroEnabled,
    private val getAdZeroRemainingHours: GetAdZeroRemainingHours,
    private val hasSeenStudyHelp: HasSeenStudyHelp,
    private val setStudyHelpSeen: SetStudyHelpSeen,
    private val ttsManager: TTSManager,
) : BaseViewModel<StudyState, StudySideEffect>() {
    private var timerJob: Job? = null
    private var isNextRoundRequested = false
    private val fullScreenContentCallback =
        object : PlatformFullScreenContentCallback {
            override fun onAdShowedFullScreenContent() {
                onInterstitialAdShowSuccess()
            }

            override fun onAdFailedToShowFullScreenContent() {
                onInterstitialAdShowFailed()
            }

            override fun onAdDismissedFullScreenContent() {
                interstitialAdManager.clearAd()
            }
        }

    private val ttsCallback =
        object : TTSCallback {
            override fun onInit(status: Int) {
                // Do Nothing
            }

            override fun onStart(utteranceId: String) {
                changeTTSPlayingState(utteranceId, isPlaying = true)
            }

            override fun onDone(utteranceId: String) {
                changeTTSPlayingState(utteranceId, isPlaying = false)
            }

            override fun onError(
                utteranceId: String,
                errorCode: Int,
            ) {
                changeTTSPlayingState(utteranceId, isPlaying = false)
            }
        }

    override val container: Container<StudyState, StudySideEffect> =
        container(
            initialState =
                run {
                    val studyDTO =
                        savedStateHandle
                            .toRoute<StudyRoute>(
                                typeMap = mapOf(typeOf<StudyDTO>() to StudyNavType()),
                            ).studyDTO
                    StudyState(
                        sessionId = studyDTO.sessionId,
                        sessionIndex = studyDTO.sessionIndex,
                    )
                },
        )

    fun onCreate() {
        StudyIntent.Initialize.post()
    }

    fun onStart() {
        StudyIntent.StartTimerIfStudying.post()
    }

    fun onStop() {
        StudyIntent.SaveStudyStatus.post()
    }

    private fun startTimer() {
        // 이미 실행 중인 타이머가 있으면 시작하지 않음
        if (timerJob?.isActive == true) {
            return
        }

        timerJob =
            viewModelScope.launch {
                while (true) {
                    delay(1000) // 1초 대기
                    StudyIntent.IncreaseElapsedTime.post()
                }
            }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onAnswerRevealClick() {
        StudyIntent.ToggleAnswerReveal.post()
    }

    fun onExampleShowClick() {
        StudyIntent.ShowExample.post()
    }

    fun onNavigationButtonClick() {
        StudyIntent.HandleBackPress.post()
    }

    fun onContinueNextRoundButtonClick() {
        // Orbit 인텐트는 각각 별도 코루틴이라 상태 검사만으로는 연타를 놓친다. 클릭은 메인 스레드로
        // 들어오므로 여기서 막아야 다음 라운드가 두 번 시작되지 않는다.
        if (isNextRoundRequested) return
        isNextRoundRequested = true
        StudyIntent.ContinueNextRound.post()
    }

    fun onCopyClick(
        kanji: String,
    ) {
        StudyIntent
            .CopyToClipBoard(
                kanji = kanji,
            ).post()
    }

    fun onWordTTSClick(
        pronunciation: String,
    ) {
        StudyIntent
            .PlayTTS(
                text = pronunciation,
                utteranceId = TTSUtteranceId.WORD,
            ).post()
    }

    fun onExampleTTSClick(
        text: String,
    ) {
        StudyIntent
            .PlayTTS(
                text = text,
                utteranceId = TTSUtteranceId.EXAMPLE,
            ).post()
    }

    fun onFinishSessionButtonClick() {
        StudyIntent.NavigateToBack.post()
    }

    fun onDetailClick(
        kanji: String,
        pronunciation: String,
    ) {
        StudyIntent.NavigateToWordDetail(kanji, pronunciation).post()
    }

    fun onHelpClick() {
        StudyIntent.ShowHelpOverlay.post()
    }

    fun onHelpCloseClick() {
        StudyIntent.HideHelpOverlay.post()
    }

    fun onExitSessionButtonClick() {
        StudyIntent
            .HideExitSessionBottomSheet(
                isExit = true,
            ).post()
    }

    fun onContinueSessionButtonClick() {
        StudyIntent
            .HideExitSessionBottomSheet(
                isExit = false,
            ).post()
    }

    fun onExitSessionBottomSheetDismiss() {
        StudyIntent
            .HideExitSessionBottomSheet(
                isExit = false,
            ).post()
    }

    fun onTTSWarningDialogConfirmClick() {
        StudyIntent.OpenInstallLanguagePack.post()
    }

    fun onTTSWarningDialogDismiss() {
        StudyIntent.DismissTTSWarningDialog.post()
    }

    fun onBack() {
        StudyIntent.HandleBackPress.post()
    }

    private fun StudyIntent.post() {
        intent {
            when (this@post) {
                StudyIntent.Initialize -> initialize()

                StudyIntent.ToggleAnswerReveal -> {
                    reduce {
                        state.copy(
                            isAnswerRevealed = state.isAnswerRevealed.not(),
                        )
                    }
                }

                StudyIntent.ShowExample -> {
                    reduce {
                        state.copy(
                            isExampleRevealed = true,
                        )
                    }
                }

                is StudyIntent.MoveNextWord -> moveNextWord(isKnownWord = isKnownWord)

                StudyIntent.IncreaseElapsedTime -> {
                    reduce {
                        state.copy(
                            totalElapsedTimeSeconds = state.totalElapsedTimeSeconds + 1,
                            currentElapsedTimeSeconds = state.currentElapsedTimeSeconds + 1,
                        )
                    }
                }

                StudyIntent.NavigateToBack -> {
                    postSideEffect(
                        StudySideEffect.NavigateToBack,
                    )
                }

                is StudyIntent.NavigateToWordDetail -> {
                    postSideEffect(
                        StudySideEffect.NavigateToWordDetail(kanji, pronunciation),
                    )
                }

                StudyIntent.ShowHelpOverlay -> {
                    reduce {
                        state.copy(isHelpOverlayVisible = true)
                    }
                }

                StudyIntent.HideHelpOverlay -> {
                    hideHelpOverlay()
                }

                is StudyIntent.ShuffleLeftWordsAndRestart ->
                    shuffleLeftWordsAndRestart(isAdShown = isAdShown)

                StudyIntent.SaveStudyStatus -> saveStudyStatus()

                StudyIntent.StartTimerIfStudying -> {
                    interstitialAdManager.startRefreshAd()
                    if (state.isStudySectionVisible) {
                        startTimer()
                    }
                }

                is StudyIntent.HideExitSessionBottomSheet -> {
                    reduce {
                        state.copy(
                            isExitSessionBottomSheetVisible = false,
                        )
                    }

                    if (isExit) {
                        postSideEffect(StudySideEffect.NavigateToBack)
                    }
                }

                StudyIntent.HandleBackPress -> handleBackPress()

                StudyIntent.ContinueNextRound -> {
                    if (shouldShowInterstitialAd()) {
                        StudyIntent.ShowInterstitialAd.post()
                    } else {
                        StudyIntent.ShuffleLeftWordsAndRestart(false).post()
                    }
                }

                StudyIntent.ShowInterstitialAd -> {
                    reduce {
                        state.copy(
                            isNextRoundLoading = true,
                        )
                    }
                    val ad = interstitialAdManager.getValidAd()
                    ad?.let {
                        it.setFullScreenContentCallback(this@StudyViewModel.fullScreenContentCallback)
                        postSideEffect(
                            StudySideEffect.ShowInterstitialAd(
                                interstitialAd = it,
                            ),
                        )
                    } ?: run {
                        // 광고가 유효하지 않으면 바로 다음 라운드 시작
                        onInterstitialAdShowFailed()
                    }
                }

                is StudyIntent.CopyToClipBoard -> {
                    runCatchingCancellable {
                        copyToClipboard.invoke(
                            kanji = kanji,
                        )
                    }.onSuccess {
                        if (isSystemClipboardFeedbackShown.not()) {
                            postSideEffect(StudySideEffect.ShowCopySuccessMessage)
                        }
                    }.onFailure {
                        postSideEffect(StudySideEffect.ShowCopyFailureMessage)
                    }
                }

                StudyIntent.ShowAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = true,
                        )
                    }
                }

                StudyIntent.DismissAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = false,
                        )
                    }
                    // 리워드 광고 시청 후 광고 제거 시간이 갱신될 수 있어 다이얼로그를 닫을 때 다시 조회한다.
                    val adZeroRemainingHours = getAdZeroRemainingHours()
                    reduce {
                        state.copy(
                            adZeroRemainingHours = adZeroRemainingHours,
                        )
                    }
                }

                is StudyIntent.PlayTTS -> {
                    if (ttsManager.isAvailable()) {
                        ttsManager.speak(text = text, utteranceId = utteranceId)
                        // 볼륨 확인은 발화를 시작시킨 뒤에 한다. iOS 는 오디오 세션이 활성화된 뒤라야
                        // outputVolume 이 현재 값을 돌려준다.
                        if (ttsManager.isMuted()) {
                            postSideEffect(StudySideEffect.ShowVolumeMutedMessage)
                        }
                    } else {
                        reduce {
                            state.copy(
                                isTTSWarningDialogVisible = true,
                            )
                        }
                    }
                }

                StudyIntent.DismissTTSWarningDialog -> {
                    reduce {
                        state.copy(
                            isTTSWarningDialogVisible = false,
                        )
                    }
                }

                StudyIntent.OpenInstallLanguagePack -> {
                    postSideEffect(StudySideEffect.OpenInstallLanguagePack)
                    reduce {
                        state.copy(
                            isTTSWarningDialogVisible = false,
                        )
                    }
                }

                is StudyIntent.ChangeTTSPlayingState -> {
                    reduce {
                        when {
                            isPlaying -> state.copy(playingUtteranceId = utteranceId)
                            // 다음 발화가 이미 시작된 뒤 도착한 종료 콜백은 흘려보낸다.
                            state.playingUtteranceId == utteranceId -> state.copy(playingUtteranceId = null)
                            else -> state
                        }
                    }
                }

                StudyIntent.ClickSettings -> {
                    postSideEffect(StudySideEffect.NavigateToSetting)
                }
            }
        }
    }

    private suspend fun Syntax<StudyState, StudySideEffect>.initialize() {
        // 광고 제거 남은 시간(시간 단위) 로드. 만료/미적용이면 0 -> 상단바 "AD" 표시
        val adZeroRemainingHours = getAdZeroRemainingHours()
        reduce {
            state.copy(adZeroRemainingHours = adZeroRemainingHours)
        }

        // 구성 변경(회전 등)이면 화면만 다시 만들어졌을 뿐 세션은 이어지는 중이다.
        // 아래 1회성 작업(TTS 엔진 생성·출석 기록·단어 로드)을 다시 타면 엔진이 중복 생성된다.
        if (state.words.isNotEmpty()) return

        ttsManager.init(ttsCallback)
        // 출석일수 추가
        addStreak(state.sessionId)

        // 저장된 진행 상황이 현재 세션의 것이면 복구, 아니면 처음부터 시작
        val savedStatus = getStudyStatus()?.takeIf { it.sessionId == state.sessionId }
        if (savedStatus != null) {
            // 이전 진행 상황 복구
            val wordPageCardItems =
                savedStatus.words.map {
                    it.toWordPageCardItem()
                }
            reduce {
                state.copy(
                    level = savedStatus.level,
                    words = wordPageCardItems.toPersistentList(),
                    totalKnownWordIds = savedStatus.totalKnownWordIds.toPersistentSet(),
                    currentKnownWordIds = savedStatus.currentKnownWordIds.toPersistentSet(),
                    currentPage = savedStatus.currentPage,
                    totalElapsedTimeSeconds = savedStatus.totalElapsedTimeSeconds,
                    currentElapsedTimeSeconds = savedStatus.currentElapsedTimeSeconds,
                    totalWordSize = savedStatus.totalWordSize,
                    totalAppearanceCount = savedStatus.totalAppearanceCount,
                    roundProgresses = savedStatus.roundProgresses.toPersistentList(),
                )
            }
        } else {
            // 처음부터 시작
            val wordPageCardItems =
                getWordsByStudySessionId(state.sessionId)
                    .map {
                        it.toWordPageCardItem()
                    }.shuffled()
            val level = getLevelByStudySessionId(state.sessionId)
            reduce {
                state.copy(
                    words = wordPageCardItems.toPersistentList(),
                    totalWordSize = wordPageCardItems.size,
                    level = level,
                )
            }
        }

        // 스와이프로 학습한다는 걸 알 방법이 없으므로, 도움말을 본 적 없으면 먼저 펼쳐 둔다.
        if (!hasSeenStudyHelp()) {
            reduce {
                state.copy(isHelpOverlayVisible = true)
            }
        }
    }

    private suspend fun Syntax<StudyState, StudySideEffect>.moveNextWord(isKnownWord: Boolean) {
        reduce {
            state.copy(
                isSwipeEnabled = false,
            )
        }
        // 현재 단어 정답률 갱신
        val currentWord = state.words[state.currentPage]
        val updatedTotalKnownWordIds =
            state.totalKnownWordIds.toMutableSet().apply {
                if (isKnownWord) {
                    add(currentWord.id)
                }
            }
        val updatedCurrentKnownWordIds =
            state.currentKnownWordIds.toMutableSet().apply {
                if (isKnownWord) {
                    add(currentWord.id)
                }
            }

        if (isKnownWord) {
            increaseCorrectAndAppearanceCount(currentWord.id)
        } else {
            increaseAppearanceCount(currentWord.id)
        }

        val nextPage = state.currentPage + 1
        val isRoundFinish = nextPage == state.words.size
        val isSessionFinish = isRoundFinish && state.words.size == updatedCurrentKnownWordIds.size
        when {
            isSessionFinish -> {
                // 세션을 끝냈기 때문에 타이머를 잠시 멈춘다.
                stopTimer()
                val totalAppearanceCount = state.totalAppearanceCount + 1
                val totalAccuracy = (state.totalWordSize * 100) / totalAppearanceCount
                // 세션 기록 저장
                addStudyRecord(
                    sessionId = state.sessionId,
                    completionTimeSeconds = state.totalElapsedTimeSeconds,
                    accuracy = totalAccuracy,
                    createAt = TimeProvider.currentTimeMillis(),
                )
                logStudySessionCompleted(
                    level = state.level,
                    // 화면에 노출되는 회차 표기와 같은 1-base 번호로 남긴다.
                    sessionNumber = state.sessionIndex + 1,
                    accuracy = totalAccuracy,
                    elapsedTimeSeconds = state.totalElapsedTimeSeconds,
                )
                // 회차별 추이 그래프용 기록 조회(방금 저장분 포함, create_at 오름차순)
                val sessionRecords = getStudyRecordsBySessionId(state.sessionId)
                // 학습 진행 상황 제거 (다 했기 때문)
                setStudyStatus(null)
                reduce {
                    state.copy(
                        totalKnownWordIds = updatedTotalKnownWordIds.toPersistentSet(),
                        currentKnownWordIds = updatedCurrentKnownWordIds.toPersistentSet(),
                        totalAppearanceCount = totalAppearanceCount,
                        totalAccuracy = totalAccuracy,
                        sessionRecords = sessionRecords.toPersistentList(),
                        isSessionCompletionVisible = true,
                    )
                }
            }
            isRoundFinish -> {
                // 한 라운드를 끝냈기 때문에 타이머를 잠시 멈춘다.
                stopTimer()
                // 이번 라운드까지의 누적 진행률(전체 단어 중 외운 비율)을 추이에 누적
                val roundProgress =
                    (updatedTotalKnownWordIds.size * 100) / state.totalWordSize.coerceAtLeast(1)
                reduce {
                    state.copy(
                        totalKnownWordIds = updatedTotalKnownWordIds.toPersistentSet(),
                        currentKnownWordIds = updatedCurrentKnownWordIds.toPersistentSet(),
                        totalAppearanceCount = state.totalAppearanceCount + 1,
                        roundProgresses = (state.roundProgresses + roundProgress).toPersistentList(),
                        isRoundCompletionVisible = true,
                    )
                }
            }
            else -> {
                reduce {
                    state.copy(
                        currentPage = nextPage,
                        isAnswerRevealed = false,
                        isExampleRevealed = false,
                        isSwipeEnabled = true,
                        totalKnownWordIds = updatedTotalKnownWordIds.toPersistentSet(),
                        currentKnownWordIds = updatedCurrentKnownWordIds.toPersistentSet(),
                        totalAppearanceCount = state.totalAppearanceCount + 1,
                    )
                }
            }
        }
    }

    private suspend fun Syntax<StudyState, StudySideEffect>.shuffleLeftWordsAndRestart(isAdShown: Boolean) {
        reduce {
            state.copy(
                isNextRoundLoading = true,
            )
        }

        val leftWords =
            state.words
                .filter {
                    state.currentKnownWordIds.contains(it.id).not()
                }.map {
                    // 패스한 단어들은 조회수 추가
                    it.copy(
                        appearanceCount = it.appearanceCount + 1,
                    )
                }.shuffled()

        reduce {
            state.copy(
                words = leftWords.toPersistentList(),
                currentKnownWordIds = persistentSetOf(),
                currentPage = 0,
                currentElapsedTimeSeconds = 0,
                isRoundCompletionVisible = false,
                isAnswerRevealed = false,
                isExampleRevealed = false,
                isSwipeEnabled = true,
                isNextRoundLoading = false,
            )
        }
        isNextRoundRequested = false
        startTimer()

        if (isAdShown) {
            setLastInterstitialAdShowTime(TimeProvider.currentTimeMillis())
            interstitialAdManager.load()
        }
    }

    private suspend fun Syntax<StudyState, StudySideEffect>.saveStudyStatus() {
        interstitialAdManager.stopRefreshAd()
        // 세션이 끝난 상태라면 저장 안함
        if (state.isSessionCompletionVisible) {
            return
        }
        val savedState =
            if (state.isRoundCompletionVisible) {
                // 라운드가 끝난 상태에서 변환할 때는 남은 단어를 섞고 0페이지에서 시작
                val leftWords =
                    state.words
                        .filter {
                            state.currentKnownWordIds.contains(it.id).not()
                        }.shuffled()
                state.copy(
                    words = leftWords.toPersistentList(),
                    currentKnownWordIds = persistentSetOf(),
                    currentPage = 0,
                    currentElapsedTimeSeconds = 0,
                )
            } else {
                state
            }
        setStudyStatus(
            studyStatus = savedState.toStudyStatus(),
        )
        stopTimer()
    }

    private suspend fun Syntax<StudyState, StudySideEffect>.handleBackPress() {
        when {
            // 도움말은 다이얼로그가 아니라 화면 위에 겹치는 오버레이라, back 을 여기서 직접 흡수하지
            // 않으면 튜토리얼이 떠 있는 채로 종료 시트가 겹쳐 뜬다.
            state.isHelpOverlayVisible -> hideHelpOverlay()

            state.isSessionCompletionVisible ->
                postSideEffect(StudySideEffect.NavigateToBack)

            else ->
                reduce {
                    state.copy(
                        isExitSessionBottomSheetVisible = true,
                    )
                }
        }
    }

    /** 도움말을 닫고 본 것으로 기록한다. */
    private suspend fun Syntax<StudyState, StudySideEffect>.hideHelpOverlay() {
        reduce {
            state.copy(isHelpOverlayVisible = false)
        }
        setStudyHelpSeen()
    }

    fun onPassButtonClick() {
        StudyIntent
            .MoveNextWord(
                isKnownWord = false,
            ).post()
    }

    fun onKnowButtonClick() {
        StudyIntent
            .MoveNextWord(
                isKnownWord = true,
            ).post()
    }

    fun onAdZeroItemClick() {
        StudyIntent.ShowAdZeroDialog.post()
    }

    fun onAdZeroDialogDismiss() {
        StudyIntent.DismissAdZeroDialog.post()
    }

    fun onSettingsClick() {
        StudyIntent.ClickSettings.post()
    }

    private fun onInterstitialAdShowSuccess() {
        StudyIntent.ShuffleLeftWordsAndRestart(true).post()
    }

    private fun onInterstitialAdShowFailed() {
        StudyIntent.ShuffleLeftWordsAndRestart(false).post()
    }

    private suspend fun shouldShowInterstitialAd(): Boolean {
        val lastShowTime = getLastInterstitialAdShowTime()
        val currentTime = TimeProvider.currentTimeMillis()
        return (currentTime - lastShowTime) >= SHOW_AD_INTERVAL && isAdZeroEnabled().not()
    }

    private suspend fun isAdZeroEnabled(): Boolean {
        return runCatchingCancellable {
            isAdZeroEnabled.invoke()
        }.getOrDefault(false)
    }

    private fun changeTTSPlayingState(
        utteranceId: String,
        isPlaying: Boolean,
    ) {
        StudyIntent.ChangeTTSPlayingState(utteranceId, isPlaying).post()
    }

    private fun Word.toWordPageCardItem(): WordPageCardItem {
        return WordPageCardItem(
            id = id,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            partOfSpeech = partOfSpeech,
            appearanceCount = appearanceCount,
            correctCount = correctCount,
            // 학습 카드에는 대표 예문 1개만 노출한다(프리뷰). 전체 예문은 단어 상세 화면이
            // DB(getWordsByRange+attachExamples)에서 별도로 로드한다. 이 cap 덕분에 세션 상태
            // (nav Bundle/Proto)도 단어당 예문 1개만 운반되어 복구 페이로드가 작게 유지된다
            // (검증: RANDOM 세션 최악 ≈ 261KB, Binder 한계 1MB 대비 안전).
            examples = examples.take(1).toImmutableList(),
        )
    }

    private fun WordPageCardItem.toWord(): Word {
        return Word(
            id = id,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            partOfSpeech = partOfSpeech,
            appearanceCount = appearanceCount,
            correctCount = correctCount,
            examples = examples,
        )
    }

    private fun StudyState.toStudyStatus(): StudyStatus {
        return StudyStatus(
            sessionId = sessionId,
            sessionPosition = sessionIndex,
            level = level,
            words = words.map { it.toWord() },
            totalKnownWordIds = totalKnownWordIds.toSet(),
            currentKnownWordIds = currentKnownWordIds.toSet(),
            currentPage = currentPage,
            totalElapsedTimeSeconds = totalElapsedTimeSeconds,
            currentElapsedTimeSeconds = currentElapsedTimeSeconds,
            totalWordSize = totalWordSize,
            totalAppearanceCount = totalAppearanceCount,
            roundProgresses = roundProgresses,
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        ttsManager.release()
    }

    companion object {
        private const val SHOW_AD_INTERVAL = 10 * 60 * 1000L
    }
}
