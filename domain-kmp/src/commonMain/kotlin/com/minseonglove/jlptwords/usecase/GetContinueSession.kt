package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.ContinueSessionResult
import com.minseonglove.jlptwords.entity.ContinueSessionStatus
import com.minseonglove.jlptwords.entity.ContinueStudySession
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudySessionRepository
import com.minseonglove.jlptwords.repository.StudyStatusRepository

/**
 * 홈 화면 '계속 학습하기'에 표시할 세션을 결정한다.
 * 1. 진행 중인 세션이 있으면 그 세션
 * 2. 없으면 가장 최근 완료한 세션의 다음 세션
 * 3. 직전 완료 세션이 급수의 마지막이면 다음 급수(N5→N1 방향)의 첫 세션
 * 4. N1 의 마지막이면 N1 마지막 세션을 완료 상태로 그대로 표시
 * 5. 이력이 전혀 없으면 [currentLevel] 의 첫 세션
 *
 * 대상 급수의 세션이 아직 생성되지 않았다면(단어 미초기화)
 * [ContinueSessionResult.RequiresInitialization] 를 반환한다.
 */
class GetContinueSession(
    private val studyStatusRepository: StudyStatusRepository,
    private val studyRecordRepository: StudyRecordRepository,
    private val studySessionRepository: StudySessionRepository,
) {
    suspend operator fun invoke(
        currentLevel: JLPTLevel,
    ): ContinueSessionResult {
        val studyStatus = studyStatusRepository.getStudyStatus()
        if (studyStatus != null) {
            val sessions = studySessionRepository.getStudySessionsByLevel(studyStatus.level)
            val index = sessions.indexOfFirst { it.id == studyStatus.sessionId }
            if (index >= 0) {
                val progress =
                    (studyStatus.totalKnownWordIds.size * 100) /
                        studyStatus.totalWordSize.coerceAtLeast(1)
                return ContinueSessionResult.Ready(
                    ContinueStudySession(
                        session = sessions[index],
                        sessionIndex = index,
                        status = ContinueSessionStatus.IN_PROGRESS,
                        progress = progress,
                    ),
                )
            }
        }

        val lastRecord =
            studyRecordRepository.getLatestStudyRecord()
                ?: return firstSessionOf(currentLevel)

        val level = studySessionRepository.getLevelBySessionId(lastRecord.sessionId)
        val sessions = studySessionRepository.getStudySessionsByLevel(level)
        val index = sessions.indexOfFirst { it.id == lastRecord.sessionId }
        if (index == -1) {
            return firstSessionOf(currentLevel)
        }

        if (index < sessions.lastIndex) {
            return ContinueSessionResult.Ready(
                ContinueStudySession(
                    session = sessions[index + 1],
                    sessionIndex = index + 1,
                    status = ContinueSessionStatus.NOT_STARTED,
                    progress = 0,
                ),
            )
        }

        val nextLevel = JLPTLevel.entries.getOrNull(level.ordinal + 1)
        if (nextLevel == null) {
            // N1 마지막 세션까지 완료 → 마지막 세션을 완료 상태로 그대로 표시
            return ContinueSessionResult.Ready(
                ContinueStudySession(
                    session = sessions[index],
                    sessionIndex = index,
                    status = ContinueSessionStatus.COMPLETED,
                    progress = 100,
                ),
            )
        }
        return firstSessionOf(nextLevel)
    }

    private suspend fun firstSessionOf(
        level: JLPTLevel,
    ): ContinueSessionResult {
        val firstSession =
            studySessionRepository
                .getStudySessionsByLevel(level)
                .firstOrNull()
                ?: return ContinueSessionResult.RequiresInitialization(level)
        return ContinueSessionResult.Ready(
            ContinueStudySession(
                session = firstSession,
                sessionIndex = 0,
                status = ContinueSessionStatus.NOT_STARTED,
                progress = 0,
            ),
        )
    }
}
