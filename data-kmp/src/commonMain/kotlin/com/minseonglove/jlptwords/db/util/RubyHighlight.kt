package com.minseonglove.jlptwords.db.util

import com.minseonglove.jlptwords.entity.RubySegment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * 저장된 루비 세그먼트에 `*` 마커 원문으로부터 하이라이트를 복원한다.
 * 데이터 정합이 깨진 경우(연결 불일치·마커가 세그먼트 중간) 빈 목록을 반환해
 * 화면이 기존 무루비 표시로 폴백하게 한다.
 */
fun buildRubySegments(
    japanese: String,
    rubies: List<DecodedRuby>,
): ImmutableList<RubySegment> {
    if (rubies.isEmpty()) return persistentListOf()
    val stripped = japanese.replace("*", "")
    if (rubies.joinToString("") { it.text } != stripped) return persistentListOf()

    // 마커 제거 좌표계에서 각 하이라이트 구간 [시작, 끝)
    val highlightRanges = mutableListOf<IntRange>()
    var pos = 0
    var openedAt = -1
    for (ch in japanese) {
        if (ch == '*') {
            if (openedAt < 0) {
                openedAt = pos
            } else {
                highlightRanges.add(openedAt until pos)
                openedAt = -1
            }
        } else {
            pos++
        }
    }

    val result = mutableListOf<RubySegment>()
    var offset = 0
    for (ruby in rubies) {
        val range = offset until offset + ruby.text.length
        val inside = highlightRanges.any { it.first <= range.first && range.last <= it.last }
        val crosses = !inside && highlightRanges.any { it.first <= range.last && range.first <= it.last }
        if (crosses) return persistentListOf()
        result.add(RubySegment(text = ruby.text, reading = ruby.reading, isHighlight = inside))
        offset += ruby.text.length
    }
    return result.toImmutableList()
}
