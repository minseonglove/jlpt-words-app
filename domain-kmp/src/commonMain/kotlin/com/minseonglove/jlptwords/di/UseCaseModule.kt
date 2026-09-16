package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.usecase.AddAdZeroTime
import com.minseonglove.jlptwords.usecase.AddStreak
import com.minseonglove.jlptwords.usecase.AddStudyRecord
import com.minseonglove.jlptwords.usecase.CopyToClipboard
import com.minseonglove.jlptwords.usecase.GetAdZeroRemainingHours
import com.minseonglove.jlptwords.usecase.GetAdZeroTime
import com.minseonglove.jlptwords.usecase.GetAllLevelSummaries
import com.minseonglove.jlptwords.usecase.GetAllStudyRecords
import com.minseonglove.jlptwords.usecase.GetAppVersionName
import com.minseonglove.jlptwords.usecase.GetContinueSession
import com.minseonglove.jlptwords.usecase.GetInquiryInfo
import com.minseonglove.jlptwords.usecase.GetLastInterstitialAdShowTime
import com.minseonglove.jlptwords.usecase.GetLastSelectedLevel
import com.minseonglove.jlptwords.usecase.GetLevelByStudySessionId
import com.minseonglove.jlptwords.usecase.GetRealTimeMillis
import com.minseonglove.jlptwords.usecase.GetSettingPreferences
import com.minseonglove.jlptwords.usecase.GetStreakInfo
import com.minseonglove.jlptwords.usecase.GetStudyOverview
import com.minseonglove.jlptwords.usecase.GetStudyRecordsByLevel
import com.minseonglove.jlptwords.usecase.GetStudyRecordsBySessionId
import com.minseonglove.jlptwords.usecase.GetStudySessionsByLevel
import com.minseonglove.jlptwords.usecase.GetStudyStatistic
import com.minseonglove.jlptwords.usecase.GetStudyStatus
import com.minseonglove.jlptwords.usecase.GetTodayExample
import com.minseonglove.jlptwords.usecase.GetWordDetail
import com.minseonglove.jlptwords.usecase.GetWordsByStudySessionId
import com.minseonglove.jlptwords.usecase.HasSeenStudyHelp
import com.minseonglove.jlptwords.usecase.IncreaseAppearanceCount
import com.minseonglove.jlptwords.usecase.IncreaseCorrectAndAppearanceCount
import com.minseonglove.jlptwords.usecase.InitializeAllContents
import com.minseonglove.jlptwords.usecase.InitializeWordsIfEmpty
import com.minseonglove.jlptwords.usecase.IsAdZeroEnabled
import com.minseonglove.jlptwords.usecase.LogStudySessionCompleted
import com.minseonglove.jlptwords.usecase.ObserveJapaneseLanguageEnabled
import com.minseonglove.jlptwords.usecase.ObserveLastSelectedLevel
import com.minseonglove.jlptwords.usecase.ResolveStudyEntry
import com.minseonglove.jlptwords.usecase.SearchWords
import com.minseonglove.jlptwords.usecase.SetJapaneseLanguageEnabled
import com.minseonglove.jlptwords.usecase.SetLastInterstitialAdShowTime
import com.minseonglove.jlptwords.usecase.SetLastSelectedLevel
import com.minseonglove.jlptwords.usecase.SetStudyHelpSeen
import com.minseonglove.jlptwords.usecase.SetStudyNotificationEnabled
import com.minseonglove.jlptwords.usecase.SetStudyNotificationTime
import com.minseonglove.jlptwords.usecase.SetStudyStatus
import org.koin.dsl.module

val useCaseModule =
    module {
        // All UseCases as factory (created per request)
        factory { AddAdZeroTime(get()) }
        factory { AddStreak(get()) }
        factory { AddStudyRecord(get()) }
        factory { CopyToClipboard(get()) }
        factory { GetAdZeroRemainingHours(get(), get()) }
        factory { GetAdZeroTime(get()) }
        factory { GetAllLevelSummaries(get()) }
        factory { GetAllStudyRecords(get()) }
        factory { GetAppVersionName(get()) }
        factory { GetContinueSession(get(), get(), get()) }
        factory { GetInquiryInfo(get()) }
        factory { GetLastInterstitialAdShowTime(get()) }
        factory { GetLastSelectedLevel(get()) }
        factory { GetLevelByStudySessionId(get()) }
        factory { GetRealTimeMillis(get()) }
        factory { GetSettingPreferences(get()) }
        factory { GetStreakInfo(get()) }
        factory { GetStudyOverview(get(), get(), get(), get(), get()) }
        factory { GetStudyRecordsByLevel(get()) }
        factory { GetStudyRecordsBySessionId(get()) }
        factory { GetStudySessionsByLevel(get()) }
        factory { GetStudyStatistic(get(), get(), get()) }
        factory { GetStudyStatus(get()) }
        factory { GetTodayExample(get()) }
        factory { GetWordDetail(get(), get()) }
        factory { GetWordsByStudySessionId(get()) }
        factory { HasSeenStudyHelp(get()) }
        factory { IncreaseAppearanceCount(get()) }
        factory { IncreaseCorrectAndAppearanceCount(get()) }
        factory { InitializeAllContents(get()) }
        factory { InitializeWordsIfEmpty(get()) }
        factory { IsAdZeroEnabled(get()) }
        factory { LogStudySessionCompleted(get()) }
        factory { ObserveJapaneseLanguageEnabled(get()) }
        factory { ObserveLastSelectedLevel(get()) }
        factory { ResolveStudyEntry(get()) }
        factory { SearchWords(get()) }
        factory { SetJapaneseLanguageEnabled(get()) }
        factory { SetLastInterstitialAdShowTime(get()) }
        factory { SetLastSelectedLevel(get()) }
        factory { SetStudyHelpSeen(get()) }
        factory { SetStudyNotificationEnabled(get()) }
        factory { SetStudyNotificationTime(get()) }
        factory { SetStudyStatus(get()) }
    }
