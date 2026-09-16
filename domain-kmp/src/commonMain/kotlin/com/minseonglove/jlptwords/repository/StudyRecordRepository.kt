package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyRecord

interface StudyRecordRepository {
    suspend fun getAllStudyRecords(): List<StudyRecord>

    /** 가장 최근에 생성된 완료 기록 1건. 기록이 없으면 null. */
    suspend fun getLatestStudyRecord(): StudyRecord?

    suspend fun getStudyRecordsByLevel(level: JLPTLevel): List<StudyRecord>

    suspend fun getStudyRecordsBySessionId(sessionId: Int): List<StudyRecord>

    suspend fun addStudyRecord(
        sessionId: Int,
        completionTimeSeconds: Int,
        accuracy: Int,
        createAt: Long,
    )
}
