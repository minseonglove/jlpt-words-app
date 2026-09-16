package com.minseonglove.jlptwords.util

expect object TimeProvider {
    fun currentTimeMillis(): Long
}
