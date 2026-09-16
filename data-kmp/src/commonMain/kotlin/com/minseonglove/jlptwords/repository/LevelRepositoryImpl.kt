package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.LevelDataSource
import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.datasource.StudyRecordDataSource
import com.minseonglove.jlptwords.datasource.StudySessionDataSource
import com.minseonglove.jlptwords.db.util.StudySessionGenerator
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.LevelSummary
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class LevelRepositoryImpl(
    private val levelDataSource: LevelDataSource,
    private val studyRecordDataSource: StudyRecordDataSource,
    private val studySessionDataSource: StudySessionDataSource,
    private val preferenceDataSource: PreferenceDataSource,
) : LevelRepository {
    override suspend fun getLevelSummaries(): List<LevelSummary> =
        coroutineScope {
            // 모든 레벨의 단어 개수
            val wordCountMapDeferred = async { levelDataSource.getAllWordCount() }
            // 모든 StudyRecord
            val allStudyRecordSessionIdsDeferred =
                async {
                    studyRecordDataSource.getAllStudyRecordSessionIds().toHashSet()
                }
            val wordCountMap = wordCountMapDeferred.await()
            val allStudyRecordSessionIds = allStudyRecordSessionIdsDeferred.await()

            JLPTLevel.entries
                .map { level ->
                    async {
                        val wordCount = wordCountMap[level] ?: 0
                        val totalSessionIds =
                            studySessionDataSource.getStudySessionIdsByLevel(level).toHashSet()
                        val sessionProgress =
                            calculateSessionProgress(
                                allStudyRecordSessionIds = allStudyRecordSessionIds,
                                totalSessionIds = totalSessionIds,
                            )
                        val sessionCount =
                            if (totalSessionIds.isNotEmpty()) {
                                totalSessionIds.size
                            } else {
                                StudySessionGenerator
                                    .generateStudySessions(
                                        level = level,
                                        totalWordCount = wordCount,
                                    ).size
                            }

                        LevelSummary(
                            level = level,
                            wordCount = wordCount,
                            sessionCount = sessionCount,
                            sessionProgress = sessionProgress,
                        )
                    }
                }.awaitAll()
        }

    private fun calculateSessionProgress(
        allStudyRecordSessionIds: HashSet<Int>,
        totalSessionIds: HashSet<Int>,
    ): Float {
        return runCatching {
            if (totalSessionIds.isEmpty()) {
                return 0f
            }

            val completedSessions =
                allStudyRecordSessionIds
                    .filter { id ->
                        totalSessionIds.contains(id)
                    }.size

            // sessionProgress = 완료한 세션 개수 / 총 세션 개수
            completedSessions.toFloat() / totalSessionIds.size.toFloat().coerceAtLeast(1f)
        }.getOrDefault(0f)
    }

    override suspend fun getLastSelectedLevel(): JLPTLevel? {
        return preferenceDataSource.getLastSelectedLevel()
    }

    override fun observeLastSelectedLevel(): Flow<JLPTLevel?> = preferenceDataSource.observeLastSelectedLevel()

    override suspend fun setLastSelectedLevel(level: JLPTLevel) {
        preferenceDataSource.setLastSelectedLevel(level)
    }
}
