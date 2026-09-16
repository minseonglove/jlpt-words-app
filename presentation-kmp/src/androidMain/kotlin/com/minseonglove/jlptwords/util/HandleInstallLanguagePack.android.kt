package com.minseonglove.jlptwords.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.tts_install_guide_android
import org.jetbrains.compose.resources.getString
import java.util.Locale

actual suspend fun handleInstallLanguagePack(showMessage: (String) -> Unit) {
    val context = ContextProvider.applicationContext ?: return
    val intent =
        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
            // Application context 로 액티비티를 띄울 때는 새 태스크 플래그가 필수다.
            // 없으면 프레임워크가 AndroidRuntimeException 을 던진다.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES,
                arrayListOf(Locale.JAPANESE.toLanguageTag()),
            )
        }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        showMessage(getString(Res.string.tts_install_guide_android))
    }
}
