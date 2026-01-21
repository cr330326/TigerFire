package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SceneStatus 单元测试
 */
class SceneStatusTest {

    @Test
    fun testSceneStatusEntries() {
        // 验证所有场景状态都存在
        val statuses = SceneStatus.entries
        assertEquals(3, statuses.size, "应该有 3 种场景状态")
        assertTrue(statuses.contains(SceneStatus.LOCKED), "应该包含锁定状态")
        assertTrue(statuses.contains(SceneStatus.UNLOCKED), "应该包含解锁状态")
        assertTrue(statuses.contains(SceneStatus.COMPLETED), "应该包含完成状态")
    }

    @Test
    fun testSceneStatusOrder() {
        // 验证场景状态的顺序：LOCKED -> UNLOCKED -> COMPLETED
        val statuses = SceneStatus.entries
        assertEquals(SceneStatus.LOCKED, statuses.first(), "第一个状态应该是 LOCKED")
        assertEquals(SceneStatus.UNLOCKED, statuses[1], "第二个状态应该是 UNLOCKED")
        assertEquals(SceneStatus.COMPLETED, statuses.last(), "第三个状态应该是 COMPLETED")
    }

    @Test
    fun testSceneStatusProgression() {
        // 验证场景状态的正常进展流程
        var status = SceneStatus.LOCKED
        assertTrue(status == SceneStatus.LOCKED, "初始状态应该是 LOCKED")

        // 解锁场景
        status = SceneStatus.UNLOCKED
        assertFalse(status == SceneStatus.LOCKED, "解锁后不应该再是 LOCKED")
        assertTrue(status == SceneStatus.UNLOCKED, "解锁后应该是 UNLOCKED")

        // 完成场景
        status = SceneStatus.COMPLETED
        assertFalse(status == SceneStatus.UNLOCKED, "完成后不应该再是 UNLOCKED")
        assertTrue(status == SceneStatus.COMPLETED, "完成后应该是 COMPLETED")
    }
}
