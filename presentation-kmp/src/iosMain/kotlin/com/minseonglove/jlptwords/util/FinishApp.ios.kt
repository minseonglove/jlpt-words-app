package com.minseonglove.jlptwords.util

/**
 * iOS 에서는 아무것도 하지 않는다. exit()/abort() 로 프로세스를 끝내면 사용자에게는 크래시로 보이고,
 * DataStore·Room 의 미기록 쓰기가 유실되며, 심사 가이드라인에도 어긋난다.
 * [isAppExitSupported] 가 false 라 종료 흐름이 시작되지 않으므로 실제로 호출되지도 않는다.
 */
actual fun finishApp() = Unit

actual val isAppExitSupported: Boolean = false
