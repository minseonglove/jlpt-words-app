package com.minseonglove.jlptwords.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.ui.base.RubyExampleText
import com.minseonglove.jlptwords.ui.base.buildHighlightedExample
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.home_today_example_title
import jlptwords.presentation_kmp.generated.resources.ic_raccoon_example
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 라쿤 일러스트를 섹션 위로 끌어올린 양. 섹션 클리핑을 같은 만큼 위로 열어 둔다. */
private val RaccoonTopOverflow = 62.dp

/**
 * 홈 '오늘의 예문' 섹션(Figma Daily_Sentence). 라쿤 일러스트를 섹션 배경 우측에 크게 깔고
 * (상단만 열어 둔 섹션 경계 클리핑 + 좌측 페이드), 예문 카드를 위에 얹는다. 카드 클릭 시 단어
 * 상세로 이동한다.
 * 일러스트는 우측 가장자리 기준으로 배치해 화면 폭이 디자인(393)과 달라도 오른쪽에 붙는다.
 */
@Composable
fun TodayExampleSection(
    example: DailyExample,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 162.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clipToBoundsOpenTop(RaccoonTopOverflow),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_raccoon_example),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = 0.8f,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 116.dp, y = -RaccoonTopOverflow)
                        .wrapContentSize(Alignment.TopEnd, unbounded = true)
                        .size(width = 343.dp, height = 429.dp)
                        .horizontalAlphaFade(
                            0f to 0.55f,
                            0.09f to 1f,
                        ),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 40.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_today_example_title),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
            )
            TodayExampleCard(
                example = example,
                onClick = onClick,
            )
        }
    }
}

/**
 * 위쪽으로 [openTop] 만큼 넓힌 영역에 콘텐츠를 클리핑한다. 섹션 위로 올라간 일러스트는 평소에는
 * 스크롤 뷰포트 밖이라 보이지 않다가, iOS 오버스크롤로 콘텐츠가 밀려 내려오면 잘리지 않은 채 드러난다.
 */
private fun Modifier.clipToBoundsOpenTop(openTop: Dp): Modifier =
    drawWithContent {
        clipRect(top = -openTop.toPx()) {
            this@drawWithContent.drawContent()
        }
    }

@Composable
private fun TodayExampleCard(
    example: DailyExample,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MolluTheme.colorScheme.white)
                .border(width = 0.5.dp, color = MolluTheme.colorScheme.black)
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (example.furigana.isNotEmpty()) {
            RubyExampleText(
                furigana = example.furigana,
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                highlightColor = MolluTheme.colorScheme.highlightRed,
            )
        } else {
            Text(
                text =
                    buildHighlightedExample(
                        sentence = example.japanese,
                        highlightColor = MolluTheme.colorScheme.highlightRed,
                    ),
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = example.korean,
            style = MolluTheme.typography.caption1,
            color = MolluTheme.colorScheme.sub,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun TodayExampleSectionPreview() {
    MolluTheme {
        TodayExampleSection(
            example =
                DailyExample(
                    kanji = "肝要",
                    pronunciation = "かんよう",
                    japanese = "何事も、最後まで諦めない姿勢が*肝要*だ",
                    korean = "무슨 일이든, 끝까지 포기하지 않는 자세가 중요하다.",
                ),
            onClick = {},
        )
    }
}
