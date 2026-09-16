package com.minseonglove.jlptwords.tts

import kotlinx.cinterop.ObjCSignatureOverride
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.outputVolume
import platform.darwin.NSObject

private const val JAPANESE_LANGUAGE = "ja-JP"

actual class TTSManager {
    private var synthesizer: AVSpeechSynthesizer? = null
    private var callback: TTSCallback? = null
    private var delegate: SpeechDelegate? = null
    private var japaneseVoice: AVSpeechSynthesisVoice? = null

    actual fun init(callback: TTSCallback) {
        // 엔진을 갈아끼우면 이전 synthesizer 가 계속 발화한 채 남으므로 먼저 정리한다.
        release()
        this.callback = callback
        synthesizer = AVSpeechSynthesizer()
        delegate = SpeechDelegate(callback)
        synthesizer?.delegate = delegate

        // iOS는 즉시 사용 가능하므로 성공 상태로 콜백 호출
        callback.onInit(0) // 0 = SUCCESS
    }

    actual fun speak(
        text: String,
        utteranceId: String,
    ) {
        val voice = japaneseVoice() ?: return
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.voice = voice

        delegate?.register(utteranceId)
        synthesizer?.speakUtterance(utterance)
    }

    /**
     * 일본어 음성이 확보된 경우에만 사용 가능으로 본다. voice 가 없는데 그대로 발화시키면
     * utterance 가 기기 기본 음성(예: 한국어)으로 일본어를 읽어 알아들을 수 없는 소리가 난다.
     */
    actual fun isAvailable(): Boolean {
        return synthesizer != null && japaneseVoice() != null
    }

    actual fun isMuted(): Boolean {
        // outputVolume 은 미디어 볼륨이며 무음 스위치는 반영하지 않는다(읽을 수 있는 API 가 없다).
        return AVAudioSession.sharedInstance().outputVolume <= 0f
    }

    actual fun stop() {
        synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }

    actual fun release() {
        synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        synthesizer?.delegate = null
        synthesizer = null
        callback = null
        delegate?.clear()
        delegate = null
    }

    /** 조회는 저렴하지만 부팅 직후 등 첫 호출이 실패할 수 있어, 확보되면 그때 캐시한다. */
    private fun japaneseVoice(): AVSpeechSynthesisVoice? =
        japaneseVoice
            ?: AVSpeechSynthesisVoice
                .voiceWithLanguage(JAPANESE_LANGUAGE)
                ?.also { japaneseVoice = it }

    private class SpeechDelegate(
        private val callback: TTSCallback,
    ) : NSObject(),
        AVSpeechSynthesizerDelegateProtocol {
        // AVSpeechUtterance 에는 식별자를 붙일 수 없고 델리게이트도 발화 객체만 돌려주므로,
        // 마지막으로 요청한 식별자를 들고 있다가 그대로 콜백에 실어 보낸다. 화면에서 발화는
        // 한 번에 하나씩만 시작되고, 늦게 도착한 종료 콜백은 호출부가 식별자를 비교해 걸러낸다.
        private var utteranceId: String = ""

        fun register(utteranceId: String) {
            this.utteranceId = utteranceId
        }

        fun clear() {
            utteranceId = ""
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            didStartSpeechUtterance: AVSpeechUtterance,
        ) {
            callback.onStart(utteranceId)
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            didFinishSpeechUtterance: AVSpeechUtterance,
        ) {
            callback.onDone(utteranceId)
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            didCancelSpeechUtterance: AVSpeechUtterance,
        ) {
            callback.onError(utteranceId, -1)
        }
    }
}
