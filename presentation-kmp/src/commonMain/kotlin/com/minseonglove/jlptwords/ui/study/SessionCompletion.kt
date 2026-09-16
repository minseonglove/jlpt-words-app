package com.minseonglove.jlptwords.ui.study

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.extension.formatElapsedTime
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.selection.session.ACCURATE_THRESHOLD_PERCENT
import com.minseonglove.jlptwords.ui.selection.session.speedThresholdSeconds
import com.minseonglove.jlptwords.ui.study.component.CompletionHeadline
import com.minseonglove.jlptwords.ui.study.component.CompletionStatRow
import com.minseonglove.jlptwords.ui.study.component.CompletionTitle
import com.minseonglove.jlptwords.ui.study.component.SessionStatsChart
import com.minseonglove.jlptwords.ui.study.component.StudyProgressBar
import com.minseonglove.jlptwords.ui.study.component.accuracyAxisOf
import com.minseonglove.jlptwords.ui.study.component.timeAxisOf
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ic_stamp_accurate
import jlptwords.presentation_kmp.generated.resources.ic_stamp_complete
import jlptwords.presentation_kmp.generated.resources.ic_stamp_speed
import jlptwords.presentation_kmp.generated.resources.session_card_completion_count
import jlptwords.presentation_kmp.generated.resources.session_completion_all_done
import jlptwords.presentation_kmp.generated.resources.session_completion_chapter_done
import jlptwords.presentation_kmp.generated.resources.session_completion_finish
import jlptwords.presentation_kmp.generated.resources.session_statistics_accuracy
import jlptwords.presentation_kmp.generated.resources.session_statistics_accuracy_percentage
import jlptwords.presentation_kmp.generated.resources.session_statistics_completed_words
import jlptwords.presentation_kmp.generated.resources.session_statistics_total_time
import jlptwords.presentation_kmp.generated.resources.session_statistics_words_count
import jlptwords.presentation_kmp.generated.resources.study_chart_time_label
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

/** 그래프에 표시할 최근 회차 수(디자인 기준). 이보다 많으면 최근 N회만 보여준다. */
private const val MAX_VISIBLE_ROUNDS = 6

/** 완료 도장 기울기(디자인 2016:4292 · 반시계 17°). */
private const val COMPLETE_STAMP_ROTATION_DEGREES = -17f

/** 완료 도장 한 변(회전 전). 회전 후 바운딩 박스는 약 73.6dp 가 된다. */
private val CompleteStampSize = 59.dp

/** 迅速·正確 도장 높이. 통계 줄(24dp)보다 커서 줄 위아래로 넘친다. */
private val SubStampHeight = 32.dp

/** 통계 컬럼 오른쪽 끝과 迅速·正確 도장 왼쪽 사이 간격(디자인 2016:4292). */
private val SubStampGap = 9.dp

@Composable
fun SessionCompletion(
    completedText: String,
    chapterNumber: Int,
    totalElapsedTimeSeconds: Int,
    totalWordSize: Int,
    totalAccuracy: Int,
    sessionRecords: ImmutableList<StudyRecord>,
    onFinishButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 최근 N회만 그래프에 표시하고, 실제 회차번호(startRound..)로 눈금을 매긴다.
    // 매 재구성마다 새 List 를 만들면 SessionStatsChart 가 스킵되지 않으므로 remember 로 고정한다.
    val visibleRecords = remember(sessionRecords) { sessionRecords.takeLast(MAX_VISIBLE_ROUNDS) }
    val timeSeries = remember(visibleRecords) { visibleRecords.map { it.completionTimeSeconds }.toImmutableList() }
    val accuracySeries = remember(visibleRecords) { visibleRecords.map { it.accuracy }.toImmutableList() }
    val startRound = sessionRecords.size - visibleRecords.size + 1
    // 축은 보이는 회차가 아니라 세션 전체 기록으로 잡는다. 창이 밀려도 눈금과 점 높이가 유지된다.
    val timeAxis = remember(sessionRecords) { timeAxisOf(sessionRecords.map { it.completionTimeSeconds }) }
    val accuracyAxis = remember(sessionRecords) { accuracyAxisOf(sessionRecords.map { it.accuracy }) }

    val timeDeltaText =
        sessionRecords.deltaOrNull { it.completionTimeSeconds }?.let { delta ->
            val sign = if (delta < 0) "-" else "+"
            "$sign${abs(delta).formatElapsedTime()}"
        }
    val accuracyDeltaText =
        sessionRecords.deltaOrNull { it.accuracy }?.let { delta ->
            if (delta >= 0) "+$delta" else "$delta"
        }

    val hasSpeedStamp = totalElapsedTimeSeconds in 1..speedThresholdSeconds(totalWordSize)
    val hasAccurateStamp = totalAccuracy >= ACCURATE_THRESHOLD_PERCENT
    val totalElapsedTimeText = totalElapsedTimeSeconds.formatElapsedTime()

    Column(modifier = modifier.fillMaxSize()) {
        StudyProgressBar(
            completedText = completedText,
            elapsedTimeText = { totalElapsedTimeText },
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
            Spacer(Modifier.height(63.dp))
            Box {
                CompletionHeadline(
                    text = stringResource(Res.string.session_completion_chapter_done, chapterNumber),
                )
                StampImage(
                    res = Res.drawable.ic_stamp_complete,
                    rotationDegrees = COMPLETE_STAMP_ROTATION_DEGREES,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(x = (-92).dp, y = (-19).dp)
                            .size(CompleteStampSize),
                )
            }
            Spacer(Modifier.height(55.dp))
            CompletionTitle(text = stringResource(Res.string.session_completion_all_done))
            Spacer(Modifier.height(35.dp))
            SessionStatsChart(
                times = timeSeries,
                accuracies = accuracySeries,
                timeAxis = timeAxis,
                accuracyAxis = accuracyAxis,
                timeLabel = stringResource(Res.string.study_chart_time_label),
                accuracyLabel = stringResource(Res.string.session_statistics_accuracy),
                xAxisLabel = stringResource(Res.string.session_card_completion_count),
                timeFormatter = Int::formatElapsedTime,
                startRound = startRound,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(146.dp)
                        .background(MolluTheme.colorScheme.white),
            )
            Spacer(Modifier.height(35.dp))
            Column(
                modifier = Modifier.width(201.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CompletionStatRow(
                    label = stringResource(Res.string.session_statistics_completed_words),
                    value =
                        valueWithDelta(
                            mainText = stringResource(Res.string.session_statistics_words_count, totalWordSize),
                            mainColor = MolluTheme.colorScheme.black,
                            deltaText = null,
                            deltaColor = MolluTheme.colorScheme.black,
                        ),
                )
                CompletionStatRow(
                    label = stringResource(Res.string.session_statistics_total_time),
                    value =
                        valueWithDelta(
                            mainText = totalElapsedTimeText,
                            mainColor = MolluTheme.colorScheme.highlightRed,
                            deltaText = timeDeltaText,
                            deltaColor = MolluTheme.colorScheme.black,
                        ),
                    trailing =
                        if (hasSpeedStamp) {
                            { SubStamp(res = Res.drawable.ic_stamp_speed, width = 47.dp) }
                        } else {
                            null
                        },
                )
                CompletionStatRow(
                    label = stringResource(Res.string.session_statistics_accuracy),
                    value =
                        valueWithDelta(
                            mainText = stringResource(Res.string.session_statistics_accuracy_percentage, totalAccuracy),
                            mainColor = MolluTheme.colorScheme.highlightRed,
                            deltaText = accuracyDeltaText,
                            deltaColor = MolluTheme.colorScheme.black,
                        ),
                    trailing =
                        if (hasAccurateStamp) {
                            { SubStamp(res = Res.drawable.ic_stamp_accurate, width = 48.dp) }
                        } else {
                            null
                        },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        MolluBottomButton(
            text = stringResource(Res.string.session_completion_finish),
            onClick = onFinishButtonClick,
        )
    }
}

/**
 * 통계 줄 오른쪽 끝에 붙는 도장. [CompletionStatRow] 의 trailing 자리에 놓인다.
 * requiredSize 로 줄 높이 제약을 무시해 줄 위아래로 넘치고, offset 으로 통계 컬럼 바깥에 놓인다.
 */
@Composable
private fun SubStamp(
    res: DrawableResource,
    width: Dp,
) {
    StampImage(
        res = res,
        modifier =
            Modifier
                .offset(x = width + SubStampGap)
                .requiredSize(width = width, height = SubStampHeight),
    )
}

/**
 * 도장 이미지. 원본 PNG 가 흰 배경(불투명)이라 [BlendMode.Multiply] 로 그려
 * 흰 배경은 투과시키고 겹친 콘텐츠가 비쳐 보이게 한다(디자인의 mix-blend-multiply).
 *
 * [rotationDegrees] 는 Modifier.rotate 가 아니라 DrawScope 변환으로 적용한다.
 * graphicsLayer 로 레이어가 분리되면 Multiply 대상이 배경이 아닌 빈 레이어가 되기 때문.
 */
@Composable
private fun StampImage(
    res: DrawableResource,
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f,
) {
    val image = imageResource(res)
    Canvas(modifier = modifier) {
        rotate(rotationDegrees) {
            drawImage(
                image = image,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                blendMode = BlendMode.Multiply,
            )
        }
    }
}

/** 마지막 두 회차의 차이(마지막 - 직전). 회차가 2개 미만이면 null. */
private inline fun List<StudyRecord>.deltaOrNull(selector: (StudyRecord) -> Int): Int? {
    if (size < 2) return null
    return selector(this[lastIndex]) - selector(this[lastIndex - 1])
}

private fun valueWithDelta(
    mainText: String,
    mainColor: Color,
    deltaText: String?,
    deltaColor: Color,
): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = mainColor)) {
            append(mainText)
        }
        if (deltaText != null) {
            withStyle(SpanStyle(color = deltaColor)) {
                append(" ($deltaText)")
            }
        }
    }
