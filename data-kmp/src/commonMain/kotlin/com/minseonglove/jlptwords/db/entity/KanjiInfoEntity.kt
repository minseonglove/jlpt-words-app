package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kanji_info")
data class KanjiInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "kanji")
    val kanji: String,
    /** 한국 한자 (예: "인간 세"). 없으면 null. */
    @ColumnInfo(name = "korean_hanja")
    val koreanHanja: String?,
    /** 음독 — ·구분자 문자열 (예: "せ·せい"). 없으면 null. */
    @ColumnInfo(name = "on_yomi")
    val onYomi: String?,
    /** 훈독 — ·구분자 문자열 (예: "よ"). 없으면 null. */
    @ColumnInfo(name = "kun_yomi")
    val kunYomi: String?,
)
