package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/**
 * 학습 진행바(mollu). 좌측 "N개 완료 / M개 남음", 우측 경과시간 "MM:SS".
 * 하단은 디자인의 뜯는 선(Line_Dettachable)에 맞춰 점선으로 그린다.
 *
 * [elapsedTimeText] 는 1초마다 바뀌므로 값이 아니라 람다로 받는다. 호출부가 값을 읽으면
 * 학습 화면 최상위부터 매초 재구성되지만, 람다면 읽기가 이 컴포저블 안으로 한정된다.
 */
@Composable
fun StudyProgressBar(
    completedText: String,
    elapsedTimeText: () -> String,
    modifier: Modifier = Modifier,
) {
    val lineColor = MolluTheme.colorScheme.sub

    Column(modifier = modifier.fillMaxWidth().molluPaperBackground(alpha = 0.6f)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = completedText,
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.sub,
            )
            Text(
                text = elapsedTimeText(),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.sub,
            )
        }
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp),
        ) {
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = size.height,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
            )
        }
    }
}

@Preview
@Composable
private fun StudyProgressBarPreview() {
    MolluTheme {
        StudyProgressBar(
            completedText = "4개 완료 / 6개 남음",
            elapsedTimeText = { "00:19" },
        )
    }
}
