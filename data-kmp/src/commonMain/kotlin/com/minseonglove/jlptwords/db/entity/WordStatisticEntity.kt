package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "words_statistics",
    primaryKeys = ["word_id"],
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE, // WordEntity가 지워지면 통계도 함께 지워지도록
        ),
    ],
)
data class WordStatisticEntity(
    @ColumnInfo(name = "word_id")
    val wordId: Int,
    @ColumnInfo(name = "appearance_count", defaultValue = "0")
    val appearanceCount: Int = 0,
    @ColumnInfo(name = "correct_count", defaultValue = "0")
    val correctCount: Int = 0,
)
