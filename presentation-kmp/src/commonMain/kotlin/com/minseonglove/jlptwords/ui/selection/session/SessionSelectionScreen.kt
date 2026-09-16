package com.minseonglove.jlptwords.ui.selection.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.adzero.AdZeroDialog
import com.minseonglove.jlptwords.ui.base.LoadingScreen
import com.minseonglove.jlptwords.ui.base.MolluDefaultTopBar
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.RetryScreen
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.session_selection_chapter_title
import jlptwords.presentation_kmp.generated.resources.session_selection_chapter_total
import jlptwords.presentation_kmp.generated.resources.session_selection_title
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SessionSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionSelectionViewModel = koinViewModel(),
    navigationToStudyScreen: (Int, Int) -> Unit,
    navigateToBack: () -> Unit,
    navigateToSetting: () -> Unit,
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SessionSelectionSideEffect.NavigationToStudyScreen -> {
                navigationToStudyScreen(
                    sideEffect.sessionId,
                    sideEffect.sessionIndex,
                )
            }

            SessionSelectionSideEffect.NavigateToBack -> {
                navigateToBack()
            }

            SessionSelectionSideEffect.NavigateToSetting -> {
                navigateToSetting()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    MolluTheme {
        MolluScreenScaffold(
            modifier = modifier,
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            topBar = {
                MolluDefaultTopBar(
                    title = stringResource(Res.string.session_selection_title),
                    adZeroRemainingHours = state.adZeroRemainingHours,
                    onBackClick = viewModel::onBack,
                    onAdZeroClick = viewModel::onAdZeroItemClick,
                    onSettingsClick = viewModel::onMoreButtonClick,
                )
            },
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
            ) {
                when {
                    state.isLoading -> LoadingScreen()
                    state.isRetryScreenVisible ->
                        RetryScreen(
                            modifier = Modifier.fillMaxSize(),
                            onRetry = viewModel::onRetryButtonClick,
                        )

                    else ->
                        ChapterList(
                            chapters = state.chapters,
                            expandedChapters = state.expandedChapters,
                            onToggleChapter = viewModel::onChapterHeaderClick,
                            onStackClick = viewModel::onStackedCardsClick,
                            onCollapseAnimationCompleted = viewModel::onCollapseAnimationCompleted,
                            onSessionClick = viewModel::onSessionCardClick,
                        )
                }

                if (state.isAdZeroDialogShown) {
                    AdZeroDialog(onDismissRequest = viewModel::onAdZeroDialogDismiss)
                }

                state.sessionWarning?.let { warning ->
                    SessionWarningDialog(
                        level = warning.level,
                        chapterNumber = warning.chapterNumber,
                        sessionNumber = warning.sessionNumber,
                        progressPercent = warning.progressPercent,
                        onConfirm = {
                            viewModel.onSessionWarningDialogConfirmClick(warning.selectedSessionId)
                        },
                        onCancel = viewModel::onSessionWarningDialogDismiss,
                        onDismissRequest = viewModel::onSessionWarningDialogDismiss,
                    )
                }
            }
        }
    }
}

/**
 * State·ViewModel 을 통째로 받으면 광고 잔여시간·다이얼로그 표시 같은 무관한 변경에도
 * 챕터 목록 전체가 재구성되므로, 목록이 실제로 쓰는 값과 콜백만 받는다.
 */
@Composable
private fun ChapterList(
    chapters: ImmutableList<SessionChapter>,
    expandedChapters: PersistentSet<Int>,
    onToggleChapter: (Int) -> Unit,
    onStackClick: (Int) -> Unit,
    onCollapseAnimationCompleted: () -> Unit,
    onSessionClick: (Int) -> Unit,
) {
    // 목록에 넘기는 종이 겹은 스크롤 콘텐츠에 붙어 함께 움직이므로, iOS 오버스크롤로 밀려난 자리를
    // 메우도록 뷰포트에 고정된 겹을 한 장 더 깐다. 두 겹이 겹쳐 질감이 진해지지 않게 콘텐츠 겹
    // 아래에는 불투명 배경색을 둔다.
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .molluPaperBackground(),
    ) {
        ExpandableChapterList(
            chapters = chapters,
            expandedChapters = expandedChapters,
            config = remember { AnimationTuningConfig() },
            onToggleChapter = onToggleChapter,
            onStackClick = onStackClick,
            onCollapseAnimationCompleted = onCollapseAnimationCompleted,
            onSessionClick = onSessionClick,
            chapterTitleText = { stringResource(Res.string.session_selection_chapter_title, it) },
            chapterTotalText = { stringResource(Res.string.session_selection_chapter_total, it) },
            stickyHeaderBackground = MolluTheme.colorScheme.backgroundNormal,
            dividerColor = MolluTheme.colorScheme.lineStrong,
            modifier =
                Modifier
                    .background(MolluTheme.colorScheme.backgroundNormal)
                    .molluPaperBackground(),
            contentHorizontalPadding = 20.dp,
            cardContent = { item, position, onClick ->
                SessionChapterCard(item = item, position = position, onClick = onClick)
            },
            stackPlaceholderContent = { onClick ->
                SessionChapterCardStackPlaceholder(onClick = onClick)
            },
        )
    }
}
