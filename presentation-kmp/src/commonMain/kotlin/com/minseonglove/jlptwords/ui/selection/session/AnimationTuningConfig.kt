package com.minseonglove.jlptwords.ui.selection.session

data class AnimationTuningConfig(
    // 레이아웃
    val stackOffsetDp: Int = 4,
    val expandedSpacingDp: Int = 16,
    val maxStackSize: Int = 4,
    // 애니메이션 타이밍
    val expandDurationMs: Int = 350,
    val settleDurationMs: Int = 250,
    // Pull-to-Close
    val pullThresholdDp: Int = 60,
    val pullMaxOffsetDp: Int = 200,
    val pullRubberBandFactor: Float = 0.4f,
    // 페이드/이펙트
    val pullFadeFactor: Float = 0.9f,
    val pullPreviewMoveFactor: Float = 0.35f,
    // 스프링
    val springDampingRatio: Float = 0.5f,
    val springStiffness: Float = 1500f,
)
