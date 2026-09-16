package com.minseonglove.jlptwords.datasource

expect class AnalyticsDataSource {
    fun logEvent(
        name: String,
        params: Map<String, AnalyticsParam>,
    )
}

// Firebase Analytics 파라미터로 넣을 수 있는 값은 문자열/숫자로 제한된다.
sealed interface AnalyticsParam {
    data class Text(
        val value: String,
    ) : AnalyticsParam

    data class Numeric(
        val value: Long,
    ) : AnalyticsParam
}
