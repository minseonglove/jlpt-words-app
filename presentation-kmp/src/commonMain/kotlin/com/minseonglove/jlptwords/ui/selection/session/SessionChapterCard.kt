package com.minseonglove.jlptwords.ui.selection.session

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.extension.formatElapsedTime
import com.minseonglove.jlptwords.ui.base.DashedDivider
import com.minseonglove.jlptwords.ui.study.StudySessionCardItem
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ic_session_card_background
import jlptwords.presentation_kmp.generated.resources.ic_stamp_accurate
import jlptwords.presentation_kmp.generated.resources.ic_stamp_complete
import jlptwords.presentation_kmp.generated.resources.ic_stamp_progress
import jlptwords.presentation_kmp.generated.resources.ic_stamp_speed
import jlptwords.presentation_kmp.generated.resources.session_card_accuracy
import jlptwords.presentation_kmp.generated.resources.session_card_completion_count
import jlptwords.presentation_kmp.generated.resources.session_card_elapsed_time
import jlptwords.presentation_kmp.generated.resources.session_card_no_data
import jlptwords.presentation_kmp.generated.resources.session_card_progress_label
import jlptwords.presentation_kmp.generated.resources.session_card_progress_percentage
import jlptwords.presentation_kmp.generated.resources.session_card_word_range
import jlptwords.presentation_kmp.generated.resources.session_selection_card_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionChapterCard(
    item: StudySessionCardItem,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val black = MolluTheme.colorScheme.black
    val red = MolluTheme.colorScheme.highlightRed

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MolluTheme.colorScheme.white)
                .paint(
                    painter = painterResource(Res.drawable.ic_session_card_background),
                    contentScale = ContentScale.Crop,
                    alpha = 0.4f,
                ).border(width = 0.5.dp, color = black, shape = RectangleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = stringResource(Res.string.session_selection_card_title, position),
                    style = MolluTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                    color = black,
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.session_card_word_range, item.startNumber, item.endNumber),
                    style = MolluTheme.typography.caption1,
                    color = black,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Column(
                modifier =
                    Modifier
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                StampSlot(
                    visible = item.hasSpeedStamp,
                    res = Res.drawable.ic_stamp_speed,
                    modifier = Modifier.width(42.dp).height(28.dp),
                )
                StampSlot(
                    visible = item.hasAccurateStamp,
                    res = Res.drawable.ic_stamp_accurate,
                    modifier = Modifier.width(42.dp).height(28.dp),
                )
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VerticalDivider(
                    thickness = 0.25.dp,
                    color = black,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("確", style = MolluTheme.typography.caption1, color = black)
                    Text("認", style = MolluTheme.typography.caption1, color = black)
                }
                VerticalDivider(
                    thickness = 0.25.dp,
                    color = black,
                )
                Box(Modifier.size(45.dp), contentAlignment = Alignment.Center) {
                    when (item.mainStamp) {
                        MainStamp.COMPLETE -> {
                            Image(
                                painterResource(Res.drawable.ic_stamp_complete),
                                null,
                                modifier = Modifier.fillMaxHeight(),
                                contentScale = ContentScale.Fit,
                            )
                        }

                        MainStamp.PROGRESS -> {
                            Image(
                                painterResource(Res.drawable.ic_stamp_progress),
                                null,
                                modifier = Modifier.fillMaxHeight(),
                                contentScale = ContentScale.Fit,
                            )
                        }

                        MainStamp.NONE -> {
                            Unit
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.25.dp, color = black)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(Res.string.session_card_progress_label),
                    style = MolluTheme.typography.caption1,
                    color = black,
                )
                Text(
                    stringResource(Res.string.session_card_progress_percentage, item.progress),
                    style = MolluTheme.typography.caption1,
                    color = black,
                )
            }
            Spacer(Modifier.height(4.dp))
            ProgressBar(progress = item.progress / 100f, color = black)
            Spacer(Modifier.height(16.dp))
            DashedDivider(color = black)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    value = item.completionCount.toString(),
                    label = stringResource(Res.string.session_card_completion_count),
                    valueColor = black,
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    value =
                        if (item.accuracy > 0) {
                            "${item.accuracy}%"
                        } else {
                            stringResource(Res.string.session_card_no_data)
                        },
                    label = stringResource(Res.string.session_card_accuracy),
                    valueColor = red,
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    value =
                        if (item.elapsedTimeSeconds > 0) {
                            item.elapsedTimeSeconds.formatElapsedTime()
                        } else {
                            stringResource(Res.string.session_card_no_data)
                        },
                    label = stringResource(Res.string.session_card_elapsed_time),
                    valueColor = red,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * 접힌 스택에서 최상단 카드 뒤에 깔리는 배킹 카드용 자리표시자.
 * 드러나는 영역이 카드 하단 띠뿐이라 [SessionChapterCard] 의 바깥 테두리·배경만 그린다.
 */
@Composable
fun SessionChapterCardStackPlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MolluTheme.colorScheme.white)
                .paint(
                    painter = painterResource(Res.drawable.ic_session_card_background),
                    contentScale = ContentScale.Crop,
                    alpha = 0.4f,
                ).border(width = 0.5.dp, color = MolluTheme.colorScheme.black, shape = RectangleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                }
                // 스택 뒤에 깔린 장식용 카드라 읽을 내용이 없다. 스크린리더는 최상단 카드로 스택을 연다.
                .clearAndSetSemantics { },
    )
}

@Composable
private fun StampSlot(
    visible: Boolean,
    res: DrawableResource,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (visible) {
            Image(
                painterResource(res),
                null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    color: Color,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .border(0.5.dp, color, RectangleShape),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .background(color),
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            color = valueColor,
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = MolluTheme.typography.caption2, color = MolluTheme.colorScheme.black)
    }
}
