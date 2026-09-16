package com.minseonglove.jlptwords.util

import java.util.Locale

// androidx.compose.ui.text.intl.Locale.current 는 LocaleList.getAdjustedDefault()[0](= Locale.getDefault())를
// 따르므로, 기본 로케일을 바꾸면 Compose 리소스가 해당 언어로 해석된다.
actual fun applyAppLanguage(language: AppLanguage) {
    Locale.setDefault(Locale.forLanguageTag(language.languageTag))
}
