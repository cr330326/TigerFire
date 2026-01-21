package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SceneType 单元测试
 */
class SceneTypeTest {

    @Test
    fun testSceneTypeEntries() {
        // 验证所有场景类型都存在
        val scenes = SceneType.entries
        assertEquals(3, scenes.size, "应该有 3 个场景类型")
        assertTrue(scenes.contains(SceneType.FIRE_STATION), "应该包含消防站场景")
        assertTrue(scenes.contains(SceneType.SCHOOL), "应该包含学校场景")
        assertTrue(scenes.contains(SceneType.FOREST), "应该包含森林场景")
    }

    @Test
    fun testSceneTypeOrder() {
        // 验证场景类型的顺序
        val scenes = SceneType.entries
        assertEquals(SceneType.FIRE_STATION, scenes.first(), "第一个场景应该是消防站")
        assertEquals(SceneType.SCHOOL, scenes[1], "第二个场景应该是学校")
        assertEquals(SceneType.FOREST, scenes.last(), "第三个场景应该是森林")
    }
}
