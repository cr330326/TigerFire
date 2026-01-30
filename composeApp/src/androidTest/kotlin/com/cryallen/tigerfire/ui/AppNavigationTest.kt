package com.cryallen.tigerfire.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryallen.tigerfire.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI自动化测试 - App导航流程测试
 * 测试范围：欢迎页 → 主地图 → 各场景页面的导航流程
 *
 * 注意：欢迎页是全自动导航，无"开始游戏"按钮
 * - 欢迎页会自动播放动画（约5-6秒）然后导航到主地图
 * - 测试需要等待自动导航完成
 */
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * 测试1: App启动和欢迎页显示
     */
    @Test
    fun test_app_launches_successfully() {
        // 验证欢迎页面背景图加载（背景图有contentDescription）
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("启动页背景", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 验证欢迎文字显示
        composeTestRule.onNodeWithText("HI！今天和我一起救火吧！", substring = true)
            .assertIsDisplayed()
    }

    /**
     * 测试2: 从欢迎页自动导航到主地图
     */
    @Test
    fun test_navigate_from_welcome_to_map() {
        // 等待自动导航完成（欢迎页完全自动化，约5-6秒）
        // 检查"正在进入冒险场景中..."状态文本
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("正在进入冒险场景中", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 验证进入主地图页面（等待地图元素加载）
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            // 检查是否有场景图标（消防站、学校、森林）
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("学校", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("森林", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * 测试3: 导航到消防站场景
     */
    @Test
    fun test_navigate_to_fire_station() {
        // 等待自动导航到主地图
        waitForAutoNavigationToMap()

        // 点击消防站图标
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()

        // 验证进入消防站场景（检查设备图标）
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防栓", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("消防车", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * 测试4: 导航到学校场景
     */
    @Test
    fun test_navigate_to_school() {
        waitForAutoNavigationToMap()

        // 点击学校图标
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("学校", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("学校", substring = true).performClick()

        // 验证进入学校场景
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("灭火器", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("火警报警器", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * 测试5: 导航到森林场景
     */
    @Test
    fun test_navigate_to_forest() {
        waitForAutoNavigationToMap()

        // 点击森林图标
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("森林", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("森林", substring = true).performClick()

        // 验证进入森林场景
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("防火瞭望塔", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("消防直升机", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * 测试6: 返回按钮导航
     */
    @Test
    fun test_back_navigation() {
        waitForAutoNavigationToMap()

        // 进入消防站
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()

        // 等待场景加载
        Thread.sleep(1000)

        // 点击返回按钮（通过contentDescription查找）
        composeTestRule.onNodeWithContentDescription("返回", substring = true, ignoreCase = true)
            .performClick()

        // 验证返回到主地图
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
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
