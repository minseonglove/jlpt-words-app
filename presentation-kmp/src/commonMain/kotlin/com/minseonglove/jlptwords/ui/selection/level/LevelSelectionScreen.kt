package com.minseonglove.jlptwords.ui.selection.level

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.base.LoadingScreen
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.MolluTopBar
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.current_level_section_title
import jlptwords.presentation_kmp.generated.resources.level_selection_confirm
import jlptwords.presentation_kmp.generated.resources.level_selection_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LevelSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: LevelSelectionViewModel = koinViewModel(),
    onLevelConfirmed: () -> Unit,
    navigateToBack: () -> Unit,
) {
    val state by viewModel.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LevelSelectionSideEffect.NavigateToSessionSelectionScreen -> {
                onLevelConfirmed()
            }

            LevelSelectionSideEffect.NavigateToBack -> {
                navigateToBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    MolluTheme {
        MolluScreenScaffold(
            modifier = modifier,
            containerColor = MolluTheme.colorScheme.white,
            topBar = {
                MolluTopBar(
                    title = stringResource(Res.string.level_selection_title),
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        viewModel.onNavigationButtonClick()
                    },
                )
            },
            bottomBar = {
                MolluBottomButton(
                    text = stringResource(Res.string.level_selection_confirm),
                    onClick = viewModel::onConfirmButtonClick,
                )
            },
        ) { paddingValue ->
            if (state.isLoading) {
                LoadingScreen(modifier = Modifier.padding(paddingValue))
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValue)
                            .background(MolluTheme.colorScheme.white),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 16.dp,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.current_level_section_title),
                                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                                color = MolluTheme.colorScheme.black,
                            )
                            Text(
                                text = state.lastSelectedLevel.name,
                                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                                color = MolluTheme.colorScheme.black,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LevelGradeTabRow(
                            selectedLevel = state.selectedLevel,
                            onLevelSelected = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                viewModel.onLevelSelectionTabClick(it)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )

                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    // 카드 상단 테두리를 탭 테두리와 겹치도록 stroke 두께만큼 끌어올림
                                    .offset(y = -LevelGradeCardBorderWidth),
                            // 카드는 너구리 높이로 wrap 되므로 상단 정렬해 디자인처럼 아래는 산 배경 영역으로 둔다
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            state.levelSummaries
                                .firstOrNull { it.level == state.selectedLevel }
                                ?.let { summary ->
                                    LevelGradeCard(summary = summary)
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    LevelMountainBackground(
                        level = state.selectedLevel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
