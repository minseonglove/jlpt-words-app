package com.minseonglove.jlptwords.datasource

import android.os.Build

actual class DeviceInfoDataSource {
    actual suspend fun getModelName(): String {
        return Build.MODEL
    }

    actual suspend fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    }
}
