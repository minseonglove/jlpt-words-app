package com.minseonglove.jlptwords.ui.selection.level

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.LevelSummary
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.bg_pattern_white
import org.jetbrains.compose.resources.painterResource

/** 급수카드 테두리 두께. 탭 테두리와 겹치도록 카드를 끌어올릴 때도 재사용(LevelSelectionScreen). */
internal val LevelGradeCardBorderWidth = 1.dp

// 카드 하단을 점진적으로 투명 처리해 산 배경으로 자연스럽게 녹아들게 한다(하드한 하단 테두리 제거).
private const val CARD_BOTTOM_FADE_START = 0.72f

/**
 * 급수카드. 흰 종이질감 배경(60%) + 검정 테두리, 급수 정보 좌측,
 * 레벨별 너구리 일러스트 우측(상단 정렬로 크게, 아래로 흰색 페이드).
 * 카드 하단은 opacity 그라데이션으로 페이드아웃되어 산 배경과 이어진다.
 */
@Composable
internal fun LevelGradeCard(
    summary: LevelSummary,
    modifier: Modifier = Modifier,
) {
    val riseOffsetPx = rememberLevelChangeRiseOffset()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                CARD_BOTTOM_FADE_START to Color.Black,
                                1f to Color.Transparent,
                            ),
                        blendMode = BlendMode.DstIn,
                    )
                }.clipToBounds()
                .border(LevelGradeCardBorderWidth, MolluTheme.colorScheme.black),
    ) {
        Image(
            painter = painterResource(Res.drawable.bg_pattern_white),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.6f,
            modifier = Modifier.matchParentSize(),
        )
        AnimatedContent(
            targetState = summary.level,
            transitionSpec = {
                levelChangeTransform(
                    delayMillis = LEVEL_CHANGE_DELAY_RACCOON_MS,
                    riseOffsetPx = riseOffsetPx,
                )
            },
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.78f),
        ) { level ->
            Image(
                painter = painterResource(level.raccoonResource()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopCenter,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush =
                                    Brush.verticalGradient(
                                        0.62f to Color.Transparent,
                                        1f to Color.White,
                                    ),
                            )
                        },
            )
        }
        LevelGradeInfo(
            summary = summary,
            modifier = Modifier.padding(24.dp),
        )
    }
}
