package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import com.minseonglove.jlptwords.entity.RubySegment
import kotlinx.collections.immutable.ImmutableList

/** 루비 칸 높이·본문 베이스라인을 재는 표본. 일본어 본문의 기준 폰트(CJK) 메트릭을 얻기 위한 것이다. */
private const val METRIC_SAMPLE = "あ"

/**
 * 예문 문장을 루비(후리가나) 포함으로 그린다. Compose 에 루비 조판이 없어
 * 세그먼트를 셀 단위로 배치한다 — 루비 구간은 세그먼트 1셀, 무루비 구간은
 * 글자당 1셀로 쪼개 자연스러운 줄바꿈을 얻는다. 문장은 기존 표시와 동일하게 “ ” 로 감싼다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RubyExampleText(
    furigana: ImmutableList<RubySegment>,
    style: TextStyle,
    color: Color,
    highlightColor: Color,
    modifier: Modifier = Modifier,
) {
    val rubyStyle =
        TextStyle(
            fontSize = style.fontSize * 0.5f,
            fontWeight = style.fontWeight,
        )
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // 루비 칸은 내용과 무관하게 같은 높이여야 한다. 셀마다 Text 로 재면 빈 루비는 라틴 폰트
    // 메트릭(≈1.17em), 가나 루비는 CJK 메트릭(≈1.48em)으로 잡혀 그 차이만큼 본문이 아래로 밀린다.
    val rubyHeight =
        remember(rubyStyle, density) {
            val rubyLayout = textMeasurer.measure(METRIC_SAMPLE, rubyStyle)
            with(density) { rubyLayout.size.height.toDp() }
        }
    // 셀 본문의 폰트가 서로 다를 수 있어(인용부호·숫자는 라틴, 나머지는 CJK) 베이스라인이 어긋난다.
    // 모든 셀을 CJK 기준 베이스라인에 맞춰 한 줄로 정렬한다.
    val baselineTop =
        remember(style, density) {
            val baseLayout = textMeasurer.measure(METRIC_SAMPLE, style)
            with(density) { baseLayout.firstBaseline.toDp() }
        }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        RubyCell(
            text = "“",
            reading = "",
            color = color,
            style = style,
            rubyStyle = rubyStyle,
            rubyHeight = rubyHeight,
            baselineTop = baselineTop,
        )
        furigana.forEach { segment ->
            val cellColor = if (segment.isHighlight) highlightColor else color
            if (segment.reading.isEmpty()) {
                segment.text.forEach { ch ->
                    RubyCell(
                        text = ch.toString(),
                        reading = "",
                        color = cellColor,
                        style = style,
                        rubyStyle = rubyStyle,
                        rubyHeight = rubyHeight,
                        baselineTop = baselineTop,
                    )
                }
            } else {
                RubyCell(
                    text = segment.text,
                    reading = segment.reading,
                    color = cellColor,
                    style = style,
                    rubyStyle = rubyStyle,
                    rubyHeight = rubyHeight,
                    baselineTop = baselineTop,
                )
            }
        }
        RubyCell(
            text = "”",
            reading = "",
            color = color,
            style = style,
            rubyStyle = rubyStyle,
            rubyHeight = rubyHeight,
            baselineTop = baselineTop,
        )
    }
}

@Composable
private fun RubyCell(
    text: String,
    reading: String,
    color: Color,
    style: TextStyle,
    rubyStyle: TextStyle,
    rubyHeight: Dp,
    baselineTop: Dp,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.height(rubyHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (reading.isNotEmpty()) {
                Text(
                    text = reading,
                    style = rubyStyle,
                    color = color,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        Text(
            text = text,
            style = style,
            color = color,
            modifier = Modifier.paddingFromBaseline(top = baselineTop),
        )
    }
}
