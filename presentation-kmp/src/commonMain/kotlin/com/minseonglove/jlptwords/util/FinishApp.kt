package com.minseonglove.jlptwords.util

expect fun finishApp()

/**
 * 백 내비게이션으로 앱을 벗어나는 것이 관례인 플랫폼인지.
 *
 * iOS 는 앱을 프로그램적으로 종료하는 것을 Apple 이 금지하고 있어(HIG "Don't quit programmatically"),
 * 종료 확인 시트를 띄울 수도 [finishApp] 을 수행할 수도 없다. 종료 흐름 자체를 이 값으로 가른다.
 */
expect val isAppExitSupported: Boolean
