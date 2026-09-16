package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "word_level_mapping",
    primaryKeys = ["word_id", "level_code"],
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["word_id"]),
        Index(value = ["level_code"]),
    ],
)
data class WordLevelMappingEntity(
    @ColumnInfo(name = "word_id")
    val wordId: Int,
    @ColumnInfo(name = "level_code")
    val levelCode: Int,
)
