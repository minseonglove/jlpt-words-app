package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.PreferenceDataSource

class StudyHelpRepositoryImpl(
    private val preferenceDataSource: PreferenceDataSource,
) : StudyHelpRepository {
    override suspend fun hasSeenStudyHelp(): Boolean {
        return preferenceDataSource.getStudyHelpSeen()
    }

    override suspend fun setStudyHelpSeen() {
        preferenceDataSource.setStudyHelpSeen()
    }
}
