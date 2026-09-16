package com.minseonglove.jlptwords.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.minseonglove.jlptwords.proto.ProtoStudyStatus
import com.minseonglove.jlptwords.serializer.StudyStatusSerializer
import okio.FileSystem
import okio.Path.Companion.toPath

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

private lateinit var appContext: Context

fun initDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun createDataStore(): DataStore<Preferences> {
    return appContext.dataStore
}

actual fun createStudyStatusDataStore(): DataStore<ProtoStudyStatus> {
    return DataStoreFactory.create(
        storage =
            OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = StudyStatusSerializer,
                producePath = {
                    appContext.filesDir
                        .resolve("study_status.pb")
                        .absolutePath
                        .toPath()
                },
            ),
    )
}
