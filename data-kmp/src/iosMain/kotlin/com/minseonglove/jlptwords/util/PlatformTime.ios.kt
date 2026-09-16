package com.minseonglove.jlptwords.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.posix.CLOCK_MONOTONIC
import platform.posix.clock_gettime_nsec_np

private const val NANOS_PER_MILLI = 1_000_000uL

/**
 * Darwin 의 CLOCK_MONOTONIC 은 기기가 절전에 든 시간까지 함께 센다(mach_continuous_time 과 같은 시계).
 *
 * CACurrentMediaTime()(= mach_absolute_time) 은 절전 중 멈추므로, 캐시해 둔 시각으로부터의 경과를
 * 재는 용도로 쓰면 기기를 재워 둔 만큼 현재 시각이 과거로 밀린다. Android 의
 * SystemClock.elapsedRealtime() 과 의미를 맞추기 위해 절전을 포함하는 시계를 쓴다.
 */
actual fun elapsedRealtime(): Long = (clock_gettime_nsec_np(CLOCK_MONOTONIC.toUInt()) / NANOS_PER_MILLI).toLong()

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
