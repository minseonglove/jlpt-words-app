package com.minseonglove.jlptwords.ui.base

import androidx.compose.runtime.Composable
import com.minseonglove.jlptwords.entity.ContinueSessionStatus
import com.minseonglove.jlptwords.entity.SessionType
import com.minseonglove.jlptwords.entity.StudySession
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.session_card_low_accuracy
import jlptwords.presentation_kmp.generated.resources.session_card_random
import jlptwords.presentation_kmp.generated.resources.session_card_status_completed
import jlptwords.presentation_kmp.generated.resources.session_card_status_in_progress
import jlptwords.presentation_kmp.generated.resources.session_card_status_not_started
import jlptwords.presentation_kmp.generated.resources.session_card_word_range
import org.jetbrains.compose.resources.stringResource

// mollu 세션 카드 공용 라벨 매핑.
// 홈 '계속 학습하기' 카드와 (mollu 개편 후) 세션 선택 카드가 동일 문자열 규칙을 공유한다.

/** 세션 타입별 범위 라벨: 일반 → "단어 a-b번", 랜덤 → "무작위 n개", 약한 단어 → "약한 단어 n개". */
@Composable
fun StudySession.rangeLabel(): String =
    when (type) {
        SessionType.NORMAL -> stringResource(Res.string.session_card_word_range, startNumber, endNumber)
        SessionType.RANDOM -> stringResource(Res.string.session_card_random, wordsSize)
        SessionType.LOW_ACCURACY -> stringResource(Res.string.session_card_low_accuracy, wordsSize)
    }

/** 세션 진행 상태 라벨: 시작 전 / 진행 중 / 완료. */
@Composable
fun ContinueSessionStatus.statusLabel(): String =
    when (this) {
        ContinueSessionStatus.NOT_STARTED -> stringResource(Res.string.session_card_status_not_started)
        ContinueSessionStatus.IN_PROGRESS -> stringResource(Res.string.session_card_status_in_progress)
        ContinueSessionStatus.COMPLETED -> stringResource(Res.string.session_card_status_completed)
    }
