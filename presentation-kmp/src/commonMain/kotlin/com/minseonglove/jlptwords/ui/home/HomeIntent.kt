package com.minseonglove.jlptwords.ui.home

sealed interface HomeIntent {
    data object Initialize : HomeIntent

    data object ClickTodayExample : HomeIntent

    data object ClickContinueSession : HomeIntent

    data object ClickTitle : HomeIntent

    data object ShowAdZeroDialog : HomeIntent

    data object DismissAdZeroDialog : HomeIntent

    data object ClickSettings : HomeIntent

    data object ShowExitAppBottomSheet : HomeIntent

    data object DismissExitAppBottomSheet : HomeIntent

    data object FinishApp : HomeIntent
}
