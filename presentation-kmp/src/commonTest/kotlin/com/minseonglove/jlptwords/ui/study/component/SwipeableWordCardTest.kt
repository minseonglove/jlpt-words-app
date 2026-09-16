package com.minseonglove.jlptwords.ui.study.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 카드를 놓았을 때 넘길지 되돌릴지 정하는 규칙을 고정한다. */
class SwipeableWordCardTest {
    private val fling = 800f
    private val threshold = 100f

    private fun decide(
        velocityX: Float = 0f,
        offsetX: Float = 0f,
        isEdgeDrag: Boolean = false,
    ) = decideSwipeOutcome(
        isBackGestureEdgeDrag = isEdgeDrag,
        velocityX = velocityX,
        flingVelocityPx = fling,
        offsetX = offsetX,
        thresholdPx = threshold,
    )

    // ── 거리로 넘기기 ──

    @Test
    fun `임계 거리를 넘겨 끌면 그 방향으로 넘어간다`() {
        assertEquals(SwipeOutcome.TEAR_LEFT, decide(offsetX = -150f))
        assertEquals(SwipeOutcome.TEAR_RIGHT, decide(offsetX = 150f))
    }

    @Test
    fun `임계 거리에 못 미치면 제자리로 돌아온다`() {
        assertEquals(SwipeOutcome.STAY, decide(offsetX = -50f))
        assertEquals(SwipeOutcome.STAY, decide(offsetX = 50f))
    }

    @Test
    fun `임계 거리와 같으면 아직 넘어가지 않는다`() {
        assertEquals(SwipeOutcome.STAY, decide(offsetX = -threshold))
        assertEquals(SwipeOutcome.STAY, decide(offsetX = threshold))
    }

    // ── 속도로 넘기기 ──

    @Test
    fun `빠르게 튕기면 조금만 끌어도 넘어간다`() {
        assertEquals(SwipeOutcome.TEAR_LEFT, decide(velocityX = -1000f, offsetX = -5f))
        assertEquals(SwipeOutcome.TEAR_RIGHT, decide(velocityX = 1000f, offsetX = 5f))
    }

    @Test
    fun `임계 속도와 같아도 넘어간다`() {
        assertEquals(SwipeOutcome.TEAR_LEFT, decide(velocityX = -fling))
        assertEquals(SwipeOutcome.TEAR_RIGHT, decide(velocityX = fling))
    }

    @Test
    fun `천천히 끌면 속도로는 넘어가지 않는다`() {
        assertEquals(SwipeOutcome.STAY, decide(velocityX = -500f, offsetX = -50f))
    }

    // ── 속도가 거리를 이긴다 ──

    @Test
    fun `멀리 끌었어도 반대로 튕기면 튕긴 방향을 따른다`() {
        // 왼쪽으로 임계를 넘겨 끌어 두었지만 마지막에 오른쪽으로 튕겼다
        assertEquals(SwipeOutcome.TEAR_RIGHT, decide(velocityX = 1000f, offsetX = -150f))
        assertEquals(SwipeOutcome.TEAR_LEFT, decide(velocityX = -1000f, offsetX = 150f))
    }

    // ── OS back 제스처 ──

    @Test
    fun `화면 가장자리에서 시작한 드래그는 카드를 움직이지 않는다`() {
        assertEquals(SwipeOutcome.STAY, decide(velocityX = -2000f, offsetX = -500f, isEdgeDrag = true))
        assertEquals(SwipeOutcome.STAY, decide(velocityX = 2000f, offsetX = 500f, isEdgeDrag = true))
    }

    // ── 기울기 ──

    @Test
    fun `끌지 않으면 기울지 않는다`() {
        assertEquals(0f, tearRotationDegrees(offsetX = 0f, widthPx = 1000f))
    }

    @Test
    fun `끄는 방향으로 기운다`() {
        assertTrue(tearRotationDegrees(offsetX = -200f, widthPx = 1000f) < 0f)
        assertTrue(tearRotationDegrees(offsetX = 200f, widthPx = 1000f) > 0f)
    }

    @Test
    fun `두 배로 끌면 두 배로 기운다`() {
        val once = tearRotationDegrees(offsetX = 100f, widthPx = 1000f)
        val twice = tearRotationDegrees(offsetX = 200f, widthPx = 1000f)
        assertEquals(once * 2f, twice, absoluteTolerance = 0.0001f)
    }

    @Test
    fun `같은 거리라도 화면이 넓으면 덜 기운다`() {
        val narrow = tearRotationDegrees(offsetX = 200f, widthPx = 500f)
        val wide = tearRotationDegrees(offsetX = 200f, widthPx = 1000f)
        assertTrue(wide < narrow)
    }
}
