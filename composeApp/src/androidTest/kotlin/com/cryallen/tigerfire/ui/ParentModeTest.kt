package com.cryallen.tigerfire.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryallen.tigerfire.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI自动化测试 - 家长模式测试
 * 测试范围：家长模式进入、使用时长查看、使用时长限制
 *
 * 注意：欢迎页是全自动导航，无"开始游戏"按钮
 * - 欢迎页会自动播放动画（约5-6秒）然后导航到主地图
 * - 测试需要等待自动导航完成
 */
@RunWith(AndroidJUnit4::class)
class ParentModeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * 测试10: 进入家长模式
     */
    @Test
    fun test_enter_parent_mode() {
        waitForAutoNavigationToMap()

        // 查找家长模式入口（可能在设置图标或特定位置）
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("家长模式", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("家长模式", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 点击进入家长模式
        val parentNodes = composeTestRule.onAllNodesWithContentDescription("家长模式", substring = true)
            .fetchSemanticsNodes()
        if (parentNodes.isNotEmpty()) {
            composeTestRule.onNodeWithContentDescription("家长模式", substring = true).performClick()
        } else {
            composeTestRule.onNodeWithText("家长模式", substring = true).performClick()
        }

        // 等待页面加载
        Thread.sleep(2000)

        // 验证进入家长模式页面
        composeTestRule.onNodeWithText("家长模式", substring = true, ignoreCase = true).assertExists()
    }

    /**
     * 测试11: 查看使用时长统计
     */
    @Test
    fun test_view_usage_statistics() {
        waitForAutoNavigationToMap()

        // 进入家长模式
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("家长模式", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("家长模式", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val parentNodes = composeTestRule.onAllNodesWithContentDescription("家长模式", substring = true)
            .fetchSemanticsNodes()
        if (parentNodes.isNotEmpty()) {
            composeTestRule.onNodeWithContentDescription("家长模式", substring = true).performClick()
        } else {
            composeTestRule.onNodeWithText("家长模式", substring = true).performClick()
        }

        Thread.sleep(2000)

        // 验证使用时长相关元素存在
        // 可能包含：今日使用时长、本周统计、图表等
        composeTestRule.onNodeWithText("使用时长", substring = true, ignoreCase = true).assertExists()
    }

    /**
     * 测试12: 设置使用时长限制
     */
    @Test
    fun test_set_time_limit() {
        waitForAutoNavigationToMap()

        // 进入家长模式
        enterParentMode()

        // 查找时间限制设置选项
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("时间限制", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("设置", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 这里需要根据实际UI进行设置操作
        // 例如：点击设置按钮、调整滑块、输入时间等
    }

    /**
     * 测试13: 从家长模式返回
     */
    @Test
    fun test_exit_parent_mode() {
        waitForAutoNavigationToMap()

        // 进入家长模式
        enterParentMode()

        // 点击返回或关闭按钮
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

    /**
     * 辅助函数：进入家长模式
     */
    private fun enterParentMode() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("家长模式", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("家长模式", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val parentNodes = composeTestRule.onAllNodesWithContentDescription("家长模式", substring = true)
            .fetchSemanticsNodes()
        if (parentNodes.isNotEmpty()) {
            composeTestRule.onNodeWithContentDescription("家长模式", substring = true).performClick()
        } else {
            composeTestRule.onNodeWithText("家长模式", substring = true).performClick()
        }

        Thread.sleep(2000)
    }
}
