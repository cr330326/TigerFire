package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Badge 单元测试
 */
class BadgeTest {

    @Test
    fun testBadgeCreation() {
        // 测试基本的徽章创建
        val badge = Badge(
            id = "badge_1",
            scene = SceneType.FIRE_STATION,
            baseType = "extinguisher",
            variant = 0,
            earnedAt = 1000L
        )

        assertEquals("badge_1", badge.id, "徽章 ID 应该正确")
        assertEquals(SceneType.FIRE_STATION, badge.scene, "场景应该正确")
        assertEquals("extinguisher", badge.baseType, "基础类型应该正确")
        assertEquals(0, badge.variant, "变体应该正确")
        assertEquals(1000L, badge.earnedAt, "获得时间应该正确")
    }

    @Test
    fun testBadgeVariants() {
        // 测试徽章变体
        val baseType = "extinguisher"
        val badges = (0 until Badge.MAX_VARIANTS_PER_TYPE).map { variant ->
            Badge(
                id = "${baseType}_$variant",
                scene = SceneType.FIRE_STATION,
                baseType = baseType,
                variant = variant,
                earnedAt = variant * 1000L
            )
        }

        assertEquals(Badge.MAX_VARIANTS_PER_TYPE, badges.size, "应该有 4 个变体")

        // 验证每个变体的编号
        badges.forEachIndexed { index, badge ->
            assertEquals(index, badge.variant, "变体 $index 的编号应该正确")
        }
    }

    @Test
    fun testBadgeEquality() {
        // 测试徽章相等性
        val badge1 = Badge(
            id = "badge_1",
            scene = SceneType.FIRE_STATION,
            baseType = "extinguisher",
            variant = 0,
            earnedAt = 1000L
        )

        val badge2 = Badge(
            id = "badge_1",
            scene = SceneType.FIRE_STATION,
            baseType = "extinguisher",
            variant = 0,
            earnedAt = 1000L
        )

        val badge3 = Badge(
            id = "badge_2",
            scene = SceneType.FIRE_STATION,
            baseType = "extinguisher",
            variant = 1,
            earnedAt = 2000L
        )

        assertEquals(badge1, badge2, "相同的徽章应该相等")
        assertNotEquals(badge1, badge3, "不同的徽章不应该相等")
    }

    @Test
    fun testBadgeMaxVariants() {
        // 测试最大变体数常量
        assertEquals(4, Badge.MAX_VARIANTS_PER_TYPE, "每个基础类型应该有 4 个变体")
    }

    @Test
    fun testBadgeWithDifferentScenes() {
        // 测试不同场景的徽章
        val fireStationBadge = Badge(
            id = "fire_badge",
            scene = SceneType.FIRE_STATION,
            baseType = "extinguisher",
            variant = 0,
            earnedAt = 1000L
        )

        val schoolBadge = Badge(
            id = "school_badge",
            scene = SceneType.SCHOOL,
            baseType = "school",
            variant = 0,
            earnedAt = 2000L
        )

        val forestBadge = Badge(
            id = "forest_badge",
            scene = SceneType.FOREST,
            baseType = "sheep1",
            variant = 0,
            earnedAt = 3000L
        )

        assertEquals(SceneType.FIRE_STATION, fireStationBadge.scene)
        assertEquals(SceneType.SCHOOL, schoolBadge.scene)
        assertEquals(SceneType.FOREST, forestBadge.scene)

        assertFalse(fireStationBadge.scene == schoolBadge.scene, "不同场景的徽章场景应该不同")
    }
}
