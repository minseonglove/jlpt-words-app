package com.minseonglove.jlptwords.datasource

expect class AppVersionDataSource {
    suspend fun getAppVersionName(): String?
}
