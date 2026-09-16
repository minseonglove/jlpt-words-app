package com.minseonglove.jlptwords.ui.splash

import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.entity.WordsInitializeProgress

data class SplashState(
    val progress: WordsInitializeProgress = WordsInitializeProgress.IDLE,
    val syncProgress: AllContentsSyncProgress? = null,
)
