package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.minseonglove.jlptwords.entity.Word

@Entity(
    tableName = "words",
    // 동형이의어(같은 표기, 다른 발음·뜻)가 별개 단어로 공존해야 하므로 (표기, 발음) 복합 unique.
    indices = [Index(value = ["kanji", "pronunciation"], unique = true)],
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "kanji")
    val kanji: String,
    @ColumnInfo(name = "pronunciation")
    val pronunciation: String,
    @ColumnInfo(name = "meaning")
    val meaning: String,
    @ColumnInfo(name = "part_of_speech", defaultValue = "")
    val partOfSpeech: String = "",
)
