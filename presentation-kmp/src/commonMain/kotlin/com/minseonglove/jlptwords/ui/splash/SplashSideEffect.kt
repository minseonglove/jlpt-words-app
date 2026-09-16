package com.minseonglove.jlptwords.ui.splash

import com.minseonglove.jlptwords.entity.JLPTLevel

sealed interface SplashSideEffect {
    data object NavigateToHome : SplashSideEffect

    /** 완전 첫 실행(급수 미선택) 시 급수 선택 화면으로 진입한다. */
    data class NavigateToLevelSelection(
        val level: JLPTLevel,
    ) : SplashSideEffect
}
