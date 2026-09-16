package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.StudyRecordDao
import com.minseonglove.jlptwords.db.entity.StudyRecordEntity
import com.minseonglove.jlptwords.db.entity.toStudyRecordEntity
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyRecord

class StudyRecordDataSource(
    private val studyRecordDao: StudyRecordDao,
) {
    suspend fun getAllStudyRecords(): List<StudyRecordEntity> {
        return studyRecordDao.getAllStudyRecords()
    }

    suspend fun getLatestStudyRecord(): StudyRecordEntity? {
        return studyRecordDao.getLatestStudyRecord()
    }

    suspend fun getAllStudyRecordSessionIds(): List<Int> {
        return studyRecordDao.getAllStudyRecordSessionIds()
    }

    suspend fun getStudyRecordsByLevel(level: JLPTLevel): List<StudyRecordEntity> {
        return studyRecordDao.getStudyRecordsByLevel(level.code)
    }

    suspend fun getStudyRecordsBySessionId(sessionId: Int): List<StudyRecordEntity> {
        return studyRecordDao.getStudyRecordsBySessionId(sessionId)
    }

    suspend fun addStudyRecord(
        studyRecord: StudyRecord,
    ) {
        studyRecordDao.insert(studyRecord.toStudyRecordEntity())
    }
}
