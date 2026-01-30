package com.cryallen.tigerfire.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryallen.tigerfire.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI自动化测试 - 徽章收集功能测试
 * 测试范围：设备视频播放、徽章获取、收藏页面显示
 *
 * 注意：欢迎页是全自动导航，无"开始游戏"按钮
 * - 欢迎页会自动播放动画（约5-6秒）然后导航到主地图
 * - 测试需要等待自动导航完成
 */
@RunWith(AndroidJUnit4::class)
class BadgeCollectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * 测试7: 播放设备视频并获取徽章
     */
    @Test
    fun test_play_device_video_and_collect_badge() {
        waitForAutoNavigationToMap()

        // 进入消防站
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()

        // 等待场景加载
        Thread.sleep(2000)

        // 点击消防栓设备
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防栓", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防栓", substring = true).performClick()

        // 等待视频播放和徽章动画（约8秒）
        Thread.sleep(8000)

        // 验证徽章弹窗或动画出现
        // 注意：这里需要根据实际UI元素调整验证逻辑
    }

    /**
     * 测试8: 查看收藏页面的徽章
     */
    @Test
    fun test_view_collection_page() {
        waitForAutoNavigationToMap()

        // 点击收藏图标（假设在底部导航或侧边栏）
        // 注意：需要根据实际UI调整选择器
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("收藏", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("我的收藏", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 尝试通过text或contentDescription查找
        val collectionNodes = composeTestRule.onAllNodesWithContentDescription("收藏", substring = true)
            .fetchSemanticsNodes()
        if (collectionNodes.isNotEmpty()) {
            composeTestRule.onNodeWithContentDescription("收藏", substring = true).performClick()
        } else {
            composeTestRule.onNodeWithText("我的收藏", substring = true).performClick()
        }

        // 等待收藏页面加载
        Thread.sleep(2000)

        // 验证收藏页面显示（至少有标题或徽章网格）
        composeTestRule.onNodeWithText("我的收藏", substring = true, ignoreCase = true).assertExists()
    }

    /**
     * 测试9: 重复观看同一设备获得不同变体
     */
    @Test
    fun test_collect_different_badge_variants() {
        waitForAutoNavigationToMap()

        // 进入消防站
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()
        Thread.sleep(2000)

        // 第一次点击消防栓
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithContentDescription("消防栓", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("消防栓", substring = true).performClick()
        Thread.sleep(8000) // 等待视频和徽章

        // 返回消防站场景
        composeTestRule.onNodeWithContentDescription("返回", substring = true, ignoreCase = true)
            .performClick()
        Thread.sleep(1000)

        // 再次进入消防站
        composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()
        Thread.sleep(2000)

        // 第二次点击消防栓（应该获得不同变体）
        composeTestRule.onNodeWithContentDescription("消防栓", substring = true).performClick()
        Thread.sleep(8000)

        // 验证获得了不同变体的徽章
        // 可以通过查看收藏页面验证
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
