package com.minseonglove.jlptwords.ui.selection.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 펼친 챕터의 스크롤 규칙을 고정한다. fling 이나 드래그 자체는 실행 없이 확인할 수 없으므로,
 * 정착 위치와 러버밴드 저항을 정하는 계산만 회귀를 잡아 둔다.
 */
class ChapterScrollConnectionTest {
    private val range = 0..1000
    private val stride = 116f // 카드 100 + 간격 16
    private val stackOffset = 4f
    private val maxStackSlots = 3 // maxStackSize 4 - 1

    private fun snap(scroll: Int) =
        nearestCardSnap(
            scroll = scroll,
            scrollRange = range,
            stride = stride,
            stackOffsetPx = stackOffset,
            maxStackSlots = maxStackSlots,
        )

    // ── 카드 경계 스냅 ──

    @Test
    fun `정착 지점에 있으면 그대로 둔다`() {
        assertEquals(0, snap(0))
        assertEquals(112, snap(112)) // 카드 1장이 스택에 들어간 자리
    }

    @Test
    fun `가까운 쪽 정착 지점으로 붙는다`() {
        assertEquals(112, snap(100))
        assertEquals(0, snap(40))
        assertEquals(224, snap(200))
    }

    @Test
    fun `스택이 차기 전에는 겹침만큼 덜 내려간 자리에 선다`() {
        // m 장이 스택에 들어가면 m * stackOffset 만큼 덜 내려간다
        assertEquals(1 * 116 - 1 * 4, snap(112))
        assertEquals(2 * 116 - 2 * 4, snap(224))
        assertEquals(3 * 116 - 3 * 4, snap(336))
    }

    @Test
    fun `스택이 찬 뒤에는 카드 간격만큼 균등하게 벌어진다`() {
        val fourth = 4 * 116 - 3 * 4 // 겹침은 3장까지만
        val fifth = 5 * 116 - 3 * 4
        assertEquals(fourth, snap(fourth))
        assertEquals(fifth, snap(fifth))
        assertEquals(stride.toInt(), fifth - fourth)
    }

    @Test
    fun `허용 범위 밖으로는 나가지 않는다`() {
        assertTrue(snap(5000) in range, "위쪽 한계를 넘지 않아야 한다")
        assertTrue(snap(-5000) in range, "아래쪽 한계를 넘지 않아야 한다")
        assertEquals(range.last, snap(range.last))
    }

    @Test
    fun `범위가 한 점뿐이면 그 값만 나온다`() {
        assertEquals(
            500,
            nearestCardSnap(700, 500..500, stride, stackOffset, maxStackSlots),
        )
    }

    // ── 러버밴드 ──

    private fun pull(
        offset: Float,
        delta: Float,
    ) = rubberBandOffset(
        currentOffset = offset,
        dragDelta = delta,
        maxOffsetPx = 200f,
        rubberBandFactor = 0.4f,
    )

    @Test
    fun `당기기 시작할 때는 저항이 가장 약하다`() {
        assertEquals(40f, pull(offset = 0f, delta = 100f), absoluteTolerance = 0.001f)
    }

    @Test
    fun `많이 당겨 둘수록 같은 드래그로 덜 늘어난다`() {
        val fromStart = pull(offset = 0f, delta = 100f) - 0f
        val fromHalf = pull(offset = 100f, delta = 100f) - 100f
        assertTrue(fromHalf < fromStart, "이미 당겨 둔 쪽이 덜 늘어나야 한다")
    }

    @Test
    fun `최대치에 닿으면 더 당겨지지 않는다`() {
        assertEquals(200f, pull(offset = 200f, delta = 500f), absoluteTolerance = 0.001f)
    }

    @Test
    fun `되돌리는 방향으로 끌면 줄어들고 0 아래로는 내려가지 않는다`() {
        assertTrue(pull(offset = 100f, delta = -100f) < 100f)
        assertEquals(0f, pull(offset = 10f, delta = -5000f), absoluteTolerance = 0.001f)
    }
}
