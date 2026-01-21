package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * GameProgress 单元测试
 */
class GameProgressTest {

    @Test
    fun testInitialProgress() {
        // 测试初始进度
        val progress = GameProgress.initial()

        // 验证场景状态
        assertEquals(SceneStatus.UNLOCKED, progress.getSceneStatus(SceneType.FIRE_STATION), "消防站应该默认解锁")
        assertEquals(SceneStatus.LOCKED, progress.getSceneStatus(SceneType.SCHOOL), "学校应该默认锁定")
        assertEquals(SceneStatus.LOCKED, progress.getSceneStatus(SceneType.FOREST), "森林应该默认锁定")

        // 验证初始数据
        assertTrue(progress.badges.isEmpty(), "初始应该没有徽章")
        assertEquals(0L, progress.totalPlayTime, "初始游玩时间应该是 0")
        assertTrue(progress.fireStationCompletedItems.isEmpty(), "初始应该没有完成的设备")
        assertEquals(0, progress.forestRescuedSheep, "初始应该没有救援小羊")
    }

    @Test
    fun testDefaultSceneStatuses() {
        // 测试默认场景状态
        val statuses = GameProgress.defaultSceneStatuses()

        assertEquals(3, statuses.size, "应该有 3 个场景状态")
        assertEquals(SceneStatus.UNLOCKED, statuses[SceneType.FIRE_STATION], "消防站应该解锁")
        assertEquals(SceneStatus.LOCKED, statuses[SceneType.SCHOOL], "学校应该锁定")
        assertEquals(SceneStatus.LOCKED, statuses[SceneType.FOREST], "森林应该锁定")
    }

    @Test
    fun testGetSceneStatus() {
        // 测试获取场景状态
        val progress = GameProgress.initial()

        assertEquals(SceneStatus.UNLOCKED, progress.getSceneStatus(SceneType.FIRE_STATION))
        assertEquals(SceneStatus.LOCKED, progress.getSceneStatus(SceneType.SCHOOL))
        assertEquals(SceneStatus.LOCKED, progress.getSceneStatus(SceneType.FOREST))
    }

    @Test
    fun testIsSceneUnlocked() {
        // 测试场景解锁状态
        val progress = GameProgress.initial()

        assertTrue(progress.isSceneUnlocked(SceneType.FIRE_STATION), "消防站应该解锁")
        assertFalse(progress.isSceneUnlocked(SceneType.SCHOOL), "学校不应该解锁")
        assertFalse(progress.isSceneUnlocked(SceneType.FOREST), "森林不应该解锁")
    }

    @Test
    fun testIsSceneCompleted() {
        // 测试场景完成状态
        val progress = GameProgress.initial()

        assertFalse(progress.isSceneCompleted(SceneType.FIRE_STATION), "消防站不应该完成")
        assertFalse(progress.isSceneCompleted(SceneType.SCHOOL), "学校不应该完成")
        assertFalse(progress.isSceneCompleted(SceneType.FOREST), "森林不应该完成")

        // 标记消防站为完成
        val completedProgress = progress.updateSceneStatus(SceneType.FIRE_STATION, SceneStatus.COMPLETED)
        assertTrue(completedProgress.isSceneCompleted(SceneType.FIRE_STATION), "消防站应该完成")
    }

    @Test
    fun testIsFireStationCompleted() {
        // 测试消防站完成检测
        val progress = GameProgress.initial()

        assertFalse(progress.isFireStationCompleted(), "初始不应该完成")

        // 添加 3 个完成的设备
        val partialProgress = (1 until GameProgress.FIRE_STATION_TOTAL_ITEMS).fold(progress) { acc, i ->
            acc.addFireStationCompletedItem("device_$i")
        }
        assertFalse(partialProgress.isFireStationCompleted(), "3 个设备不应该完成")

        // 添加第 4 个设备
        val completeProgress = partialProgress.addFireStationCompletedItem("device_4")
        assertTrue(completeProgress.isFireStationCompleted(), "4 个设备应该完成")
    }

    @Test
    fun testIsForestCompleted() {
        // 测试森林完成检测
        val progress = GameProgress.initial()

        assertFalse(progress.isForestCompleted(), "初始不应该完成")

        // 救援 1 只小羊
        val partialProgress = progress.incrementForestRescuedSheep()
        assertFalse(partialProgress.isForestCompleted(), "1 只小羊不应该完成")

        // 救援第 2 只小羊
        val completeProgress = partialProgress.incrementForestRescuedSheep()
        assertTrue(completeProgress.isForestCompleted(), "2 只小羊应该完成")
    }

    @Test
    fun testFireStationConstants() {
        // 测试消防站常量
        assertEquals(4, GameProgress.FIRE_STATION_TOTAL_ITEMS, "消防站应该有 4 个设备")
        assertEquals(2, GameProgress.FOREST_TOTAL_SHEEP, "森林应该有 2 只小羊")
        assertEquals(7, GameProgress.TOTAL_UNIQUE_BADGES, "总共应该有 7 种基础徽章")
    }

    @Test
    fun testAddPlayTime() {
        // 测试添加游玩时间
        val progress = GameProgress.initial()

        val updated = progress.addPlayTime(60000L) // 1 分钟
        assertEquals(60000L, updated.totalPlayTime, "应该添加 1 分钟")

        val updated2 = updated.addPlayTime(120000L) // 再添加 2 分钟
        assertEquals(180000L, updated2.totalPlayTime, "总共应该是 3 分钟")
    }

    @Test
    fun testAddBadge() {
        // 测试添加徽章
        val progress = GameProgress.initial()
        val badge = Badge(
            id = "badge_1",
            scene = SceneType.FIRE_STATION,
            baseType = "extinguisher",
            variant = 0,
            earnedAt = 1000L
        )

        val updated = progress.addBadge(badge)
        assertEquals(1, updated.getTotalBadgeCount(), "应该有 1 个徽章")
        assertTrue(updated.badges.contains(badge), "应该包含添加的徽章")
    }

    @Test
    fun testGetTotalBadgeCount() {
        // 测试获取徽章总数
        val progress = GameProgress.initial()
        assertEquals(0, progress.getTotalBadgeCount(), "初始应该没有徽章")

        val badges = (0 until 5).map { i ->
            Badge(
                id = "badge_$i",
                scene = SceneType.FIRE_STATION,
                baseType = "test",
                variant = i,
                earnedAt = i.toLong()
            )
        }

        val withBadges = badges.fold(progress) { acc, badge -> acc.addBadge(badge) }
        assertEquals(5, withBadges.getTotalBadgeCount(), "应该有 5 个徽章")
    }

    @Test
    fun testGetUniqueBadgeCount() {
        // 测试获取不同类型的徽章数量
        val progress = GameProgress.initial()

        // 添加相同基础类型的不同变体
        val progressWithVariants = progress
            .addBadge(Badge("badge_1", SceneType.FIRE_STATION, "extinguisher", 0, 1000L))
            .addBadge(Badge("badge_2", SceneType.FIRE_STATION, "extinguisher", 1, 2000L))
            .addBadge(Badge("badge_3", SceneType.SCHOOL, "school", 0, 3000L))

        assertEquals(3, progressWithVariants.getTotalBadgeCount(), "总共应该有 3 个徽章")
        assertEquals(2, progressWithVariants.getUniqueBadgeCount(), "应该有 2 种不同类型的徽章")
    }

    @Test
    fun testHasCollectedAllBadges() {
        // 测试是否集齐所有徽章
        val progress = GameProgress.initial()
        assertFalse(progress.hasCollectedAllBadges(), "初始不应该集齐")

        // 添加 6 种基础徽章
        val partialProgress = (0 until 6).fold(progress) { acc, i ->
            acc.addBadge(Badge("badge_$i", SceneType.FIRE_STATION, "type_$i", 0, i.toLong()))
        }
        assertFalse(partialProgress.hasCollectedAllBadges(), "6 种徽章不应该集齐")

        // 添加第 7 种基础徽章
        val fullProgress = partialProgress.addBadge(
            Badge("badge_6", SceneType.FOREST, "type_6", 0, 6L)
        )
        assertTrue(fullProgress.hasCollectedAllBadges(), "7 种徽章应该集齐")
    }

    @Test
    fun testUpdateSceneStatus() {
        // 测试更新场景状态
        val progress = GameProgress.initial()

        // 解锁学校
        val unlocked = progress.updateSceneStatus(SceneType.SCHOOL, SceneStatus.UNLOCKED)
        assertEquals(SceneStatus.UNLOCKED, unlocked.getSceneStatus(SceneType.SCHOOL), "学校应该解锁")

        // 完成学校
        val completed = unlocked.updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertEquals(SceneStatus.COMPLETED, completed.getSceneStatus(SceneType.SCHOOL), "学校应该完成")

        // 验证原进度不受影响
        assertEquals(SceneStatus.LOCKED, progress.getSceneStatus(SceneType.SCHOOL), "原进度应该不变")
    }

    @Test
    fun testAddFireStationCompletedItem() {
        // 测试添加消防站完成设备
        val progress = GameProgress.initial()

        val updated = progress.addFireStationCompletedItem("extinguisher")
        assertEquals(1, updated.fireStationCompletedItems.size, "应该有 1 个完成的设备")
        assertTrue(updated.fireStationCompletedItems.contains("extinguisher"), "应该包含灭火器")

        // 添加多个设备
        val updated2 = updated
            .addFireStationCompletedItem("hydrant")
            .addFireStationCompletedItem("ladder")
        assertEquals(3, updated2.fireStationCompletedItems.size, "应该有 3 个完成的设备")
    }

    @Test
    fun testIncrementForestRescuedSheep() {
        // 测试增加森林救援小羊
        val progress = GameProgress.initial()

        val updated1 = progress.incrementForestRescuedSheep()
        assertEquals(1, updated1.forestRescuedSheep, "应该有 1 只小羊")

        val updated2 = updated1.incrementForestRescuedSheep()
        assertEquals(2, updated2.forestRescuedSheep, "应该有 2 只小羊")
    }

    @Test
    fun testProgressImmutability() {
        // 测试进度不可变性
        val progress = GameProgress.initial()
        val originalStatus = progress.getSceneStatus(SceneType.SCHOOL)
        val originalBadges = progress.badges.size

        // 更新进度
        val updated = progress
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.UNLOCKED)
            .addBadge(Badge("badge_1", SceneType.FIRE_STATION, "test", 0, 1000L))

        // 验证原进度未改变
        assertEquals(originalStatus, progress.getSceneStatus(SceneType.SCHOOL), "原状态应该不变")
        assertEquals(originalBadges, progress.badges.size, "原徽章数量应该不变")

        // 验证新进度已改变
        assertEquals(SceneStatus.UNLOCKED, updated.getSceneStatus(SceneType.SCHOOL), "新状态应该改变")
        assertEquals(1, updated.badges.size, "新徽章数量应该改变")
    }
}
