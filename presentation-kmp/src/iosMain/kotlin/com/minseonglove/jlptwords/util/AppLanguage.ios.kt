package com.minseonglove.jlptwords.util

import platform.Foundation.NSUserDefaults

// NSLocale.preferredLanguages(= androidx.compose.ui.text.intl.Locale.current 의 소스)는 NSUserDefaults 의
// "AppleLanguages" 키를 읽는다. 이 값을 바꾸면 Compose 리소스가 해당 언어로 해석된다.
actual fun applyAppLanguage(language: AppLanguage) {
    NSUserDefaults.standardUserDefaults.setObject(
        listOf(language.languageTag),
        forKey = "AppleLanguages",
    )
}
