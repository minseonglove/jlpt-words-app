package com.minseonglove.jlptwords.ad

interface AdManager<T : PlatformAd> {
    suspend fun load(): T?

    suspend fun getValidAd(): T?

    fun clearAd()
}
