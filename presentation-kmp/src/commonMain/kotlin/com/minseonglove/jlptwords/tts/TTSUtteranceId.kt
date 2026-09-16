package com.minseonglove.jlptwords.tts

/**
 * TTS 발화 식별자. [TTSManager.speak] 에 실려 나갔다가 [TTSCallback] 으로 그대로 되돌아오므로,
 * 화면은 이 값으로 어느 스피커 버튼이 재생 중인지 구분한다.
 *
 * Android 는 utteranceId 가 null 이면 진행 콜백(onStart/onDone)을 아예 보내지 않으므로,
 * 재생 상태를 쓰지 않는 호출부라도 빈 문자열이 아닌 값을 넘겨야 한다.
 */
object TTSUtteranceId {
    const val WORD: String = "word"

    const val EXAMPLE: String = "example"

    /** 예문을 여러 개 노출하는 화면에서 몇 번째 예문인지까지 구분한다. */
    fun example(index: Int): String = "${EXAMPLE}_$index"
}
