package com.minseonglove.jlptwords.repository

interface TimeRepository {
    suspend fun getRealCurrentTimeMillis(): Long?

    /** 기기 로컬 시계 기준 현재 시각(epoch millis). 네트워크를 사용하지 않는다. */
    fun getDeviceCurrentTimeMillis(): Long
}
