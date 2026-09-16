package com.minseonglove.jlptwords.di

import com.minseonglove.jlptwords.service.FirestoreService
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import swiftPMImport.jlpt.words.data.kmp.FIRFirestore

@OptIn(ExperimentalForeignApi::class)
actual val platformFirestoreModule: Module =
    module {
        single<FIRFirestore> {
            FIRFirestore.firestoreForDatabase("words")
        }

        single<FirestoreService> {
            FirestoreService(get())
        }
    }
