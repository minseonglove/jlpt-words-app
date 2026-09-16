package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.OverviewValue

interface HomeRepository {
    /**
     * 오늘의 예문을 반환한다. 하루(자정 기준) 동안 동일한 예문이 유지되며,
     * 날짜가 바뀌거나 저장된 급수와 [level] 이 다르면 해당 급수에서 새로 추첨한다.
     * 급수에 예문 데이터가 없으면 null.
     */
    suspend fun getTodayExample(
        level: JLPTLevel,
    ): DailyExample?

    /**
     * 한 번이라도 학습(노출)된 단어 수(전 급수 합산)와 자정 이후 변경 여부를 반환한다.
     * 단어 통계에는 변경 시각이 없으므로, 자정 기준 스냅샷과의 비교로 변경 여부를 판정한다.
     */
    suspend fun getStudiedWordCount(): OverviewValue
}
