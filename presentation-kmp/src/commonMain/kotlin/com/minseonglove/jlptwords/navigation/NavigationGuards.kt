package com.minseonglove.jlptwords.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

/**
 * 화면 전환 요청이 연달아 들어오는 것을 막는 가드.
 *
 * Orbit 은 인텐트를 각각 별도 코루틴으로 실행하므로, 버튼을 연타하면 같은 SideEffect 가 두 번
 * 도착해 전환도 두 번 일어난다. 특히 pop 이 두 번 실행되면 백스택이 비어 NavHost 가 그릴 화면이
 * 없어진다. 전환이 시작되면 현재 엔트리는 RESUMED 를 벗어나므로, 그 사이의 요청을 흘려보낸다.
 */
private fun NavController.isIdle(): Boolean = currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

fun NavController.navigateOnce(route: Any) {
    if (isIdle()) navigate(route)
}

fun NavController.popBackStackOnce() {
    if (isIdle()) popBackStack()
}

/**
 * 전환 중이면 아무것도 하지 않고 true 를 돌려준다. 호출부가 반환값으로 "더 올라갈 곳이 없다"고
 * 판단해 앱을 종료하는 경우가 있어, 건너뛴 것을 실패로 오해하지 않게 한다.
 */
fun NavController.navigateUpOnce(): Boolean = if (isIdle()) navigateUp() else true
