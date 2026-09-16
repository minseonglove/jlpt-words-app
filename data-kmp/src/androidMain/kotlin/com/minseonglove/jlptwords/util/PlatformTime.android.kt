package com.minseonglove.jlptwords.util

import android.os.SystemClock

actual fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
