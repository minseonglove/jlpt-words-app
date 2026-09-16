package com.minseonglove.jlptwords.ad

actual class TrackingConsentManagerImpl : TrackingConsentManager {
    override suspend fun requestAuthorizationIfNeeded() = Unit
}
