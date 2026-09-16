package com.minseonglove.jlptwords.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.minseonglove.jlptwords.navigation.AppNavHost
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        JLPTWordsTheme(darkTheme = false) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppNavHost()
            }
        }
    }
