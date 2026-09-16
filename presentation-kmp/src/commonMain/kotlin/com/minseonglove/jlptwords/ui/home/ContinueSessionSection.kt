package com.minseonglove.jlptwords.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.ContinueSessionStatus
import com.minseonglove.jlptwords.entity.ContinueStudySession
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.ui.base.rangeLabel
import com.minseonglove.jlptwords.ui.base.statusLabel
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.home_continue_study_title
import jlptwords.presentation_kmp.generated.resources.ic_raccoon_continue
import jlptwords.presentation_kmp.generated.resources.session_card_progress_label
import jlptwords.presentation_kmp.generated.resources.session_card_progress_percentage
import jlptwords.presentation_kmp.generated.resources.test_session_card_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * 홈 '계속 학습하기' 섹션(Figma Keep_Learning). 라쿤 일러스트를 섹션 배경 좌측에 크게 깔고
 * (섹션 경계 클리핑 + 우측 페이드), 고정폭 세션 카드를 우측에 배치한다.
 */
@Composable
fun ContinueSessionSection(
    item: ContinueStudySession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 172.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clipToBounds(),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_raccoon_continue),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = 0.8f,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-137).dp, y = 4.dp)
                        .wrapContentSize(Alignment.TopStart, unbounded = true)
                        .size(width = 374.dp, height = 467.dp)
                        .horizontalAlphaFade(
                            0.53f to 1f,
                            0.89f to 0f,
                        ),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 40.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = stringResource(Res.string.home_continue_study_title),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
                modifier = Modifier.fillMaxWidth(),
            )
            ContinueSessionCard(
                item = item,
                onClick = onClick,
                modifier = Modifier.width(252.dp),
            )
        }
    }
}

@Composable
private fun ContinueSessionCard(
    item: ContinueStudySession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(MolluTheme.colorScheme.white)
                .border(width = 0.5.dp, color = MolluTheme.colorScheme.black)
                .clickable(onClick = onClick)
                .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(Res.string.test_session_card_title, item.sessionIndex + 1),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.session.rangeLabel(),
                style = MolluTheme.typography.caption1,
                color = MolluTheme.colorScheme.black,
                modifier = Modifier.padding(bottom = 2.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.status.statusLabel(),
                style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                color =
                    when (item.status) {
                        ContinueSessionStatus.COMPLETED -> MolluTheme.colorScheme.black
                        else -> MolluTheme.colorScheme.highlightRed
                    },
                modifier = Modifier.padding(bottom = 1.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.session_card_progress_label),
                    style = MolluTheme.typography.caption1,
                    color = MolluTheme.colorScheme.black,
                )
                Text(
                    text = stringResource(Res.string.session_card_progress_percentage, item.progress),
                    style = MolluTheme.typography.caption1,
                    color = MolluTheme.colorScheme.black,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .border(width = 0.5.dp, color = MolluTheme.colorScheme.black),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction = (item.progress / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(MolluTheme.colorScheme.black),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ContinueSessionSectionPreview() {
    MolluTheme {
        ContinueSessionSection(
            item =
                ContinueStudySession(
                    session =
                        StudySession(
                            id = 1,
                            level = JLPTLevel.N1,
                            type = SessionType.NORMAL,
                            startNumber = 1,
                            endNumber = 10,
                            wordsSize = 10,
                        ),
                    sessionIndex = 0,
                    status = ContinueSessionStatus.NOT_STARTED,
                    progress = 0,
                ),
            onClick = {},
        )
    }
}
