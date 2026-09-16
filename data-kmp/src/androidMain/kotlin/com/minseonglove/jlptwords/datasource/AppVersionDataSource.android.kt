package com.minseonglove.jlptwords.datasource

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

actual class AppVersionDataSource(
    private val context: Context,
) {
    actual suspend fun getAppVersionName(): String? {
        return runCatching {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0),
                    )
                } else {
                    packageManager.getPackageInfo(packageName, 0)
                }
            packageInfo.versionName
        }.getOrDefault(null)
    }
}
