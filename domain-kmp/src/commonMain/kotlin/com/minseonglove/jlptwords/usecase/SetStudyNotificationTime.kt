package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.SettingRepository

class SetStudyNotificationTime(
    private val settingRepository: SettingRepository,
) {
    suspend operator fun invoke(
        hour: Int,
        minute: Int,
    ) {
        settingRepository.setStudyNotificationTime(
            hour = hour,
            minute = minute,
        )
    }
}
