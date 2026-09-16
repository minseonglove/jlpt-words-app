package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyStatistic
import com.minseonglove.jlptwords.repository.StreakRepository
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudySessionRepository
import com.minseonglove.jlptwords.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone

class GetStudyStatistic(
    private val streakRepository: StreakRepository,
    private val studyRecordRepository: StudyRecordRepository,
    private val studySessionRepository: StudySessionRepository,
) {
    suspend operator fun invoke(
        levelForTotalProgress: JLPTLevel,
    ): StudyStatistic =
        withContext(Dispatchers.Default) {
            val streakInfoDeferred =
                async {
                    streakRepository.getStreakInfo()
                }
            val studyRecordsDeferred =
                async {
                    studyRecordRepository.getAllStudyRecords()
                }
            val (streakInfo, studyRecords) = streakInfoDeferred.await() to studyRecordsDeferred.await()

            val timeZone = TimeZone.currentSystemDefault()

            // 전체 진행률 계산 (0.0 ~ 1.0)
            val levelSessionIds =
                studySessionRepository
                    .getStudySessionsByLevel(levelForTotalProgress)
                    .map {
                        it.id
                    }.toSet()
            val completedSessionCount =
                studyRecords
                    .filter { it.sessionId in levelSessionIds }
                    .map { it.sessionId }
                    .toSet()
                    .size
            // totalSessionCount 는 위에서 이미 로드한 세션 목록 크기로 대체(중복 COUNT 쿼리 제거).
            // session id 는 PK 라 levelSessionIds.size == COUNT(*) 가 보장된다.
            val totalProgress = completedSessionCount / levelSessionIds.size.toFloat().coerceAtLeast(1f)
            val totalStudyDays =
                studyRecords
                    .map { it.createAt.toLocalDate(timeZone) }
                    .distinct()
                    .count()

            // 총 누적 학습 시간 (초)
            val totalElapsedTimeSeconds = studyRecords.sumOf { it.completionTimeSeconds }

            // 평균 일일 학습 시간 (초)
            val averageDailyElapsedTimeSeconds =
                totalElapsedTimeSeconds / totalStudyDays.coerceAtLeast(1)

            StudyStatistic(
                streakInfo = streakInfo,
                totalProgress = totalProgress,
                totalElapsedTimeSeconds = totalElapsedTimeSeconds,
                averageDailyElapsedTimeSeconds = averageDailyElapsedTimeSeconds,
                weeklyElapsedTimeSeconds = 0, // 계산 안함
            )
        }
}
