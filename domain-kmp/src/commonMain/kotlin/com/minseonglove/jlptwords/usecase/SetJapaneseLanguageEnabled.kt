package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.SettingRepository

class SetJapaneseLanguageEnabled(
    private val settingRepository: SettingRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingRepository.setJapaneseLanguageEnabled(enabled)
    }
}
