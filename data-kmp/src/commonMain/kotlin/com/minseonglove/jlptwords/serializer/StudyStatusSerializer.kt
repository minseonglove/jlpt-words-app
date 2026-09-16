package com.minseonglove.jlptwords.serializer

import androidx.datastore.core.okio.OkioSerializer
import com.minseonglove.jlptwords.proto.ProtoStudyStatus
import okio.BufferedSink
import okio.BufferedSource
import okio.IOException

object StudyStatusSerializer : OkioSerializer<ProtoStudyStatus> {
    override val defaultValue: ProtoStudyStatus
        get() = ProtoStudyStatus()

    override suspend fun readFrom(source: BufferedSource): ProtoStudyStatus {
        return try {
            ProtoStudyStatus.ADAPTER.decode(source)
        } catch (exception: IOException) {
            throw IOException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ProtoStudyStatus,
        sink: BufferedSink,
    ) {
        ProtoStudyStatus.ADAPTER.encode(sink, t)
    }
}
