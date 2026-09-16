package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary_words",
    indices = [Index(value = ["surface"], unique = true)],
)
data class VocabularyWordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "surface")
    val surface: String,
    @ColumnInfo(name = "reading")
    val reading: String,
)
