package com.minseonglove.jlptwords.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] 과 같지만 코루틴 취소는 잡지 않고 그대로 전파한다.
 *
 * [runCatching] 은 [CancellationException] 까지 잡아 실패로 바꾼다. 그러면 화면을 벗어나
 * 취소된 뒤에도 다음 단계가 이어서 실행되고, 취소가 "실패" 로그로 위장돼 원인 추적이 어려워진다.
 */
inline fun <R> runCatchingCancellable(block: () -> R): Result<R> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
