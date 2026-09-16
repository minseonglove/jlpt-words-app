package com.minseonglove.jlptwords.util

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun isSystemLanguageJapanese(): Boolean = (NSLocale.preferredLanguages.firstOrNull() as? String)?.startsWith("ja") == true
