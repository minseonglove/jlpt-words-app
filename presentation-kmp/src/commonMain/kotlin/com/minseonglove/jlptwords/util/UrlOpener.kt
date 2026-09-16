package com.minseonglove.jlptwords.util

/**
 * [url] 을 외부 앱으로 연다.
 * @return 열 수 있는 앱이 없으면 false. 호출부가 사용자에게 알린다(조용히 실패하면
 * 눌러도 아무 일이 없는 것처럼 보인다).
 */
expect suspend fun openUrl(url: String): Boolean
