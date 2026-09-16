package com.minseonglove.jlptwords.util

actual object TimeProvider {
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
}
