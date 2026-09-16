package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.StudyStatus

interface StudyStatusRepository {
    suspend fun getStudyStatus(): StudyStatus?

    suspend fun setStudyStatus(
        status: StudyStatus?,
    )
}
