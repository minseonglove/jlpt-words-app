package com.minseonglove.jlptwords.ui.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

private val TrackWidth = 46.dp
private val TrackHeight = 26.dp
private val ThumbSize = 18.dp
private val ThumbPadding = 4.dp

/**
 * mollu 디자인 토글 스위치.
 * ON 은 검정 트랙 + 회색 썸, OFF 는 흰 트랙 + 검정 테두리 + 흰 썸으로 표시한다.
 * 시각 크기(46x26)는 디자인을 따르되, 터치 영역은 최소 48dp 권장 크기로 확장한다.
 */
@Composable
fun MolluSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    // 애니메이션 값을 컴포지션 단계에서 읽으면 전환 150ms 동안 매 프레임 재구성된다.
    // State 를 그대로 두고 draw/layout 람다 안에서 읽어 다시 그리기·재배치만 일어나게 한다.
    val trackColor =
        animateColorAsState(
            targetValue =
                if (checked) {
                    MolluTheme.colorScheme.black
                } else {
                    MolluTheme.colorScheme.white
                },
            animationSpec = tween(durationMillis = 150),
        )
    val thumbOffset =
        animateDpAsState(
            targetValue =
                if (checked) {
                    TrackWidth - ThumbSize - ThumbPadding * 2
                } else {
                    0.dp
                },
            animationSpec = tween(durationMillis = 150),
        )

    Box(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .clickable(
                    interactionSource = null,
                    indication = null,
                ) {
                    hapticFeedback.performHapticFeedback(
                        if (checked) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn,
                    )
                    onCheckedChange(checked.not())
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = TrackWidth, height = TrackHeight)
                    .clip(CircleShape)
                    .drawBehind { drawRect(trackColor.value) }
                    .border(
                        width = 1.dp,
                        color = MolluTheme.colorScheme.black,
                        shape = CircleShape,
                    ).padding(ThumbPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(x = thumbOffset.value.roundToPx(), y = 0) }
                        .size(ThumbSize)
                        .clip(CircleShape)
                        .background(
                            if (checked) {
                                MolluTheme.colorScheme.sub
                            } else {
                                MolluTheme.colorScheme.white
                            },
                        ).border(
                            width = 1.dp,
                            color =
                                if (checked) {
                                    MolluTheme.colorScheme.sub
                                } else {
                                    MolluTheme.colorScheme.black
                                },
                            shape = CircleShape,
                        ),
            )
        }
    }
}

@Preview
@Composable
private fun MolluSwitchPreview() {
    MolluTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MolluSwitch(
                checked = true,
                onCheckedChange = {},
            )
            MolluSwitch(
                checked = false,
                onCheckedChange = {},
            )
        }
    }
}
