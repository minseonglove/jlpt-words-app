package com.minseonglove.jlptwords.ui.selection.session

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 펼친 챕터에서 스크롤이 챕터 영역을 벗어나지 않도록 미리 소비하는 규칙을 고정한다.
 * 화면에 붙이지 않고 커넥션에 직접 스크롤 델타를 흘려보내 반환값을 확인한다.
 */
class ChapterScrollBoundaryTest {
    private val chapterRange = 100..500

    private fun connection(
        expandedChapter: Int? = 1,
        scroll: Int = 300,
        pullState: ChapterPullToCloseState = ChapterPullToCloseState(),
        isCollapsing: Boolean = false,
        isExpandSettled: Boolean = true,
        onCollapseRequest: (Int) -> Unit = {},
    ) = ChapterScrollConnection(
        expandedChapterNumber = expandedChapter,
        config = AnimationTuningConfig(),
        scrollState = ScrollState(scroll),
        pullState = pullState,
        pullThresholdPx = 60f,
        pullMaxOffsetPx = 200f,
        expandedSpacingPx = 16f,
        stackOffsetPx = 4f,
        scrollRangeOf = { chapterRange },
        cardHeightOf = { 100 },
        isCollapsing = { isCollapsing },
        isExpandSettled = { isExpandSettled },
        onCollapseRequest = onCollapseRequest,
        scrollCancelScope = CoroutineScope(Dispatchers.Unconfined),
    )

    private fun ChapterScrollConnection.drag(
        dy: Float,
        source: NestedScrollSource = NestedScrollSource.UserInput,
    ) = onPreScroll(Offset(0f, dy), source)

    @Test
    fun `펼친 챕터가 없으면 스크롤에 관여하지 않는다`() {
        assertEquals(Offset.Zero, connection(expandedChapter = null).drag(-50f))
    }

    @Test
    fun `범위 안에서는 스크롤을 그대로 흘려보낸다`() {
        assertEquals(Offset.Zero, connection(scroll = 300).drag(-50f))
        assertEquals(Offset.Zero, connection(scroll = 300).drag(50f))
    }

    @Test
    fun `아래쪽 한계를 넘는 만큼만 미리 소비한다`() {
        // 현재 480, 한계 500 -> 20 만 더 갈 수 있는데 50 을 요청
        val consumed = connection(scroll = 480).drag(-50f)
        assertEquals(-30f, consumed.y, absoluteTolerance = 0.001f)
    }

    @Test
    fun `아래쪽 한계에 닿으면 스크롤 전량을 소비해 더 내려가지 않게 한다`() {
        assertEquals(-50f, connection(scroll = 500).drag(-50f).y, absoluteTolerance = 0.001f)
    }

    @Test
    fun `위쪽 한계를 넘는 만큼만 미리 소비한다`() {
        // 현재 120, 한계 100 -> 20 만 더 갈 수 있는데 50 을 요청
        assertEquals(30f, connection(scroll = 120).drag(50f).y, absoluteTolerance = 0.001f)
    }

    @Test
    fun `위쪽 한계에서 손으로 당기면 풀투클로즈가 시작된다`() {
        val pullState = ChapterPullToCloseState()
        val consumed = connection(scroll = 100, pullState = pullState).drag(50f)

        assertTrue(pullState.isActive, "풀 상태가 켜져야 한다")
        assertTrue(pullState.offset > 0f, "당긴 만큼 오프셋이 쌓여야 한다")
        assertEquals(50f, consumed.y, absoluteTolerance = 0.001f)
    }

    @Test
    fun `접히는 중에는 풀투클로즈를 시작하지 않는다`() {
        val pullState = ChapterPullToCloseState()
        connection(scroll = 100, pullState = pullState, isCollapsing = true).drag(50f)
        assertTrue(pullState.isActive.not())
    }

    @Test
    fun `펼침 애니메이션이 끝나기 전에는 풀투클로즈를 시작하지 않는다`() {
        val pullState = ChapterPullToCloseState()
        connection(scroll = 100, pullState = pullState, isExpandSettled = false).drag(50f)
        assertTrue(pullState.isActive.not())
    }

    @Test
    fun `관성으로 한계에 닿은 것은 풀투클로즈가 아니다`() {
        val pullState = ChapterPullToCloseState()
        connection(scroll = 100, pullState = pullState)
            .drag(50f, source = NestedScrollSource.SideEffect)
        assertTrue(pullState.isActive.not())
    }

    @Test
    fun `당겨 둔 상태에서 반대로 밀면 그만큼 풀 오프셋이 줄어든다`() {
        val pullState =
            ChapterPullToCloseState().apply {
                offset = 30f
                isActive = true
            }
        connection(scroll = 100, pullState = pullState).drag(-10f)

        assertEquals(20f, pullState.offset, absoluteTolerance = 0.001f)
        assertTrue(pullState.isActive, "아직 남아 있으면 풀 상태를 유지한다")
    }

    @Test
    fun `풀 오프셋을 다 되돌리면 풀 상태가 꺼진다`() {
        val pullState =
            ChapterPullToCloseState().apply {
                offset = 10f
                isActive = true
            }
        connection(scroll = 300, pullState = pullState).drag(-40f)

        assertEquals(0f, pullState.offset, absoluteTolerance = 0.001f)
        assertTrue(pullState.isActive.not())
    }

    @Test
    fun `풀을 되돌리는 동안 되돌린 만큼 스크롤도 함께 움직인다`() {
        // 되돌린 양(drain)은 소비로 보고하지 않고 스크롤로 흘려보낸다. 아래쪽 카드가 따라
        // 올라가면서 이만큼 더 움직이면 챕터가 접힌다는 것을 보여주는 연출이다.
        val half =
            ChapterPullToCloseState().apply {
                offset = 30f
                isActive = true
            }
        assertEquals(0f, connection(scroll = 100, pullState = half).drag(-10f).y, absoluteTolerance = 0.001f)

        val drained =
            ChapterPullToCloseState().apply {
                offset = 10f
                isActive = true
            }
        assertEquals(-30f, connection(scroll = 300, pullState = drained).drag(-40f).y, absoluteTolerance = 0.001f)
    }
}
