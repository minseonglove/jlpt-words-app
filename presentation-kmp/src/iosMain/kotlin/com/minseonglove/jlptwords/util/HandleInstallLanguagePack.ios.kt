package com.minseonglove.jlptwords.util

import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.tts_install_guide_ios
import org.jetbrains.compose.resources.getString

actual suspend fun handleInstallLanguagePack(showMessage: (String) -> Unit) {
    val message = getString(Res.string.tts_install_guide_ios)
    showMessage(message)
}
