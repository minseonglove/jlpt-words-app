package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/**
 * 완료 화면 대형 헤드라인(mollu, 디자인 2016:4021 / 2016:4292).
 * 48sp Black 텍스트 + 텍스트 폭만큼의 검정 밑줄(Line_Bold_Continual).
 */
@Composable
fun CompletionHeadline(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style =
                TextStyle(
                    fontFamily = MolluTheme.typography.display.fontFamily,
                    fontSize = 48.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Black,
                ),
            color = MolluTheme.colorScheme.black,
            maxLines = 1,
            softWrap = false,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MolluTheme.colorScheme.black),
        )
    }
}

/** 완료 화면 부제목(head2 Bold, 중앙). */
@Composable
fun CompletionTitle(text: String) {
    Text(
        text = text,
        style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
        color = MolluTheme.colorScheme.black,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    )
}

/** 완료 화면 통계 한 줄의 디자인 높이(body1 행간). */
private val StatRowHeight = 24.dp

/**
 * 완료 화면 통계 한 줄. 좌측 라벨(기본색) + 우측 값(색은 [value] 에 포함).
 *
 * [trailing] 은 줄의 오른쪽 끝을 기준으로 세로 중앙에 놓이며 줄 크기에 영향을 주지 않는다.
 * 줄보다 크거나 줄 바깥에 놓이는 장식(도장 등)을 위한 자리로, offset·requiredSize 로 넘겨 그린다.
 */
@Composable
fun CompletionStatRow(
    label: String,
    value: AnnotatedString,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        // Compose 는 단일 행에서 lineHeight 여백을 잘라내므로(LineHeightStyle.Trim.Both)
        // 텍스트만으로는 디자인의 24dp 행 높이가 나오지 않는다.
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = StatRowHeight),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
            )
            Text(
                text = value,
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            )
        }
        if (trailing != null) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd,
                content = { trailing() },
            )
        }
    }
}
