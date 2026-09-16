package com.minseonglove.jlptwords.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateAdZeroRemainingHoursTest {
    private val hour = 3_600_000L

    @Test
    fun `만료 시각이 현재보다 미래면 남은 시간을 올림한다`() {
        // 11시간 20분 남음 -> 12
        val now = 0L
        val expireAt = 11 * hour + 20 * 60_000L
        assertEquals(12, calculateAdZeroRemainingHours(expireAt, now))
    }

    @Test
    fun `정확히 N시간이면 N을 반환한다`() {
        assertEquals(3, calculateAdZeroRemainingHours(3 * hour, 0L))
    }

    @Test
    fun `만료됐거나 0이면 0을 반환한다`() {
        assertEquals(0, calculateAdZeroRemainingHours(0L, 1000L))
        assertEquals(0, calculateAdZeroRemainingHours(500L, 1000L))
    }
}
