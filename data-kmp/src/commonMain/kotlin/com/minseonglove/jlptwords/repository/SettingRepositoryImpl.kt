package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.entity.SettingPreferences
import kotlinx.coroutines.flow.Flow

class SettingRepositoryImpl(
    private val preferenceDataSource: PreferenceDataSource,
) : SettingRepository {
    override fun observeJapaneseLanguageEnabled(): Flow<Boolean> = preferenceDataSource.observeJapaneseLanguageEnabled()

    override suspend fun getSettingPreferences(): SettingPreferences {
        val record = preferenceDataSource.getSettingPreferencesRecord()
        return SettingPreferences(
            isStudyNotificationEnabled = record.isStudyNotificationEnabled,
            notificationHour = record.notificationHour,
            notificationMinute = record.notificationMinute,
            isJapaneseLanguageEnabled = record.isJapaneseLanguageEnabled,
        )
    }

    override suspend fun setStudyNotificationEnabled(enabled: Boolean) {
        preferenceDataSource.setStudyNotificationEnabled(enabled)
    }

    override suspend fun setStudyNotificationTime(
        hour: Int,
        minute: Int,
    ) {
        preferenceDataSource.setStudyNotificationTime(
            hour = hour,
            minute = minute,
        )
    }

    override suspend fun setJapaneseLanguageEnabled(enabled: Boolean) {
        preferenceDataSource.setJapaneseLanguageEnabled(enabled)
    }
}
