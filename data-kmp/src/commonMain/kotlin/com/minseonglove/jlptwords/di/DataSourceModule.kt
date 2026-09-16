package com.minseonglove.jlptwords.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.minseonglove.jlptwords.datasource.ContentMetaDataSource
import com.minseonglove.jlptwords.datasource.KanjiInfoDataSource
import com.minseonglove.jlptwords.datasource.LevelDataSource
import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.datasource.StreakDataSource
import com.minseonglove.jlptwords.datasource.StudyRecordDataSource
import com.minseonglove.jlptwords.datasource.StudySessionDataSource
import com.minseonglove.jlptwords.datasource.StudyStatusDataSource
import com.minseonglove.jlptwords.datasource.TimeDataSource
import com.minseonglove.jlptwords.datasource.WordDataSource
import com.minseonglove.jlptwords.datasource.WordInitializeDataSource
import com.minseonglove.jlptwords.datasource.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDataSourceModule: Module

val dataSourceModule =
    module {
        includes(platformDataSourceModule)

        single<DataStore<Preferences>> { createDataStore() }
        single { PreferenceDataSource(get()) }
        single { StudyStatusDataSource() }
        single { TimeDataSource(get()) }
        single { LevelDataSource(get(), get()) }
        single { WordDataSource(get(), get(), get()) } // wordDao, exampleDao, firestoreService
        single { StudySessionDataSource(get()) }
        single { StreakDataSource(get()) }
        single { StudyRecordDataSource(get()) }
        single { WordInitializeDataSource(get(), get(), get(), get()) } // wordInitializeDao, wordDao, exampleDao, firestoreService
        single { KanjiInfoDataSource(get(), get()) } // kanjiInfoDao, firestoreService
        single { ContentMetaDataSource(get()) } // contentMetaDao
    }
