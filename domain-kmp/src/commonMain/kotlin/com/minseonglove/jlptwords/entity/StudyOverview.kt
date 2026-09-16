package com.minseonglove.jlptwords.entity

/** 홈 화면 '학습 현황'의 단일 수치. [isUpdatedToday] 가 true 면 자정 이후 변경된 값(빨간색 강조). */
data class OverviewValue(
    val value: Int,
    val isUpdatedToday: Boolean,
)

data class StudyOverview(
    val streakDays: OverviewValue,
    val totalAttendanceDays: OverviewValue,
    /** 총 학습시간(초). 표시 단위(1시간 미만은 분, 이상은 시간)는 화면에서 결정한다. */
    val totalStudyTimeSeconds: OverviewValue,
    val studiedWordCount: OverviewValue,
    val totalProgressPercent: OverviewValue,
)
