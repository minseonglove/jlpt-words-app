package com.minseonglove.jlptwords

import com.minseonglove.jlptwords.di.adModule
import com.minseonglove.jlptwords.di.dataSourceModule
import com.minseonglove.jlptwords.di.databaseModule
import com.minseonglove.jlptwords.di.firestoreModule
import com.minseonglove.jlptwords.di.repositoryModule
import com.minseonglove.jlptwords.di.ttsModule
import com.minseonglove.jlptwords.di.useCaseModule
import com.minseonglove.jlptwords.di.viewModelModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoinModules(
    appDeclaration: KoinAppDeclaration = {},
) = startKoin {
    appDeclaration()
    modules(
        // Data layer modules
        databaseModule,
        dataSourceModule,
        firestoreModule,
        repositoryModule,
        // Domain layer modules
        useCaseModule,
        // Presentation layer modules
        viewModelModule,
        adModule,
        ttsModule,
    )
}
