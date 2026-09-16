package com.minseonglove.jlptwords.ad

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined
import kotlin.coroutines.resume

actual class TrackingConsentManagerImpl : TrackingConsentManager {
    // ATT 팝업은 앱이 active 일 때만 뜬다. 이미 응답한 사용자에게는 OS 가 팝업을 다시 띄우지 않으므로
    // 미결정 상태에서만 요청해 불필요한 호출을 줄인다.
    override suspend fun requestAuthorizationIfNeeded() {
        withContext(Dispatchers.Main) {
            if (ATTrackingManager.trackingAuthorizationStatus !=
                ATTrackingManagerAuthorizationStatusNotDetermined
            ) {
                return@withContext
            }
            suspendCancellableCoroutine { continuation ->
                ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }
}
