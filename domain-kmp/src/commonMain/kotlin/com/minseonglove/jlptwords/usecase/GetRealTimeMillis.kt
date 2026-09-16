package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.TimeRepository

class GetRealTimeMillis(
    private val repository: TimeRepository,
) {
    suspend operator fun invoke(): Long? {
        return repository.getRealCurrentTimeMillis()
    }
}
