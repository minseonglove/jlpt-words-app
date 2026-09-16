package com.minseonglove.jlptwords.di

import androidx.room.RoomDatabase
import com.minseonglove.jlptwords.db.JLPTWordsDatabase
import com.minseonglove.jlptwords.db.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module =
    module {
        single<RoomDatabase.Builder<JLPTWordsDatabase>> {
            getDatabaseBuilder(androidContext())
        }
    }
