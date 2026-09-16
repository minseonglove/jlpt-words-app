package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.repository.WordInitializeRepository
import kotlinx.coroutines.flow.Flow

/**
 * 전 급수의 단어·예문과 전역 사전(요미·한자)을 동기화한다.
 * 스플래시에서 차단형으로 호출해 모든 화면(검색·단어 상세 등)에서
 * 급수와 무관하게 단어·예문이 보이도록 보장한다.
 *
 * 진행 상태를 단계별로 방출하며 마지막 원소는 항상 [AllContentsSyncProgress.Finished] 다.
 */
class InitializeAllContents(
    private val repository: WordInitializeRepository,
) {
    operator fun invoke(): Flow<AllContentsSyncProgress> = repository.initializeAllContents()
}
