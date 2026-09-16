package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.SettingRepository
import kotlinx.coroutines.flow.Flow

class ObserveJapaneseLanguageEnabled(
    private val settingRepository: SettingRepository,
) {
    operator fun invoke(): Flow<Boolean> = settingRepository.observeJapaneseLanguageEnabled()
}
