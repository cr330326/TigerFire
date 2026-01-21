package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Badge 扩展函数单元测试
 */
class BadgeExtensionsTest {

    @Test
    fun testCalculateNextVariant() {
        // 测试计算下一个变体编号
        val badges = emptyList<Badge>()

        // 没有徽章时，返回 0
        assertEquals(0, badges.calculateNextVariant("extinguisher"), "没有徽章时应该返回 0")

        // 有 1 个徽章时，返回 1
        val oneBadge = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L)
        )
        assertEquals(1, oneBadge.calculateNextVariant("extinguisher"), "1 个徽章时应该返回 1")

        // 有 2 个徽章时，返回 2
        val twoBadges = oneBadge + Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 1, 2000L)
        assertEquals(2, twoBadges.calculateNextVariant("extinguisher"), "2 个徽章时应该返回 2")

        // 有 3 个徽章时，返回 3
        val threeBadges = twoBadges + Badge("badge_3", SceneType.FIRE_STATION, "extinguisher", 2, 3000L)
        assertEquals(3, threeBadges.calculateNextVariant("extinguisher"), "3 个徽章时应该返回 3")

        // 有 4 个徽章时，循环返回 0
        val fourBadges = threeBadges + Badge("badge_4", SceneType.FIRE_STATION, "extinguisher", 3, 4000L)
        assertEquals(0, fourBadges.calculateNextVariant("extinguisher"), "4 个徽章时应该循环返回 0")

        // 有 5 个徽章时，返回 1
        val fiveBadges = fourBadges + Badge("badge_5", SceneType.FIRE_STATION, "extinguisher", 0, 5000L)
        assertEquals(1, fiveBadges.calculateNextVariant("extinguisher"), "5 个徽章时应该返回 1")
    }

    @Test
    fun testCalculateNextVariantDifferentBaseTypes() {
        // 测试不同基础类型的变体计算
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 1, 2000L),
            Badge("badge_3", SceneType.SCHOOL, "school", 0, 3000L)
        )

        // 灭火器有 2 个，下一个是 2
        assertEquals(2, badges.calculateNextVariant("extinguisher"))

        // 学校有 1 个，下一个是 1
        assertEquals(1, badges.calculateNextVariant("school"))

        // 水带没有，下一个是 0
        assertEquals(0, badges.calculateNextVariant("hose"))
    }

    @Test
    fun testGetBadgesByBaseType() {
        // 测试按基础类型获取徽章
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 1, 2000L),
            Badge("badge_3", SceneType.SCHOOL, "school", 0, 3000L),
            Badge("badge_4", SceneType.FIRE_STATION, "hydrant", 0, 4000L)
        )

        // 获取灭火器徽章
        val extinguishers = badges.getBadgesByBaseType("extinguisher")
        assertEquals(2, extinguishers.size, "应该有 2 个灭火器徽章")
        assertTrue(extinguishers.all { it.baseType == "extinguisher" }, "所有徽章应该是灭火器类型")

        // 获取学校徽章
        val schools = badges.getBadgesByBaseType("school")
        assertEquals(1, schools.size, "应该有 1 个学校徽章")

        // 获取不存在的类型
        val hoses = badges.getBadgesByBaseType("hose")
        assertTrue(hoses.isEmpty(), "不存在的类型应该返回空列表")
    }

    @Test
    fun testGetBadgesByScene() {
        // 测试按场景获取徽章
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "hydrant", 0, 2000L),
            Badge("badge_3", SceneType.SCHOOL, "school", 0, 3000L),
            Badge("badge_4", SceneType.FOREST, "sheep1", 0, 4000L)
        )

        // 获取消防站徽章
        val fireStationBadges = badges.getBadgesByScene(SceneType.FIRE_STATION)
        assertEquals(2, fireStationBadges.size, "消防站应该有 2 个徽章")

        // 获取学校徽章
        val schoolBadges = badges.getBadgesByScene(SceneType.SCHOOL)
        assertEquals(1, schoolBadges.size, "学校应该有 1 个徽章")

        // 获取森林徽章
        val forestBadges = badges.getBadgesByScene(SceneType.FOREST)
        assertEquals(1, forestBadges.size, "森林应该有 1 个徽章")
    }

    @Test
    fun testHasBadgeType() {
        // 测试检查是否有某类型徽章
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "hydrant", 0, 2000L)
        )

        assertTrue(badges.hasBadgeType("extinguisher"), "应该有灭火器")
        assertTrue(badges.hasBadgeType("hydrant"), "应该有消防栓")
        assertFalse(badges.hasBadgeType("hose"), "不应该有水带")
    }

    @Test
    fun testGetLatestBadgeByBaseType() {
        // 测试获取最新徽章
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 1, 3000L),
            Badge("badge_3", SceneType.FIRE_STATION, "extinguisher", 2, 2000L),
            Badge("badge_4", SceneType.SCHOOL, "school", 0, 4000L)
        )

        // 获取灭火器的最新徽章（获得时间最晚的）
        val latestExtinguisher = badges.getLatestBadgeByBaseType("extinguisher")
        assertEquals("badge_2", latestExtinguisher?.id, "应该返回最新获得的灭火器")

        // 获取不存在的类型
        val latestHose = badges.getLatestBadgeByBaseType("hose")
        assertNull(latestHose, "不存在的类型应该返回 null")
    }

    @Test
    fun testGetMaxVariant() {
        // 测试获取最高变体编号
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 2, 2000L),
            Badge("badge_3", SceneType.FIRE_STATION, "extinguisher", 1, 3000L),
            Badge("badge_4", SceneType.SCHOOL, "school", 0, 4000L)
        )

        // 灭火器的最高变体是 2
        assertEquals(2, badges.getMaxVariant("extinguisher"), "灭火器的最高变体应该是 2")

        // 学校的最高变体是 0
        assertEquals(0, badges.getMaxVariant("school"), "学校的最高变体应该是 0")

        // 不存在的类型返回 -1
        assertEquals(-1, badges.getMaxVariant("hose"), "不存在的类型应该返回 -1")
    }

    @Test
    fun testCountByScene() {
        // 测试按场景统计徽章数量
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "hydrant", 0, 2000L),
            Badge("badge_3", SceneType.FIRE_STATION, "ladder", 0, 3000L),
            Badge("badge_4", SceneType.SCHOOL, "school", 0, 4000L),
            Badge("badge_5", SceneType.FOREST, "sheep1", 0, 5000L)
        )

        assertEquals(3, badges.countByScene(SceneType.FIRE_STATION), "消防站应该有 3 个徽章")
        assertEquals(1, badges.countByScene(SceneType.SCHOOL), "学校应该有 1 个徽章")
        assertEquals(1, badges.countByScene(SceneType.FOREST), "森林应该有 1 个徽章")
    }

    @Test
    fun testGroupByScene() {
        // 测试按场景分组徽章
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "hydrant", 0, 2000L),
            Badge("badge_3", SceneType.SCHOOL, "school", 0, 3000L),
            Badge("badge_4", SceneType.FOREST, "sheep1", 0, 4000L)
        )

        val grouped = badges.groupByScene()

        assertEquals(3, grouped.size, "应该有 3 个分组")
        assertEquals(2, grouped[SceneType.FIRE_STATION]?.size, "消防站应该有 2 个徽章")
        assertEquals(1, grouped[SceneType.SCHOOL]?.size, "学校应该有 1 个徽章")
        assertEquals(1, grouped[SceneType.FOREST]?.size, "森林应该有 1 个徽章")
    }

    @Test
    fun testGroupByBaseType() {
        // 测试按基础类型分组徽章
        val badges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 1, 2000L),
            Badge("badge_3", SceneType.FIRE_STATION, "hydrant", 0, 3000L),
            Badge("badge_4", SceneType.SCHOOL, "school", 0, 4000L)
        )

        val grouped = badges.groupByBaseType()

        assertEquals(3, grouped.size, "应该有 3 个分组")
        assertEquals(2, grouped["extinguisher"]?.size, "灭火器应该有 2 个徽章")
        assertEquals(1, grouped["hydrant"]?.size, "消防栓应该有 1 个徽章")
        assertEquals(1, grouped["school"]?.size, "学校应该有 1 个徽章")
    }

    @Test
    fun testHasAllUniqueBadges() {
        // 测试是否集齐所有基础徽章
        val requiredTypes = setOf("extinguisher", "hydrant", "ladder", "hose", "school", "sheep1", "sheep2")

        // 空列表
        assertFalse(emptyList<Badge>().hasAllUniqueBadges(), "空列表不应该集齐")

        // 部分徽章
        val partialBadges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "hydrant", 0, 2000L),
            Badge("badge_3", SceneType.FIRE_STATION, "ladder", 0, 3000L)
        )
        assertFalse(partialBadges.hasAllUniqueBadges(), "部分徽章不应该集齐")

        // 6 种徽章
        val sixBadges = partialBadges + listOf(
            Badge("badge_4", SceneType.FIRE_STATION, "hose", 0, 4000L),
            Badge("badge_5", SceneType.SCHOOL, "school", 0, 5000L),
            Badge("badge_6", SceneType.FOREST, "sheep1", 0, 6000L)
        )
        assertFalse(sixBadges.hasAllUniqueBadges(), "6 种徽章不应该集齐")

        // 7 种徽章（全部）
        val allBadges = sixBadges + Badge("badge_7", SceneType.FOREST, "sheep2", 0, 7000L)
        assertTrue(allBadges.hasAllUniqueBadges(), "7 种徽章应该集齐")
    }

    @Test
    fun testGetCollectionProgress() {
        // 测试获取收集进度
        // 空列表
        assertEquals(0f, emptyList<Badge>().getCollectionProgress(), "空列表进度应该是 0")

        // 3 种徽章
        val threeBadges = listOf(
            Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L),
            Badge("badge_2", SceneType.FIRE_STATION, "hydrant", 0, 2000L),
            Badge("badge_3", SceneType.FIRE_STATION, "ladder", 0, 3000L)
        )
        val progress3 = threeBadges.getCollectionProgress()
        assertEquals(3f / 7f, progress3, 0.001f, "3 种徽章进度应该是 3/7")

        // 5 种徽章
        val fiveBadges = threeBadges + listOf(
            Badge("badge_4", SceneType.FIRE_STATION, "hose", 0, 4000L),
            Badge("badge_5", SceneType.SCHOOL, "school", 0, 5000L)
        )
        val progress5 = fiveBadges.getCollectionProgress()
        assertEquals(5f / 7f, progress5, 0.001f, "5 种徽章进度应该是 5/7")

        // 7 种徽章（全部）
        val allBadges = fiveBadges + listOf(
            Badge("badge_6", SceneType.FOREST, "sheep1", 0, 6000L),
            Badge("badge_7", SceneType.FOREST, "sheep2", 0, 7000L)
        )
        val progress7 = allBadges.getCollectionProgress()
        assertEquals(1f, progress7, 0.001f, "7 种徽章进度应该是 100%")
    }

    @Test
    fun testVariantCycling() {
        // 测试变体循环逻辑
        val baseType = "test"

        // 创建超过 MAX_VARIANTS_PER_TYPE 的徽章
        val badges = (0 until 10).map { i ->
            Badge("badge_$i", SceneType.FIRE_STATION, baseType, i % Badge.MAX_VARIANTS_PER_TYPE, i.toLong() * 1000)
        }

        // 验证变体循环
        val nextVariant = badges.calculateNextVariant(baseType)
        val expectedVariant = 10 % Badge.MAX_VARIANTS_PER_TYPE // 10 % 4 = 2
        assertEquals(expectedVariant, nextVariant, "变体应该正确循环")
    }
}
