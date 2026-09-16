package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.study.WordPageCardItem
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 종이 찢기 스와이프 카드. 가로 드래그로 단어를 처리한다.
 * - 왼쪽으로 끌면 [onSwipedLeft](알겠음), 오른쪽으로 끌면 [onSwipedRight](모름).
 * - 폭의 15% 를 넘겨 끌거나 빠르게 튕기면 넘어가고, 둘 다 아니면 스프링으로 원위치 복귀.
 * - 드래그에 따라 상단(천공선)을 축으로 카드가 기울며(rotationZ) 뜯겨 나가고
 *   뒤에 깔린 다음 단어 카드([nextItem], 마지막 단어면 빈 종이 받침)가 드러난다.
 * - 카드 컴포지션은 단어(item.id) 단위로 키잉되어, 단어가 바뀌면 새 종이가 초기 위치에서 등장한다.
 */
@Composable
fun SwipeableWordCard(
    item: WordPageCardItem,
    nextItem: WordPageCardItem?,
    isRevealed: Boolean,
    isExampleVisible: Boolean,
    isWordTTSPlaying: Boolean,
    isExampleTTSPlaying: Boolean,
    enabled: Boolean,
    onHelpClick: () -> Unit,
    onDetailClick: () -> Unit,
    onCopyClick: () -> Unit,
    onWordTTSClick: () -> Unit,
    onExampleTTSClick: (String) -> Unit,
    onExampleShowClick: () -> Unit,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    modifier: Modifier = Modifier,
    nextCardBottomButton: @Composable () -> Unit,
    bottomButton: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }.coerceAtLeast(1f)
        val threshold = widthPx * SWIPE_DISTANCE_FRACTION
        val backGestureEdgePx = with(LocalDensity.current) { BackGestureEdgeWidth.toPx() }
        val flingVelocityPx = with(LocalDensity.current) { FlingVelocityThreshold.toPx() }

        // 현재 카드를 찢는 동안 뒤에 보이는 다음 단어 카드. 뜯기 전에 미리 조작되지 않도록 콜백은 모두 no-op 이다.
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MolluTheme.colorScheme.backgroundNormal)
                    .molluPaperBackground(alpha = 0.6f),
        ) {
            if (nextItem != null) {
                NextWordPreview(
                    item = nextItem,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                )
                nextCardBottomButton()
            }
        }

        // 드래그 상태(offsetX 등)가 단어와 생명주기를 같이해, 단어가 바뀌는 프레임에 새 카드가 초기 위치에서 그려진다.
        key(item.id) {
            val offsetX = remember { Animatable(0f) }
            val scope = rememberCoroutineScope()
            var isSwipingOut by remember { mutableStateOf(false) }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // 상단 천공선을 축으로 카드가 뜯겨 나가도록 회전축을 위쪽 중앙에 둔다.
                            transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0f)
                            translationX = offsetX.value
                            rotationZ = tearRotationDegrees(offsetX.value, widthPx)
                        }.background(MolluTheme.colorScheme.backgroundNormal)
                        .molluPaperBackground(alpha = 0.6f)
                        .tearSwipeGesture(
                            enabled = enabled,
                            isSwipingOut = isSwipingOut,
                            offsetX = offsetX,
                            scope = scope,
                            widthPx = widthPx,
                            thresholdPx = threshold,
                            backGestureEdgePx = backGestureEdgePx,
                            flingVelocityPx = flingVelocityPx,
                            onTearStart = { isSwipingOut = true },
                            onSwipedLeft = onSwipedLeft,
                            onSwipedRight = onSwipedRight,
                        ),
            ) {
                StudyWordSection(
                    item = item,
                    isRevealed = isRevealed,
                    isExampleVisible = isExampleVisible,
                    isWordTTSPlaying = isWordTTSPlaying,
                    isExampleTTSPlaying = isExampleTTSPlaying,
                    onHelpClick = onHelpClick,
                    onDetailClick = onDetailClick,
                    onCopyClick = onCopyClick,
                    onWordTTSClick = onWordTTSClick,
                    onExampleTTSClick = onExampleTTSClick,
                    onExampleShowClick = onExampleShowClick,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                )
                bottomButton()
            }
        }
    }
}

/**
 * 카드를 가로로 끌어 뜯어내는 제스처.
 *
 * 화면 밖으로 나가는 애니메이션이 끝난 뒤에야 단어를 넘기므로, 그동안 다시 잡히지 않도록
 * [isSwipingOut] 으로 입력을 닫는다. 속도는 포인터 좌표가 아니라 카드가 실제로 움직인 양으로
 * 재는데, 포인터 좌표에는 graphicsLayer 의 translationX 가 함께 반영돼 손가락이 이동한
 * 거리와 어긋나기 때문이다.
 */
private fun Modifier.tearSwipeGesture(
    enabled: Boolean,
    isSwipingOut: Boolean,
    offsetX: Animatable<Float, AnimationVector1D>,
    scope: CoroutineScope,
    widthPx: Float,
    thresholdPx: Float,
    backGestureEdgePx: Float,
    flingVelocityPx: Float,
    onTearStart: () -> Unit,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
): Modifier =
    pointerInput(enabled, isSwipingOut) {
        if (!enabled || isSwipingOut) return@pointerInput
        // 화면 가장자리에서 시작한 드래그는 OS back 제스처 몫이다.
        var isBackGestureEdgeDrag = false
        // 속도는 카드가 실제로 움직인 양으로 잰다. 포인터 좌표에는 graphicsLayer 의
        // translationX 가 함께 반영돼 손가락이 이동한 거리와 어긋난다.
        var draggedX = 0f
        val velocityTracker = VelocityTracker()
        detectHorizontalDragGestures(
            onDragStart = { start ->
                isBackGestureEdgeDrag = start.x < backGestureEdgePx
                draggedX = 0f
                velocityTracker.resetTracking()
            },
            onDragCancel = {
                isBackGestureEdgeDrag = false
                velocityTracker.resetTracking()
            },
            onHorizontalDrag = { change, dragAmount ->
                if (!isBackGestureEdgeDrag) {
                    draggedX += dragAmount
                    velocityTracker.addPosition(change.uptimeMillis, Offset(draggedX, 0f))
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount)
                    }
                }
            },
            onDragEnd = {
                val outcome =
                    decideSwipeOutcome(
                        isBackGestureEdgeDrag = isBackGestureEdgeDrag,
                        velocityX = velocityTracker.calculateVelocity().x,
                        flingVelocityPx = flingVelocityPx,
                        offsetX = offsetX.value,
                        thresholdPx = thresholdPx,
                    )
                isBackGestureEdgeDrag = false

                if (outcome == SwipeOutcome.STAY) {
                    scope.launch {
                        offsetX.animateTo(0f, spring())
                    }
                } else {
                    onTearStart()
                    val isLeft = outcome == SwipeOutcome.TEAR_LEFT
                    scope.launch {
                        // 뜯기 애니메이션이 끝난 뒤에만 단어를 넘긴다. 취소되는 경우는
                        // 컴포지션 이탈(화면 회전·이동)뿐이고, 그때는 카드 위치도 함께
                        // 초기화되므로 넘기면 사용자가 보지 않은 단어가 처리된다.
                        val target =
                            if (isLeft) {
                                -widthPx * TEAR_OUT_WIDTH_FACTOR
                            } else {
                                widthPx * TEAR_OUT_WIDTH_FACTOR
                            }
                        offsetX.animateTo(target, tween(TEAR_OUT_DURATION_MS))
                        if (isLeft) onSwipedLeft() else onSwipedRight()
                    }
                }
            },
        )
    }

/** 드래그가 끝났을 때 카드가 할 일. */
internal enum class SwipeOutcome {
    /** 왼쪽으로 뜯어낸다. */
    TEAR_LEFT,

    /** 오른쪽으로 뜯어낸다. */
    TEAR_RIGHT,

    /** 제자리로 돌아온다. */
    STAY,
}

/**
 * 드래그를 놓았을 때 카드를 넘길지 되돌릴지 정한다.
 *
 * 속도를 거리보다 먼저 본다. 짧게 튕기는 동작도 넘기려는 의도로 받아들이기 위해서고,
 * 끝에서 반대로 튕기면 마지막 방향을 따른다. 화면 가장자리에서 시작한 드래그는 OS back
 * 제스처 몫이라 카드를 움직이지 않는다.
 */
internal fun decideSwipeOutcome(
    isBackGestureEdgeDrag: Boolean,
    velocityX: Float,
    flingVelocityPx: Float,
    offsetX: Float,
    thresholdPx: Float,
): SwipeOutcome =
    when {
        isBackGestureEdgeDrag -> SwipeOutcome.STAY
        velocityX <= -flingVelocityPx -> SwipeOutcome.TEAR_LEFT
        velocityX >= flingVelocityPx -> SwipeOutcome.TEAR_RIGHT
        offsetX < -thresholdPx -> SwipeOutcome.TEAR_LEFT
        offsetX > thresholdPx -> SwipeOutcome.TEAR_RIGHT
        else -> SwipeOutcome.STAY
    }

/** 끌려간 거리에 비례해 카드가 기우는 각도. 화면 폭만큼 끌면 [MAX_ROTATION_DEGREES] 가 된다. */
internal fun tearRotationDegrees(
    offsetX: Float,
    widthPx: Float,
): Float = (offsetX / widthPx) * MAX_ROTATION_DEGREES

/** 찢는 중인 카드 뒤로 드러나는 다음 단어. 뜯기 전에 조작되면 안 되므로 콜백을 받지 않는다. */
@Composable
private fun NextWordPreview(
    item: WordPageCardItem,
    modifier: Modifier = Modifier,
) {
    StudyWordSection(
        item = item,
        isRevealed = false,
        isExampleVisible = false,
        isWordTTSPlaying = false,
        isExampleTTSPlaying = false,
        onHelpClick = {},
        onDetailClick = {},
        onCopyClick = {},
        onWordTTSClick = {},
        onExampleTTSClick = {},
        onExampleShowClick = {},
        modifier = modifier,
    )
}

// 디자인 기준 완만한 기울기(약 7~8°). 회전축이 상단이라 같은 각도라도 카드 하단이 더 크게 흔들린다.
private const val MAX_ROTATION_DEGREES = 8f

/** 카드를 넘기는 데 필요한 이동 거리(화면 폭 대비). */
private const val SWIPE_DISTANCE_FRACTION = 0.15f

/**
 * 이 속도(dp/초) 이상으로 튕기면 이동 거리가 [SWIPE_DISTANCE_FRACTION] 에 못 미쳐도 카드를 넘긴다.
 * 천천히 끄는 동작(대략 500dp/초 이하)과 구분되도록 잡았다.
 */
private val FlingVelocityThreshold = 800.dp

/**
 * 화면 좌측 가장자리에서 OS 가 back 제스처로 먼저 잡아가는 폭(iOS UIScreenEdgePanGestureRecognizer 기준).
 * 이 안에서 시작한 드래그까지 카드가 따라가면, 제스처가 back 으로 확정되는 순간 카드만 어정쩡하게
 * 밀려 있는 상태로 종료 시트가 겹친다.
 */
private val BackGestureEdgeWidth = 20.dp

/** 뜯긴 카드가 화면 밖으로 사라지는 거리(화면 폭 대비)와 걸리는 시간. */
private const val TEAR_OUT_WIDTH_FACTOR = 1.5f
private const val TEAR_OUT_DURATION_MS = 300
