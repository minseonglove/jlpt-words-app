package com.minseonglove.jlptwords.util

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri

private const val MAILTO_SCHEME = "mailto"

actual suspend fun openUrl(url: String): Boolean {
    val context = ContextProvider.applicationContext ?: return false
    val uri = url.toUri()
    // 메일 앱은 ACTION_SENDTO + mailto 로 인텐트 필터를 등록한다(ACTION_VIEW 는 등록하지 않는 앱이 많다).
    val action = if (uri.scheme == MAILTO_SCHEME) Intent.ACTION_SENDTO else Intent.ACTION_VIEW
    val intent =
        Intent(action, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    // resolveActivity 사전 검사는 두지 않는다. 조회 결과는 <queries> 로 선언한 범위로 걸러지므로
    // 실제로 열 수 있는 앱이 있어도 null 이 나와 조용히 무시되는 경우가 생긴다. startActivity 는
    // 이 필터링을 받지 않으므로 바로 시도하고 없을 때만 실패로 처리한다.
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
