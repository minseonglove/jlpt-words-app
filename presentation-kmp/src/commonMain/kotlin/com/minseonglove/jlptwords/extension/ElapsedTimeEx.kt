package com.minseonglove.jlptwords.extension

/**
 * 초 단위 소요 시간 표기. 1시간 미만은 "MM:SS", 1시간 이상은 "H:MM:SS".
 *
 * 같은 시간을 화면마다 다르게 적으면 학습 진행바·완료 통계·추이 그래프가 서로 다른 값처럼 보이므로
 * 소요 시간을 노출하는 모든 화면이 이 함수를 쓴다.
 */
fun Int.formatElapsedTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    val formattedMinutes = minutes.toString().padStart(2, '0')
    val formattedSeconds = seconds.toString().padStart(2, '0')

    return if (hours > 0) {
        "$hours:$formattedMinutes:$formattedSeconds"
    } else {
        "$formattedMinutes:$formattedSeconds"
    }
}
