package com.minseonglove.jlptwords.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.OverviewValue
import com.minseonglove.jlptwords.entity.StudyOverview
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.home_overview_streak_days
import jlptwords.presentation_kmp.generated.resources.home_overview_studied_words
import jlptwords.presentation_kmp.generated.resources.home_overview_total_attendance
import jlptwords.presentation_kmp.generated.resources.home_overview_total_progress
import jlptwords.presentation_kmp.generated.resources.home_overview_total_study_time
import jlptwords.presentation_kmp.generated.resources.home_overview_unit_days
import jlptwords.presentation_kmp.generated.resources.home_overview_unit_hours
import jlptwords.presentation_kmp.generated.resources.home_overview_unit_minutes
import jlptwords.presentation_kmp.generated.resources.home_overview_unit_percent
import jlptwords.presentation_kmp.generated.resources.home_overview_unit_words
import jlptwords.presentation_kmp.generated.resources.home_study_overview_title
import jlptwords.presentation_kmp.generated.resources.ic_raccoon_stat
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * 홈 '학습 현황' 섹션(Figma Statement). 라쿤 일러스트를 우측에 크게 깔되 디자인처럼
 * 우측 가장자리가 화면 밖으로 살짝 잘려나가게 배치하고, 수치 행들을 좌측에 나열한다.
 * 자정 이후 변경된 수치만 빨간색으로 표시한다.
 */
@Composable
fun StudyOverviewSection(
    overview: StudyOverview,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 316.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clipToBounds(),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_raccoon_stat),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = 0.9f,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 28.dp, y = 12.dp)
                        .wrapContentSize(Alignment.TopEnd, unbounded = true)
                        .size(width = 225.dp, height = 281.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_study_overview_title),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StudyOverviewRow(
                    label = stringResource(Res.string.home_overview_streak_days),
                    item = overview.streakDays,
                    unit = stringResource(Res.string.home_overview_unit_days),
                )
                StudyOverviewRow(
                    label = stringResource(Res.string.home_overview_total_attendance),
                    item = overview.totalAttendanceDays,
                    unit = stringResource(Res.string.home_overview_unit_days),
                )
                StudyTimeOverviewRow(
                    studyTimeSeconds = overview.totalStudyTimeSeconds,
                )
                StudyOverviewRow(
                    label = stringResource(Res.string.home_overview_studied_words),
                    item = overview.studiedWordCount,
                    unit = stringResource(Res.string.home_overview_unit_words),
                )
                StudyOverviewRow(
                    label = stringResource(Res.string.home_overview_total_progress),
                    item = overview.totalProgressPercent,
                    unit = stringResource(Res.string.home_overview_unit_percent),
                )
            }
        }
    }
}

/** 총 학습시간 행. 1시간 미만이면 분 단위로, 이상이면 시간 단위(내림)로 표시한다. */
@Composable
private fun StudyTimeOverviewRow(
    studyTimeSeconds: OverviewValue,
) {
    val hours = studyTimeSeconds.value / SECONDS_PER_HOUR
    val isHourUnit = hours >= 1
    // copy() 는 매번 새 인스턴스라 remember 없이 넘기면 StudyOverviewRow 가 항상 재구성된다.
    val displayValue =
        remember(studyTimeSeconds) {
            studyTimeSeconds.copy(
                value = if (isHourUnit) hours else studyTimeSeconds.value / SECONDS_PER_MINUTE,
            )
        }
    StudyOverviewRow(
        label = stringResource(Res.string.home_overview_total_study_time),
        item = displayValue,
        unit =
            stringResource(
                if (isHourUnit) {
                    Res.string.home_overview_unit_hours
                } else {
                    Res.string.home_overview_unit_minutes
                },
            ),
    )
}

@Composable
private fun StudyOverviewRow(
    label: String,
    item: OverviewValue,
    unit: String,
) {
    Row {
        Text(
            text = label,
            style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            color = MolluTheme.colorScheme.black,
            modifier = Modifier.alignByBaseline(),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.value.toString(),
            style = MolluTheme.typography.display.copy(fontWeight = FontWeight.Bold),
            color =
                if (item.isUpdatedToday) {
                    MolluTheme.colorScheme.highlightRed
                } else {
                    MolluTheme.colorScheme.black
                },
            modifier = Modifier.alignByBaseline(),
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = unit,
            style = MolluTheme.typography.caption1,
            color = MolluTheme.colorScheme.black,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

@Preview
@Composable
private fun StudyOverviewSectionPreview() {
    MolluTheme {
        StudyOverviewSection(
            overview =
                StudyOverview(
                    streakDays = OverviewValue(2, true),
                    totalAttendanceDays = OverviewValue(25, true),
                    totalStudyTimeSeconds = OverviewValue(65 * SECONDS_PER_HOUR, false),
                    studiedWordCount = OverviewValue(315, false),
                    totalProgressPercent = OverviewValue(14, false),
                ),
        )
    }
}
