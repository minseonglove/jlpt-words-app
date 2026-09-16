package com.minseonglove.jlptwords.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncStateEnumTest {
    @Test
    fun `AllContentsSyncResult 는 MISSING 만 준비되지 않은 상태다`() {
        assertTrue(AllContentsSyncResult.UP_TO_DATE.isReady)
        assertTrue(AllContentsSyncResult.UPDATED.isReady)
        assertFalse(AllContentsSyncResult.MISSING.isReady)
    }

    @Test
    fun `WordsInitializeProgress 는 완료 최신 실패만 종결 상태다`() {
        assertTrue(WordsInitializeProgress.COMPLETE.isTerminal)
        assertTrue(WordsInitializeProgress.ALREADY_UP_TO_DATE.isTerminal)
        assertTrue(WordsInitializeProgress.ERROR.isTerminal)
    }

    @Test
    fun `WordsInitializeProgress 는 진행 중 상태를 종결로 보지 않는다`() {
        assertFalse(WordsInitializeProgress.IDLE.isTerminal)
        assertFalse(WordsInitializeProgress.INITIALIZING.isTerminal)
        assertFalse(WordsInitializeProgress.UPDATING.isTerminal)
    }
}
