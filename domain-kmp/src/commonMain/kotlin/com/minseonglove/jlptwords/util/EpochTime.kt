package com.minseonglove.jlptwords.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** epoch millis 를 [timeZone] 기준 날짜로 변환한다. '자정 기준 오늘' 판정에 사용한다. */
@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(
    timeZone: TimeZone,
): LocalDate =
    Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(timeZone)
        .date
