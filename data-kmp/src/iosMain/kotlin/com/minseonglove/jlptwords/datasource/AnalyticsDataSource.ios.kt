package com.minseonglove.jlptwords.datasource

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.jlpt.words.data.kmp.FIRAnalytics

@OptIn(ExperimentalForeignApi::class)
actual class AnalyticsDataSource {
    actual fun logEvent(
        name: String,
        params: Map<String, AnalyticsParam>,
    ) {
        // NSDictionary<NSString *, id> 바인딩이라 키/값 타입을 Any 로 열어 둬야 넘길 수 있다.
        // Kotlin 의 박싱된 Long 은 ObjC 경계에서 NSNumber 로 브리징된다.
        val parameters =
            buildMap<Any?, Any> {
                params.forEach { (key, param) ->
                    put(
                        key,
                        when (param) {
                            is AnalyticsParam.Text -> param.value
                            is AnalyticsParam.Numeric -> param.value
                        },
                    )
                }
            }
        FIRAnalytics.logEventWithName(name, parameters)
    }
}
