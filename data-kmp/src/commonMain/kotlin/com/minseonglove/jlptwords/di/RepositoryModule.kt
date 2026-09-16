package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.repository.AdRepository
import com.minseonglove.jlptwords.repository.AdRepositoryImpl
import com.minseonglove.jlptwords.repository.AnalyticsRepository
import com.minseonglove.jlptwords.repository.AnalyticsRepositoryImpl
import com.minseonglove.jlptwords.repository.AppVersionRepository
import com.minseonglove.jlptwords.repository.AppVersionRepositoryImpl
import com.minseonglove.jlptwords.repository.ClipBoardRepository
import com.minseonglove.jlptwords.repository.ClipBoardRepositoryImpl
import com.minseonglove.jlptwords.repository.HomeRepository
import com.minseonglove.jlptwords.repository.HomeRepositoryImpl
import com.minseonglove.jlptwords.repository.InquiryRepository
import com.minseonglove.jlptwords.repository.InquiryRepositoryImpl
import com.minseonglove.jlptwords.repository.KanjiInfoRepository
import com.minseonglove.jlptwords.repository.KanjiInfoRepositoryImpl
import com.minseonglove.jlptwords.repository.LevelRepository
import com.minseonglove.jlptwords.repository.LevelRepositoryImpl
import com.minseonglove.jlptwords.repository.SettingRepository
import com.minseonglove.jlptwords.repository.SettingRepositoryImpl
import com.minseonglove.jlptwords.repository.StreakRepository
import com.minseonglove.jlptwords.repository.StreakRepositoryImpl
import com.minseonglove.jlptwords.repository.StudyHelpRepository
import com.minseonglove.jlptwords.repository.StudyHelpRepositoryImpl
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudyRecordRepositoryImpl
import com.minseonglove.jlptwords.repository.StudySessionRepository
import com.minseonglove.jlptwords.repository.StudySessionRepositoryImpl
import com.minseonglove.jlptwords.repository.StudyStatusRepository
import com.minseonglove.jlptwords.repository.StudyStatusRepositoryImpl
import com.minseonglove.jlptwords.repository.TimeRepository
import com.minseonglove.jlptwords.repository.TimeRepositoryImpl
import com.minseonglove.jlptwords.repository.WordInitializeRepository
import com.minseonglove.jlptwords.repository.WordInitializeRepositoryImpl
import com.minseonglove.jlptwords.repository.WordRepository
import com.minseonglove.jlptwords.repository.WordRepositoryImpl
import org.koin.dsl.module

val repositoryModule =
    module {
        // Singleton Repositories
        single<StudyStatusRepository> { StudyStatusRepositoryImpl(get()) }
        single<AdRepository> { AdRepositoryImpl(get(), get()) }
        single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
        single<TimeRepository> { TimeRepositoryImpl(get()) }

        single<KanjiInfoRepository> { KanjiInfoRepositoryImpl(get(), get()) } // KanjiInfoDataSource, PreferenceDataSource

        // Factory (ViewModel scoped) Repositories
        factory<WordInitializeRepository> { WordInitializeRepositoryImpl(get(), get(), get(), get(), get()) }
        factory<StudySessionRepository> { StudySessionRepositoryImpl(get(), get()) }
        factory<WordRepository> { WordRepositoryImpl(get()) }
        factory<StudyRecordRepository> { StudyRecordRepositoryImpl(get()) }
        factory<StreakRepository> { StreakRepositoryImpl(get()) }
        factory<LevelRepository> { LevelRepositoryImpl(get(), get(), get(), get()) }
        factory<ClipBoardRepository> { ClipBoardRepositoryImpl(get()) }
        factory<AppVersionRepository> { AppVersionRepositoryImpl(get()) }
        factory<InquiryRepository> { InquiryRepositoryImpl(get()) }
        factory<HomeRepository> { HomeRepositoryImpl(get(), get()) } // WordDataSource, PreferenceDataSource
        factory<SettingRepository> { SettingRepositoryImpl(get()) } // PreferenceDataSource
        factory<StudyHelpRepository> { StudyHelpRepositoryImpl(get()) } // PreferenceDataSource
    }
