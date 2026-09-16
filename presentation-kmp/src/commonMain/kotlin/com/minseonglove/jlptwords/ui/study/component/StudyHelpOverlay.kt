package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ic_help
import jlptwords.presentation_kmp.generated.resources.study_help_close
import jlptwords.presentation_kmp.generated.resources.study_swipe_guide_known
import jlptwords.presentation_kmp.generated.resources.study_swipe_guide_unknown
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 하단 "뜻 보기" 버튼 높이. dim 영역에서 제외해 버튼이 어두워지지 않게 한다(Figma 733:6793). */
private val BottomButtonHeight = 72.dp

/** 손가락 가이드 이미지 크기(Figma: 프레임 폭의 약 50% = 196). */
private val FingerSize = 196.dp

/** 손가락 이미지 하단 여백(Figma: 오버레이 바닥에서 약 6). */
private val FingerBottomMargin = 6.dp

/**
 * 학습 화면 도움말 오버레이(mollu, Figma 733:6785 의 Overlay 733:6793).
 * 반투명 검정(50%) 위에 스와이프 방향 가이드를 표시한다. 왼쪽 = 아는 단어, 오른쪽 = 모르는 단어.
 * 손가락 이미지는 원본에서 `mix-blend-screen` 이라 검정 배경이 사라지고 흰 손가락만 비친다.
 * 에셋을 "RGB=흰색, 알파=명도" 로 전처리해 두어(= screen blend 와 동일) 일반 합성으로 그린다.
 * 하단 "뜻 보기" 버튼 영역은 어둡게 하지 않으며, 영역 어디든 탭하면 닫힌다.
 */
@Composable
fun StudyHelpOverlay(
    progressBarHeight: Dp,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onCloseClick,
                ),
    ) {
        // 반투명 dim — 하단 "뜻 보기" 버튼 영역은 제외
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = BottomButtonHeight)
                    .background(MolluTheme.colorScheme.black.copy(alpha = 0.5f)),
        )

        // 가려진 "도움말" 자리에 그대로 겹치도록, 진행바 높이에 단어 영역 위 여백을 더해 내린다.
        // 진행바 높이는 글꼴 크기 설정에 따라 변하므로 고정값을 쓰지 않는다.
        Text(
            text = stringResource(Res.string.study_help_close),
            style = MolluTheme.typography.caption1.copy(textDecoration = TextDecoration.Underline),
            color = MolluTheme.colorScheme.white,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        top = progressBarHeight + StudyWordSectionTopPadding,
                        start = 20.dp,
                    ),
        )

        // 손가락 가이드 + 좌우 안내 텍스트 (하단)
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = BottomButtonHeight + FingerBottomMargin)
                    .height(FingerSize),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_help),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .size(FingerSize),
            )
            GuideText(
                text = stringResource(Res.string.study_swipe_guide_known),
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp),
            )
            GuideText(
                text = stringResource(Res.string.study_swipe_guide_unknown),
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 40.dp),
            )
        }
    }
}

@Composable
private fun GuideText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
        color = MolluTheme.colorScheme.white,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun StudyHelpOverlayPreview() {
    MolluTheme {
        Box(modifier = Modifier.size(360.dp)) {
            StudyHelpOverlay(progressBarHeight = 68.dp, onCloseClick = {})
        }
    }
}
