package com.minseonglove.jlptwords.entity

/**
 * 홈 화면 '계속 학습하기'에 표시할 세션.
 * [sessionIndex] 는 세션이 속한 레벨 내 순번(0-base)으로, 학습 화면 진입 시 그대로 전달한다.
 */
data class ContinueStudySession(
    val session: StudySession,
    val sessionIndex: Int,
    val status: ContinueSessionStatus,
    val progress: Int,
)

enum class ContinueSessionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}

sealed interface ContinueSessionResult {
    data class Ready(
        val item: ContinueStudySession,
    ) : ContinueSessionResult

    /** 대상 급수의 세션이 아직 생성되지 않음(단어 미초기화). 초기화 후 재시도가 필요하다. */
    data class RequiresInitialization(
        val level: JLPTLevel,
    ) : ContinueSessionResult
}
