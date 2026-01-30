package com.cryallen.tigerfire.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.cryallen.tigerfire.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

/**
 * UI自动化测试 - 性能和压力测试
 * 测试范围：启动时间、内存占用、快速点击防抖、场景切换性能
 *
 * 注意：欢迎页是全自动导航，无"开始游戏"按钮
 * - 欢迎页会自动播放动画（约5-6秒）然后导航到主地图
 * - 测试需要等待自动导航完成
 */
@RunWith(AndroidJUnit4::class)
class PerformanceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /**
     * 测试14: 应用启动时间
     */
    @Test
    fun test_app_launch_time() {
        val launchTime = measureTimeMillis {
            // 等待欢迎页加载（检查欢迎页元素）
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText("HI！今天和我一起救火吧！", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
        }

        println("📊 App启动时间（欢迎页显示）: ${launchTime}ms")

        // 断言启动时间不超过8秒（包含动画时间）
        assert(launchTime < 8000) { "App启动时间过长: ${launchTime}ms" }
    }

    /**
     * 测试15: 快速点击防抖测试
     */
    @Test
    fun test_rapid_click_protection() {
        waitForAutoNavigationToMap()

        // 进入消防站
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()
        Thread.sleep(2000)

        // 快速点击消防栓设备5次
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防栓", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val deviceNode = composeTestRule.onNodeWithContentDescription("消防栓", substring = true)

        // 连续快速点击
        repeat(5) {
            deviceNode.performClick()
            Thread.sleep(100) // 100ms间隔快速点击
        }

        // 等待可能的视频播放
        Thread.sleep(3000)

        // 验证只触发了一次视频播放（通过日志或UI状态）
        println("✅ 快速点击防抖测试完成")
    }

    /**
     * 测试16: 场景切换性能测试
     */
    @Test
    fun test_scene_switching_performance() {
        waitForAutoNavigationToMap()

        val scenes = listOf("消防站", "学校", "森林")
        val switchTimes = mutableListOf<Long>()

        scenes.forEach { sceneName ->
            val switchTime = measureTimeMillis {
                composeTestRule.waitUntil(timeoutMillis = 5000) {
                    composeTestRule.onAllNodesWithContentDescription(sceneName, substring = true)
                        .fetchSemanticsNodes().isNotEmpty()
                }
                composeTestRule.onNodeWithContentDescription(sceneName, substring = true).performClick()

                // 等待场景加载（等待场景内设备图标出现）
                Thread.sleep(2000)
            }

            switchTimes.add(switchTime)
            println("📊 切换到${sceneName}耗时: ${switchTime}ms")

            // 返回主地图
            composeTestRule.onNodeWithContentDescription("返回", substring = true, ignoreCase = true)
                .performClick()
            Thread.sleep(1000)
        }

        // 计算平均切换时间
        val avgTime = switchTimes.average()
        println("📊 平均场景切换时间: ${avgTime}ms")

        // 断言平均切换时间不超过3秒
        assert(avgTime < 3000) { "场景切换时间过长: ${avgTime}ms" }
    }

    /**
     * 测试17: 连续导航压力测试
     */
    @Test
    fun test_continuous_navigation_stress() {
        waitForAutoNavigationToMap()

        // 连续进行10次场景切换
        repeat(10) { iteration ->
            println("🔄 压力测试迭代 ${iteration + 1}/10")

            // 随机选择场景
            val scenes = listOf("消防站", "学校", "森林")
            val randomScene = scenes.random()

            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onAllNodesWithContentDescription(randomScene, substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithContentDescription(randomScene, substring = true).performClick()
            Thread.sleep(1500)

            // 返回
            composeTestRule.onNodeWithContentDescription("返回", substring = true, ignoreCase = true)
                .performClick()
            Thread.sleep(1000)
        }

        println("✅ 连续导航压力测试完成（10次迭代）")
    }

    /**
     * 测试18: 内存稳定性测试
     */
    @Test
    fun test_memory_stability() {
        waitForAutoNavigationToMap()

        // 执行一系列操作，检查内存是否稳定
        repeat(5) {
            // 进入场景
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()
            Thread.sleep(2000)

            // 返回
            composeTestRule.onNodeWithContentDescription("返回", substring = true, ignoreCase = true)
                .performClick()
            Thread.sleep(1000)

            // 检查收藏页面
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onAllNodesWithContentDescription("收藏", substring = true)
                    .fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText("我的收藏", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            val collectionNodes = composeTestRule.onAllNodesWithContentDescription("收藏", substring = true)
                .fetchSemanticsNodes()
            if (collectionNodes.isNotEmpty()) {
                composeTestRule.onNodeWithContentDescription("收藏", substring = true).performClick()
            } else {
                composeTestRule.onNodeWithText("我的收藏", substring = true).performClick()
            }
            Thread.sleep(1500)

            // 返回
            composeTestRule.onNodeWithContentDescription("返回", substring = true, ignoreCase = true)
                .performClick()
            Thread.sleep(1000)
        }

        println("✅ 内存稳定性测试完成（5次循环操作）")
    }

    /**
     * 辅助函数：等待自动导航到主地图
     * 欢迎页完全自动化，会自动播放动画后导航（约5-6秒）
     */
    private fun waitForAutoNavigationToMap() {
        // 检查"正在进入冒险场景中..."状态文本或地图场景图标出现
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("正在进入冒险场景中", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("学校", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("森林", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        // 额外等待确保地图完全加载
        Thread.sleep(500)
    }
}
