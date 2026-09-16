package com.minseonglove.jlptwords.ui.home

import com.minseonglove.jlptwords.entity.ContinueStudySession
import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyOverview
import com.minseonglove.jlptwords.ui.exit.ExitAdState

data class HomeState(
    val level: JLPTLevel = JLPTLevel.N3,
    val todayExample: DailyExample? = null,
    val continueSession: ContinueStudySession? = null,
    val studyOverview: StudyOverview? = null,
    val adZeroRemainingHours: Int = 0,
    val isLoading: Boolean = true,
    val isRetryScreenVisible: Boolean = false,
    val isAdZeroDialogShown: Boolean = false,
    val isExitAppBottomSheetShown: Boolean = false,
    val exitAdState: ExitAdState = ExitAdState.Loading,
)
