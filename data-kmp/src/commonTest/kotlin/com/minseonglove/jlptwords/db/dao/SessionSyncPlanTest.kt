package com.minseonglove.jlptwords.db.dao

import com.minseonglove.jlptwords.db.entity.StudySessionEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 단어 수가 바뀌어 세션을 다시 구성할 때, 학습 기록이 매달린 행을 지키면서
 * 무엇을 지우고 넣고 고칠지 정하는 규칙을 고정한다.
 */
class SessionSyncPlanTest {
    private fun session(
        id: Int = 0,
        levelCode: Int = 5,
        typeCode: Int = 0,
        start: Int = 1,
        end: Int = 50,
        size: Int = 50,
    ) = StudySessionEntity(
        id = id,
        levelCode = levelCode,
        sessionTypeCode = typeCode,
        startNumber = start,
        endNumber = end,
        wordsSize = size,
    )

    @Test
    fun `구성이 그대로면 아무것도 바꾸지 않는다`() {
        val previous = listOf(session(id = 1), session(id = 2, start = 1, end = 100, size = 100))
        val current = listOf(session(), session(start = 1, end = 100, size = 100))

        val plan = planSessionSync(current = current, previous = previous)

        assertTrue(plan.deleteIds.isEmpty())
        assertTrue(plan.toInsert.isEmpty())
        assertTrue(plan.toUpdate.isEmpty())
    }

    @Test
    fun `세션이 줄면 남는 꼬리를 지운다`() {
        val previous = listOf(session(id = 1), session(id = 2), session(id = 3))
        val current = listOf(session())

        val plan = planSessionSync(current = current, previous = previous)

        assertEquals(listOf(2, 3), plan.deleteIds)
        assertTrue(plan.toInsert.isEmpty())
    }

    @Test
    fun `세션이 늘면 모자라는 만큼 새로 넣는다`() {
        val previous = listOf(session(id = 1))
        val added = session(start = 1, end = 100, size = 100)
        val current = listOf(session(), added)

        val plan = planSessionSync(current = current, previous = previous)

        assertTrue(plan.deleteIds.isEmpty())
        assertEquals(listOf(added), plan.toInsert)
    }

    @Test
    fun `기존 세션이 없으면 전부 새로 넣는다`() {
        val current = listOf(session(), session(start = 1, end = 100, size = 100))

        val plan = planSessionSync(current = current, previous = emptyList())

        assertEquals(current, plan.toInsert)
        assertTrue(plan.deleteIds.isEmpty())
        assertTrue(plan.toUpdate.isEmpty())
    }

    @Test
    fun `겹치는 구간에서 범위가 달라지면 기존 행을 그대로 두고 내용만 고친다`() {
        val previous = listOf(session(id = 7, levelCode = 3, end = 50, size = 50))
        val current = listOf(session(end = 60, size = 60))

        val plan = planSessionSync(current = current, previous = previous)

        assertEquals(1, plan.toUpdate.size)
        val updated = plan.toUpdate.single()
        assertEquals(7, updated.id, "학습 기록이 매달린 id 를 유지해야 한다")
        assertEquals(3, updated.levelCode, "급수도 기존 행을 따른다")
        assertEquals(60, updated.endNumber)
        assertEquals(60, updated.wordsSize)
    }

    @Test
    fun `세션 종류가 달라져도 갱신 대상이다`() {
        val previous = listOf(session(id = 1, typeCode = 0))
        val current = listOf(session(typeCode = 2))

        assertEquals(1, planSessionSync(current, previous).toUpdate.size)
    }

    @Test
    fun `내용이 같은 세션은 갱신하지 않는다`() {
        // id 만 다르고 나머지가 같으면 손대지 않는다
        val previous = listOf(session(id = 99), session(id = 100))
        val current = listOf(session(), session())

        assertTrue(planSessionSync(current, previous).toUpdate.isEmpty())
    }

    @Test
    fun `줄어드는 경우에도 남은 구간의 변경은 함께 반영한다`() {
        val previous = listOf(session(id = 1, end = 50, size = 50), session(id = 2), session(id = 3))
        val current = listOf(session(end = 40, size = 40))

        val plan = planSessionSync(current = current, previous = previous)

        assertEquals(listOf(2, 3), plan.deleteIds)
        assertEquals(1, plan.toUpdate.single().id)
        assertEquals(40, plan.toUpdate.single().endNumber)
    }
}
