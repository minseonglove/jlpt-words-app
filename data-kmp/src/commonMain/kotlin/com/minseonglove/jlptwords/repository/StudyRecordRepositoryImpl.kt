package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.StudyRecordDataSource
import com.minseonglove.jlptwords.db.entity.toStudyRecord
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyRecord

class StudyRecordRepositoryImpl(
    private val studyRecordDataSource: StudyRecordDataSource,
) : StudyRecordRepository {
    override suspend fun getAllStudyRecords(): List<StudyRecord> {
        return studyRecordDataSource.getAllStudyRecords().map {
            it.toStudyRecord()
        }
    }

    override suspend fun getLatestStudyRecord(): StudyRecord? {
        return studyRecordDataSource.getLatestStudyRecord()?.toStudyRecord()
    }

    override suspend fun getStudyRecordsByLevel(level: JLPTLevel): List<StudyRecord> {
        return studyRecordDataSource.getStudyRecordsByLevel(level).map {
            it.toStudyRecord()
        }
    }

    override suspend fun getStudyRecordsBySessionId(sessionId: Int): List<StudyRecord> {
        return studyRecordDataSource.getStudyRecordsBySessionId(sessionId).map {
            it.toStudyRecord()
        }
    }

    override suspend fun addStudyRecord(
        sessionId: Int,
        completionTimeSeconds: Int,
        accuracy: Int,
        createAt: Long,
    ) {
        studyRecordDataSource.addStudyRecord(
            StudyRecord(
                id = 0,
                sessionId = sessionId,
                completionTimeSeconds = completionTimeSeconds,
                accuracy = accuracy,
                createAt = createAt,
            ),
        )
    }
}
