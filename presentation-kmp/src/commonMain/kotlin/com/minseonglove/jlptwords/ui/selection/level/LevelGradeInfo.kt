package com.minseonglove.jlptwords.ui.selection.level

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.minseonglove.jlptwords.entity.LevelSummary
import com.minseonglove.jlptwords.extension.formatWithCommas
import com.minseonglove.jlptwords.ui.theme.MolluFontFamily
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.level_page_card_study_progress
import jlptwords.presentation_kmp.generated.resources.level_page_card_total_sessions
import jlptwords.presentation_kmp.generated.resources.level_page_card_total_words
import org.jetbrains.compose.resources.stringResource

// 카드 큰 레벨명(40sp) — mollu 타입 스케일에 없는 사이즈라 인라인 정의.
private val GradeNumberStyle =
    TextStyle(
        fontFamily = MolluFontFamily,
        fontSize = 40.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em,
        fontWeight = FontWeight.Bold,
    )

/**
 * 급수카드 정보 블록. 큰 레벨명 + 부제목 + 통계 3종(장/단어/진행률). 진행률 % 만 빨강 강조.
 * 통계는 Figma 처럼 2개(장·단어) + 1개(진행률) 로 줄바꿈.
 */
@Composable
internal fun LevelGradeInfo(
    summary: LevelSummary,
    modifier: Modifier = Modifier,
) {
    val riseOffsetPx = rememberLevelChangeRiseOffset()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimatedContent(
            targetState = summary.level,
            transitionSpec = {
                levelChangeTransform(
                    delayMillis = LEVEL_CHANGE_DELAY_HEADLINE_MS,
                    riseOffsetPx = riseOffsetPx,
                )
            },
        ) { level ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = level.name,
                    style = GradeNumberStyle,
                    color = MolluTheme.colorScheme.black,
                )
                Text(
                    text = stringResource(level.subtitleResource()),
                    style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
                    color = MolluTheme.colorScheme.black,
                )
            }
        }
        AnimatedContent(
            targetState = summary,
            transitionSpec = {
                levelChangeTransform(
                    delayMillis = LEVEL_CHANGE_DELAY_STATS_MS,
                    riseOffsetPx = riseOffsetPx,
                )
            },
        ) { target ->
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    GradeStat(
                        value = target.sessionCount.toString(),
                        label = stringResource(Res.string.level_page_card_total_sessions),
                    )
                    GradeStat(
                        value = target.wordCount.formatWithCommas(),
                        label = stringResource(Res.string.level_page_card_total_words),
                    )
                }
                GradeStat(
                    value = "${(target.sessionProgress * 100).toInt()}%",
                    label = stringResource(Res.string.level_page_card_study_progress),
                    valueColor = MolluTheme.colorScheme.highlightRed,
                )
            }
        }
    }
}

@Composable
private fun GradeStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MolluTheme.colorScheme.black,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
        Text(
            text = label,
            style = MolluTheme.typography.caption1,
            color = MolluTheme.colorScheme.black,
        )
    }
}
