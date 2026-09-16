package com.minseonglove.jlptwords.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.minseonglove.jlptwords.service.FirestoreService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformFirestoreModule: Module =
    module {
        single<FirebaseFirestore> {
            Firebase.firestore("words")
        }

        single<FirestoreService> {
            FirestoreService(get())
        }
    }
