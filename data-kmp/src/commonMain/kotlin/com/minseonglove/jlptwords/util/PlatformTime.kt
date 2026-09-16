package com.minseonglove.jlptwords.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Returns milliseconds since boot, including time spent in sleep.
 * This is the elapsed real-time clock in milliseconds.
 */
expect fun elapsedRealtime(): Long

/**
 * Returns the current time in milliseconds since Unix epoch (January 1, 1970 00:00:00 UTC).
 */
expect fun currentTimeMillis(): Long

/**
 * 시스템 기본 시간대에서 1970-01-01 부터 오늘까지의 일수. 출석 스트릭·오늘의 예문 등
 * '자정에 바뀌는' 판정의 단일 기준이다.
 *
 * 플랫폼 달력 API 로 각각 구현하면 하루의 경계가 갈린다 — NSCalendar 의 두 시각 간 day 차이는
 * 기준 시각(1970-01-01 00:00 UTC)의 현지 벽시계마다 증가해 자정이 아닌 시각에 날이 바뀐다.
 * 그래서 양 플랫폼 공통으로 달력 날짜(LocalDate)를 거쳐 계산한다.
 */
@OptIn(ExperimentalTime::class)
fun getCurrentDaysFromEpoch(): Int =
    Clock.System
        .todayIn(TimeZone.currentSystemDefault())
        .toEpochDays()
        .toInt()
