package com.minseonglove.jlptwords.util

/**
 * 클립보드 복사 결과를 OS 가 스스로 알려 주는 환경인지.
 *
 * Android 13(API 33)부터는 시스템이 복사 미리보기를 띄우므로 앱이 또 알리면 안내가 겹친다.
 * 그 미만 버전과 iOS 는 아무 표시도 없어, 앱이 알리지 않으면 복사됐는지 알 수 없다.
 */
expect val isSystemClipboardFeedbackShown: Boolean
