package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.KanjiInfo
import com.minseonglove.jlptwords.entity.SyncMode

interface KanjiInfoRepository {
    /**
     * Firestore 업데이트 날짜가 로컬보다 새롭거나 로컬에 데이터가 없으면
     * 전체 한자 사전을 다운로드해 로컬에 저장한다.
     *
     * @param remoteDate 호출자가 미리 조회해 둔 원격 업데이트 날짜. null 이면 직접 조회한다
     *   (일괄 동기화가 content_update_date 컬렉션을 1회 조회한 값을 재사용하기 위한 것).
     * @param onSyncStart 다운로드 시작 직전에 호출된다 (진행 상태 표시용).
     *   로컬이 비어 있으면 [SyncMode.INITIALIZE], 갱신이면 [SyncMode.UPDATE].
     * @return 실제 동기화가 일어났으면 true
     */
    suspend fun syncIfNeeded(
        remoteDate: Long? = null,
        onSyncStart: suspend (SyncMode) -> Unit = {},
    ): Boolean

    /**
     * 주어진 한자들의 사전 정보를 조회한다. 사전에 없는 한자는 결과에서 제외된다.
     */
    suspend fun getKanjiInfos(kanjis: List<String>): List<KanjiInfo>
}
