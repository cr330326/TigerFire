package com.cryallen.tigerfire.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SessionTimerState 单元测试
 */
class SessionTimerStateTest {

    @Test
    fun testSessionTimerStateEntries() {
        // 测试所有状态存在
        val states = SessionTimerState.entries
        assertEquals(4, states.size, "应该有 4 种状态")

        assertTrue(states.contains(SessionTimerState.IDLE))
        assertTrue(states.contains(SessionTimerState.RUNNING))
        assertTrue(states.contains(SessionTimerState.PAUSED))
        assertTrue(states.contains(SessionTimerState.TIME_LIMIT_REACHED))
    }

    @Test
    fun testSessionTimerStateOrder() {
        // 测试状态顺序
        val states = SessionTimerState.entries

        assertEquals(SessionTimerState.IDLE, states.first(), "第一个状态应该是 IDLE")
        assertEquals(SessionTimerState.RUNNING, states[1], "第二个状态应该是 RUNNING")
        assertEquals(SessionTimerState.PAUSED, states[2], "第三个状态应该是 PAUSED")
        assertEquals(SessionTimerState.TIME_LIMIT_REACHED, states.last(), "最后一个状态应该是 TIME_LIMIT_REACHED")
    }

    @Test
    fun testSessionTimerStateTransitions() {
        // 测试状态转换流程
        var state = SessionTimerState.IDLE

        // IDLE -> RUNNING
        state = SessionTimerState.RUNNING
        assertTrue(state == SessionTimerState.RUNNING)

        // RUNNING -> PAUSED
        state = SessionTimerState.PAUSED
        assertTrue(state == SessionTimerState.PAUSED)

        // PAUSED -> RUNNING
        state = SessionTimerState.RUNNING
        assertTrue(state == SessionTimerState.RUNNING)

        // RUNNING -> TIME_LIMIT_REACHED
        state = SessionTimerState.TIME_LIMIT_REACHED
        assertTrue(state == SessionTimerState.TIME_LIMIT_REACHED)

        // TIME_LIMIT_REACHED -> IDLE
        state = SessionTimerState.IDLE
        assertTrue(state == SessionTimerState.IDLE)
    }
}
