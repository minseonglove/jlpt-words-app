package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.StudySessionDao
import com.minseonglove.jlptwords.db.entity.StudySessionEntity
import com.minseonglove.jlptwords.entity.JLPTLevel

class StudySessionDataSource(
    private val studySessionDao: StudySessionDao,
) {
    suspend fun getSession(
        id: Int,
    ): StudySessionEntity? {
        return studySessionDao.getSessionById(id)
    }

    suspend fun getStudySessionsByLevel(
        level: JLPTLevel,
    ): List<StudySessionEntity> {
        return studySessionDao.getStudySessionsByLevel(level.code)
    }

    suspend fun getStudySessionIdsByLevel(
        level: JLPTLevel,
    ): List<Int> {
        return studySessionDao.getStudySessionIdsByLevel(level.code)
    }

    suspend fun getStudySessionCount(
        level: JLPTLevel,
    ): Int {
        return studySessionDao.getStudySessionCountByLevel(level.code)
    }

    suspend fun getLevelBySessionId(id: Int): JLPTLevel {
        val session = studySessionDao.getSessionById(id)
        return JLPTLevel.fromCode(session?.levelCode ?: 1)
    }
}
