package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.minseonglove.jlptwords.db.entity.StudyRecordEntity

@Dao
interface StudyRecordDao {
    @Query("SELECT * FROM study_records")
    suspend fun getAllStudyRecords(): List<StudyRecordEntity>

    @Query("SELECT * FROM study_records ORDER BY create_at DESC LIMIT 1")
    suspend fun getLatestStudyRecord(): StudyRecordEntity?

    @Query("SELECT session_id FROM study_records")
    suspend fun getAllStudyRecordSessionIds(): List<Int>

    @Query(
        """
        SELECT sr.* FROM study_records sr
        INNER JOIN study_sessions su ON sr.session_id = su.id
        WHERE su.level_code = :levelCode
    """,
    )
    suspend fun getStudyRecordsByLevel(levelCode: Int): List<StudyRecordEntity>

    @Query("SELECT * FROM study_records WHERE session_id = :sessionId ORDER BY create_at ASC")
    suspend fun getStudyRecordsBySessionId(sessionId: Int): List<StudyRecordEntity>

    @Insert
    suspend fun insert(studyRecord: StudyRecordEntity)
}
