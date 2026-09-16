package com.minseonglove.jlptwords.entity

/**
 * 환경 설정 화면의 사용자 설정 값.
 * [notificationHour] 는 0~23 의 24시간제 시각이다 (표시 시 오전/오후로 변환).
 */
data class SettingPreferences(
    val isStudyNotificationEnabled: Boolean,
    val notificationHour: Int,
    val notificationMinute: Int,
    val isJapaneseLanguageEnabled: Boolean,
)
