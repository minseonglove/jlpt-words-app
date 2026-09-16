package com.minseonglove.jlptwords.ui.selection.session

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.minseonglove.jlptwords.ui.study.StudySessionCardItem
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.session_selection_collapse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

data class SessionChapter(
    val chapterNumber: Int,
    val sessions: ImmutableList<StudySessionCardItem>,
    val currentSessionIndex: Int = 0,
)

/**
 * 카드 노드가 자기 세션 인덱스를 measure 단계로 전달하는 [androidx.compose.ui.layout.layoutId] 값.
 * 생략된 카드는 노드를 만들지 않아 노드 순서와 세션 인덱스가 어긋나므로 인덱스를 직접 실어 보낸다.
 */
private data class SessionCardSlot(
    val index: Int,
    val isPlaceholder: Boolean,
)

/** 챕터 번호와 Y좌표를 번갈아 담은 평탄한 리스트로 오가는 [ExpandableChapterList] 의 좌표 맵 saver. */
private val chapterOffsetsSaver =
    listSaver<MutableMap<Int, Int>, Int>(
        save = { offsets -> offsets.flatMap { (chapterNumber, y) -> listOf(chapterNumber, y) } },
        restore = { flat ->
            flat.chunked(2).associate { (chapterNumber, y) -> chapterNumber to y }.toMutableMap()
        },
    )

@Composable
internal fun ChapterHeader(
    modifier: Modifier = Modifier,
    titleText: String,
    totalText: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MolluTheme.colorScheme.black,
        )
        Spacer(modifier = Modifier.width(13.dp))
        Text(
            text = totalText,
            style = MaterialTheme.typography.bodySmall,
            color = MolluTheme.colorScheme.sub,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isExpanded) {
            Text(
                text = stringResource(Res.string.session_selection_collapse),
                style = MaterialTheme.typography.bodySmall,
                color = MolluTheme.colorScheme.sub,
            )
        }
    }
}

/**
 * 카드 스택 ↔ 펼침 스프레딩 애니메이션을 처리하는 커스텀 Layout.
 *
 * 접힌 상태: 항상 고정된 스택 ([currentSessionIndex] 무관, fixedStackCount장)
 * 펼친 상태: 모든 카드를 개별 배치 (flat 레이아웃)
 *
 * 단일 카드 세트에서 각 카드의 collapsedY와 expandedY를 정의하고
 * expandProgress (0f=스택, 1f=펼침) 로 Y좌표와 컨테이너 높이를 보간하여
 * 카드들이 아래로 스르륵 펼쳐지는/위로 모이는 애니메이션을 구현한다.
 */
@Composable
internal fun ExpandableSessionCards(
    modifier: Modifier = Modifier,
    sessions: ImmutableList<StudySessionCardItem>,
    config: AnimationTuningConfig,
    maxStackSize: Int,
    currentSessionIndex: Int,
    isExpanded: Boolean,
    scrollState: ScrollState,
    chapterOffset: Int,
    collapseProgress: (() -> Float)? = null,
    isCollapsePending: Boolean = false,
    pullToCloseProgress: () -> Float = { 0f },
    onCardHeightMeasured: (Int) -> Unit,
    onStackClick: () -> Unit,
    onSessionClick: (Int) -> Unit,
    cardContent: @Composable (item: StudySessionCardItem, position: Int, onClick: () -> Unit) -> Unit,
    stackPlaceholderContent: @Composable (onClick: () -> Unit) -> Unit,
) {
    // 펼침: 내부 animateFloatAsState 사용
    // 접힘: 외부에서 주입된 collapseProgress 사용 (Single-Driver 동기화)
    val internalProgress =
        animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = tween(durationMillis = config.expandDurationMs, easing = EaseInOutCubic),
        )
    // 진행값은 람다로만 노출한다. 컴포지션에서 읽으면 애니메이션 프레임마다 카드 전체가
    // 재구성되므로, 실제 읽기는 measure/placement/draw 단계에서만 일어나게 한다.
    val expandProgress: () -> Float = { collapseProgress?.invoke() ?: internalProgress.value }
    // 접힘 트리거~Animatable 구동 사이의 갭(isCollapsePending)도 접힘 중으로 취급해
    // 카드 생략·스냅샷 정리가 펼쳐진 화면 위에서 순간 실행되지 않게 한다.
    val isCollapsing = collapseProgress != null || isCollapsePending

    val density = LocalDensity.current
    val stackOffsetPx = remember(density, config) { with(density) { config.stackOffsetDp.dp.toPx() } }
    val expandedSpacingPx = remember(density, config) { with(density) { config.expandedSpacingDp.dp.toPx() } }

    // 고정 스택 카드 선택: currentSessionIndex와 무관하게 항상 동일한 스택 크기
    val currentIdx = currentSessionIndex.coerceIn(0, sessions.lastIndex.coerceAtLeast(0))
    val fixedStackCount = minOf(sessions.size, maxStackSize)

    // currentIdx 기준 가장 가까운 카드부터 배킹 카드 선택 (before 우선, 부족하면 after)
    // 슬롯 맵: sessionIndex → slot (0=최하단(가장 먼 카드), fixedStackCount-1=currentIdx 최상단)
    val collapsedStackSlots =
        remember(currentIdx, sessions.size, fixedStackCount) {
            buildCollapsedStackSlots(
                currentIdx = currentIdx,
                sessionCount = sessions.size,
                stackCount = fixedStackCount,
            )
        }

    // 접힘 애니메이션 시작 시 각 카드의 Y좌표를 캡처하여 불연속 점프 방지.
    // 스냅샷은 Layout 이 매 프레임 갱신하므로 별도 캡처 시점은 두지 않는다.
    val collapseSnapshotY = remember { mutableMapOf<Int, Int>() }

    // 접힘이 끝났거나(collapseProgress 해제) 한 번도 펼쳐진 적 없으면 스냅샷을 비운다.
    // 진행값 비교를 컴포지션 본문에서 하면 재구성 중 상태를 바꾸게 되므로 이펙트로 처리한다.
    LaunchedEffect(isExpanded, isCollapsing) {
        if (!isExpanded && !isCollapsing) {
            collapseSnapshotY.clear()
        }
    }

    Layout(
        content = {
            for (i in 0 until sessions.size) {
                // 완전 접힘 상태의 스택 밖 카드는 alpha 0 으로 보이지 않으므로 노드를 만들지
                // 않는다 — 탭 재진입 비용이 전체 카드 수가 아닌 스택 크기에 묶인다.
                // 접힘 진행 중에는 스택 밖 카드도 보여야 한다. 진행값 대신 접힘 여부로 판단해
                // 애니메이션 프레임마다 이 분기가 다시 평가되지 않게 한다.
                val isCardVisible =
                    collapsedStackSlots.containsKey(i) || isExpanded || isCollapsing
                if (isCardVisible.not()) continue

                // 접힘 상태에서 최상단 카드 뒤에 깔린 배킹 카드는 stackOffsetDp 만큼의 하단 띠만
                // 드러나므로, 테두리·배경만 그리는 자리표시자로 대체한다.
                val isPlaceholder = isExpanded.not() && isCollapsing.not() && i != currentIdx

                key(sessions[i].id) {
                    Box(
                        modifier =
                            Modifier
                                .layoutId(SessionCardSlot(index = i, isPlaceholder = isPlaceholder))
                                .graphicsLayer {
                                    alpha =
                                        cardAlpha(
                                            isInStack = collapsedStackSlots.containsKey(i),
                                            progress = expandProgress(),
                                            pull = pullToCloseProgress(),
                                            fadeFactor = config.pullFadeFactor,
                                        )
                                },
                        propagateMinConstraints = true,
                    ) {
                        if (isPlaceholder) {
                            stackPlaceholderContent(onStackClick)
                        } else {
                            cardContent(
                                sessions[i],
                                i + 1,
                                if (isExpanded) {
                                    { onSessionClick(sessions[i].id) }
                                } else {
                                    onStackClick
                                },
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val slots = measurables.map { it.layoutId as SessionCardSlot }
        val placeables = arrayOfNulls<Placeable>(measurables.size)

        // 실제 카드를 먼저 측정해 카드 높이를 확정한다.
        var cardHeight = 0
        measurables.forEachIndexed { node, measurable ->
            if (slots[node].isPlaceholder) return@forEachIndexed
            val placeable = measurable.measure(constraints)
            placeables[node] = placeable
            cardHeight = maxOf(cardHeight, placeable.height)
        }
        // 자리표시자는 실제 카드와 같은 높이여야 스택 하단에 드러나는 띠 위치가 어긋나지 않는다.
        val placeholderHeight = cardHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        val placeholderConstraints =
            Constraints(
                minWidth = constraints.minWidth,
                maxWidth = constraints.maxWidth,
                minHeight = placeholderHeight,
                maxHeight = placeholderHeight,
            )
        measurables.forEachIndexed { node, measurable ->
            if (slots[node].isPlaceholder) {
                placeables[node] = measurable.measure(placeholderConstraints)
            }
        }
        onCardHeightMeasured(cardHeight)

        val metrics =
            CardMetrics(
                cardHeight = cardHeight,
                stackOffsetPx = stackOffsetPx,
                expandedSpacingPx = expandedSpacingPx,
                maxStackSize = maxStackSize,
                fixedStackCount = fixedStackCount,
            )

        // 컨테이너 높이를 보간하여 아래 콘텐츠가 자연스럽게 밀려남.
        // 진행값은 여기(measure)에서 읽으므로 값이 바뀌어도 재구성 없이 다시 측정만 한다.
        val totalHeight =
            lerp(
                metrics.collapsedHeight().toFloat(),
                metrics.expandedHeight(sessions.size).toFloat(),
                expandProgress(),
            ).toInt()

        layout(constraints.maxWidth, totalHeight) {
            // placement 단계에서 읽어 recomposition 없이 re-placement만 트리거
            val progress = expandProgress()
            val pull = pullToCloseProgress()
            val stackPosition =
                if (progress >= 1f) {
                    maxOf(0, scrollState.value - chapterOffset)
                } else {
                    0
                }

            val targetScrollDelta =
                expandScrollDelta(
                    currentIdx = currentIdx,
                    cardHeight = cardHeight,
                    expandedSpacingPx = expandedSpacingPx,
                    stackOffsetPx = stackOffsetPx,
                    maxStackSize = maxStackSize,
                )

            placeables.forEachIndexed { node, placeable ->
                if (placeable == null) return@forEachIndexed
                val i = slots[node].index
                val slot = collapsedStackSlots[i]
                val collapsedY = metrics.collapsedYOf(slot)

                val y =
                    when {
                        progress >= 1f -> {
                            // 스크롤에 따라 상단에 쌓인 위치. 접힘이 여기서 이어지도록 스냅샷에 남긴다.
                            val stacked = metrics.stackedYOf(index = i, stackPosition = stackPosition)
                            val settled =
                                if (pull > 0f) {
                                    lerp(
                                        stacked.toFloat(),
                                        collapsedY.toFloat(),
                                        pull * config.pullPreviewMoveFactor,
                                    ).toInt()
                                } else {
                                    stacked
                                }
                            collapseSnapshotY[i] = settled
                            settled
                        }

                        progress > 0f ->
                            if (isExpanded && currentIdx > i) {
                                // 펼치는 중 currentIdx 앞의 카드는 스택 형태를 유지한 채 옮겨간다.
                                // scrollState.value 를 쓰면 목표가 매 프레임 움직이므로 고정값을 쓴다.
                                val expandStackSlotY =
                                    targetScrollDelta +
                                        (minOf(i, maxStackSize - 1) * stackOffsetPx).toInt()
                                lerp(collapsedY.toFloat(), expandStackSlotY.toFloat(), progress).toInt()
                            } else {
                                val fromY = collapseSnapshotY[i] ?: metrics.expandedYOf(i)
                                lerp(collapsedY.toFloat(), fromY.toFloat(), progress).toInt()
                            }

                        else -> collapsedY
                    }

                val zIndex =
                    lerp(
                        collapsedZIndexOf(
                            index = i,
                            currentIdx = currentIdx,
                            sessionCount = sessions.size,
                            slot = slot,
                        ),
                        i.toFloat(),
                        progress,
                    )

                placeable.placeRelative(x = 0, y = y, zIndex = zIndex)
            }
        }
    }
}

/** [ExpandableSessionCards] 의 measure 단계에서 확정되어 placement 로 넘어가는 배치 기준값. */
internal class CardMetrics(
    val cardHeight: Int,
    val stackOffsetPx: Float,
    val expandedSpacingPx: Float,
    val maxStackSize: Int,
    val fixedStackCount: Int,
)

/** 접힌 상태 컨테이너 높이. currentSessionIndex 와 무관하게 고정이다. */
internal fun CardMetrics.collapsedHeight(): Int = cardHeight + (maxOf(0, fixedStackCount - 1) * stackOffsetPx).toInt()

/** 펼친 상태 컨테이너 높이. 모든 카드가 균등 간격으로 늘어선다. */
internal fun CardMetrics.expandedHeight(sessionCount: Int): Int =
    if (sessionCount > 0) {
        (sessionCount * (cardHeight + expandedSpacingPx) - expandedSpacingPx).toInt()
    } else {
        0
    }

/** 펼친 상태에서 index 번째 카드의 Y. */
internal fun CardMetrics.expandedYOf(index: Int): Int = (index * (cardHeight + expandedSpacingPx)).toInt()

/** 접힌 상태에서의 Y. 슬롯이 없는 카드는 최상단 카드 뒤에 겹쳐 숨는다. */
internal fun CardMetrics.collapsedYOf(slot: Int?): Int = if (slot == null) 0 else ((fixedStackCount - 1 - slot) * stackOffsetPx).toInt()

/** 펼침이 끝난 뒤 스크롤을 따라 상단에 쌓이는 Y. 제자리보다 위로는 올라가지 않는다. */
internal fun CardMetrics.stackedYOf(
    index: Int,
    stackPosition: Int,
): Int =
    maxOf(
        expandedYOf(index),
        stackPosition + (minOf(index, maxStackSize - 1) * stackOffsetPx).toInt(),
    )

/**
 * 챕터를 펼칠 때 스크롤이 향하는 최종 오프셋.
 *
 * [ExpandableChapterList] 의 스크롤 애니메이션과 [ExpandableSessionCards] 의 카드 배치가
 * 같은 지점을 향해야 두 애니메이션이 어긋나지 않으므로 계산을 한곳에 둔다.
 */
internal fun expandScrollDelta(
    currentIdx: Int,
    cardHeight: Int,
    expandedSpacingPx: Float,
    stackOffsetPx: Float,
    maxStackSize: Int,
): Int {
    if (currentIdx <= 0 || cardHeight <= 0) return 0
    val visibleStack = minOf(currentIdx, maxStackSize - 1)
    return (currentIdx * (cardHeight + expandedSpacingPx) - visibleStack * stackOffsetPx).toInt()
}

/**
 * 접힘 상태에서 스택에 남길 카드의 슬롯 배정.
 *
 * [currentIdx] 에서 가까운 카드부터 채우되 앞쪽을 우선하고, 모자라면 뒤쪽에서 가져온다.
 * 슬롯 0 이 최하단(가장 먼 카드), [stackCount] - 1 이 [currentIdx] 로 최상단이다.
 */
internal fun buildCollapsedStackSlots(
    currentIdx: Int,
    sessionCount: Int,
    stackCount: Int,
): Map<Int, Int> {
    if (stackCount == 0) return emptyMap()
    val backingIndices =
        ((currentIdx - 1 downTo 0) + ((currentIdx + 1) until sessionCount))
            .take(stackCount - 1)
    val slots = mutableMapOf<Int, Int>()
    backingIndices.reversed().forEachIndexed { slot, sessionIdx ->
        slots[sessionIdx] = slot
    }
    slots[currentIdx] = stackCount - 1
    return slots.toMap()
}

/** 스택 카드는 접혀 있어도 보이고, 스택 밖 카드는 펼침 진행에 따라 나타난다. */
internal fun cardAlpha(
    isInStack: Boolean,
    progress: Float,
    pull: Float,
    fadeFactor: Float,
): Float {
    val base = if (isInStack) 1f else progress
    return if (pull > 0f && progress >= 1f) base * (1f - pull * fadeFactor) else base
}

/** 접힌 상태의 z 순서. [currentIdx] 카드가 스택 맨 위에 온다. */
internal fun collapsedZIndexOf(
    index: Int,
    currentIdx: Int,
    sessionCount: Int,
    slot: Int?,
): Float =
    when {
        index == currentIdx -> (sessionCount + 1).toFloat()
        slot != null -> (slot + 1).toFloat()
        else -> 0f
    }

/** [ExpandableChapterList] 가 뷰포트 윈도잉과 자리표시자 높이를 계산할 때 쓰는 레이아웃 기준값. */
internal class SectionLayout(
    val cardHeight: Int,
    val headerHeight: Int,
    val expandedSpacingPx: Float,
    val stackOffsetPx: Float,
    val maxStackSize: Int,
    val dividerBlockPx: Float,
    val topSpacerPx: Float,
)

/** 챕터 카드 섹션의 예상 높이. 실측 전이거나 뷰포트 밖이라 컴포지션하지 않는 챕터에 쓴다. */
internal fun SectionLayout.heightOf(
    chapter: SessionChapter,
    isExpanded: Boolean,
): Float {
    val sessionCount = chapter.sessions.size
    if (isExpanded.not()) {
        val stackCount = minOf(sessionCount, maxStackSize)
        return cardHeight + maxOf(0, stackCount - 1) * stackOffsetPx
    }
    return if (sessionCount > 0) {
        sessionCount * (cardHeight + expandedSpacingPx) - expandedSpacingPx
    } else {
        0f
    }
}

/** 각 챕터 카드 섹션의 [top, bottom) 산술 위치. 헤더·디바이더·고정 여백을 누적한다. */
internal fun SectionLayout.boundsOf(
    chapters: ImmutableList<SessionChapter>,
    expandedChapters: PersistentSet<Int>,
): List<Pair<Float, Float>> =
    buildList {
        var y = topSpacerPx
        chapters.forEachIndexed { index, chapter ->
            y += headerHeight
            val sectionHeight =
                heightOf(chapter = chapter, isExpanded = chapter.chapterNumber in expandedChapters)
            add(y to (y + sectionHeight))
            y += sectionHeight
            if (index < chapters.lastIndex) y += dividerBlockPx
        }
    }

/**
 * 펼친 챕터의 헤더를 뷰포트 상단에 붙인다.
 *
 * 스크롤이 헤더를 지나간 만큼 아래로 밀어 제자리에 머무는 것처럼 보이게 하고, 뒤의 카드가
 * 비치지 않도록 배경을 깐다. 좌표는 layout 단계에서만 읽어 스크롤마다 재구성되지 않게 한다.
 */
private fun Modifier.stickyChapterHeader(
    isSticky: Boolean,
    background: Color,
    chapterTop: () -> Int,
    scrollValue: () -> Int,
): Modifier =
    if (isSticky.not()) {
        this
    } else {
        this
            .zIndex(1f)
            .offset { IntOffset(0, maxOf(0, scrollValue() - chapterTop())) }
            .background(background)
    }

/**
 * 챕터 접힘: 스크롤 복귀와 카드 접힘을 같은 easing 으로 동시에 진행한다.
 *
 * 둘이 어긋나면 스택에 남는 카드가 제자리에서 튀므로 한 시퀀스로 묶어 둔다. 접히고 나면
 * 하단 여백 Spacer 가 사라져 최대 스크롤이 [viewportHeight] 만큼 줄어들기 때문에,
 * 그 밖으로 나간 스크롤은 유효 범위 안으로 다시 정착시킨다.
 */
private suspend fun animateChapterCollapse(
    scrollState: ScrollState,
    collapseAnimatable: Animatable<Float, AnimationVector1D>,
    targetScroll: Int,
    viewportHeight: Int,
    config: AnimationTuningConfig,
) = coroutineScope {
    collapseAnimatable.snapTo(1f)
    val scrollJob =
        launch {
            scrollState.animateScrollTo(
                targetScroll,
                tween(config.expandDurationMs, easing = EaseInOutCubic),
            )
        }
    collapseAnimatable.animateTo(0f, tween(config.expandDurationMs, easing = EaseInOutCubic))
    scrollJob.join()

    val maxScrollWithoutSpacer = maxOf(0, scrollState.maxValue - viewportHeight)
    if (scrollState.value > maxScrollWithoutSpacer) {
        scrollState.animateScrollTo(
            maxScrollWithoutSpacer,
            tween(config.settleDurationMs, easing = EaseInOutCubic),
        )
    }
}

/**
 * 챕터 카드 스택의 스크롤/펼침/접힘 애니메이션 호스트.
 *
 * sticky 헤더, 단일 Animatable 기반 접힘 동기화, pull-to-close, 스크롤 범위 제한
 * (scrollClampConnection) 을 포함한다. 프로덕션/테스트 화면이 동일 동작을 공유한다.
 */
@Composable
internal fun ExpandableChapterList(
    chapters: ImmutableList<SessionChapter>,
    expandedChapters: PersistentSet<Int>,
    config: AnimationTuningConfig,
    onToggleChapter: (Int) -> Unit,
    onStackClick: (Int) -> Unit,
    onCollapseAnimationCompleted: () -> Unit,
    onSessionClick: (Int) -> Unit,
    chapterTitleText: @Composable (chapterNumber: Int) -> String,
    chapterTotalText: @Composable (totalSessions: Int) -> String,
    stickyHeaderBackground: Color,
    dividerColor: Color,
    modifier: Modifier = Modifier,
    contentHorizontalPadding: Dp = 16.dp,
    cardContent: @Composable (item: StudySessionCardItem, position: Int, onClick: () -> Unit) -> Unit,
    stackPlaceholderContent: @Composable (onClick: () -> Unit) -> Unit,
) {
    val scrollState = rememberScrollState()
    val expandedChapterNumber = expandedChapters.firstOrNull()
    // Column 내 각 챕터 헤더의 콘텐츠 Y좌표를 저장 (sticky offset 계산용).
    // sticky offset 은 이 맵을 placement 단계에서 읽지만, 맵 갱신은 스냅샷 상태가 아니라
    // 재배치를 일으키지 않는다. 값이 빈 채로 첫 배치가 끝나면 헤더가 chapterY 만큼 아래로
    // 밀린 채 다음 스크롤까지 남으므로, saveable 로 보존해 재진입 첫 배치부터 채워 둔다.
    val chapterOffsets = rememberSaveable(saver = chapterOffsetsSaver) { mutableMapOf<Int, Int>() }
    // 각 챕터의 ExpandableSessionCards 상단/하단 좌표를 저장 (스크롤 범위 제한용)
    val cardSectionTops = remember { mutableMapOf<Int, Int>() }
    val cardSectionBottoms = remember { mutableMapOf<Int, Int>() }
    // 각 챕터의 카드 높이를 저장 (maxScroll 계산에 사용)
    val cardHeights = remember { mutableMapOf<Int, Int>() }
    // 마지막 챕터도 상단까지 스크롤할 수 있도록 하단 여백 계산에 사용
    // saveable: 재진입 첫 프레임의 윈도잉이 0 높이 뷰포트로 계산되지 않게 한다.
    var viewportHeight by rememberSaveable { mutableIntStateOf(0) }
    val density = LocalDensity.current
    // 스택 오프셋 최대값 (maxScroll fallback용)
    val maxStackOffsetPx =
        remember(density, config) {
            with(density) { ((config.maxStackSize - 1) * config.stackOffsetDp).dp.toPx() }.toInt()
        }
    val stackOffsetPx = remember(density, config) { with(density) { config.stackOffsetDp.dp.toPx() } }
    val expandedSpacingPx = remember(density, config) { with(density) { config.expandedSpacingDp.dp.toPx() } }
    val pullThresholdPx = remember(density, config) { with(density) { config.pullThresholdDp.dp.toPx() } }
    val pullMaxOffsetPx = remember(density, config) { with(density) { config.pullMaxOffsetDp.dp.toPx() } }

    // 재진입(탭 복귀) 첫 프레임부터 뷰포트 밖 챕터를 건너뛰기 위해 카드·헤더 높이를
    // saveable 로 보존한다. 높이를 아는 순간부터 챕터 카드 섹션 위치를 산술 계산할 수 있다.
    // 최초 진입에는 보존값이 없으므로 실측치보다 작게 잡은 추정값을 시드로 넣어 첫 프레임부터
    // 윈도잉이 동작하게 한다. 작게 잡으면 산술 위치가 앞당겨져 경계 챕터를 더 포함하므로
    // 추정 오차가 빈 화면으로 이어지지 않고, 첫 측정 직후 실측치로 대체된다.
    val estimatedCardHeightPx = remember(density) { with(density) { 176.dp.roundToPx() } }
    val estimatedHeaderHeightPx = remember(density) { with(density) { 44.dp.roundToPx() } }
    var savedCardHeight by rememberSaveable { mutableIntStateOf(estimatedCardHeightPx) }
    var savedHeaderHeight by rememberSaveable { mutableIntStateOf(estimatedHeaderHeightPx) }
    // 디바이더 블록(상하 패딩 16 + 두께 1)과 상단 여백은 레이아웃 고정값이라 산술 위치에 합산한다.
    val dividerBlockPx = remember(density) { with(density) { 33.dp.toPx() } }
    val topSpacerPx = remember(density) { with(density) { 8.dp.toPx() } }
    val windowBufferPx = remember(density) { with(density) { 120.dp.toPx() } }

    // 마지막으로 펼침 처리를 마친 챕터. saveable 로 보존해 재진입(탭 복귀) 재구성 시
    // 이미 펼쳐져 있던 챕터를 새 펼침으로 오인해 스크롤을 리셋하는 것을 막는다.
    var lastHandledExpandedChapter by rememberSaveable { mutableStateOf<Int?>(null) }

    // 접힘 애니메이션 전용 Animatable (1f=펼침 → 0f=접힘)
    val collapseAnimatable = remember { Animatable(0f) }
    // 접힘 애니메이션 중인 챕터 번호 (null이면 접힘 중 아님)
    var isCollapsingChapter by remember { mutableStateOf<Int?>(null) }

    val pullState = remember { ChapterPullToCloseState() }
    var expandAnimationSettled by remember { mutableStateOf(false) }

    // 펼친 챕터에서 허용되는 스크롤 값 범위. 헤더가 상단에 붙는 지점부터,
    // 마지막 카드가 스택에 들어가는 지점까지.
    fun chapterScrollRange(chapterNumber: Int): IntRange {
        val minScroll = chapterOffsets[chapterNumber] ?: 0
        val headerHeight = (cardSectionTops[chapterNumber] ?: 0) - minScroll
        val maxScroll =
            maxOf(
                minScroll,
                (cardSectionBottoms[chapterNumber] ?: 0) -
                    (cardHeights[chapterNumber] ?: viewportHeight) -
                    headerHeight -
                    maxStackOffsetPx,
            )
        return minScroll..maxScroll
    }

    // 챕터 펼침 시 해당 헤더 위치로 스크롤 애니메이션.
    // 펼침: animateScrollTo + animateFloatAsState 병행
    // 접힘: 단일 Animatable이 스크롤과 카드 레이아웃을 모두 구동 (프레임 동기화)
    LaunchedEffect(expandedChapterNumber) {
        // 접힘 애니메이션 도중 같은 챕터를 다시 펼치면 접힘 코루틴이 취소되어 정리 코드가
        // 못 돈다. 이때 스킵하면 isCollapsingChapter 가 남아 반쯤 접힌 채 고착되므로,
        // 스킵은 진행 중인 접힘이 없을 때(순수 재진입 재구성)로 한정한다.
        if (expandedChapterNumber == lastHandledExpandedChapter && isCollapsingChapter == null) {
            // 재진입 직후 재구성: 새 전환이 아니므로 복원된 스크롤 위치를 유지하고 정착 상태만 세팅한다.
            if (expandedChapterNumber != null) expandAnimationSettled = true
            return@LaunchedEffect
        }
        pullState.reset()
        expandAnimationSettled = false

        if (expandedChapterNumber != null) {
            lastHandledExpandedChapter = expandedChapterNumber
            isCollapsingChapter = null
            val targetOffset = chapterOffsets[expandedChapterNumber] ?: 0

            val chapter = chapters.find { it.chapterNumber == expandedChapterNumber }
            val delta =
                expandScrollDelta(
                    currentIdx = chapter?.currentSessionIndex ?: 0,
                    cardHeight = cardHeights[expandedChapterNumber] ?: 0,
                    expandedSpacingPx = expandedSpacingPx,
                    stackOffsetPx = stackOffsetPx,
                    maxStackSize = config.maxStackSize,
                )

            // 단일 스크롤 (expand 애니메이션과 동시 진행, 동일 350ms)
            scrollState.animateScrollTo(
                targetOffset + delta,
                tween(config.expandDurationMs, easing = EaseInOutCubic),
            )
            expandAnimationSettled = true
        } else if (lastHandledExpandedChapter != null) {
            val chapter = lastHandledExpandedChapter!!
            isCollapsingChapter = chapter
            animateChapterCollapse(
                scrollState = scrollState,
                collapseAnimatable = collapseAnimatable,
                targetScroll = chapterOffsets[chapter] ?: 0,
                viewportHeight = viewportHeight,
                config = config,
            )
            isCollapsingChapter = null
            lastHandledExpandedChapter = null
            onCollapseAnimationCompleted()
        }
    }

    val scrollClampConnection =
        rememberChapterScrollConnection(
            expandedChapterNumber = expandedChapterNumber,
            config = config,
            scrollState = scrollState,
            pullState = pullState,
            pullThresholdPx = pullThresholdPx,
            pullMaxOffsetPx = pullMaxOffsetPx,
            expandedSpacingPx = expandedSpacingPx,
            stackOffsetPx = stackOffsetPx,
            scrollRangeOf = ::chapterScrollRange,
            cardHeightOf = { cardHeights[it] ?: 0 },
            isCollapsing = { isCollapsingChapter != null },
            isExpandSettled = { expandAnimationSettled },
            onCollapseRequest = onToggleChapter,
        )

    // 접힘 트리거 직후 LaunchedEffect 가 isCollapsingChapter 를 세우기 전의 1프레임 갭.
    // 이 갭에서 윈도잉·카드 생략·스티키 해제가 일어나면 콘텐츠 높이가 순간 수축해
    // 스크롤 값이 비가역적으로 클램프되므로, 갭 동안에도 접힘 진행 중으로 취급한다.
    val pendingCollapseChapter =
        if (expandedChapterNumber == null) lastHandledExpandedChapter else null

    // 정적 상태(펼침/접힘 애니메이션 없음)에서만 챕터 카드 섹션을 뷰포트 기준으로 윈도잉한다.
    // 애니메이션 중에는 섹션 높이가 보간되어 산술 위치가 틀어지므로 전부 컴포지션한다(기존 동작).
    val canWindowSections =
        isCollapsingChapter == null &&
            pendingCollapseChapter == null &&
            (expandedChapterNumber == null || expandAnimationSettled)

    val sectionLayout =
        SectionLayout(
            cardHeight = savedCardHeight,
            headerHeight = savedHeaderHeight,
            expandedSpacingPx = expandedSpacingPx,
            stackOffsetPx = stackOffsetPx,
            maxStackSize = config.maxStackSize,
            dividerBlockPx = dividerBlockPx,
            topSpacerPx = topSpacerPx,
        )
    val sectionBounds =
        if (canWindowSections) sectionLayout.boundsOf(chapters, expandedChapters) else null

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollClampConnection)
                .onSizeChanged { viewportHeight = it.height },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .then(modifier)
                    .padding(horizontal = contentHorizontalPadding),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            chapters.forEachIndexed { index, chapter ->
                val isExpanded =
                    chapter.chapterNumber in expandedChapters

                key(chapter.chapterNumber) {
                    // 접힘 진행값은 ExpandableSessionCards 가 직접 구동한다. 여기서 진행값을
                    // 읽으면 애니메이션 프레임마다 챕터 전체가 재구성되므로 상태 플래그만 본다.
                    val isChapterCollapsing =
                        isCollapsingChapter == chapter.chapterNumber ||
                            pendingCollapseChapter == chapter.chapterNumber

                    val onHeaderClick: () -> Unit =
                        remember {
                            { onToggleChapter(chapter.chapterNumber) }
                        }
                    // Sticky Header 동작 원리:
                    // 1. onGloballyPositioned: Column 내 헤더의 콘텐츠 Y좌표 기록
                    // 2. zIndex(1f): 헤더가 카드 위에 렌더링되도록 z축 우선순위 부여
                    // 3. offset: scrollState.value - chapterY 만큼 아래로 밀어
                    //    헤더가 항상 viewport 상단에 위치하도록 보정
                    // 4. background: 뒤의 콘텐츠가 비치지 않도록 배경색 적용
                    ChapterHeader(
                        modifier =
                            Modifier
                                .onSizeChanged {
                                    if (it.height > 0) savedHeaderHeight = it.height
                                }.onGloballyPositioned { coords ->
                                    chapterOffsets[chapter.chapterNumber] =
                                        coords.positionInParent().y.toInt()
                                }.stickyChapterHeader(
                                    isSticky = isExpanded || isChapterCollapsing,
                                    background = stickyHeaderBackground,
                                    chapterTop = { chapterOffsets[chapter.chapterNumber] ?: 0 },
                                    scrollValue = { scrollState.value },
                                ),
                        titleText = chapterTitleText(chapter.chapterNumber),
                        totalText = chapterTotalText(chapter.sessions.size),
                        isExpanded = isExpanded,
                        onClick = onHeaderClick,
                    )

                    val onStackCardClick: () -> Unit =
                        remember {
                            { onStackClick(chapter.chapterNumber) }
                        }
                    val isSectionVisible =
                        if (sectionBounds == null) {
                            true
                        } else {
                            val bounds = sectionBounds[index]
                            // 스크롤마다가 아니라 가시성 경계를 넘을 때만 이 챕터가 재구성되도록 파생 상태로 읽는다.
                            remember(bounds, viewportHeight) {
                                derivedStateOf {
                                    bounds.second >= scrollState.value - windowBufferPx &&
                                        bounds.first <= scrollState.value + viewportHeight + windowBufferPx
                                }
                            }.value
                        }

                    if (isSectionVisible || isExpanded) {
                        ExpandableSessionCards(
                            modifier =
                                Modifier.onGloballyPositioned { coords ->
                                    val top = coords.positionInParent().y.toInt()
                                    cardSectionTops[chapter.chapterNumber] = top
                                    cardSectionBottoms[chapter.chapterNumber] = top + coords.size.height
                                },
                            sessions = chapter.sessions,
                            config = config,
                            maxStackSize = config.maxStackSize,
                            currentSessionIndex = chapter.currentSessionIndex,
                            isExpanded = isExpanded,
                            scrollState = scrollState,
                            chapterOffset = chapterOffsets[chapter.chapterNumber] ?: 0,
                            // 진행 람다는 실제 Animatable 이 구동될 때만 넘긴다. 갭 프레임에
                            // 넘기면 snapTo(1f) 전의 낡은 값(0f)으로 카드가 순간 접혀 보인다.
                            collapseProgress =
                                if (isCollapsingChapter == chapter.chapterNumber) {
                                    { collapseAnimatable.value }
                                } else {
                                    null
                                },
                            isCollapsePending = pendingCollapseChapter == chapter.chapterNumber,
                            pullToCloseProgress = {
                                if (expandedChapterNumber == chapter.chapterNumber) {
                                    (pullState.offset / pullThresholdPx).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                            },
                            onCardHeightMeasured = {
                                cardHeights[chapter.chapterNumber] = it
                                if (it > 0) savedCardHeight = it
                            },
                            onStackClick = onStackCardClick,
                            onSessionClick = { onSessionClick(it) },
                            cardContent = cardContent,
                            stackPlaceholderContent = stackPlaceholderContent,
                        )
                    } else {
                        // 뷰포트 밖 접힘 챕터: 동일 높이 자리표시자로 스크롤 좌표계만 유지한다.
                        val sectionHeight =
                            sectionLayout.heightOf(
                                chapter = chapter,
                                isExpanded = isExpanded,
                            )
                        Spacer(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(with(density) { sectionHeight.toDp() }),
                        )
                    }

                    if (index < chapters.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = dividerColor,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 마지막 챕터 스크롤 보장용 하단 여백.
            // 모든 챕터가 접힌 상태에서는 전체 콘텐츠가 뷰포트보다 작을 수 있어
            // 하단 챕터의 헤더를 상단으로 스크롤할 수 없다.
            // 뷰포트 높이만큼 Spacer를 추가하여 어떤 챕터든 상단까지 스크롤 가능하게 한다.
            if (expandedChapterNumber != null || lastHandledExpandedChapter != null || isCollapsingChapter != null) {
                Spacer(
                    modifier =
                        Modifier.height(
                            with(LocalDensity.current) { viewportHeight.toDp() },
                        ),
                )
            }
        }
    }
}
