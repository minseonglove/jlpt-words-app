package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.TimeDataSource
import com.minseonglove.jlptwords.util.runCatchingCancellable

class TimeRepositoryImpl(
    private val dataSource: TimeDataSource,
) : TimeRepository {
    override suspend fun getRealCurrentTimeMillis(): Long? {
        return runCatchingCancellable {
            dataSource.getNetworkTimeMillis()
        }.getOrNull()
    }

    override fun getDeviceCurrentTimeMillis(): Long = dataSource.getDeviceTimeMillis()
}
