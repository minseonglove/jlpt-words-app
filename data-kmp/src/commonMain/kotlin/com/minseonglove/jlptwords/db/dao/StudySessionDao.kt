package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.minseonglove.jlptwords.db.entity.StudySessionEntity

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE level_code = :levelCode ORDER BY id")
    suspend fun getStudySessionsByLevel(levelCode: Int): List<StudySessionEntity>

    @Query("SELECT id FROM study_sessions WHERE level_code = :levelCode")
    suspend fun getStudySessionIdsByLevel(levelCode: Int): List<Int>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE level_code = :levelCode")
    suspend fun getStudySessionCountByLevel(levelCode: Int): Int
}
