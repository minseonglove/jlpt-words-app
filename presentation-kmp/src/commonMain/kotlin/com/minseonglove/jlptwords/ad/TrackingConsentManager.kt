package com.minseonglove.jlptwords.ad

interface TrackingConsentManager {
    /**
     * 맞춤 광고에 쓰이는 광고 식별자 접근 권한을 요청한다.
     * iOS 는 ATT 팝업을 띄우고, 안드로이드는 대응 개념이 없어 아무 것도 하지 않는다.
     */
    suspend fun requestAuthorizationIfNeeded()
}
