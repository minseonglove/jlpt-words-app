package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/**
 * Figma `Line_Thin_Dashed` 스펙(stroke 0.5dp, dash 3 / gap 3)을 재현한 점선 구분선.
 * 기본 `HorizontalDivider` 는 점선을 지원하지 않아 [Canvas] + [PathEffect.dashPathEffect] 로 그린다.
 */
@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = MolluTheme.colorScheme.black,
    thickness: Dp = 0.5.dp,
    dashWidth: Dp = 3.dp,
    gapWidth: Dp = 3.dp,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(thickness),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            pathEffect =
                PathEffect.dashPathEffect(
                    floatArrayOf(dashWidth.toPx(), gapWidth.toPx()),
                    0f,
                ),
        )
    }
}
