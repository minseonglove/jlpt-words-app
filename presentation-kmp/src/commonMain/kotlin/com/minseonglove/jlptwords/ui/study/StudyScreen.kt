package com.minseonglove.jlptwords.ui.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.minseonglove.jlptwords.extension.formatElapsedTime
import com.minseonglove.jlptwords.ui.adzero.AdZeroDialog
import com.minseonglove.jlptwords.ui.base.MolluDefaultTopBar
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.molluButtonBackground
import com.minseonglove.jlptwords.ui.study.component.StudyHelpOverlay
import com.minseonglove.jlptwords.ui.study.component.StudyProgressBar
import com.minseonglove.jlptwords.ui.study.component.SwipeableWordCard
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.ui.tts.TTSWarningDialog
import com.minseonglove.jlptwords.util.handleInstallLanguagePack
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.copy_to_clip_board_fail
import jlptwords.presentation_kmp.generated.resources.copy_to_clip_board_success
import jlptwords.presentation_kmp.generated.resources.study_chapter_title
import jlptwords.presentation_kmp.generated.resources.study_hint_meaning
import jlptwords.presentation_kmp.generated.resources.study_meaning_hide
import jlptwords.presentation_kmp.generated.resources.study_status_completed
import jlptwords.presentation_kmp.generated.resources.study_status_remaining
import jlptwords.presentation_kmp.generated.resources.tts_volume_muted
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = koinViewModel(),
    navigateToWordDetail: (kanji: String, pronunciation: String) -> Unit,
    navigateToBack: () -> Unit,
    navigateToSetting: () -> Unit,
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // 도움말 오버레이의 "도움말 닫기"를 가려진 "도움말"과 같은 높이에 놓기 위해 실측한다.
    var progressBarHeight by remember { mutableStateOf(0.dp) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            StudySideEffect.NavigateToBack -> {
                navigateToBack()
            }

            is StudySideEffect.NavigateToWordDetail -> {
                navigateToWordDetail(sideEffect.kanji, sideEffect.pronunciation)
            }

            is StudySideEffect.ShowInterstitialAd -> {
                sideEffect.interstitialAd.show()
            }

            StudySideEffect.ShowCopyFailureMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.copy_to_clip_board_fail))
                }
            }

            StudySideEffect.ShowCopySuccessMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.copy_to_clip_board_success))
                }
            }

            StudySideEffect.ShowVolumeMutedMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.tts_volume_muted))
                }
            }

            StudySideEffect.OpenInstallLanguagePack -> {
                handleInstallLanguagePack(
                    showMessage = {
                        scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    },
                )
            }

            StudySideEffect.NavigateToSetting -> {
                navigateToSetting()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    val backNavigationState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(
        state = backNavigationState,
        onBackCompleted = viewModel::onBack,
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        viewModel.onStart()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        viewModel.onStop()
                    }

                    else -> {}
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentWord = state.words.getOrNull(state.currentPage)
    val nextWord = state.words.getOrNull(state.currentPage + 1)
    val progressCompletedText =
        stringResource(Res.string.study_status_completed, state.totalKnownWordIds.size) +
            " / " +
            stringResource(Res.string.study_status_remaining, state.leftWordsSize)
    // 경과시간은 1초마다 바뀐다. 값으로 넘기면 진행바를 품은 섹션들이 매초 함께 재구성되므로,
    // 람다로 넘겨 실제 읽기가 진행바 안에서만 일어나게 한다.
    val progressElapsedTimeText = { state.totalElapsedTimeSeconds.formatElapsedTime() }

    MolluTheme {
        MolluScreenScaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                MolluDefaultTopBar(
                    title = stringResource(Res.string.study_chapter_title, state.sessionIndex + 1),
                    adZeroRemainingHours = state.adZeroRemainingHours,
                    onBackClick = viewModel::onNavigationButtonClick,
                    onAdZeroClick = viewModel::onAdZeroItemClick,
                    onSettingsClick = viewModel::onSettingsClick,
                )
            },
        ) { paddingValue ->
            AnimatedVisibility(
                visible = state.isStudySectionVisible,
                enter = SectionEnter,
                exit = SectionExit,
            ) {
                if (currentWord != null) {
                    StudySection(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValue)
                                .navigationBarsPadding(),
                        completedText = progressCompletedText,
                        elapsedTimeText = progressElapsedTimeText,
                        currentWord = currentWord,
                        nextWord = nextWord,
                        isAnswerRevealed = state.isAnswerRevealed,
                        isExampleVisible = state.isExampleVisible,
                        isSwipeEnabled = state.isSwipeEnabled,
                        isWordTTSPlaying = state.isWordTTSPlaying,
                        isExampleTTSPlaying = state.isExampleTTSPlaying,
                        onAnswerRevealClick = viewModel::onAnswerRevealClick,
                        onExampleShowClick = viewModel::onExampleShowClick,
                        onHelpClick = viewModel::onHelpClick,
                        onDetailClick = viewModel::onDetailClick,
                        onKnowSwiped = viewModel::onKnowButtonClick,
                        onPassSwiped = viewModel::onPassButtonClick,
                        onCopyClick = viewModel::onCopyClick,
                        onWordTTSClick = viewModel::onWordTTSClick,
                        onExampleTTSClick = viewModel::onExampleTTSClick,
                        onProgressBarHeightChange = { progressBarHeight = it },
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isRoundCompletionVisible,
                enter = SectionEnter,
                exit = SectionExit,
            ) {
                RoundCompletion(
                    modifier = Modifier.padding(paddingValue),
                    completedText = progressCompletedText,
                    elapsedTimeText = progressElapsedTimeText,
                    completeWordsCount = state.totalKnownWordIds.size,
                    leftWordsCount = state.leftWordsSize,
                    progresses = state.roundProgresses,
                    isLoading = state.isNextRoundLoading,
                    onContinueButtonClick = viewModel::onContinueNextRoundButtonClick,
                )
            }

            AnimatedVisibility(
                visible = state.isSessionCompletionVisible,
                enter = SectionEnter,
                exit = SectionExit,
            ) {
                SessionCompletion(
                    modifier = Modifier.padding(paddingValue),
                    completedText = progressCompletedText,
                    chapterNumber = state.sessionIndex + 1,
                    totalElapsedTimeSeconds = state.totalElapsedTimeSeconds,
                    totalWordSize = state.totalWordSize,
                    totalAccuracy = state.totalAccuracy,
                    sessionRecords = state.sessionRecords,
                    onFinishButtonClick = viewModel::onFinishSessionButtonClick,
                )
            }

            if (state.isHelpOverlayVisible && state.isStudySectionVisible) {
                StudyHelpOverlay(
                    progressBarHeight = progressBarHeight,
                    onCloseClick = viewModel::onHelpCloseClick,
                    // dim 에서 제외하는 하단 버튼 자리를 학습 섹션과 같은 기준으로 잡아야 어긋나지 않는다.
                    modifier =
                        Modifier
                            .padding(paddingValue)
                            .navigationBarsPadding(),
                )
            }
        }

        if (state.isAdZeroDialogShown) {
            AdZeroDialog(
                onDismissRequest = viewModel::onAdZeroDialogDismiss,
            )
        }

        if (state.isExitSessionBottomSheetVisible) {
            ExitSessionBottomSheet(
                level = state.level,
                chapter = state.sessionIndex + 1,
                progressPercent = (state.totalProgress * 100).toInt(),
                onDismissRequest = viewModel::onExitSessionBottomSheetDismiss,
                onExitSessionButtonClick = viewModel::onExitSessionButtonClick,
                onContinueButtonClick = viewModel::onContinueSessionButtonClick,
            )
        }

        if (state.isTTSWarningDialogVisible) {
            TTSWarningDialog(
                onConfirm = viewModel::onTTSWarningDialogConfirmClick,
                onCancel = viewModel::onTTSWarningDialogDismiss,
                onDismissRequest = viewModel::onTTSWarningDialogDismiss,
            )
        }
    }
}

/** 학습·라운드 완료·세션 완료 섹션이 좌우로 밀려 교차하는 전환. */
private val SectionEnter =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = SECTION_TRANSITION_MILLIS, easing = EaseInOutCubic),
    )

private val SectionExit =
    slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(durationMillis = SECTION_TRANSITION_MILLIS, easing = EaseInOutCubic),
    )

private const val SECTION_TRANSITION_MILLIS = 300

@Composable
private fun StudySection(
    currentWord: WordPageCardItem,
    nextWord: WordPageCardItem?,
    completedText: String,
    elapsedTimeText: () -> String,
    isAnswerRevealed: Boolean,
    isExampleVisible: Boolean,
    isSwipeEnabled: Boolean,
    isWordTTSPlaying: Boolean,
    isExampleTTSPlaying: Boolean,
    onAnswerRevealClick: () -> Unit,
    onExampleShowClick: () -> Unit,
    onHelpClick: () -> Unit,
    onDetailClick: (kanji: String, pronunciation: String) -> Unit,
    onKnowSwiped: () -> Unit,
    onPassSwiped: () -> Unit,
    onCopyClick: (String) -> Unit,
    onWordTTSClick: (String) -> Unit,
    onExampleTTSClick: (String) -> Unit,
    onProgressBarHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StudyProgressBar(
            completedText = completedText,
            elapsedTimeText = elapsedTimeText,
            modifier =
                Modifier.onSizeChanged {
                    onProgressBarHeightChange(with(density) { it.height.toDp() })
                },
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MolluTheme.colorScheme.sub),
        ) {
            SwipeableWordCard(
                item = currentWord,
                nextItem = nextWord,
                isRevealed = isAnswerRevealed,
                isExampleVisible = isExampleVisible,
                isWordTTSPlaying = isWordTTSPlaying,
                isExampleTTSPlaying = isExampleTTSPlaying,
                enabled = isSwipeEnabled,
                onHelpClick = onHelpClick,
                onDetailClick = { onDetailClick(currentWord.kanji, currentWord.pronunciation) },
                onCopyClick = { onCopyClick(currentWord.kanji) },
                onWordTTSClick = { onWordTTSClick(currentWord.pronunciation) },
                onExampleTTSClick = onExampleTTSClick,
                onExampleShowClick = onExampleShowClick,
                onSwipedLeft = onKnowSwiped,
                onSwipedRight = onPassSwiped,
                nextCardBottomButton = {
                    MeaningToggleButton(
                        isRevealed = false,
                        onClick = {},
                    )
                },
                bottomButton = {
                    MeaningToggleButton(
                        isRevealed = isAnswerRevealed,
                        onClick = onAnswerRevealClick,
                    )
                },
            )
        }
    }
}

@Composable
private fun MeaningToggleButton(
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 뜻 공개(isRevealed) 상태에서는 배경을 두지 않아 카드의 종이질감 마스킹 배경이 그대로 비치게 하고,
    // 테두리만 남긴다(Figma 733:6747 "뜻 가리기" 버튼).
    val contentColor =
        if (isRevealed) MolluTheme.colorScheme.black else MolluTheme.colorScheme.white

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(72.dp)
                .then(
                    if (isRevealed) {
                        Modifier.border(width = 0.5.dp, color = MolluTheme.colorScheme.black)
                    } else {
                        Modifier.molluButtonBackground()
                    },
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                if (isRevealed) {
                    stringResource(Res.string.study_meaning_hide)
                } else {
                    stringResource(Res.string.study_hint_meaning)
                },
            style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}
