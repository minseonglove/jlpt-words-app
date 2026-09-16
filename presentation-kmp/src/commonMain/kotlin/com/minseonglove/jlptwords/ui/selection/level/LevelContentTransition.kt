package com.minseonglove.jlptwords.ui.selection.level

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

internal const val LEVEL_CHANGE_DURATION_MS = 300

// 나가는 급수는 들어오는 급수보다 짧게 사라져 두 급수가 서로 밀치는 인상을 없앤다.
private const val LEVEL_CHANGE_EXIT_DURATION_MS = 200

// 시선이 탭에서 내려오는 순서(급수명 → 통계 → 일러스트)대로 지연을 준다.
internal const val LEVEL_CHANGE_DELAY_HEADLINE_MS = 0
internal const val LEVEL_CHANGE_DELAY_STATS_MS = 50
internal const val LEVEL_CHANGE_DELAY_RACCOON_MS = 90

private val LevelChangeRiseDistance = 20.dp

@Composable
internal fun rememberLevelChangeRiseOffset(): Int {
    val density = LocalDensity.current
    return remember(density) { with(density) { LevelChangeRiseDistance.roundToPx() } }
}

/** 급수 전환 시 카드 콘텐츠가 아래에서 페이드인하며 올라오는 전환. sizeTransform 을 끄지 않으면 컨테이너가 함께 출렁인다. */
internal fun levelChangeTransform(
    delayMillis: Int,
    riseOffsetPx: Int,
): ContentTransform =
    ContentTransform(
        targetContentEnter =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = LEVEL_CHANGE_DURATION_MS,
                        delayMillis = delayMillis,
                        easing = EaseInOutCubic,
                    ),
            ) +
                slideInVertically(
                    animationSpec =
                        tween(
                            durationMillis = LEVEL_CHANGE_DURATION_MS,
                            delayMillis = delayMillis,
                            easing = EaseInOutCubic,
                        ),
                    initialOffsetY = { riseOffsetPx },
                ),
        initialContentExit =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = LEVEL_CHANGE_EXIT_DURATION_MS,
                        easing = EaseInOutCubic,
                    ),
            ),
        sizeTransform = null,
    )
