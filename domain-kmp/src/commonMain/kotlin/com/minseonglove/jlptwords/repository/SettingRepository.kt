package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.SettingPreferences
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    suspend fun getSettingPreferences(): SettingPreferences

    fun observeJapaneseLanguageEnabled(): Flow<Boolean>

    suspend fun setStudyNotificationEnabled(
        enabled: Boolean,
    )

    suspend fun setStudyNotificationTime(
        hour: Int,
        minute: Int,
    )

    suspend fun setJapaneseLanguageEnabled(
        enabled: Boolean,
    )
}
