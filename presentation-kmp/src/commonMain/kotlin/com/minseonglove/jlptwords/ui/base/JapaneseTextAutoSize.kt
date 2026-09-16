package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/**
 * 일본어 단어(한자)를 `maxLines = 1` 로 그릴 때 한 줄에 들어가는 가장 큰 글자 크기를 고른다.
 * 단어는 1자부터 10자까지 폭 편차가 커서, 가장 긴 단어에 맞춰 크기를 고정하면
 * 대부분을 차지하는 짧은 단어까지 함께 작아진다.
 */
@Composable
fun rememberJpDisplayAutoSize(): TextAutoSize =
    rememberOneLineAutoSize(
        maxFontSize = MolluTheme.typography.jpDisplay.fontSize,
        minFontSize = JpDisplayMinFontSize,
    )

/** 발음 표기용. [rememberJpDisplayAutoSize] 와 같되 jpHead 크기를 상한으로 삼는다. */
@Composable
fun rememberJpHeadAutoSize(): TextAutoSize =
    rememberOneLineAutoSize(
        maxFontSize = MolluTheme.typography.jpHead.fontSize,
        minFontSize = JpHeadMinFontSize,
    )

@Composable
private fun rememberOneLineAutoSize(
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
): TextAutoSize =
    remember(maxFontSize, minFontSize) {
        TextAutoSize.StepBased(
            minFontSize = minFontSize,
            maxFontSize = maxFontSize,
            stepSize = AutoSizeStepSize,
        )
    }

/**
 * 축소 하한. 폭이 모자라도 이보다 작아지지 않는다 — 한자는 획이 뭉개지면 학습에 쓸 수 없어,
 * 한 줄 유지보다 읽히는 크기를 앞에 둔다.
 */
private val JpDisplayMinFontSize = 24.sp
private val JpHeadMinFontSize = 20.sp

/** 1sp 미만의 차이는 눈에 띄지 않아, 그만큼 탐색 횟수를 줄인다. */
private val AutoSizeStepSize = 1.sp
