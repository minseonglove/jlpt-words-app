package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StudySession

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "level_code")
    val levelCode: Int,
    @ColumnInfo(name = "session_type_code")
    val sessionTypeCode: Int,
    @ColumnInfo(name = "start_number")
    val startNumber: Int,
    @ColumnInfo(name = "end_number")
    val endNumber: Int,
    @ColumnInfo(name = "words_size")
    val wordsSize: Int,
)

fun StudySessionEntity.toStudySession(): StudySession {
    return StudySession(
        id = id,
        level = JLPTLevel.fromCode(levelCode),
        type = SessionType.fromCode(sessionTypeCode),
        startNumber = startNumber,
        endNumber = endNumber,
        wordsSize = wordsSize,
    )
}
