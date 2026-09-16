package com.minseonglove.jlptwords.ui.selection.session

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/** 러버밴드 풀-투-클로즈 진행 상태. 스크롤 커넥션이 갱신하고 카드 레이아웃이 읽는다. */
@Stable
internal class ChapterPullToCloseState {
    var offset by mutableFloatStateOf(0f)
    var isActive by mutableStateOf(false)

    fun reset() {
        offset = 0f
        isActive = false
    }
}

/**
 * 카드 경계에 맞춘 정착 위치 중 [scroll] 에 가장 가까운 값.
 *
 * 카드 m 장이 상단 스택에 들어간 지점이 정착점인데, 스택이 다 차기 전에는 간격이
 * stride - [stackOffsetPx], 찬 뒤에는 stride 라서 등간격이 아니다. 나눗셈으로 바로
 * 구할 수 없어 후보를 훑는다 (fling 한 번에 한 번, 카드 수만큼).
 */
internal fun nearestCardSnap(
    scroll: Int,
    scrollRange: IntRange,
    stride: Float,
    stackOffsetPx: Float,
    maxStackSlots: Int,
): Int {
    val minScroll = scrollRange.first
    val maxScroll = scrollRange.last

    fun settleValue(cardsInStack: Int): Int =
        (minScroll + (cardsInStack * stride - minOf(cardsInStack, maxStackSlots) * stackOffsetPx))
            .toInt()
            .coerceIn(minScroll, maxScroll)

    val upperBound =
        (((maxScroll - minScroll) + maxStackSlots * stackOffsetPx) / stride).toInt() + 1
    var snapped = minScroll
    var bestDistance = Int.MAX_VALUE
    for (cardsInStack in 0..upperBound) {
        val candidate = settleValue(cardsInStack)
        val distance = abs(candidate - scroll)
        if (distance < bestDistance) {
            bestDistance = distance
            snapped = candidate
        }
    }
    return snapped
}

/** 러버밴드 풀 오프셋. 많이 당겨 둘수록 저항이 커져 같은 드래그로도 덜 늘어난다. */
internal fun rubberBandOffset(
    currentOffset: Float,
    dragDelta: Float,
    maxOffsetPx: Float,
    rubberBandFactor: Float,
): Float {
    val resistance = rubberBandFactor * (1f - (currentOffset / maxOffsetPx).coerceIn(0f, 1f))
    return (currentOffset + dragDelta * resistance).coerceIn(0f, maxOffsetPx)
}

/**
 * 펼친 챕터의 스크롤을 그 챕터 영역으로 가두는 커넥션.
 *
 * onPreScroll 에서 경계 초과분을 소비해 자연스럽게 멈추고, 상단 경계를 넘겨 당기면
 * 러버밴드 풀-투-클로즈로 접힘을 트리거한다. fling 이 멎을 때는 카드 경계로 스냅한다.
 * animateScrollTo 는 ScrollState 를 직접 제어하므로 이 커넥션을 거치지 않아 충돌하지 않는다.
 *
 * @param scrollRangeOf 해당 챕터에서 허용되는 스크롤 값 범위(min..max)
 * @param cardHeightOf 해당 챕터의 측정된 카드 높이(px). 아직 측정 전이면 0
 */
@Composable
internal fun rememberChapterScrollConnection(
    expandedChapterNumber: Int?,
    config: AnimationTuningConfig,
    scrollState: ScrollState,
    pullState: ChapterPullToCloseState,
    pullThresholdPx: Float,
    pullMaxOffsetPx: Float,
    expandedSpacingPx: Float,
    stackOffsetPx: Float,
    scrollRangeOf: (chapterNumber: Int) -> IntRange,
    cardHeightOf: (chapterNumber: Int) -> Int,
    isCollapsing: () -> Boolean,
    isExpandSettled: () -> Boolean,
    onCollapseRequest: (chapterNumber: Int) -> Unit,
): NestedScrollConnection {
    val scrollCancelScope = rememberCoroutineScope()

    return remember(expandedChapterNumber, config) {
        ChapterScrollConnection(
            expandedChapterNumber = expandedChapterNumber,
            config = config,
            scrollState = scrollState,
            pullState = pullState,
            pullThresholdPx = pullThresholdPx,
            pullMaxOffsetPx = pullMaxOffsetPx,
            expandedSpacingPx = expandedSpacingPx,
            stackOffsetPx = stackOffsetPx,
            scrollRangeOf = scrollRangeOf,
            cardHeightOf = cardHeightOf,
            isCollapsing = isCollapsing,
            isExpandSettled = isExpandSettled,
            onCollapseRequest = onCollapseRequest,
            scrollCancelScope = scrollCancelScope,
        )
    }
}

/**
 * 펼친 챕터의 스크롤을 그 챕터 영역 안으로 가두는 커넥션.
 *
 * 경계 초과분을 onPreScroll 에서 미리 소비해 자연스럽게 멈추고, 상단 경계를 넘겨 당기면
 * 러버밴드 풀-투-클로즈로 접힘을 트리거한다. fling 이 멎을 때는 카드 경계로 스냅한다.
 */
internal class ChapterScrollConnection(
    private val expandedChapterNumber: Int?,
    private val config: AnimationTuningConfig,
    private val scrollState: ScrollState,
    private val pullState: ChapterPullToCloseState,
    private val pullThresholdPx: Float,
    private val pullMaxOffsetPx: Float,
    private val expandedSpacingPx: Float,
    private val stackOffsetPx: Float,
    private val scrollRangeOf: (chapterNumber: Int) -> IntRange,
    private val cardHeightOf: (chapterNumber: Int) -> Int,
    private val isCollapsing: () -> Boolean,
    private val isExpandSettled: () -> Boolean,
    private val onCollapseRequest: (chapterNumber: Int) -> Unit,
    private val scrollCancelScope: CoroutineScope,
) : NestedScrollConnection {
    override suspend fun onPreFling(available: Velocity): Velocity {
        if (expandedChapterNumber == null) return Velocity.Zero

        // Pull-to-close 릴리즈 처리
        if (pullState.isActive) {
            pullState.isActive = false
            if (pullState.offset >= pullThresholdPx) {
                // 임계값 초과 → 챕터 접힘 트리거
                onCollapseRequest(expandedChapterNumber)
            } else {
                // 임계값 미만 → 스프링 바운스백
                animate(
                    initialValue = pullState.offset,
                    targetValue = 0f,
                    animationSpec =
                        spring(
                            dampingRatio = config.springDampingRatio,
                            stiffness = config.springStiffness,
                        ),
                ) { value, _ ->
                    pullState.offset = value
                }
                pullState.offset = 0f
            }
            return available
        }

        val range = scrollRangeOf(expandedChapterNumber)
        val currentScroll = scrollState.value

        // 경계에서 fling 속도 소비 → 유령 fling 방지
        if (available.y < 0f && currentScroll >= range.last) return available
        if (available.y > 0f && currentScroll <= range.first) return available

        return Velocity.Zero
    }

    // 펼친 챕터 카드 스크롤이 멈출 때 가장 가까운 카드 경계로 스냅.
    // 카드 한 장이 상단 스택으로 들어가는 스크롤 거리(stride)의 정수배 위치로 정렬한다.
    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity {
        if (expandedChapterNumber == null) return Velocity.Zero
        // 펼침 애니메이션 진행 중 / 접힘 중 / 풀-투-클로즈 중에는 스냅하지 않음
        if (!isExpandSettled() || pullState.isActive || isCollapsing()) {
            return Velocity.Zero
        }

        val stride = cardHeightOf(expandedChapterNumber) + expandedSpacingPx
        if (stride <= 0f) return Velocity.Zero

        val range = scrollRangeOf(expandedChapterNumber)
        val minScroll = range.first
        val maxScroll = range.last

        val value = scrollState.value
        val snapped =
            nearestCardSnap(
                scroll = value,
                scrollRange = minScroll..maxScroll,
                stride = stride,
                stackOffsetPx = stackOffsetPx,
                maxStackSlots = (config.maxStackSize - 1).coerceAtLeast(0),
            )

        if (snapped != value) {
            scrollState.animateScrollTo(
                snapped,
                tween(config.settleDurationMs, easing = EaseInOutCubic),
            )
        }
        return available
    }

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (expandedChapterNumber == null) return Offset.Zero

        // 풀 중 역방향 드래그: 되돌린 양은 소비로 보고하지 않고 스크롤로 흘려보낸다.
        // 아래쪽 카드가 따라 올라가, 이만큼 더 움직이면 챕터가 접힌다는 것을 보여준다.
        if (pullState.isActive && available.y < 0f) {
            val drain = minOf(-available.y, pullState.offset)
            pullState.offset -= drain
            if (pullState.offset == 0f) pullState.isActive = false
            return Offset(0f, available.y + drain)
        }

        val range = scrollRangeOf(expandedChapterNumber)
        val currentScroll = scrollState.value
        val delta = available.y
        // delta < 0: 위로 스와이프 → scrollState.value 증가 (아래쪽 콘텐츠 노출)
        // delta > 0: 아래로 스와이프 → scrollState.value 감소 (위쪽 콘텐츠 노출)

        if (delta < 0f) {
            // 아래쪽 콘텐츠를 더 보려는 스크롤 → maxScroll 초과 방지
            val remaining = (range.last - currentScroll).toFloat()
            if (remaining <= 0f) {
                if (source == NestedScrollSource.SideEffect) {
                    // fling 중 경계 도달 → fling 취소하여 유령 fling 방지
                    scrollCancelScope.launch {
                        scrollState.scrollTo(scrollState.value)
                    }
                }
                return Offset(0f, delta)
            } else if (-delta > remaining) {
                return Offset(0f, delta + remaining)
            }
        } else if (delta > 0f) {
            // 위쪽 콘텐츠를 더 보려는 스크롤 → minScroll 미만 방지
            val remaining = (currentScroll - range.first).toFloat()
            if (remaining <= 0f) {
                if (source == NestedScrollSource.UserInput &&
                    !isCollapsing() && isExpandSettled()
                ) {
                    // 러버밴드 풀-투-클로즈
                    pullState.isActive = true
                    pullState.offset =
                        rubberBandOffset(
                            currentOffset = pullState.offset,
                            dragDelta = delta,
                            maxOffsetPx = pullMaxOffsetPx,
                            rubberBandFactor = config.pullRubberBandFactor,
                        )
                }
                if (source == NestedScrollSource.SideEffect) {
                    scrollCancelScope.launch {
                        scrollState.scrollTo(scrollState.value)
                    }
                }
                return Offset(0f, delta)
            } else if (delta > remaining) {
                return Offset(0f, delta - remaining)
            }
        }

        return Offset.Zero
    }
}
