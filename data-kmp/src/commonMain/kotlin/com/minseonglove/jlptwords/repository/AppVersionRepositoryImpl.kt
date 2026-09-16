package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.AppVersionDataSource

class AppVersionRepositoryImpl(
    private val appVersionDataSource: AppVersionDataSource,
) : AppVersionRepository {
    override suspend fun getAppVersionName(): String? {
        return appVersionDataSource.getAppVersionName()
    }
}
