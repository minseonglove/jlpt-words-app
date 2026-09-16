package com.minseonglove.jlptwords.entity

/**
 * 예문 루비 세그먼트. 세그먼트 [text] 를 이어 붙이면 마커(`*`) 제거 문장이 된다.
 * [reading] 이 비어 있으면 루비 없는 본문 구간, [isHighlight] 는 표제어 강조 구간.
 */
data class RubySegment(
    val text: String,
    val reading: String,
    val isHighlight: Boolean,
)
