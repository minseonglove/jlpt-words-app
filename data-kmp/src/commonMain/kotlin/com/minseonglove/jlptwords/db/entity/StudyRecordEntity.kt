package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minseonglove.jlptwords.entity.StudyRecord

@Entity(tableName = "study_records")
data class StudyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "session_id")
    val sessionId: Int,
    @ColumnInfo(name = "completion_time_seconds")
    val completionTimeSeconds: Int,
    @ColumnInfo(name = "accuracy")
    val accuracy: Int,
    @ColumnInfo(name = "create_at")
    val createAt: Long,
)

fun StudyRecordEntity.toStudyRecord(): StudyRecord {
    return StudyRecord(
        id = id,
        sessionId = sessionId,
        completionTimeSeconds = completionTimeSeconds,
        accuracy = accuracy,
        createAt = createAt,
    )
}

fun StudyRecord.toStudyRecordEntity(): StudyRecordEntity {
    return StudyRecordEntity(
        id = id,
        sessionId = sessionId,
        completionTimeSeconds = completionTimeSeconds,
        accuracy = accuracy,
        createAt = createAt,
    )
}
