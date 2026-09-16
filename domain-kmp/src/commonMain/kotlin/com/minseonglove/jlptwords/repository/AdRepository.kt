package com.minseonglove.jlptwords.repository

interface AdRepository {
    suspend fun getLastInterstitialAdShowTime(): Long

    suspend fun setLastInterstitialAdShowTime(time: Long)

    suspend fun getAdZeroTime(): Long

    suspend fun addAdZeroTime(time: Long): Long

    suspend fun isAdZeroEnabled(): Boolean
}
