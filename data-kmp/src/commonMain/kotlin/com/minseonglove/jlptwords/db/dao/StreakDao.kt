package com.minseonglove.jlptwords.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minseonglove.jlptwords.db.entity.StreakEntity

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addStreak(streak: StreakEntity)

    @Query("SELECT DISTINCT days FROM streaks ORDER BY days DESC")
    suspend fun getAllStreakDaysDesc(): List<Int>

    @Query("SELECT DISTINCT days FROM streaks ORDER BY days ASC")
    suspend fun getAllStreakDaysAsc(): List<Int>
}
