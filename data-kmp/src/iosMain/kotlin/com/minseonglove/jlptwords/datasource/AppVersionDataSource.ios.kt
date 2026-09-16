package com.minseonglove.jlptwords.datasource

import platform.Foundation.NSBundle

actual class AppVersionDataSource {
    actual suspend fun getAppVersionName(): String? {
        return runCatching {
            NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        }.getOrDefault(null)
    }
}
