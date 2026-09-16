package com.minseonglove.jlptwords.usecase

/**
 * 광고 제거 만료 시각(epoch millis)과 현재 시각으로 남은 시간을 '시간 단위 올림'으로 계산한다.
 * 만료됐거나 만료 시각이 0이면 0 을 반환(= 광고 표시 상태, 상단바에 "AD" 노출).
 */
fun calculateAdZeroRemainingHours(
    expireAtMillis: Long,
    nowMillis: Long,
): Int {
    val remaining = expireAtMillis - nowMillis
    if (remaining <= 0L) return 0
    val hourMillis = 3_600_000L
    return ((remaining + hourMillis - 1) / hourMillis).toInt()
}
