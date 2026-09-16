package com.minseonglove.jlptwords.ui.selection.session

import com.minseonglove.jlptwords.ui.study.StudySessionCardItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 챕터 카드의 배치 규칙을 고정한다. 애니메이션은 실행 없이 확인할 방법이 없으므로,
 * 좌표·투명도·순서를 정하는 계산만이라도 회귀를 잡아 둔다.
 */
class ChapterCardLayoutTest {
    private fun metrics(
        cardHeight: Int = 100,
        stackOffsetPx: Float = 4f,
        expandedSpacingPx: Float = 16f,
        maxStackSize: Int = 4,
        fixedStackCount: Int = 4,
    ) = CardMetrics(
        cardHeight = cardHeight,
        stackOffsetPx = stackOffsetPx,
        expandedSpacingPx = expandedSpacingPx,
        maxStackSize = maxStackSize,
        fixedStackCount = fixedStackCount,
    )

    // ── 스택 슬롯 배정 ──

    @Test
    fun `현재 세션이 스택 맨 위를 차지한다`() {
        val slots = buildCollapsedStackSlots(currentIdx = 2, sessionCount = 5, stackCount = 4)
        assertEquals(3, slots[2])
    }

    @Test
    fun `배킹 카드는 현재 세션 앞쪽에서 먼저 채운다`() {
        // 앞에 2장(1,0), 뒤에 1장(3) → 앞쪽이 우선이고 먼 카드일수록 아래 슬롯
        val slots = buildCollapsedStackSlots(currentIdx = 2, sessionCount = 5, stackCount = 4)
        assertEquals(mapOf(3 to 0, 0 to 1, 1 to 2, 2 to 3), slots)
    }

    @Test
    fun `앞쪽이 모자라면 뒤쪽 카드로 채운다`() {
        val slots = buildCollapsedStackSlots(currentIdx = 0, sessionCount = 5, stackCount = 4)
        assertEquals(mapOf(3 to 0, 2 to 1, 1 to 2, 0 to 3), slots)
    }

    @Test
    fun `세션이 스택보다 적으면 있는 만큼만 배정한다`() {
        assertEquals(mapOf(0 to 0), buildCollapsedStackSlots(0, sessionCount = 1, stackCount = 1))
    }

    @Test
    fun `스택 크기가 0 이면 빈 배정이다`() {
        assertTrue(buildCollapsedStackSlots(0, sessionCount = 5, stackCount = 0).isEmpty())
    }

    // ── 컨테이너 높이 ──

    @Test
    fun `접힌 높이는 카드 한 장에 겹친 장수만큼의 띠를 더한 값이다`() {
        assertEquals(100 + 3 * 4, metrics().collapsedHeight())
    }

    @Test
    fun `펼친 높이는 카드 사이 간격을 포함하되 마지막 뒤에는 붙지 않는다`() {
        assertEquals(3 * (100 + 16) - 16, metrics().expandedHeight(sessionCount = 3))
        assertEquals(0, metrics().expandedHeight(sessionCount = 0))
    }

    // ── 카드 Y 좌표 ──

    @Test
    fun `펼친 카드는 간격을 두고 차례로 놓인다`() {
        assertEquals(0, metrics().expandedYOf(0))
        assertEquals(116, metrics().expandedYOf(1))
    }

    @Test
    fun `접힌 카드는 슬롯이 낮을수록 아래에 깔린다`() {
        val m = metrics()
        assertEquals(3 * 4, m.collapsedYOf(slot = 0))
        assertEquals(0, m.collapsedYOf(slot = 3))
    }

    @Test
    fun `스택에 못 들어간 카드는 맨 위 카드 뒤에 숨는다`() {
        assertEquals(0, metrics().collapsedYOf(slot = null))
    }

    @Test
    fun `스크롤이 카드를 지나가면 상단 스택에 걸려 멈춘다`() {
        val m = metrics()
        // 스크롤 300 이면 0번 카드의 제자리(0)보다 스택 위치가 아래여서 스택에 붙는다
        assertEquals(300, m.stackedYOf(index = 0, stackPosition = 300))
        // 아직 스크롤이 카드에 닿지 않았으면 제자리를 지킨다
        assertEquals(116, m.stackedYOf(index = 1, stackPosition = 0))
    }

    @Test
    fun `스택에 쌓이는 카드는 최대 겹침 수까지만 밀린다`() {
        val m = metrics(maxStackSize = 4)
        // index 5 여도 겹침은 3장까지만 인정된다
        assertEquals(1000 + 3 * 4, m.stackedYOf(index = 5, stackPosition = 1000))
    }

    // ── 투명도 ──

    @Test
    fun `스택 카드는 접혀 있어도 보인다`() {
        assertEquals(1f, cardAlpha(isInStack = true, progress = 0f, pull = 0f, fadeFactor = 0.9f))
    }

    @Test
    fun `스택 밖 카드는 펼침 진행만큼 나타난다`() {
        assertEquals(0f, cardAlpha(isInStack = false, progress = 0f, pull = 0f, fadeFactor = 0.9f))
        assertEquals(0.5f, cardAlpha(isInStack = false, progress = 0.5f, pull = 0f, fadeFactor = 0.9f))
    }

    @Test
    fun `다 펼친 뒤 당기면 흐려진다`() {
        val faded = cardAlpha(isInStack = true, progress = 1f, pull = 0.5f, fadeFactor = 0.9f)
        assertEquals(1f - 0.5f * 0.9f, faded)
    }

    @Test
    fun `펼치는 도중의 당김은 투명도에 영향을 주지 않는다`() {
        assertEquals(0.4f, cardAlpha(isInStack = false, progress = 0.4f, pull = 1f, fadeFactor = 0.9f))
    }

    // ── z 순서 ──

    @Test
    fun `접힌 상태에서는 현재 세션이 가장 앞에 온다`() {
        val slots = buildCollapsedStackSlots(currentIdx = 2, sessionCount = 5, stackCount = 4)
        val current = collapsedZIndexOf(index = 2, currentIdx = 2, sessionCount = 5, slot = slots[2])
        val backing = collapsedZIndexOf(index = 0, currentIdx = 2, sessionCount = 5, slot = slots[0])
        val outside = collapsedZIndexOf(index = 4, currentIdx = 2, sessionCount = 5, slot = slots[4])
        assertTrue(current > backing, "현재 세션이 배킹 카드보다 앞이어야 한다")
        assertTrue(backing > outside, "배킹 카드가 스택 밖 카드보다 앞이어야 한다")
    }

    // ── 펼침 스크롤 목표 ──

    @Test
    fun `첫 세션을 펼칠 때는 스크롤을 옮기지 않는다`() {
        assertEquals(0, expandScrollDelta(0, cardHeight = 100, 16f, 4f, maxStackSize = 4))
    }

    @Test
    fun `카드 높이를 아직 모르면 스크롤을 옮기지 않는다`() {
        assertEquals(0, expandScrollDelta(3, cardHeight = 0, 16f, 4f, maxStackSize = 4))
    }

    @Test
    fun `현재 세션이 상단에 오도록 스크롤하되 쌓인 카드만큼 덜 내린다`() {
        // 2번 카드: 2 * (100+16) 만큼 내리되, 스택에 2장 겹쳐 있으니 2 * 4 를 뺀다
        assertEquals(2 * 116 - 2 * 4, expandScrollDelta(2, 100, 16f, 4f, maxStackSize = 4))
    }

    // ── 섹션 경계(뷰포트 윈도잉) ──

    private fun chapter(
        number: Int,
        sessionCount: Int,
    ) = SessionChapter(
        chapterNumber = number,
        sessions =
            (1..sessionCount)
                .map {
                    StudySessionCardItem(
                        id = number * 100 + it,
                        startNumber = 1,
                        endNumber = 50,
                        studyProgressState = StudyProgressState.NOT_STARTED,
                        progress = 0,
                        accuracy = 0,
                        elapsedTimeSeconds = 0,
                        completionCount = 0,
                    )
                }.toImmutableList(),
    )

    private val layout =
        SectionLayout(
            cardHeight = 100,
            headerHeight = 40,
            expandedSpacingPx = 16f,
            stackOffsetPx = 4f,
            maxStackSize = 4,
            dividerBlockPx = 33f,
            topSpacerPx = 8f,
        )

    @Test
    fun `접힌 섹션 높이는 스택 두께만큼이다`() {
        assertEquals(100f + 3 * 4f, layout.heightOf(chapter(1, sessionCount = 6), isExpanded = false))
    }

    @Test
    fun `펼친 섹션 높이는 카드를 모두 늘어놓은 만큼이다`() {
        assertEquals(3 * (100f + 16f) - 16f, layout.heightOf(chapter(1, sessionCount = 3), isExpanded = true))
    }

    @Test
    fun `섹션 경계는 헤더와 구분선을 누적해 계산한다`() {
        val chapters = persistentListOf(chapter(1, 2), chapter(2, 2))
        val bounds = layout.boundsOf(chapters, persistentSetOf())

        val collapsedHeight = 100f + 4f // 세션 2장 → 겹침 1장
        val firstTop = 8f + 40f
        assertEquals(firstTop to firstTop + collapsedHeight, bounds[0])

        // 두 번째 챕터는 첫 섹션 뒤에 구분선 블록과 헤더를 더한 자리에서 시작한다
        val secondTop = firstTop + collapsedHeight + 33f + 40f
        assertEquals(secondTop to secondTop + collapsedHeight, bounds[1])
    }

    @Test
    fun `마지막 챕터 뒤에는 구분선을 더하지 않는다`() {
        val one = layout.boundsOf(persistentListOf(chapter(1, 2)), persistentSetOf())
        assertEquals(1, one.size)
        assertEquals(8f + 40f, one[0].first)
    }
}
