package com.minseonglove.jlptwords.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.minseonglove.jlptwords.db.dao.ContentMetaDao
import com.minseonglove.jlptwords.db.dao.ExampleDao
import com.minseonglove.jlptwords.db.dao.KanjiInfoDao
import com.minseonglove.jlptwords.db.dao.StreakDao
import com.minseonglove.jlptwords.db.dao.StudyRecordDao
import com.minseonglove.jlptwords.db.dao.StudySessionDao
import com.minseonglove.jlptwords.db.dao.WordDao
import com.minseonglove.jlptwords.db.dao.WordInitializeDao
import com.minseonglove.jlptwords.db.entity.ContentMetaEntity
import com.minseonglove.jlptwords.db.entity.ExampleSentenceEntity
import com.minseonglove.jlptwords.db.entity.KanjiInfoEntity
import com.minseonglove.jlptwords.db.entity.StreakEntity
import com.minseonglove.jlptwords.db.entity.StudyRecordEntity
import com.minseonglove.jlptwords.db.entity.StudySessionEntity
import com.minseonglove.jlptwords.db.entity.VocabularyWordEntity
import com.minseonglove.jlptwords.db.entity.WordEntity
import com.minseonglove.jlptwords.db.entity.WordLevelMappingEntity
import com.minseonglove.jlptwords.db.entity.WordStatisticEntity

@Database(
    entities = [
        WordEntity::class,
        WordStatisticEntity::class,
        WordLevelMappingEntity::class,
        StudySessionEntity::class,
        StudyRecordEntity::class,
        StreakEntity::class,
        VocabularyWordEntity::class,
        ExampleSentenceEntity::class,
        KanjiInfoEntity::class,
        ContentMetaEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(JLPTWordsDatabaseConstructor::class)
abstract class JLPTWordsDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    abstract fun wordInitializeDao(): WordInitializeDao

    abstract fun studySessionDao(): StudySessionDao

    abstract fun studyRecordDao(): StudyRecordDao

    abstract fun streakDao(): StreakDao

    abstract fun exampleDao(): ExampleDao

    abstract fun kanjiInfoDao(): KanjiInfoDao

    abstract fun contentMetaDao(): ContentMetaDao

    companion object {
        const val DATABASE_NAME = "jlpt_words_database"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object JLPTWordsDatabaseConstructor : RoomDatabaseConstructor<JLPTWordsDatabase> {
    override fun initialize(): JLPTWordsDatabase
}
