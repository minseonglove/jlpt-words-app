package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.extension.formatElapsedTime
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.ceil
import kotlin.math.floor

/** 세로축 눈금 단위. 이 배수로 바깥 반올림해 값이 조금 흔들려도 눈금이 그대로 유지된다. */
private const val AXIS_STEP = 5

/** 시간축 최소 폭 = 최댓값의 비율. 회차 간 차이가 미미할 때 과장돼 보이는 것을 막는다. */
private const val TIME_MIN_SPAN_RATIO = 0.20f

/** 정답률축 최소 폭(%p). */
private const val ACCURACY_MIN_SPAN = 20

private const val ACCURACY_MAX = 100

// 세로 배치 비율(디자인 733:7087 기준 354×146).
private const val TIME_TOP = 0.178f
private const val TIME_BOTTOM = 0.411f
private const val DIVIDER_Y = 0.486f
private const val ACCURACY_TOP = 0.575f
private const val ACCURACY_BOTTOM = 0.781f
private const val TICK_Y = 0.884f

/** 그래프 세로축 범위. */
@Immutable
data class ChartAxis(
    val min: Int,
    val max: Int,
)

/**
 * 시간축 범위. 보이는 회차가 아니라 **세션 전체 기록**으로 계산해야
 * 창이 밀려도 이미 그려진 회차의 높이와 눈금이 바뀌지 않는다.
 */
fun timeAxisOf(allTimes: List<Int>): ChartAxis {
    if (allTimes.isEmpty()) return ChartAxis(0, AXIS_STEP)
    val lowest = allTimes.min()
    val highest = allTimes.max()
    val span = maxOf((highest - lowest).toFloat(), highest * TIME_MIN_SPAN_RATIO)
    return axisOf(lowest, highest, span, upperBound = null)
}

/** 정답률축 범위. [timeAxisOf] 와 같은 이유로 세션 전체 기록으로 계산한다. */
fun accuracyAxisOf(allAccuracies: List<Int>): ChartAxis {
    if (allAccuracies.isEmpty()) return ChartAxis(0, ACCURACY_MAX)
    val lowest = allAccuracies.min()
    val highest = allAccuracies.max()
    val span = maxOf((highest - lowest).toFloat(), ACCURACY_MIN_SPAN.toFloat())
    return axisOf(lowest, highest, span, upperBound = ACCURACY_MAX)
}

/** 실제 범위를 [span] 폭 안에 가운데 두고, 0~[upperBound] 안으로 민 뒤 [AXIS_STEP] 배수로 바깥 반올림한다. */
private fun axisOf(
    lowest: Int,
    highest: Int,
    span: Float,
    upperBound: Int?,
): ChartAxis {
    val center = (lowest + highest) / 2f
    var low = center - span / 2f
    var high = center + span / 2f
    if (upperBound != null && high > upperBound) {
        low -= high - upperBound
        high = upperBound.toFloat()
    }
    if (low < 0f) {
        high += -low
        low = 0f
    }
    var roundedLow = (floor(low / AXIS_STEP) * AXIS_STEP).toInt().coerceAtLeast(0)
    var roundedHigh = (ceil(high / AXIS_STEP) * AXIS_STEP).toInt()
    if (upperBound != null) roundedHigh = roundedHigh.coerceAtMost(upperBound)
    if (roundedHigh <= roundedLow) {
        if (upperBound != null && roundedHigh == upperBound) {
            roundedLow = (roundedHigh - AXIS_STEP).coerceAtLeast(0)
        } else {
            roundedHigh = roundedLow + AXIS_STEP
        }
    }
    return ChartAxis(roundedLow, roundedHigh)
}

/**
 * 세션 완료 그래프(mollu, 디자인 971:17223). 시간과 정답률을 위아래 두 단으로 분리한 꺾은선 차트.
 * - 계열이 세로로 나뉘어 있어 선도 값 라벨도 서로 겹치지 않는다
 * - 각 단의 세로축에 [ChartAxis] 의 최댓값·최솟값을 실제 수치로 표기한다
 * - 현재 회차 값은 각 단의 오른쪽 끝에 붙는다
 *
 * @param times 보이는 회차의 학습 시간(초), createAt 오름차순
 * @param accuracies 보이는 회차의 정답률(0~100), [times] 와 같은 길이
 * @param timeAxis 시간축 범위. 세션 전체 기록 기준([timeAxisOf])
 * @param accuracyAxis 정답률축 범위. 세션 전체 기록 기준([accuracyAxisOf])
 * @param startRound 첫 데이터의 실제 회차번호(예: 4 → 눈금 4,5,6…)
 * @param timeFormatter 초를 표시 문자열로 바꾸는 함수(예: 74 → "01:14")
 */
@Composable
fun SessionStatsChart(
    times: ImmutableList<Int>,
    accuracies: ImmutableList<Int>,
    timeAxis: ChartAxis,
    accuracyAxis: ChartAxis,
    timeLabel: String,
    accuracyLabel: String,
    xAxisLabel: String,
    timeFormatter: (Int) -> String,
    modifier: Modifier = Modifier,
    startRound: Int = 1,
) {
    val timeColor = MolluTheme.colorScheme.highlightRed
    val accuracyColor = MolluTheme.colorScheme.highlightBlue
    val axisColor = MolluTheme.colorScheme.black
    val subColor = MolluTheme.colorScheme.sub
    val caption = MolluTheme.typography.caption2
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        if (times.isEmpty() || accuracies.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val captionX = 6.dp.toPx()
        val tickRightX = 58.dp.toPx()
        val axisX = 62.dp.toPx()
        val plotLeft = 66.dp.toPx()
        val plotRight = width - 54.dp.toPx()
        val valueGap = 6.dp.toPx()
        val axisStroke = 0.5.dp.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

        fun measure(
            text: String,
            color: Color,
            bold: Boolean = false,
        ) = textMeasurer.measure(
            text = text,
            style = caption.copy(color = color, fontWeight = if (bold) FontWeight.Bold else null),
        )

        /** [centerY] 를 세로 중앙으로 두고 그린다. 가로는 [startX]/[endX]/[centerX] 중 하나 기준. */
        fun drawLabel(
            text: String,
            color: Color,
            centerY: Float,
            startX: Float? = null,
            endX: Float? = null,
            centerX: Float? = null,
            bold: Boolean = false,
        ) {
            val measured = measure(text, color, bold)
            val x =
                when {
                    endX != null -> endX - measured.size.width
                    centerX != null -> centerX - measured.size.width / 2f
                    else -> startX ?: 0f
                }
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(x, centerY - measured.size.height / 2f),
            )
        }

        fun pointsOf(
            values: List<Int>,
            axis: ChartAxis,
            top: Float,
            bottom: Float,
        ): List<Offset> {
            val span = (axis.max - axis.min).coerceAtLeast(1)
            return values.mapIndexed { index, value ->
                val x =
                    if (values.size == 1) {
                        (plotLeft + plotRight) / 2f
                    } else {
                        plotLeft + (index.toFloat() / (values.size - 1)) * (plotRight - plotLeft)
                    }
                val fraction = ((value - axis.min).toFloat() / span).coerceIn(0f, 1f)
                Offset(x, bottom - fraction * (bottom - top))
            }
        }

        fun drawStrip(
            values: List<Int>,
            axis: ChartAxis,
            top: Float,
            bottom: Float,
            color: Color,
            name: String,
            format: (Int) -> String,
        ) {
            drawLabel(name, color, (top + bottom) / 2f, startX = captionX)
            drawLine(axisColor, Offset(axisX, top), Offset(axisX, bottom), strokeWidth = axisStroke)
            drawLabel(format(axis.max), subColor, top, endX = tickRightX)
            drawLabel(format(axis.min), subColor, bottom, endX = tickRightX)

            val points = pointsOf(values, axis, top, bottom)
            for (index in 0 until points.size - 1) {
                drawLine(color, points[index], points[index + 1], strokeWidth = 1.dp.toPx())
            }
            points.forEachIndexed { index, point ->
                val radius = if (index == points.lastIndex) 2.2.dp.toPx() else 1.3.dp.toPx()
                drawCircle(color, radius = radius, center = point)
            }
            // 회차가 적어 마지막 점이 왼쪽에 있어도 값이 점 옆에 붙도록 점 기준으로 놓는다.
            drawLabel(
                text = format(values.last()),
                color = color,
                centerY = points.last().y,
                startX = points.last().x + valueGap,
                bold = true,
            )
        }

        // 상·하 점선 경계
        drawLine(subColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = axisStroke, pathEffect = dash)
        drawLine(subColor, Offset(0f, height), Offset(width, height), strokeWidth = axisStroke, pathEffect = dash)

        drawStrip(
            values = times,
            axis = timeAxis,
            top = height * TIME_TOP,
            bottom = height * TIME_BOTTOM,
            color = timeColor,
            name = timeLabel,
            format = timeFormatter,
        )

        // 두 단 사이 점선 구분
        drawLine(
            color = subColor,
            start = Offset(captionX, height * DIVIDER_Y),
            end = Offset(width - captionX, height * DIVIDER_Y),
            strokeWidth = axisStroke,
            pathEffect = dash,
        )

        val accuracyBottom = height * ACCURACY_BOTTOM
        drawStrip(
            values = accuracies,
            axis = accuracyAxis,
            top = height * ACCURACY_TOP,
            bottom = accuracyBottom,
            color = accuracyColor,
            name = accuracyLabel,
            format = { "$it%" },
        )
        // X축은 아래쪽 단에만 둔다
        drawLine(
            color = axisColor,
            start = Offset(axisX, accuracyBottom),
            end = Offset(plotRight + 6.dp.toPx(), accuracyBottom),
            strokeWidth = axisStroke,
        )

        val tickY = height * TICK_Y
        val roundPoints = pointsOf(times, timeAxis, 0f, 0f)
        roundPoints.forEachIndexed { index, point ->
            val isCurrent = index == roundPoints.lastIndex
            drawLabel(
                text = "${startRound + index}",
                color = if (isCurrent) axisColor else subColor,
                centerY = tickY,
                centerX = point.x,
                bold = isCurrent,
            )
        }
        drawLabel(xAxisLabel, axisColor, tickY, endX = width - captionX)
    }
}

@Preview
@Composable
private fun SessionStatsChartPreview() {
    val times = persistentListOf(93, 88, 84, 80, 77, 74)
    val accuracies = persistentListOf(62, 58, 64, 68, 72, 75)
    MolluTheme {
        SessionStatsChart(
            times = times,
            accuracies = accuracies,
            timeAxis = timeAxisOf(times),
            accuracyAxis = accuracyAxisOf(accuracies),
            timeLabel = "시간",
            accuracyLabel = "정답률",
            xAxisLabel = "회차",
            timeFormatter = Int::formatElapsedTime,
            startRound = 4,
            modifier =
                Modifier
                    .background(MolluTheme.colorScheme.white)
                    .size(width = 354.dp, height = 146.dp),
        )
    }
}
