package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.AdRepository
import com.minseonglove.jlptwords.repository.TimeRepository

/**
 * 광고 제거 남은 시간(시간 단위 올림)을 조회한다.
 * 만료됐거나 미적용이면 0 (= 상단바 "AD" 노출 상태).
 *
 * 실제 광고 노출 여부([com.minseonglove.jlptwords.repository.AdRepository.isAdZeroEnabled])와 같은
 * 네트워크 시각을 쓴다. 기기 시각으로 표시하면 시각을 못 받은 상황에서 상단바에는 남은 시간이
 * 떠 있는데 광고는 계속 나오는 상태가 된다.
 */
class GetAdZeroRemainingHours(
    private val adRepository: AdRepository,
    private val timeRepository: TimeRepository,
) {
    suspend operator fun invoke(): Int {
        val nowMillis = timeRepository.getRealCurrentTimeMillis() ?: return 0
        return calculateAdZeroRemainingHours(
            expireAtMillis = adRepository.getAdZeroTime(),
            nowMillis = nowMillis,
        )
    }
}
