package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.WordsInitializeProgress
import kotlinx.coroutines.flow.Flow

interface WordInitializeRepository {
    suspend fun initializeWordsIfEmpty(
        level: JLPTLevel,
    ): Flow<WordsInitializeProgress>

    /**
     * 전 급수의 단어·예문과 전역 사전(요미·한자)을 한 번에 동기화한다 (스플래시 차단 적재용).
     * 급수별 콘텐츠 날짜를 비교해 필요한 것만 받으므로, 이미 최신이면 날짜 확인만으로 통과한다.
     * 급수별로 독립·병렬 처리되어 일부 급수의 실패가 다른 급수의 동기화를 막지 않는다.
     *
     * 진행 상태를 단계별로 방출하며 마지막 원소는 항상 [AllContentsSyncProgress.Finished] 다.
     * 내부 브랜치(요미·한자·단어·예문)가 병렬이라 단계 방출 순서는 뒤섞일 수 있다.
     */
    fun initializeAllContents(): Flow<AllContentsSyncProgress>
}
