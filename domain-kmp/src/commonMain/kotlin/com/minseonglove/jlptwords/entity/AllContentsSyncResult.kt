package com.minseonglove.jlptwords.entity

/** 전 급수 콘텐츠(단어·예문·전역 사전) 동기화 결과 */
enum class AllContentsSyncResult {
    /** 전 급수 핵심 데이터가 이미 로컬에 있고 새로 받은 것이 없음 */
    UP_TO_DATE,

    /** 새 데이터를 받아 전 급수 핵심 데이터가 확보됨 */
    UPDATED,

    /** 일부 급수의 핵심 데이터(단어 또는 예문)를 확보하지 못함 (오프라인 등) */
    MISSING,
    ;

    /** 전 급수 핵심 데이터가 로컬에 확보됐는지 */
    val isReady: Boolean
        get() = this != MISSING
}
