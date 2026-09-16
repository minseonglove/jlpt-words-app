package com.minseonglove.jlptwords.repository

interface AppVersionRepository {
    suspend fun getAppVersionName(): String?
}
