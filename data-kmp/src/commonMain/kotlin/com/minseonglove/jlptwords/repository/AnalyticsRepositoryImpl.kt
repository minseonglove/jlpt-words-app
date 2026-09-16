package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.AnalyticsDataSource
import com.minseonglove.jlptwords.datasource.AnalyticsParam
import com.minseonglove.jlptwords.entity.JLPTLevel

class AnalyticsRepositoryImpl(
    private val analyticsDataSource: AnalyticsDataSource,
) : AnalyticsRepository {
    override fun logStudySessionCompleted(
        level: JLPTLevel,
        sessionNumber: Int,
        accuracy: Int,
        elapsedTimeSeconds: Int,
    ) {
        analyticsDataSource.logEvent(
            name = EVENT_STUDY_SESSION_COMPLETE,
            params =
                mapOf(
                    PARAM_JLPT_LEVEL to AnalyticsParam.Text(level.name),
                    PARAM_SESSION_NUMBER to AnalyticsParam.Numeric(sessionNumber.toLong()),
                    PARAM_ACCURACY to AnalyticsParam.Numeric(accuracy.toLong()),
                    PARAM_ELAPSED_SECONDS to AnalyticsParam.Numeric(elapsedTimeSeconds.toLong()),
                ),
        )
    }

    companion object {
        private const val EVENT_STUDY_SESSION_COMPLETE = "study_session_complete"
        private const val PARAM_JLPT_LEVEL = "jlpt_level"
        private const val PARAM_SESSION_NUMBER = "session_number"
        private const val PARAM_ACCURACY = "accuracy"
        private const val PARAM_ELAPSED_SECONDS = "elapsed_seconds"
    }
}
