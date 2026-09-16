package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.DeviceInfoDataSource
import com.minseonglove.jlptwords.entity.InquiryInfo

class InquiryRepositoryImpl(
    private val deviceInfoDataSource: DeviceInfoDataSource,
) : InquiryRepository {
    override suspend fun getInquiryInfo(): InquiryInfo {
        val modelName = deviceInfoDataSource.getModelName()
        val osVersion = deviceInfoDataSource.getOsVersion()
        return InquiryInfo(
            modelName = modelName,
            osVersion = osVersion,
        )
    }
}
