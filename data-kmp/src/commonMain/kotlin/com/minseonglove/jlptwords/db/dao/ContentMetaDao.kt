package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minseonglove.jlptwords.db.entity.ContentMetaEntity

@Dao
interface ContentMetaDao {
    @Query("SELECT value FROM content_meta WHERE key = :key")
    suspend fun get(key: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entity: ContentMetaEntity)
}
