package com.minseonglove.jlptwords.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.minseonglove.jlptwords.proto.ProtoStudyStatus
import com.minseonglove.jlptwords.serializer.StudyStatusSerializer
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createDataStore(): DataStore<Preferences> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    val path = (requireNotNull(documentDirectory).path + "/preferences.preferences_pb").toPath()
    return PreferenceDataStoreFactory.createWithPath(produceFile = { path })
}

@OptIn(ExperimentalForeignApi::class)
actual fun createStudyStatusDataStore(): DataStore<ProtoStudyStatus> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    val path = (requireNotNull(documentDirectory).path + "/study_status.pb").toPath()
    return DataStoreFactory.create(
        storage =
            androidx.datastore.core.okio.OkioStorage(
                fileSystem = okio.FileSystem.SYSTEM,
                serializer = StudyStatusSerializer,
                producePath = { path },
            ),
    )
}
