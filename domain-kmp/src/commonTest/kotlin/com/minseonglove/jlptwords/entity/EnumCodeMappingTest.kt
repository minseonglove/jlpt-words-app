package com.minseonglove.jlptwords.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnumCodeMappingTest {
    @Test
    fun `JLPTLevel 은 코드로 정확한 급수를 돌려준다`() {
        assertEquals(JLPTLevel.N5, JLPTLevel.fromCode(5))
        assertEquals(JLPTLevel.N1, JLPTLevel.fromCode(1))
        // 모든 급수가 code -> enum 왕복을 만족한다.
        JLPTLevel.entries.forEach { level ->
            assertEquals(level, JLPTLevel.fromCode(level.code))
        }
    }

    @Test
    fun `JLPTLevel 은 알 수 없는 코드에 예외를 던진다`() {
        assertFailsWith<IllegalArgumentException> { JLPTLevel.fromCode(99) }
    }

    @Test
    fun `SessionType 은 코드로 정확한 세션 타입을 돌려준다`() {
        assertEquals(SessionType.NORMAL, SessionType.fromCode(0))
        assertEquals(SessionType.LOW_ACCURACY, SessionType.fromCode(1))
        assertEquals(SessionType.RANDOM, SessionType.fromCode(2))
        SessionType.entries.forEach { type ->
            assertEquals(type, SessionType.fromCode(type.code))
        }
    }

    @Test
    fun `SessionType 은 알 수 없는 코드에 예외를 던진다`() {
        assertFailsWith<IllegalArgumentException> { SessionType.fromCode(99) }
    }
}
