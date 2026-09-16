package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.AppVersionRepository

class GetAppVersionName(
    private val repository: AppVersionRepository,
) {
    suspend operator fun invoke(): String? {
        return repository.getAppVersionName()
    }
}
