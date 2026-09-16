package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.ContentMetaDao

class ContentMetaDataSource(
    private val contentMetaDao: ContentMetaDao,
) {
    /** 번들 스냅샷 DB 의 생성 시점 (ms). 스냅샷 없이 만들어진 DB 면 null. */
    suspend fun getSnapshotDate(): Long? = contentMetaDao.get(KEY_SNAPSHOT_DATE)

    companion object {
        const val KEY_SNAPSHOT_DATE = "snapshot_date"
    }
}
