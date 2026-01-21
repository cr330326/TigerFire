package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * GameProgress 扩展函数单元测试
 */
class GameProgressExtensionsTest {

    @Test
    fun testIsSceneUnlockedByCompletion() {
        // 测试基于完成条件的场景解锁判断
        val progress = GameProgress.initial()

        // 消防站始终解锁
        assertTrue(progress.isSceneUnlockedByCompletion(SceneType.FIRE_STATION), "消防站应该始终解锁")

        // 学校初始锁定
        assertFalse(progress.isSceneUnlockedByCompletion(SceneType.SCHOOL), "学校初始应该锁定")

        // 森林初始锁定
        assertFalse(progress.isSceneUnlockedByCompletion(SceneType.FOREST), "森林初始应该锁定")

        // 完成 4 个消防站设备后，学校解锁
        val progressWithCompleted = (1 until GameProgress.FIRE_STATION_TOTAL_ITEMS).fold(progress) { acc, i ->
            acc.addFireStationCompletedItem("device_$i")
        }.addFireStationCompletedItem("device_4")

        assertTrue(progressWithCompleted.isSceneUnlockedByCompletion(SceneType.SCHOOL), "完成 4 个设备后学校应该解锁")

        // 学校完成后，森林解锁
        val progressWithSchoolCompleted = progressWithCompleted
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)

        assertTrue(progressWithSchoolCompleted.isSceneUnlockedByCompletion(SceneType.FOREST), "学校完成后森林应该解锁")
    }

    @Test
    fun testGetUnlockProgressDescription() {
        // 测试获取解锁进度描述
        val progress = GameProgress.initial()

        // 消防站已解锁
        assertEquals("已解锁", progress.getUnlockProgressDescription(SceneType.FIRE_STATION))

        // 学校需要完成 4 个设备
        val schoolDesc = progress.getUnlockProgressDescription(SceneType.SCHOOL)
        assertTrue(schoolDesc.contains("0/4"), "学校解锁描述应该显示 0/4")

        // 完成 2 个设备
        val progress2 = progress
            .addFireStationCompletedItem("device_1")
            .addFireStationCompletedItem("device_2")
        val schoolDesc2 = progress2.getUnlockProgressDescription(SceneType.SCHOOL)
        assertTrue(schoolDesc2.contains("2/4"), "学校解锁描述应该显示 2/4")

        // 学校完成后，森林解锁
        val progressWithSchool = progress2
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertEquals("已解锁", progressWithSchool.getUnlockProgressDescription(SceneType.FOREST))

        // 学校未完成时，森林需要完成学校
        val forestDesc = progress2.getUnlockProgressDescription(SceneType.FOREST)
        assertTrue(forestDesc.contains("完成学校动画"), "森林解锁描述应该提到完成学校")
    }

    @Test
    fun testGetNextUnlockableScene() {
        // 测试获取下一个可解锁的场景
        val progress = GameProgress.initial()

        // 初始状态下没有可解锁的场景
        assertNull(progress.getNextUnlockableScene(), "初始不应该有可解锁的场景")

        // 完成 3 个设备，仍不能解锁学校
        val progress3 = (1..3).fold(progress) { acc, i ->
            acc.addFireStationCompletedItem("device_$i")
        }
        assertNull(progress3.getNextUnlockableScene(), "3 个设备不应该解锁学校")

        // 完成 4 个设备，可以解锁学校（因为消防站状态不是 COMPLETED）
        val progress4 = progress3.addFireStationCompletedItem("device_4")
        assertEquals(SceneType.SCHOOL, progress4.getNextUnlockableScene(), "4 个设备应该解锁学校")

        // 将消防站标记为完成后，学校成为下一个可解锁场景
        val progressFireCompleted = progress4
            .updateSceneStatus(SceneType.FIRE_STATION, SceneStatus.COMPLETED)

        // 学校已完成但森林还未解锁时，可以解锁森林
        val progressWithSchool = progressFireCompleted
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertEquals(SceneType.FOREST, progressWithSchool.getNextUnlockableScene(), "学校完成应该解锁森林")

        // 森林也解锁后，没有可解锁的场景
        val progressWithForest = progressWithSchool
            .updateSceneStatus(SceneType.FOREST, SceneStatus.UNLOCKED)
        assertNull(progressWithForest.getNextUnlockableScene(), "森林解锁后不应该有可解锁场景")
    }

    @Test
    fun testHasUnlockableScene() {
        // 测试是否有场景可解锁
        val progress = GameProgress.initial()

        assertFalse(progress.hasUnlockableScene(), "初始不应该有可解锁场景")

        // 完成 4 个设备后，学校可解锁
        val progress4 = (1..4).fold(progress) { acc, i ->
            acc.addFireStationCompletedItem("device_$i")
        }
        assertTrue(progress4.hasUnlockableScene(), "4 个设备后应该有可解锁场景（学校）")

        // 将消防站标记为完成后，不再有可解锁场景
        val progressCompleted = progress4
            .updateSceneStatus(SceneType.FIRE_STATION, SceneStatus.COMPLETED)
        assertFalse(progressCompleted.hasUnlockableScene(), "消防站完成后不应该有可解锁场景")

        // 学校完成后，森林可解锁
        val progressSchoolCompleted = progressCompleted
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertTrue(progressSchoolCompleted.hasUnlockableScene(), "学校完成后应该有可解锁场景（森林）")

        // 森林也解锁后
        val progressForestUnlocked = progressSchoolCompleted
            .updateSceneStatus(SceneType.FOREST, SceneStatus.UNLOCKED)
        assertFalse(progressForestUnlocked.hasUnlockableScene(), "森林解锁后不应该有可解锁场景")
    }

    @Test
    fun testGetRecommendedScene() {
        // 测试获取推荐场景
        val progress = GameProgress.initial()

        // 初始推荐消防站
        assertEquals(SceneType.FIRE_STATION, progress.getRecommendedScene(), "初始应该推荐消防站")

        // 消防站完成后，推荐学校
        val progressFireCompleted = progress
            .updateSceneStatus(SceneType.FIRE_STATION, SceneStatus.COMPLETED)
            .addFireStationCompletedItem("device_1")
            .addFireStationCompletedItem("device_2")
            .addFireStationCompletedItem("device_3")
            .addFireStationCompletedItem("device_4")

        // 需要先解锁学校
        val progressSchoolUnlocked = progressFireCompleted
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.UNLOCKED)

        assertEquals(SceneType.SCHOOL, progressSchoolUnlocked.getRecommendedScene(), "消防站完成后应该推荐学校")

        // 学校完成后，推荐森林
        val progressSchoolCompleted = progressSchoolUnlocked
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)

        // 需要先解锁森林
        val progressForestUnlocked = progressSchoolCompleted
            .updateSceneStatus(SceneType.FOREST, SceneStatus.UNLOCKED)

        assertEquals(SceneType.FOREST, progressForestUnlocked.getRecommendedScene(), "学校完成后应该推荐森林")

        // 所有场景完成后，推荐消防站（可重复游玩）
        val allCompleted = progressForestUnlocked
            .updateSceneStatus(SceneType.FOREST, SceneStatus.COMPLETED)
            .incrementForestRescuedSheep()
            .incrementForestRescuedSheep()

        assertEquals(SceneType.FIRE_STATION, allCompleted.getRecommendedScene(), "全部完成后应该推荐消防站")
    }

    @Test
    fun testGetOverallProgress() {
        // 测试获取总体完成进度
        val progress = GameProgress.initial()

        // 初始进度为 0
        assertEquals(0f, progress.getOverallProgress(), 0.001f, "初始进度应该是 0")

        // 完成 1 个场景
        val progress1 = progress
            .updateSceneStatus(SceneType.FIRE_STATION, SceneStatus.COMPLETED)
        assertEquals(1f / 3f, progress1.getOverallProgress(), 0.001f, "完成 1 个场景进度应该是 1/3")

        // 完成 2 个场景
        val progress2 = progress1
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertEquals(2f / 3f, progress2.getOverallProgress(), 0.001f, "完成 2 个场景进度应该是 2/3")

        // 完成 3 个场景
        val progress3 = progress2
            .updateSceneStatus(SceneType.FOREST, SceneStatus.COMPLETED)
        assertEquals(1f, progress3.getOverallProgress(), 0.001f, "完成 3 个场景进度应该是 100%")
    }

    @Test
    fun testGetFireStationProgress() {
        // 测试获取消防站进度
        val progress = GameProgress.initial()

        // 初始进度
        val (completed, total) = progress.getFireStationProgress()
        assertEquals(0, completed, "初始应该完成 0 个")
        assertEquals(4, total, "总共应该有 4 个设备")

        // 完成 2 个
        val progress2 = progress
            .addFireStationCompletedItem("device_1")
            .addFireStationCompletedItem("device_2")
        val (completed2, total2) = progress2.getFireStationProgress()
        assertEquals(2, completed2, "应该完成 2 个")
        assertEquals(4, total2, "总共应该有 4 个设备")

        // 完成 4 个
        val progress4 = progress2
            .addFireStationCompletedItem("device_3")
            .addFireStationCompletedItem("device_4")
        val (completed4, total4) = progress4.getFireStationProgress()
        assertEquals(4, completed4, "应该完成 4 个")
        assertEquals(4, total4, "总共应该有 4 个设备")
    }

    @Test
    fun testGetForestProgress() {
        // 测试获取森林进度
        val progress = GameProgress.initial()

        // 初始进度
        val (rescued, total) = progress.getForestProgress()
        assertEquals(0, rescued, "初始应该救援 0 只")
        assertEquals(2, total, "总共应该有 2 只小羊")

        // 救援 1 只
        val progress1 = progress.incrementForestRescuedSheep()
        val (rescued1, total1) = progress1.getForestProgress()
        assertEquals(1, rescued1, "应该救援 1 只")
        assertEquals(2, total1, "总共应该有 2 只小羊")

        // 救援 2 只
        val progress2 = progress1.incrementForestRescuedSheep()
        val (rescued2, total2) = progress2.getForestProgress()
        assertEquals(2, rescued2, "应该救援 2 只")
        assertEquals(2, total2, "总共应该有 2 只小羊")
    }

    @Test
    fun testSchoolUnlockCondition() {
        // 测试学校解锁条件
        val progress = GameProgress.initial()

        // 初始状态学校锁定
        assertFalse(progress.isSceneUnlockedByCompletion(SceneType.SCHOOL))

        // 完成 1 个设备
        val p1 = progress.addFireStationCompletedItem("device_1")
        assertFalse(p1.isSceneUnlockedByCompletion(SceneType.SCHOOL))

        // 完成 2 个设备
        val p2 = p1.addFireStationCompletedItem("device_2")
        assertFalse(p2.isSceneUnlockedByCompletion(SceneType.SCHOOL))

        // 完成 3 个设备
        val p3 = p2.addFireStationCompletedItem("device_3")
        assertFalse(p3.isSceneUnlockedByCompletion(SceneType.SCHOOL))

        // 完成 4 个设备
        val p4 = p3.addFireStationCompletedItem("device_4")
        assertTrue(p4.isSceneUnlockedByCompletion(SceneType.SCHOOL))
    }

    @Test
    fun testForestUnlockCondition() {
        // 测试森林解锁条件
        val progress = GameProgress.initial()

        // 初始状态森林锁定
        assertFalse(progress.isSceneUnlockedByCompletion(SceneType.FOREST))

        // 学校仅解锁但未完成
        val p1 = progress.updateSceneStatus(SceneType.SCHOOL, SceneStatus.UNLOCKED)
        assertFalse(p1.isSceneUnlockedByCompletion(SceneType.FOREST))

        // 学校完成
        val p2 = p1.updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertTrue(p2.isSceneUnlockedByCompletion(SceneType.FOREST))
    }

    @Test
    fun testProgressFlow() {
        // 测试完整的进度流程
        var progress = GameProgress.initial()

        // 阶段 1：消防站学习
        assertEquals(SceneType.FIRE_STATION, progress.getRecommendedScene())
        assertEquals(0f, progress.getOverallProgress())

        // 完成消防站 4 个设备
        progress = (1..4).fold(progress) { acc, i ->
            acc.addFireStationCompletedItem("device_$i")
        }
        assertEquals(SceneType.SCHOOL, progress.getNextUnlockableScene())

        // 标记消防站完成
        progress = progress
            .updateSceneStatus(SceneType.FIRE_STATION, SceneStatus.COMPLETED)
        assertEquals(1f / 3f, progress.getOverallProgress())

        // 解锁并完成学校
        progress = progress
            .updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)
        assertTrue(progress.isSceneUnlockedByCompletion(SceneType.FOREST))
        assertEquals(2f / 3f, progress.getOverallProgress())

        // 完成森林
        progress = progress
            .updateSceneStatus(SceneType.FOREST, SceneStatus.COMPLETED)
            .incrementForestRescuedSheep()
            .incrementForestRescuedSheep()
        assertEquals(1f, progress.getOverallProgress())
        assertEquals(SceneType.FIRE_STATION, progress.getRecommendedScene())
    }
}
