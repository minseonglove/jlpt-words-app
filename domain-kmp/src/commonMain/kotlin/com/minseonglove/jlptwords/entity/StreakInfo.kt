package com.minseonglove.jlptwords.entity

data class StreakInfo(
    val streak: Int,
    val isStudyToday: Boolean,
    /** 학습한 날(중복 제거)의 총 수. 홈 화면 '총 출석일' 표시에 사용한다. */
    val totalDays: Int = 0,
)
