package com.minseonglove.jlptwords.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

actual suspend fun openUrl(url: String): Boolean {
    val nsUrl = NSURL.URLWithString(url) ?: return false
    // openURL(_:) 은 iOS 10 에서 deprecated 되었다. 열 수 있는지는 completionHandler 로 통지되므로
    // canOpenURL 사전 검사도 두지 않는다(mailto 등은 조회 자체가 제한될 수 있다).
    return withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any?>(),
                completionHandler = { success ->
                    if (continuation.isActive) continuation.resume(success)
                },
            )
        }
    }
}
