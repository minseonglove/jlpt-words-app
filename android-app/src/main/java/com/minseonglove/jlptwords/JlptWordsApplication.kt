package com.minseonglove.jlptwords

import android.app.Application
import com.google.firebase.FirebaseApp
import com.minseonglove.jlptwords.datasource.initDataStore
import com.minseonglove.jlptwords.di.adModule
import com.minseonglove.jlptwords.di.dataSourceModule
import com.minseonglove.jlptwords.di.databaseModule
import com.minseonglove.jlptwords.di.firestoreModule
import com.minseonglove.jlptwords.di.repositoryModule
import com.minseonglove.jlptwords.di.ttsModule
import com.minseonglove.jlptwords.di.useCaseModule
import com.minseonglove.jlptwords.di.viewModelModule
import com.minseonglove.jlptwords.util.ActivityProvider
import com.minseonglove.jlptwords.util.ContextProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class JlptWordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ActivityProvider.register(this)
        ContextProvider.register(this)
        initDataStore(this)
        startKoin {
            androidLogger()
            androidContext(this@JlptWordsApplication)
            modules(
                databaseModule,
                firestoreModule,
                dataSourceModule,
                repositoryModule,
                useCaseModule,
                adModule,
                ttsModule,
                viewModelModule,
            )
        }
    }
}
