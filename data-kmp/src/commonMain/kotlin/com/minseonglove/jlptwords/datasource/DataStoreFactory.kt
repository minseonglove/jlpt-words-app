package com.minseonglove.jlptwords.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.minseonglove.jlptwords.proto.ProtoStudyStatus

expect fun createDataStore(): DataStore<Preferences>

expect fun createStudyStatusDataStore(): DataStore<ProtoStudyStatus>
