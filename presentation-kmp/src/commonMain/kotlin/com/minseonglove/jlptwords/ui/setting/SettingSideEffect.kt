package com.minseonglove.jlptwords.ui.setting

import com.minseonglove.jlptwords.entity.InquiryInfo

sealed interface SettingSideEffect {
    data class OpenInquiry(
        val inquiryInfo: InquiryInfo,
    ) : SettingSideEffect

    data object OpenPrivacyPolicy : SettingSideEffect

    data object OpenOpenSourceLicenses : SettingSideEffect

    data object NavigateToBack : SettingSideEffect
}
