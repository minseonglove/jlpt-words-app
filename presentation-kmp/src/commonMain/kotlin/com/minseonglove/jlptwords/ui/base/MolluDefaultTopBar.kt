package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.study_ad_zero_label
import jlptwords.presentation_kmp.generated.resources.study_settings_label
import jlptwords.presentation_kmp.generated.resources.word_detail_back_content_description
import org.jetbrains.compose.resources.stringResource

/**
 * 좌측 뒤로/타이틀, 우측 광고 제거 남은시간(AD)·설정(밑줄 텍스트) 액션을 포함하는 기본 상단바(mollu).
 * 학습·단어 검색 등 동일한 상단바를 쓰는 화면들이 타이틀만 바꿔 공유한다. [MolluTopBar] 를 감싼다.
 * [onBackClick] 이 null 이면 뒤로가기 아이콘을 표시하지 않는다(홈 등 루트 화면용).
 * [onSettingsClick] 이 null 이면 설정 텍스트를 표시하지 않는다(환경 설정 화면용).
 */
@Composable
fun MolluDefaultTopBar(
    title: String,
    adZeroRemainingHours: Int,
    onBackClick: (() -> Unit)?,
    onAdZeroClick: () -> Unit,
    onSettingsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    onTitleClick: (() -> Unit)? = null,
) {
    MolluTopBar(
        title = title,
        onBackClick = onBackClick,
        modifier = modifier,
        titleStyle = MolluTheme.typography.jpHead.copy(fontWeight = FontWeight.Bold),
        contentPadding = contentPadding,
        actionsArrangement = Arrangement.spacedBy(16.dp),
        backContentDescription = stringResource(Res.string.word_detail_back_content_description),
        // Figma Title_Only 변형(뒤로가기 없는 루트 화면)은 타이틀 밑줄을 함께 표시한다.
        isTitleUnderlined = onBackClick == null,
        onTitleClick = onTitleClick,
    ) {
        TopBarActionText(
            text =
                if (adZeroRemainingHours > 0) {
                    adZeroRemainingHours.toString()
                } else {
                    stringResource(Res.string.study_ad_zero_label)
                },
            fontWeight = FontWeight.Medium,
            onClick = onAdZeroClick,
        )
        if (onSettingsClick != null) {
            TopBarActionText(
                text = stringResource(Res.string.study_settings_label),
                fontWeight = FontWeight.Normal,
                onClick = onSettingsClick,
            )
        }
    }
}

/**
 * 상단바 우측의 밑줄 텍스트 액션.
 *
 * 밑줄을 [TextDecoration.Underline] 이 아니라 직접 긋는다. 밑줄 위치는 폰트가 들고 있는 메트릭이라,
 * "AD"(라틴)와 "설정"(한글)이 시스템 폰트 폴백으로 갈리면 두 밑줄이 서로 다른 높이에 그려진다.
 * 베이스라인에서 같은 간격으로 긋고 베이스라인끼리 맞춰야 한 줄로 보인다.
 */
@Composable
private fun RowScope.TopBarActionText(
    text: String,
    fontWeight: FontWeight,
    onClick: () -> Unit,
) {
    val color = MolluTheme.colorScheme.white
    var baseline by remember { mutableFloatStateOf(0f) }
    Text(
        text = text,
        style = MolluTheme.typography.body1.copy(fontWeight = fontWeight),
        color = color,
        onTextLayout = { baseline = it.firstBaseline },
        modifier =
            Modifier
                .alignByBaseline()
                .drawBehind {
                    val thickness = 1.dp.toPx()
                    drawRect(
                        color = color,
                        topLeft = Offset(0f, baseline + UnderlineGap.toPx()),
                        size = Size(size.width, thickness),
                    )
                }.clickable(onClick = onClick),
    )
}

private val UnderlineGap = 2.dp

@Preview
@Composable
private fun MolluDefaultTopBarPreview() {
    MolluTheme {
        Column {
            MolluDefaultTopBar(
                title = "3장",
                adZeroRemainingHours = 12,
                onBackClick = {},
                onAdZeroClick = {},
                onSettingsClick = {},
            )
            MolluDefaultTopBar(
                title = "단어 찾기",
                adZeroRemainingHours = 0,
                onBackClick = {},
                onAdZeroClick = {},
                onSettingsClick = {},
            )
        }
    }
}
