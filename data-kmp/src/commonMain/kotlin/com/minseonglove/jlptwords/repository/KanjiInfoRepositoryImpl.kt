package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.KanjiInfoDataSource
import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.entity.KanjiInfo
import com.minseonglove.jlptwords.entity.SyncMode

class KanjiInfoRepositoryImpl(
    private val kanjiInfoDataSource: KanjiInfoDataSource,
    private val preferenceDataSource: PreferenceDataSource,
) : KanjiInfoRepository {
    override suspend fun syncIfNeeded(
        remoteDate: Long?,
        onSyncStart: suspend (SyncMode) -> Unit,
    ): Boolean {
        val date = remoteDate ?: kanjiInfoDataSource.getKanjiUpdateDate()
        val localDate = preferenceDataSource.getKanjiUpdateTime()
        val localCount = kanjiInfoDataSource.getLocalKanjiCount()

        if (date == 0L) return false
        if (date <= localDate && localCount > 0) return false

        onSyncStart(if (localCount == 0) SyncMode.INITIALIZE else SyncMode.UPDATE)
        kanjiInfoDataSource.syncKanjiInfo()
        preferenceDataSource.setKanjiUpdateTime(date)
        return true
    }

    override suspend fun getKanjiInfos(kanjis: List<String>): List<KanjiInfo> = kanjiInfoDataSource.getKanjiInfos(kanjis)
}
