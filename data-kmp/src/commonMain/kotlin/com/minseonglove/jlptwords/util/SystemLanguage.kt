package com.minseonglove.jlptwords.util

/**
 * 기기(시스템)의 기본 언어가 일본어인지 여부.
 * 앱 언어 설정을 한 번도 바꾸지 않은 첫 실행 상태의 기본값을 정할 때 사용한다
 * (일본어 기기면 일본어, 그 외에는 한국어로 폴백).
 */
expect fun isSystemLanguageJapanese(): Boolean
