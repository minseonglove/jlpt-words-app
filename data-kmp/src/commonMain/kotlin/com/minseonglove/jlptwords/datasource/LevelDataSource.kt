package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.WordDao
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.service.FirestoreService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LevelDataSource(
    private val firestoreService: FirestoreService,
    private val wordDao: WordDao,
) {
    private var wordCountCache: HashMap<JLPTLevel, Int>? = null

    suspend fun getAllWordCount(): HashMap<JLPTLevel, Int> {
        return wordCountCache?.takeIf {
            isWordCountCacheValid(it)
        } ?: getRemoteWordsCount()
    }

    private fun isWordCountCacheValid(
        cache: HashMap<JLPTLevel, Int>,
    ): Boolean {
        // 하나라도 사이즈가 비어있다면 사이즈를 가져오다 문제가 생긴거다
        return JLPTLevel.entries.all {
            cache.getOrElse(it) { 0 } > 0
        }
    }

    private suspend fun getWordCountByLevel(level: JLPTLevel): Int {
        val localCount = wordDao.getWordCountByLevel(level.code)
        return if (localCount > 0) {
            localCount
        } else {
            firestoreService.getWordCount(level)
        }
    }

    private suspend fun getRemoteWordsCount(): HashMap<JLPTLevel, Int> =
        coroutineScope {
            val counts =
                JLPTLevel.entries
                    .map { level ->
                        async {
                            // TODO: 2025. 7. 19. 실패시 DB에서 카운트를 가져오는 것을 시도
                            runCatching {
                                level to getWordCountByLevel(level)
                            }.getOrDefault(level to 0)
                        }
                    }.awaitAll()

            // HashMap으로 변환하여 반환
            HashMap<JLPTLevel, Int>()
                .apply {
                    counts.forEach { (level, count) ->
                        put(level, count)
                    }
                }.also {
                    wordCountCache = it
                }
        }
}
