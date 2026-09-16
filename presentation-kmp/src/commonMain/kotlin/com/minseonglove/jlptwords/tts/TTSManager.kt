package com.minseonglove.jlptwords.tts

interface TTSCallback {
    fun onInit(status: Int)

    fun onStart(utteranceId: String)

    fun onDone(utteranceId: String)

    fun onError(
        utteranceId: String,
        errorCode: Int,
    )
}

expect class TTSManager {
    fun init(callback: TTSCallback)

    /** [utteranceId] 는 [TTSCallback] 으로 되돌아오는 식별자다. [TTSUtteranceId] 참고. */
    fun speak(
        text: String,
        utteranceId: String,
    )

    fun isAvailable(): Boolean

    /**
     * 재생해도 소리가 들리지 않는 상태(기기 미디어 볼륨 0)인지.
     *
     * iOS 의 무음 스위치는 읽을 수 있는 API 가 없어 이 값으로 잡히지 않는다. 즉 false 라고 해서
     * 반드시 들린다는 뜻은 아니고, true 일 때만 원인을 확정할 수 있다.
     */
    fun isMuted(): Boolean

    fun stop()

    fun release()
}
