package com.minseonglove.jlptwords.util

actual fun finishApp() {
    ActivityProvider.currentActivity?.finishAffinity()
}

actual val isAppExitSupported: Boolean = true
