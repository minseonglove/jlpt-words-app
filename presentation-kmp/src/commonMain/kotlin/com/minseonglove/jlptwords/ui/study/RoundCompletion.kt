package com.minseonglove.jlptwords.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.study.component.CompletionHeadline
import com.minseonglove.jlptwords.ui.study.component.CompletionStatRow
import com.minseonglove.jlptwords.ui.study.component.CompletionTitle
import com.minseonglove.jlptwords.ui.study.component.RoundProgressChart
import com.minseonglove.jlptwords.ui.study.component.StudyProgressBar
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.round_completion_completed_words
import jlptwords.presentation_kmp.generated.resources.round_completion_count_format
import jlptwords.presentation_kmp.generated.resources.round_completion_remaining_words
import jlptwords.presentation_kmp.generated.resources.round_completion_shuffle_continue
import jlptwords.presentation_kmp.generated.resources.round_completion_subtitle
import jlptwords.presentation_kmp.generated.resources.round_completion_title
import jlptwords.presentation_kmp.generated.resources.study_chart_round_label
import jlptwords.presentation_kmp.generated.resources.study_progress_remaining_label
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

/** 그래프에 표시할 최근 라운드 수(디자인 기준). 이보다 많으면 최근 N개만 보여준다. */
private const val MAX_VISIBLE_ROUNDS = 6

@Composable
fun RoundCompletion(
    completedText: String,
    elapsedTimeText: () -> String,
    completeWordsCount: Int,
    leftWordsCount: Int,
    progresses: ImmutableList<Int>,
    isLoading: Boolean,
    onContinueButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentProgress = progresses.lastOrNull() ?: 0
    val progressDelta =
        if (progresses.size >= 2) {
            progresses[progresses.lastIndex] - progresses[progresses.lastIndex - 1]
        } else {
            null
        }
    // takeLast 결과를 그대로 넘기면 매 재구성마다 새 List 라 RoundProgressChart 가 스킵되지 않는다.
    val visibleProgresses = remember(progresses) { progresses.takeLast(MAX_VISIBLE_ROUNDS).toImmutableList() }
    val startRound = progresses.size - visibleProgresses.size + 1

    Column(modifier = modifier.fillMaxSize()) {
        StudyProgressBar(
            completedText = completedText,
            elapsedTimeText = elapsedTimeText,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .molluPaperBackground(alpha = 0.6f)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            CompletionHeadline(text = stringResource(Res.string.round_completion_title))
            Spacer(Modifier.height(27.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(21.dp),
            ) {
                CompletionTitle(text = stringResource(Res.string.round_completion_subtitle))
                RoundProgressChart(
                    progresses = visibleProgresses,
                    yAxisLabel = stringResource(Res.string.study_progress_remaining_label),
                    xAxisLabel = stringResource(Res.string.study_chart_round_label),
                    startRound = startRound,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(191.dp)
                            .background(MolluTheme.colorScheme.white),
                )
            }
            Spacer(Modifier.height(48.dp))
            Column(
                modifier = Modifier.width(201.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CompletionStatRow(
                    label = stringResource(Res.string.round_completion_completed_words),
                    value =
                        coloredValue(
                            text = stringResource(Res.string.round_completion_count_format, completeWordsCount),
                            color = MolluTheme.colorScheme.highlightBlue,
                        ),
                )
                CompletionStatRow(
                    label = stringResource(Res.string.round_completion_remaining_words),
                    value =
                        coloredValue(
                            text = stringResource(Res.string.round_completion_count_format, leftWordsCount),
                            color = MolluTheme.colorScheme.highlightRed,
                        ),
                )
                CompletionStatRow(
                    label = stringResource(Res.string.study_progress_remaining_label),
                    value =
                        progressValue(
                            currentProgress = currentProgress,
                            delta = progressDelta,
                            color = MolluTheme.colorScheme.black,
                        ),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        MolluBottomButton(
            text = stringResource(Res.string.round_completion_shuffle_continue),
            onClick = onContinueButtonClick,
            enabled = !isLoading,
            loading = isLoading,
        )
    }
}

private fun coloredValue(
    text: String,
    color: Color,
): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = color)) {
            append(text)
        }
    }

private fun progressValue(
    currentProgress: Int,
    delta: Int?,
    color: Color,
): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = color)) {
            append("$currentProgress%")
            if (delta != null) {
                append(" (${if (delta >= 0) "+" else ""}$delta%)")
            }
        }
    }
