package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/** 비활성(`enabled = false`) 시 버튼 전체 투명도. */
private const val DISABLED_ALPHA = 0.5f

/**
 * mollu 디자인 풀폭 버튼. 검정 텍스처 배경 + 중앙 흰 텍스트.
 *
 * [height] 지정 시: 고정 높이 + 텍스트 중앙(내비게이션바 패딩 없음). BottomSheet 내부 버튼 등에 사용.
 * [height] 미지정 시: 콘텐츠 높이 + 하단 내비게이션바 패딩. 화면 최하단 풀폭 버튼에 사용.
 * [loading] true 면 텍스트 앞에 스피너를 표시한다(광고 로딩 등 비동기 작업 피드백용).
 * [enabled] false 면 클릭이 막히고 버튼 전체가 흐려진다.
 */
@Composable
fun MolluBottomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp? = null,
    textStyle: TextStyle = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
) {
    val hapticFeedback = LocalHapticFeedback.current
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height) else Modifier)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .molluButtonBackground()
                .clickable(enabled = enabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        val contentModifier =
            if (height != null) {
                Modifier
            } else {
                Modifier
                    .navigationBarsPadding()
                    .padding(vertical = 22.dp)
            }
        if (loading) {
            Row(
                modifier = contentModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MolluTheme.colorScheme.white,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = text,
                    style = textStyle,
                    color = MolluTheme.colorScheme.white,
                )
            }
        } else {
            Text(
                text = text,
                style = textStyle,
                color = MolluTheme.colorScheme.white,
                modifier = contentModifier,
            )
        }
    }
}
