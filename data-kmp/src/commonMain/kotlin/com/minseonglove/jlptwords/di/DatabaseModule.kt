package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.db.JLPTWordsDatabase
import com.minseonglove.jlptwords.db.dao.ContentMetaDao
import com.minseonglove.jlptwords.db.dao.ExampleDao
import com.minseonglove.jlptwords.db.dao.KanjiInfoDao
import com.minseonglove.jlptwords.db.dao.StreakDao
import com.minseonglove.jlptwords.db.dao.StudyRecordDao
import com.minseonglove.jlptwords.db.dao.StudySessionDao
import com.minseonglove.jlptwords.db.dao.WordDao
import com.minseonglove.jlptwords.db.dao.WordInitializeDao
import com.minseonglove.jlptwords.db.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDatabaseModule: Module

val databaseModule =
    module {
        includes(platformDatabaseModule)

        single<JLPTWordsDatabase> {
            getRoomDatabase(get())
        }

        single<WordDao> { get<JLPTWordsDatabase>().wordDao() }
        single<StudySessionDao> { get<JLPTWordsDatabase>().studySessionDao() }
        single<StudyRecordDao> { get<JLPTWordsDatabase>().studyRecordDao() }
        single<StreakDao> { get<JLPTWordsDatabase>().streakDao() }
        single<WordInitializeDao> { get<JLPTWordsDatabase>().wordInitializeDao() }
        single<ExampleDao> { get<JLPTWordsDatabase>().exampleDao() }
        single<KanjiInfoDao> { get<JLPTWordsDatabase>().kanjiInfoDao() }
        single<ContentMetaDao> { get<JLPTWordsDatabase>().contentMetaDao() }
    }
