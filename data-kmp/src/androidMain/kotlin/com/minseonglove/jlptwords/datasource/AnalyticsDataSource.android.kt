package com.minseonglove.jlptwords.datasource

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

actual class AnalyticsDataSource(
    private val firebaseAnalytics: FirebaseAnalytics,
) {
    actual fun logEvent(
        name: String,
        params: Map<String, AnalyticsParam>,
    ) {
        val bundle =
            Bundle().apply {
                params.forEach { (key, param) ->
                    when (param) {
                        is AnalyticsParam.Text -> putString(key, param.value)
                        is AnalyticsParam.Numeric -> putLong(key, param.value)
                    }
                }
            }
        firebaseAnalytics.logEvent(name, bundle)
    }
}
