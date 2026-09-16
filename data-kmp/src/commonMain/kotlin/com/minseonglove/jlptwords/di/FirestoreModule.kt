package com.minseonglove.jlptwords.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformFirestoreModule: Module

val firestoreModule =
    module {
        includes(platformFirestoreModule)
    }
