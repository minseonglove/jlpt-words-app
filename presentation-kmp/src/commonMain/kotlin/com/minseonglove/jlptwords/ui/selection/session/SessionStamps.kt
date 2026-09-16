package com.minseonglove.jlptwords.ui.selection.session

import com.minseonglove.jlptwords.entity.StudyRecord

enum class MainStamp { NONE, PROGRESS, COMPLETE }

/** speed(迅速) 기준 시간(초). 공식: (10 + 단어수/10) 분. 50→15, 100→20 … 300→40. */
fun speedThresholdSeconds(wordsSize: Int): Int = (10 + wordsSize / 10) * 60

/** 정확도 도장(正確) 기준(%). */
const val ACCURATE_THRESHOLD_PERCENT = 75

/** 역대 최고 기록 기준 speed 도장 획득 여부. */
fun hasSpeedStamp(
    records: List<StudyRecord>,
    wordsSize: Int,
): Boolean {
    if (records.isEmpty()) return false
    val threshold = speedThresholdSeconds(wordsSize)
    return records.any { it.completionTimeSeconds in 1..threshold }
}

/** 역대 최고 기록 기준 accurate 도장 획득 여부. */
fun hasAccurateStamp(records: List<StudyRecord>): Boolean = records.any { it.accuracy >= ACCURATE_THRESHOLD_PERCENT }

/** 메인 도장: 진행 중 > 완료이력 > 없음. */
fun mainStampOf(
    isInProgress: Boolean,
    hasCompletedRecord: Boolean,
): MainStamp =
    when {
        isInProgress -> MainStamp.PROGRESS
        hasCompletedRecord -> MainStamp.COMPLETE
        else -> MainStamp.NONE
    }
