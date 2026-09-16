package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.repository.HomeRepository

class GetTodayExample(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(
        level: JLPTLevel,
    ): DailyExample? {
        return homeRepository.getTodayExample(level)
    }
}
