package com.minseonglove.jlptwords.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.minseonglove.jlptwords.entity.ContinueStudySession
import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyOverview
import com.minseonglove.jlptwords.ui.adzero.AdZeroDialog
import com.minseonglove.jlptwords.ui.base.LoadingScreen
import com.minseonglove.jlptwords.ui.base.MolluDefaultTopBar
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.RetryScreen
import com.minseonglove.jlptwords.ui.base.molluPaperVerticalScroll
import com.minseonglove.jlptwords.ui.exit.ExitAppBottomSheet
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.util.finishApp
import com.minseonglove.jlptwords.util.isAppExitSupported
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    navigateToWordDetail: (String, String) -> Unit,
    navigateToStudy: (Int, Int) -> Unit,
    navigateToSetting: () -> Unit,
    navigateToLevelSelection: (JLPTLevel) -> Unit,
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is HomeSideEffect.NavigateToLevelSelection -> {
                navigateToLevelSelection(sideEffect.level)
            }

            is HomeSideEffect.NavigateToWordDetail -> {
                navigateToWordDetail(
                    sideEffect.kanji,
                    sideEffect.pronunciation,
                )
            }

            is HomeSideEffect.NavigateToStudy -> {
                navigateToStudy(
                    sideEffect.sessionId,
                    sideEffect.sessionIndex,
                )
            }

            HomeSideEffect.NavigateToSetting -> {
                navigateToSetting()
            }

            HomeSideEffect.FinishApp -> {
                finishApp()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    // 앱 종료가 없는 플랫폼(iOS)에서는 핸들러를 아예 등록하지 않는다. 활성 핸들러가 하나도 없으면
    // Compose 가 화면 가장자리 back 제스처 인식기까지 꺼서, 홈에서 스와이프해도 아무 일도 일어나지 않는다.
    if (isAppExitSupported) {
        val backNavigationState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = backNavigationState,
            onBackCompleted = viewModel::onBack,
        )
    }

    MolluTheme {
        MolluScreenScaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            topBar = {
                MolluDefaultTopBar(
                    title = stringResource(Res.string.home_title, state.level.name),
                    adZeroRemainingHours = state.adZeroRemainingHours,
                    onBackClick = null,
                    onAdZeroClick = viewModel::onAdZeroClick,
                    onSettingsClick = viewModel::onSettingsClick,
                    // 홈(Title_Only)은 디자인상 좌측 패딩 40 을 쓴다.
                    contentPadding = PaddingValues(start = 40.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                    // 타이틀(현재 급수)을 누르면 급수 선택 화면으로 이동한다.
                    onTitleClick = viewModel::onTitleClick,
                )
            },
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                if (state.isLoading) {
                    LoadingScreen()
                } else if (state.isRetryScreenVisible) {
                    RetryScreen(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = viewModel::onRetryButtonClick,
                    )
                } else {
                    HomeContent(
                        todayExample = state.todayExample,
                        continueSession = state.continueSession,
                        studyOverview = state.studyOverview,
                        onTodayExampleClick = viewModel::onTodayExampleClick,
                        onContinueSessionClick = viewModel::onContinueSessionClick,
                    )
                }

                if (state.isAdZeroDialogShown) {
                    AdZeroDialog(
                        onDismissRequest = {
                            viewModel.onAdZeroDialogDismiss()
                        },
                    )
                }
            }
        }
    }

    if (state.isExitAppBottomSheetShown) {
        ExitAppBottomSheet(
            exitAdState = state.exitAdState,
            onDismissRequest = viewModel::onExitAppBottomSheetDismiss,
            onExitAppButtonClick = viewModel::onExitAppButtonClick,
        )
    }
}

/** 광고 잔여시간 등 다른 State 변경에 휩쓸리지 않도록 표시에 필요한 값만 받는다. */
@Composable
private fun HomeContent(
    todayExample: DailyExample?,
    continueSession: ContinueStudySession?,
    studyOverview: StudyOverview?,
    onTodayExampleClick: () -> Unit,
    onContinueSessionClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .molluPaperVerticalScroll(rememberScrollState()),
    ) {
        todayExample?.let { todayExample ->
            TodayExampleSection(
                example = todayExample,
                onClick = onTodayExampleClick,
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MolluTheme.colorScheme.black,
            )
        }
        continueSession?.let { continueSession ->
            ContinueSessionSection(
                item = continueSession,
                onClick = onContinueSessionClick,
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MolluTheme.colorScheme.black,
            )
        }
        studyOverview?.let { studyOverview ->
            StudyOverviewSection(
                overview = studyOverview,
            )
        }
    }
}
