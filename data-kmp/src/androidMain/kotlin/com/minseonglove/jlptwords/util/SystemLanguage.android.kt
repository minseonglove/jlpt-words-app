package com.minseonglove.jlptwords.util

import android.content.res.Resources

// Resources.getSystem() 은 앱의 Locale.setDefault 오버라이드에 영향받지 않는 실제 기기 설정을 반환한다.
actual fun isSystemLanguageJapanese(): Boolean {
    val systemLocale =
        Resources
            .getSystem()
            .configuration.locales
            .get(0)
    return systemLocale.language == "ja"
}
