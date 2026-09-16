package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 콘텐츠 메타데이터 (key-value). 번들 스냅샷 DB 가 자신의 생성 시점 등을 담아
 * 앱이 첫 실행에서 동기화 기준 날짜를 시딩할 수 있게 한다.
 */
@Entity(tableName = "content_meta")
data class ContentMetaEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: Long,
)
