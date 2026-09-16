package com.minseonglove.jlptwords.tts

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

actual class TTSManager(
    private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private var callback: TTSCallback? = null
    private var isJapaneseSupported = false

    actual fun init(callback: TTSCallback) {
        // 엔진을 갈아끼우면 이전 인스턴스의 서비스 연결이 남으므로 먼저 정리한다.
        release()
        this.callback = callback

        tts =
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // 학습 단어 발음은 일본어(히라가나)이므로 일본어 엔진으로 설정한다.
                    // 미설정 시 시스템 기본(한국어) 엔진이 히라가나를 잘못 읽는다.
                    val result = tts?.setLanguage(Locale.JAPANESE)
                    isJapaneseSupported =
                        result != null &&
                        result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                }
                callback.onInit(status)
            }

        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    utteranceId?.let { callback.onStart(it) }
                }

                override fun onDone(utteranceId: String?) {
                    utteranceId?.let { callback.onDone(it) }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    utteranceId?.let { callback.onError(it, -1) }
                }

                override fun onError(
                    utteranceId: String?,
                    errorCode: Int,
                ) {
                    utteranceId?.let { callback.onError(it, errorCode) }
                }
            },
        )
    }

    actual fun speak(
        text: String,
        utteranceId: String,
    ) {
        // utteranceId 를 null 로 넘기면 UtteranceProgressListener 콜백이 오지 않는다.
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    // 일본어 엔진이 준비된 경우에만 사용 가능으로 본다.
    // 미지원/미설치면 PlayTTS 흐름에서 언어팩 설치 안내(TTSWarningDialog)로 이어진다.
    actual fun isAvailable(): Boolean {
        return tts != null && isJapaneseSupported
    }

    actual fun isMuted(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
    }

    actual fun stop() {
        tts?.stop()
    }

    actual fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        callback = null
        isJapaneseSupported = false
    }
}
