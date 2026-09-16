package com.minseonglove.jlptwords.ui.study

import com.minseonglove.jlptwords.ui.selection.session.MainStamp
import com.minseonglove.jlptwords.ui.selection.session.StudyProgressState

data class StudySessionCardItem(
    val id: Int,
    val startNumber: Int,
    val endNumber: Int,
    val studyProgressState: StudyProgressState,
    val progress: Int,
    val accuracy: Int,
    val elapsedTimeSeconds: Int,
    val completionCount: Int,
    val hasSpeedStamp: Boolean = false,
    val hasAccurateStamp: Boolean = false,
    val mainStamp: MainStamp = MainStamp.NONE,
)
