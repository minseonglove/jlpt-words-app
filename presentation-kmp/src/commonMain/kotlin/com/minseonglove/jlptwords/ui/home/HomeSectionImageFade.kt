package com.minseonglove.jlptwords.ui.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 홈 섹션 배경 라쿤 일러스트의 가로 페이드 마스크.
 * 디자인의 흰색 그라데이션 오버레이를 알파 페이드로 재현해, 종이 질감 배경을 가리지 않으면서
 * 텍스트 영역 쪽으로 일러스트가 자연스럽게 사라지게 한다.
 * [stops] 는 (가로 위치 0f~1f, 알파) 쌍으로 이미지 좌표 기준이다.
 */
internal fun Modifier.horizontalAlphaFade(
    vararg stops: Pair<Float, Float>,
): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush =
                    Brush.horizontalGradient(
                        *stops
                            .map { (position, alpha) ->
                                position to Color.Black.copy(alpha = alpha)
                            }.toTypedArray(),
                    ),
                blendMode = BlendMode.DstIn,
            )
        }
