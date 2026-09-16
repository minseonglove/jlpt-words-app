package com.minseonglove.jlptwords.repository

/** 학습 화면 도움말의 노출 이력. 한 번도 본 적 없는 사용자에게 자동으로 펼쳐 주기 위해 쓴다. */
interface StudyHelpRepository {
    suspend fun hasSeenStudyHelp(): Boolean

    suspend fun setStudyHelpSeen()
}
