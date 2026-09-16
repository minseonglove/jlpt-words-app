package com.minseonglove.jlptwords.datasource

expect class DeviceInfoDataSource {
    /** 문의 대응 시 기종을 특정할 수 있는 식별자(예: "SM-S911N", "iPhone16,2"). */
    suspend fun getModelName(): String

    /** 플랫폼 이름을 포함한 OS 버전 표기(예: "Android 15", "iOS 18.2"). */
    suspend fun getOsVersion(): String
}
