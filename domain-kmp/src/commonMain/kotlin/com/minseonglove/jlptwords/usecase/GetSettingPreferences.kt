package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.SettingPreferences
import com.minseonglove.jlptwords.repository.SettingRepository

class GetSettingPreferences(
    private val settingRepository: SettingRepository,
) {
    suspend operator fun invoke(): SettingPreferences {
        return settingRepository.getSettingPreferences()
    }
}
