package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.bg_pattern_paper
import jlptwords.presentation_kmp.generated.resources.ic_button_background
import jlptwords.presentation_kmp.generated.resources.ic_toolbar_background
import org.jetbrains.compose.resources.imageResource
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * 상단바 배경(가로로 긴 검정 텍스처). [MolluTopBar] 계열에서 사용한다.
 *
 * 검정 텍스처 이미지를 콘텐츠 뒤에 [drawBehind] 로 그린다. drawBehind 는 그리기 단계에서만 동작하고
 * 레이아웃 측정에 전혀 관여하지 않으므로, 배경 이미지 원본 높이가 상단바 높이를 늘리지 않는다(높이는
 * 콘텐츠가 결정). 이미지는 영역을 꽉 채우도록 중앙 Crop 하며, 로드 전/실패 시 검정 단색이 fallback 으로 보인다.
 */
@Composable
fun Modifier.molluToolbarBackground(): Modifier {
    val image = imageResource(Res.drawable.ic_toolbar_background)
    return this
        .background(MolluTheme.colorScheme.black)
        .drawBehind { drawImageCropToBounds(image) }
}

/**
 * 풀폭 버튼 배경(검정 텍스처). [MolluBottomButton] 계열에서 사용한다.
 * [molluToolbarBackground] 와 동일하게 [drawBehind] 로 그려 레이아웃 높이에 영향을 주지 않는다.
 */
@Composable
fun Modifier.molluButtonBackground(): Modifier {
    val image = imageResource(Res.drawable.ic_button_background)
    return this
        .background(MolluTheme.colorScheme.black)
        .drawBehind { drawImageCropToBounds(image) }
}

/**
 * [image] 를 그리기 영역([DrawScope.size])에 중앙 Crop(ContentScale.Crop 과 동일)으로 그린다.
 * src 영역을 잘라 dst 를 영역과 정확히 일치시키므로 영역 밖으로 넘치지 않아 별도 클립이 필요 없다.
 */
private fun DrawScope.drawImageCropToBounds(image: ImageBitmap) {
    val dstWidth = size.width.roundToInt()
    val dstHeight = size.height.roundToInt()
    if (dstWidth <= 0 || dstHeight <= 0) return
    // 영역을 채우는 데 필요한 scale(큰 쪽 기준) → 영역에 보이는 src 크기 = dst / scale
    val scale = maxOf(size.width / image.width, size.height / image.height)
    val srcWidth = (size.width / scale).roundToInt().coerceIn(1, image.width)
    val srcHeight = (size.height / scale).roundToInt().coerceIn(1, image.height)
    val srcX = ((image.width - srcWidth) / 2).coerceAtLeast(0)
    val srcY = ((image.height - srcHeight) / 2).coerceAtLeast(0)
    drawImage(
        image = image,
        srcOffset = IntOffset(srcX, srcY),
        srcSize = IntSize(srcWidth, srcHeight),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(dstWidth, dstHeight),
    )
}

/**
 * 풀스크린 종이질감 배경. 종이 한 장이 영역 폭에 맞게 깔리고 세로는 중앙만 보인다
 * (Figma 시안이 화면 각 섹션에 이미지를 object-cover 로 채운 것과 같은 배치).
 *
 * [androidx.compose.foundation.verticalScroll] 등 스크롤 modifier '뒤'에 체이닝하면 배경이 스크롤 콘텐츠
 * 영역에 그려져 콘텐츠와 함께 움직이므로 "종이를 넘기는" 느낌을 준다. 반대로 스크롤 modifier '앞'에 두면
 * 뷰포트에 고정된다.
 *
 * 흰 배경 위에 올리는 전제이며 [alpha] 로 질감 강도를 조절한다(기본 0.4: 텍스트 가독성을 해치지 않는 은은한 수준).
 */
@Composable
fun Modifier.molluPaperBackground(alpha: Float = 0.4f): Modifier {
    val texture = imageResource(Res.drawable.bg_pattern_paper)
    return this.drawBehind { drawPaperToBounds(texture, alpha) }
}

/**
 * 세로 스크롤 콘텐츠용 종이질감 배경. [molluPaperBackground] 를 뷰포트와 스크롤 콘텐츠에 한 겹씩
 * 깔고 그 사이에 [verticalScroll] 을 끼운다. 콘텐츠 겹이 스크롤을 따라 움직여 종이를 넘기는 느낌을
 * 주고, iOS 오버스크롤(bounce)로 콘텐츠가 밀려난 자리는 뷰포트 겹이 메운다.
 *
 * 두 겹이 겹쳐 질감이 진해지지 않도록 콘텐츠 겹 아래에 불투명 배경색을 깐다. 뷰포트 겹 아래에는
 * 화면 Scaffold 의 containerColor 가 비치므로, 그 색이 여기서 쓰는 backgroundNormal 과 같아야
 * 밀려난 자리와 콘텐츠의 배경색이 일치한다.
 */
@Composable
fun Modifier.molluPaperVerticalScroll(
    state: ScrollState,
    alpha: Float = 0.4f,
): Modifier =
    this
        .molluPaperBackground(alpha)
        .verticalScroll(state)
        .background(MolluTheme.colorScheme.backgroundNormal)
        .molluPaperBackground(alpha)

/**
 * [image] 를 그리기 영역 폭에 맞춰 균등 스케일(가로 왜곡 없음)해 세로 중앙에 놓는다.
 * 영역이 종이 한 장보다 길면 모자란 만큼 위아래로 이어 붙이므로, 스크롤 콘텐츠처럼 길어져도
 * 종이를 세로로 늘리거나 입자를 굵히지 않는다.
 *
 * 이어 붙일 때는 한 장 걸러 상하를 뒤집어(scaleY = -1) 맞닿는 두 변이 서로 같은 그림이 되게 한다.
 * 종이 위·아래 변의 농도가 서로 다르더라도 이은 자리에 밝기 단차가 생기지 않는다.
 */
private fun DrawScope.drawPaperToBounds(
    image: ImageBitmap,
    alpha: Float,
) {
    val sheetWidth = size.width
    if (sheetWidth <= 0f || size.height <= 0f || image.width <= 0) return
    val sheetHeight = sheetWidth * image.height / image.width
    val dstSize = IntSize(sheetWidth.roundToInt(), sheetHeight.roundToInt())
    // 영역이 종이보다 짧으면 음수가 되어 첫 장이 위로 밀리고, 결과적으로 세로 중앙만 보인다.
    val firstTop = (size.height - sheetHeight) / 2f

    clipRect {
        var index = floor(-firstTop / sheetHeight).toInt()
        while (true) {
            val top = firstTop + index * sheetHeight
            if (top >= size.height) break
            val dstOffset = IntOffset(0, top.roundToInt())
            if (index.mod(2) == 0) {
                drawImage(image = image, dstOffset = dstOffset, dstSize = dstSize, alpha = alpha)
            } else {
                withTransform({
                    scale(
                        scaleX = 1f,
                        scaleY = -1f,
                        pivot = Offset(sheetWidth / 2f, top + sheetHeight / 2f),
                    )
                }) {
                    drawImage(image = image, dstOffset = dstOffset, dstSize = dstSize, alpha = alpha)
                }
            }
            index++
        }
    }
}
