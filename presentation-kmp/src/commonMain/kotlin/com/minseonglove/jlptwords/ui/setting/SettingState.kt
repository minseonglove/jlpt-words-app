package com.minseonglove.jlptwords.ui.setting

data class SettingState(
    val version: String = "",
    val adZeroRemainingHours: Int = 0,
    val isStudyNotificationEnabled: Boolean = false,
    val notificationHour: Int = 22,
    val notificationMinute: Int = 0,
    val isJapaneseLanguageEnabled: Boolean = false,
    val isAdZeroDialogShown: Boolean = false,
    val isNotificationTimePickerShown: Boolean = false,
)
