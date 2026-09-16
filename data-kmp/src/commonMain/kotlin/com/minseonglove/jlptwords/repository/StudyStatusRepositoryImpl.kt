package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.StudyStatusDataSource
import com.minseonglove.jlptwords.entity.StudyStatus

class StudyStatusRepositoryImpl(
    private val dataSource: StudyStatusDataSource,
) : StudyStatusRepository {
    override suspend fun getStudyStatus(): StudyStatus? {
        return dataSource.getStudyStatus()
    }

    override suspend fun setStudyStatus(status: StudyStatus?) {
        dataSource.setStudyStatus(status)
    }
}
