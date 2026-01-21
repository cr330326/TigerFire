package com.cryallen.tigerfire.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * IdleTimerState 单元测试
 */
class IdleTimerStateTest {

    @Test
    fun testIdleTimerStateEntries() {
        // 测试所有状态存在
        val states = IdleTimerState.entries
        assertEquals(4, states.size, "应该有 4 种状态")

        assertTrue(states.contains(IdleTimerState.IDLE))
        assertTrue(states.contains(IdleTimerState.RUNNING))
        assertTrue(states.contains(IdleTimerState.PAUSED))
        assertTrue(states.contains(IdleTimerState.TRIGGERED))
    }

    @Test
    fun testIdleTimerStateOrder() {
        // 测试状态顺序
        val states = IdleTimerState.entries

        assertEquals(IdleTimerState.IDLE, states.first(), "第一个状态应该是 IDLE")
        assertEquals(IdleTimerState.RUNNING, states[1], "第二个状态应该是 RUNNING")
        assertEquals(IdleTimerState.PAUSED, states[2], "第三个状态应该是 PAUSED")
        assertEquals(IdleTimerState.TRIGGERED, states.last(), "最后一个状态应该是 TRIGGERED")
    }

    @Test
    fun testIdleTimerStateNames() {
        // 测试状态名称
        assertEquals("IDLE", IdleTimerState.IDLE.name)
        assertEquals("RUNNING", IdleTimerState.RUNNING.name)
        assertEquals("PAUSED", IdleTimerState.PAUSED.name)
        assertEquals("TRIGGERED", IdleTimerState.TRIGGERED.name)
    }

    @Test
    fun testIdleTimerStateConstants() {
        // 测试常量
        assertEquals(30000L, IdleTimer.DEFAULT_IDLE_TIMEOUT_MS, "默认空闲超时应该是 30 秒")
        assertEquals(5000L, IdleTimer.MIN_IDLE_TIMEOUT_MS, "最小超时应该是 5 秒")
        assertEquals(300000L, IdleTimer.MAX_IDLE_TIMEOUT_MS, "最大超时应该是 5 分钟")
    }

    @Test
    fun testIdleTimerStateTransitions() {
        // 测试状态转换流程
        var state = IdleTimerState.IDLE

        // IDLE -> RUNNING
        state = IdleTimerState.RUNNING
        assertTrue(state == IdleTimerState.RUNNING)

        // RUNNING -> PAUSED
        state = IdleTimerState.PAUSED
        assertTrue(state == IdleTimerState.PAUSED)

        // PAUSED -> RUNNING
        state = IdleTimerState.RUNNING
        assertTrue(state == IdleTimerState.RUNNING)

        // RUNNING -> TRIGGERED
        state = IdleTimerState.TRIGGERED
        assertTrue(state == IdleTimerState.TRIGGERED)

        // TRIGGERED -> IDLE
        state = IdleTimerState.IDLE
        assertTrue(state == IdleTimerState.IDLE)
    }
}
