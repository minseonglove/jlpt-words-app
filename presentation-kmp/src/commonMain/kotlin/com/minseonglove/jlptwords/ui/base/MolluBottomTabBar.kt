package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.bottom_tab_home
import jlptwords.presentation_kmp.generated.resources.bottom_tab_search
import jlptwords.presentation_kmp.generated.resources.bottom_tab_study
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class MolluBottomTab {
    HOME,
    STUDY,
    SEARCH,
}

/**
 * mollu 디자인 하단 탭바(홈 / 단어학습 / 단어 검색).
 * 선택 탭은 검정 텍스처 배경 + 흰 글자, 비선택 탭은 흰 배경 + 검정 글자로 표시한다.
 * 하단 내비게이션바 영역까지 검정 텍스처가 이어지도록 배경 뒤에 패딩을 둔다.
 */
@Composable
fun MolluBottomTabBar(
    selectedTab: MolluBottomTab,
    onTabClick: (MolluBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .molluButtonBackground()
                .navigationBarsPadding(),
    ) {
        MolluBottomTab.entries.forEachIndexed { index, tab ->
            val isSelected = tab == selectedTab
            // 인접한 비선택 탭이 각자 세로선을 그리면 맞닿는 지점이 두 배 두께가 되므로,
            // 시작선은 왼쪽 이웃이 선을 그리지 않을 때만 그린다.
            val drawStartLine = index == 0 || MolluBottomTab.entries[index - 1] == selectedTab
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .then(
                            if (isSelected) {
                                Modifier
                            } else {
                                Modifier
                                    .background(MolluTheme.colorScheme.white)
                                    .tabStroke(
                                        color = MolluTheme.colorScheme.black,
                                        width = 0.5.dp,
                                        drawStartLine = drawStartLine,
                                    )
                            },
                        ).clickable(enabled = isSelected.not()) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onTabClick(tab)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(tab.labelRes()),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    color =
                        if (isSelected) {
                            MolluTheme.colorScheme.white
                        } else {
                            MolluTheme.colorScheme.black
                        },
                )
            }
        }
    }
}

private fun Modifier.tabStroke(
    color: Color,
    width: Dp,
    drawStartLine: Boolean,
): Modifier =
    drawBehind {
        val stroke = width.toPx()
        drawRect(color = color, topLeft = Offset.Zero, size = Size(size.width, stroke))
        drawRect(
            color = color,
            topLeft = Offset(0f, size.height - stroke),
            size = Size(size.width, stroke),
        )
        drawRect(
            color = color,
            topLeft = Offset(size.width - stroke, 0f),
            size = Size(stroke, size.height),
        )
        if (drawStartLine) {
            drawRect(color = color, topLeft = Offset.Zero, size = Size(stroke, size.height))
        }
    }

private fun MolluBottomTab.labelRes(): StringResource =
    when (this) {
        MolluBottomTab.HOME -> Res.string.bottom_tab_home
        MolluBottomTab.STUDY -> Res.string.bottom_tab_study
        MolluBottomTab.SEARCH -> Res.string.bottom_tab_search
    }

@Preview
@Composable
private fun MolluBottomTabBarPreview() {
    MolluTheme {
        MolluBottomTabBar(
            selectedTab = MolluBottomTab.HOME,
            onTabClick = {},
        )
    }
}
