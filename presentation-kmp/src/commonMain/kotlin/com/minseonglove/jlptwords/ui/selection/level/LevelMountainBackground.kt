package com.minseonglove.jlptwords.ui.selection.level

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.minseonglove.jlptwords.entity.JLPTLevel
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/**
 * 화면 레벨 산 일러스트(레벨별, 점선 경로·화살표 포함). 풀폭으로 그리며 하단 정렬.
 * 소스 webp 가 흰 배경이라 BlendMode.Multiply 로 합성해 흰 부분은 투명 처리(카드·배경 보존),
 * 산 형상만 카드/배경 위로 자연스럽게 떠오르게 한다(Figma mix-blend-multiply 재현).
 * 급수 전환은 alpha 를 나눠 그리는 방식으로 교차시킨다.
 * graphicsLayer alpha(Crossfade 등)로 감싸면 레이어가 분리돼 Multiply 대상이 부모가 아닌 빈 레이어가 된다.
 */
@Composable
internal fun LevelMountainBackground(
    level: JLPTLevel,
    modifier: Modifier = Modifier,
) {
    var currentLevel by remember { mutableStateOf(level) }
    var previousLevel by remember { mutableStateOf(level) }
    val crossfade = remember { Animatable(1f) }

    LaunchedEffect(level) {
        if (level == currentLevel) return@LaunchedEffect
        previousLevel = currentLevel
        currentLevel = level
        crossfade.snapTo(0f)
        crossfade.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = LEVEL_CHANGE_DURATION_MS,
                    easing = EaseInOutCubic,
                ),
        )
    }

    val currentImage = imageResource(currentLevel.mountainResource())
    val previousImage = imageResource(previousLevel.mountainResource())
    val progress = crossfade.value

    Canvas(modifier = modifier) {
        drawMountain(image = previousImage, alpha = 1f - progress)
        drawMountain(image = currentImage, alpha = progress)
    }
}

private fun DrawScope.drawMountain(
    image: ImageBitmap,
    alpha: Float,
) {
    if (alpha <= 0f || image.width == 0 || image.height == 0) return
    val scale = size.width / image.width.toFloat()
    val dstHeight = image.height * scale
    val topOffset = (size.height - dstHeight).roundToInt()
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(0, topOffset),
        dstSize = IntSize(size.width.roundToInt(), dstHeight.roundToInt()),
        alpha = alpha,
        blendMode = BlendMode.Multiply,
    )
}
