package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streaks",
    indices = [
        Index(value = ["session_id", "days"], unique = true),
        Index(value = ["days"]),
        Index(value = ["session_id"]),
    ],
)
data class StreakEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "session_id")
    val sessionId: Int,
    @ColumnInfo(name = "days")
    val days: Int, // 1970.1.1 부터의 일수
)
