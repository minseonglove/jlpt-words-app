package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.datasource.TimeDataSource

class AdRepositoryImpl(
    private val preferenceDataSource: PreferenceDataSource,
    private val timeDataSource: TimeDataSource,
) : AdRepository {
    override suspend fun getLastInterstitialAdShowTime(): Long {
        return preferenceDataSource.getLastInterstitialAdShowTime()
    }

    override suspend fun setLastInterstitialAdShowTime(time: Long) {
        preferenceDataSource.setLastInterstitialAdShowTime(time)
    }

    override suspend fun getAdZeroTime(): Long {
        return preferenceDataSource.getAdZeroTime()
    }

    override suspend fun addAdZeroTime(time: Long): Long {
        // 실패하면 0으로 간주 = 광고 제거가 안추가된 것과 같음
        val networkTimeMillis = timeDataSource.getNetworkTimeMillis() ?: 0L
        val currentAdZeroTime = getAdZeroTime().coerceAtLeast(networkTimeMillis)
        val adZeroLimitTime = networkTimeMillis + AD_ZERO_LIMIT_TIME
        val updatedAdZeroTime = (currentAdZeroTime + time).coerceAtMost(adZeroLimitTime)
        preferenceDataSource.setAdZeroTime(updatedAdZeroTime)
        return updatedAdZeroTime
    }

    override suspend fun isAdZeroEnabled(): Boolean {
        val currentTime = timeDataSource.getNetworkTimeMillis() ?: return false
        return currentTime < getAdZeroTime()
    }

    companion object {
        private const val AD_ZERO_LIMIT_TIME = 2 * 24 * 60 * 60 * 1000
    }
}
