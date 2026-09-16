package com.minseonglove.jlptwords.ui.setting

sealed interface SettingIntent {
    data object Initialize : SettingIntent

    data object ToggleStudyNotification : SettingIntent

    data object ShowNotificationTimePicker : SettingIntent

    data class ChangeNotificationTime(
        val hour: Int,
        val minute: Int,
    ) : SettingIntent

    data object DismissNotificationTimePicker : SettingIntent

    data object ToggleJapaneseLanguage : SettingIntent

    data object OpenInquiry : SettingIntent

    data object OpenPrivacyPolicy : SettingIntent

    data object OpenOpenSourceLicenses : SettingIntent

    data object ShowAdZeroDialog : SettingIntent

    data object DismissAdZeroDialog : SettingIntent

    data object NavigateBack : SettingIntent
}
