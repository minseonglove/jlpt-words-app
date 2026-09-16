package com.minseonglove.jlptwords.entity

/** 콘텐츠를 처음 받는지, 받아둔 것을 갱신하는지 구분 (로딩 문구 분기용) */
enum class SyncMode {
    INITIALIZE,
    UPDATE,
}

/**
 * 전 급수 콘텐츠 동기화(스플래시 차단 적재)의 진행 상태.
 * 단계 상태([Readings]·[Words]·[Examples]·[Kanji])는 실제 다운로드가 일어날 때만 방출되므로,
 * 이미 최신인 실행에서는 [Checking] 이후 곧바로 [Finished] 가 온다.
 */
sealed interface AllContentsSyncProgress {
    /** 급수별 콘텐츠 날짜를 비교해 받을 것이 있는지 확인하는 중 */
    data object Checking : AllContentsSyncProgress

    /** 요미(발음) 사전 다운로드 중 */
    data class Readings(
        val mode: SyncMode,
    ) : AllContentsSyncProgress

    /** 급수별 단어 본체 다운로드 중. [done]/[total] 은 다운로드가 필요한 급수 기준 진행률 */
    data class Words(
        val mode: SyncMode,
        val done: Int,
        val total: Int,
    ) : AllContentsSyncProgress

    /** 급수별 예문 다운로드 중. [done]/[total] 은 다운로드가 필요한 급수 기준 진행률 */
    data class Examples(
        val mode: SyncMode,
        val done: Int,
        val total: Int,
    ) : AllContentsSyncProgress

    /** 한자 사전 다운로드 중 */
    data class Kanji(
        val mode: SyncMode,
    ) : AllContentsSyncProgress

    /** 동기화 종결. [result] 로 핵심 데이터 확보 여부를 판단한다 */
    data class Finished(
        val result: AllContentsSyncResult,
    ) : AllContentsSyncProgress
}
