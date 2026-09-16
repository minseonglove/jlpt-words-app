package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.datasource.AnalyticsDataSource
import com.minseonglove.jlptwords.datasource.AppVersionDataSource
import com.minseonglove.jlptwords.datasource.ClipBoardDataSource
import com.minseonglove.jlptwords.datasource.DeviceInfoDataSource
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataSourceModule: Module =
    module {
        single<HttpClient> { HttpClient() }
        single { AnalyticsDataSource() }
        single { ClipBoardDataSource() }
        single { AppVersionDataSource() }
        single { DeviceInfoDataSource() }
    }
