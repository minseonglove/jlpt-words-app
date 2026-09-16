package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.study_chart_round_label
import jlptwords.presentation_kmp.generated.resources.study_progress_remaining_label
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * 라운드 완료 그래프(mollu, 디자인 733:7073). 라운드별 누적 진행률(0~100) 추이를 그린다.
 * - 상/하 점선 경계 + 좌측 Y축·하단 X축 실선, 좌상단 [yAxisLabel]·우하단 [xAxisLabel] 축 라벨
 * - 하단 X눈금(실제 라운드 번호 [startRound]+index, 최신은 진하게), 각 라운드 위치 세로 격자선(baseline→포인트)
 * - 빨강 꺾은선 + 선 아래 그라데이션 영역(area fill), 최신 포인트에 점과 값 라벨(예: "60%")
 */
@Composable
fun RoundProgressChart(
    progresses: ImmutableList<Int>,
    yAxisLabel: String,
    xAxisLabel: String,
    modifier: Modifier = Modifier,
    startRound: Int = 1,
) {
    val lineColor = MolluTheme.colorScheme.highlightRed
    val gridColor = MolluTheme.colorScheme.sub
    val axisLabelColor = MolluTheme.colorScheme.black
    val tickColor = MolluTheme.colorScheme.sub
    val tickCurrentColor = MolluTheme.colorScheme.black
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        if (progresses.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height

        // 디자인(354 x 191) 비율 기반 플롯 영역
        val plotLeft = width * (52f / 354f)
        val plotRight = width * (286f / 354f)
        val baselineY = height * (162f / 191f)
        val topY = height * (34f / 191f)
        val tickY = height * (172f / 191f)
        val plotHeight = baselineY - topY

        val count = progresses.size
        val maxScale = 100f

        val points =
            progresses.mapIndexed { index, value ->
                val x =
                    if (count == 1) {
                        (plotLeft + plotRight) / 2f
                    } else {
                        plotLeft + (index.toFloat() / (count - 1)) * (plotRight - plotLeft)
                    }
                val y = baselineY - (value.coerceIn(0, 100) / maxScale) * plotHeight
                Offset(x, y)
            }

        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

        // 상/하 점선 경계
        drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1f, pathEffect = dash)
        drawLine(gridColor, Offset(0f, height), Offset(width, height), strokeWidth = 1f, pathEffect = dash)

        // X, Y 축 선 (디자인 733:7741, 좌측 세로 Y축 + 하단 가로 X축, L자)
        val axisLeftX = width * (36f / 354f)
        val axisRightX = width * (342f / 354f)
        drawLine(
            color = gridColor,
            start = Offset(axisLeftX, topY),
            end = Offset(axisLeftX, baselineY),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = gridColor,
            start = Offset(axisLeftX, baselineY),
            end = Offset(axisRightX, baselineY),
            strokeWidth = 1.dp.toPx(),
        )

        // 각 라운드 세로 격자선 (baseline -> 포인트)
        points.forEach { point ->
            drawLine(
                color = gridColor,
                start = Offset(point.x, baselineY),
                end = Offset(point.x, point.y),
                strokeWidth = 1f,
            )
        }

        // 선 아래 그라데이션 영역(area fill)
        if (points.size >= 2) {
            val areaPath =
                Path().apply {
                    moveTo(points.first().x, baselineY)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, baselineY)
                    close()
                }
            drawPath(
                path = areaPath,
                brush =
                    Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = topY,
                        endY = baselineY,
                    ),
            )
        }

        // 꺾은선
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx(),
            )
        }

        // 최신 포인트 점 + 값 라벨
        val last = points.last()
        drawCircle(color = lineColor, radius = 3.dp.toPx(), center = last)
        val valueLabel =
            textMeasurer.measure(
                text = "${progresses.last().coerceIn(0, 100)}%",
                style = TextStyle(color = lineColor, fontSize = 10.sp),
            )
        drawText(
            textLayoutResult = valueLabel,
            topLeft =
                Offset(
                    x = (last.x - valueLabel.size.width / 2f).coerceIn(0f, width - valueLabel.size.width),
                    y = (last.y - valueLabel.size.height - 4.dp.toPx()).coerceAtLeast(0f),
                ),
        )

        // Y축 라벨 (좌상단)
        val yLabel =
            textMeasurer.measure(
                text = yAxisLabel,
                style = TextStyle(color = axisLabelColor, fontSize = 10.sp),
            )
        drawText(
            textLayoutResult = yLabel,
            topLeft = Offset(x = plotLeft - yLabel.size.width / 2f, y = topY - yLabel.size.height - 2.dp.toPx()),
        )

        // X축 라벨 (우하단)
        val xLabel =
            textMeasurer.measure(
                text = xAxisLabel,
                style = TextStyle(color = axisLabelColor, fontSize = 10.sp),
            )
        drawText(
            textLayoutResult = xLabel,
            topLeft =
                Offset(
                    x =
                        (plotRight + (width - plotRight) / 2f - xLabel.size.width / 2f)
                            .coerceAtMost(width - xLabel.size.width),
                    y = tickY - xLabel.size.height / 2f,
                ),
        )

        // X눈금 (실제 라운드 번호, 최신은 진하게)
        points.forEachIndexed { index, point ->
            val isCurrent = index == points.lastIndex
            val tick =
                textMeasurer.measure(
                    text = "${startRound + index}",
                    style = TextStyle(color = if (isCurrent) tickCurrentColor else tickColor, fontSize = 10.sp),
                )
            drawText(
                textLayoutResult = tick,
                topLeft = Offset(x = point.x - tick.size.width / 2f, y = tickY - tick.size.height / 2f),
            )
        }
    }
}

@Preview
@Composable
private fun RoundProgressChartPreview() {
    MolluTheme {
        RoundProgressChart(
            progresses = persistentListOf(20, 50, 50, 60),
            yAxisLabel = stringResource(Res.string.study_progress_remaining_label),
            xAxisLabel = stringResource(Res.string.study_chart_round_label),
            modifier =
                Modifier
                    .background(MolluTheme.colorScheme.white)
                    .size(width = 354.dp, height = 191.dp),
        )
    }
}
