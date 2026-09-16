package com.minseonglove.jlptwords.ui.splash

sealed interface SplashIntent {
    data object Initialize : SplashIntent
}
