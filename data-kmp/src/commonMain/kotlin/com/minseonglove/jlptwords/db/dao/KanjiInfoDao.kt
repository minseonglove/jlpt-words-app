package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minseonglove.jlptwords.db.entity.KanjiInfoEntity

@Dao
interface KanjiInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(kanjis: List<KanjiInfoEntity>)

    @Query("SELECT COUNT(*) FROM kanji_info")
    suspend fun getCount(): Int

    @Query("SELECT * FROM kanji_info WHERE kanji IN (:kanjis)")
    suspend fun getByKanjis(kanjis: List<String>): List<KanjiInfoEntity>
}
