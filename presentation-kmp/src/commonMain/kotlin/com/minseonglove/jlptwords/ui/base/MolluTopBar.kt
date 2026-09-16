package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/**
 * mollu 디자인 상단바. 검정 텍스처 배경 + 좌측 뒤로/타이틀, 우측 actions 슬롯.
 * 상태바 영역을 덮고(statusBarsPadding) edge-to-edge 로 동작한다.
 * [onBackClick] 이 null 이면 뒤로가기 아이콘을 표시하지 않는다(홈 등 루트 화면용).
 *
 * 화면별 차이(타이틀 타이포, 패딩, actions 간격)는 슬롯/파라미터로 흡수한다.
 * 광고 제거/설정 액션을 포함하는 기본 구성은 [MolluDefaultTopBar] 가
 * 이 컴포넌트를 감싸 제공한다.
 */
@Composable
fun MolluTopBar(
    title: String,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MolluTheme.typography.display.copy(fontWeight = FontWeight.Bold),
    contentPadding: PaddingValues = PaddingValues(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
    actionsArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    backContentDescription: String? = null,
    isTitleUnderlined: Boolean = false,
    onTitleClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val titleColor = MolluTheme.colorScheme.white
    // 시스템 폰트는 스크립트(라틴/한글/일본어)마다 자연 행간이 달라, 타이틀 글자에 따라 상단바 높이가
    // 화면마다 달라진다. 타이틀 영역을 디자인 토큰의 행간만큼으로 고정해 높이를 통일한다.
    // 자연 행간이 더 큰 스크립트에서 글자가 잘리지 않도록 실제 텍스트는 unbounded 로 재고 가운데 정렬한다.
    val titleHeight = with(LocalDensity.current) { titleStyle.lineHeight.toDp() }
    val resolvedTitleStyle =
        titleStyle.copy(
            lineHeightStyle =
                LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None,
                ),
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .molluToolbarBackground()
                .statusBarsPadding()
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (onBackClick != null) {
                Icon(
                    imageVector = Icons.Back,
                    contentDescription = backContentDescription,
                    tint = MolluTheme.colorScheme.white,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clickable(onClick = onBackClick),
                )
            }
            Text(
                text = title,
                style = resolvedTitleStyle,
                color = titleColor,
                modifier =
                    Modifier
                        .then(
                            if (onTitleClick != null) {
                                Modifier.clickable(onClick = onTitleClick)
                            } else {
                                Modifier
                            },
                        ).then(
                            if (isTitleUnderlined) {
                                // Figma Title_Only 변형: 타이틀 너비만큼 흰색 밑줄을 깐다(홈 등 루트 화면용).
                                // 레이아웃 높이에 영향을 주지 않도록 drawBehind 로 그린다.
                                Modifier.drawBehind {
                                    val thickness = 1.dp.toPx()
                                    drawRect(
                                        color = titleColor,
                                        topLeft = Offset(0f, size.height - thickness),
                                        size = Size(size.width, thickness),
                                    )
                                }
                            } else {
                                Modifier
                            },
                        ).height(titleHeight)
                        .wrapContentHeight(unbounded = true),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = actionsArrangement,
            content = actions,
        )
    }
}
