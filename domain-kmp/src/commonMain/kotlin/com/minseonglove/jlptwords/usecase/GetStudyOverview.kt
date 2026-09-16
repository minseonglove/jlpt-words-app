package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.OverviewValue
import com.minseonglove.jlptwords.entity.StudyOverview
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.repository.HomeRepository
import com.minseonglove.jlptwords.repository.StreakRepository
import com.minseonglove.jlptwords.repository.StudyRecordRepository
import com.minseonglove.jlptwords.repository.StudySessionRepository
import com.minseonglove.jlptwords.repository.TimeRepository
import com.minseonglove.jlptwords.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * 홈 화면 '학습 현황' 수치를 계산한다. 각 수치의 '자정 이후 변경 여부'를 함께 판정한다.
 * - 연속·총 출석일: 스트릭(학습 행위일) 기준, 오늘 학습했으면 변경으로 본다.
 * - 총 학습시간: 완료 기록 누적(초 단위), 오늘 생성된 기록이 있으면 변경.
 * - 학습 진행률: [levelForProgress] 급수의 세션 완료율, 오늘 '최초 완료'된 세션이 있으면 변경.
 * - 학습한 단어: 전 급수 합산 노출 단어 수, 자정 기준 스냅샷과 비교(저장소 내부 판정).
 */
class GetStudyOverview(
    private val streakRepository: StreakRepository,
    private val studyRecordRepository: StudyRecordRepository,
    private val studySessionRepository: StudySessionRepository,
    private val homeRepository: HomeRepository,
    private val timeRepository: TimeRepository,
) {
    suspend operator fun invoke(
        levelForProgress: JLPTLevel,
    ): StudyOverview =
        withContext(Dispatchers.Default) {
            val streakInfoDeferred =
                async {
                    streakRepository.getStreakInfo()
                }
            val studyRecordsDeferred =
                async {
                    studyRecordRepository.getAllStudyRecords()
                }
            val levelSessionIdsDeferred =
                async {
                    studySessionRepository
                        .getStudySessionsByLevel(levelForProgress)
                        .map { it.id }
                }
            val studiedWordCountDeferred =
                async {
                    homeRepository.getStudiedWordCount()
                }

            val streakInfo = streakInfoDeferred.await()
            val studyRecords = studyRecordsDeferred.await()
            val levelSessionIds = levelSessionIdsDeferred.await().toSet()

            val timeZone = TimeZone.currentSystemDefault()
            val today = timeRepository.getDeviceCurrentTimeMillis().toLocalDate(timeZone)

            val totalStudySeconds = studyRecords.sumOf { it.completionTimeSeconds }
            val isStudyTimeUpdatedToday =
                studyRecords.any { it.isCreatedAt(today, timeZone) }

            val levelRecords = studyRecords.filter { it.sessionId in levelSessionIds }
            val completedSessionCount =
                levelRecords
                    .map { it.sessionId }
                    .toSet()
                    .size
            val progressPercent =
                (completedSessionCount * 100) / levelSessionIds.size.coerceAtLeast(1)
            // 어떤 세션의 '최초 완료'가 오늘이면 진행률이 오늘 변한 것이다(재학습 완료는 진행률 불변).
            val isProgressUpdatedToday =
                levelRecords
                    .groupBy { it.sessionId }
                    .any { (_, records) ->
                        records.minByOrNull { it.createAt }?.isCreatedAt(today, timeZone) == true
                    }

            StudyOverview(
                streakDays = OverviewValue(streakInfo.streak, streakInfo.isStudyToday),
                totalAttendanceDays = OverviewValue(streakInfo.totalDays, streakInfo.isStudyToday),
                totalStudyTimeSeconds = OverviewValue(totalStudySeconds, isStudyTimeUpdatedToday),
                studiedWordCount = studiedWordCountDeferred.await(),
                totalProgressPercent = OverviewValue(progressPercent, isProgressUpdatedToday),
            )
        }

    private fun StudyRecord.isCreatedAt(
        date: LocalDate,
        timeZone: TimeZone,
    ): Boolean = createAt.toLocalDate(timeZone) == date
}
