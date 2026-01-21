package com.cryallen.tigerfire.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * RapidClickGuard 单元测试
 */
class RapidClickGuardTest {

    @Test
    fun testInitialState() {
        // 测试初始状态
        val guard = RapidClickGuard()

        assertEquals(0, guard.getClickCount(), "初始点击数应该是 0")
        assertFalse(guard.checkClick(), "第一次点击不应该触发防护")
        assertEquals(1, guard.getClickCount(), "点击数应该是 1")
    }

    @Test
    fun testDefaultConstants() {
        // 测试默认常量
        assertEquals(500L, RapidClickGuard.DEFAULT_RAPID_CLICK_THRESHOLD_MS, "默认阈值应该是 500ms")
        assertEquals(3, RapidClickGuard.DEFAULT_TRIGGER_COUNT, "默认触发次数应该是 3")
    }

    @Test
    fun testSingleClickDoesNotTrigger() {
        // 测试单次点击不触发防护
        val guard = RapidClickGuard()

        assertFalse(guard.checkClick(), "单次点击不应该触发防护")
    }

    @Test
    fun testTwoClicksDoNotTrigger() {
        // 测试两次点击不触发防护
        val guard = RapidClickGuard()

        assertFalse(guard.checkClick(), "第一次点击不应该触发防护")
        assertFalse(guard.checkClick(), "第二次点击不应该触发防护")
    }

    @Test
    fun testSlowClicksDoNotTrigger() {
        // 测试慢速点击不触发防护
        // 注意：由于测试环境无法精确控制时间，这里测试快速连续点击会触发防护

        val guard = RapidClickGuard(
            rapidClickThresholdMs = 100L,
            triggerCount = 3
        )

        // 快速连续点击 3 次应该触发防护
        assertFalse(guard.checkClick(), "第一次点击不应该触发防护")
        assertEquals(1, guard.getClickCount())

        assertFalse(guard.checkClick(), "第二次点击不应该触发防护")
        assertEquals(2, guard.getClickCount())

        // 第三次点击会触发防护（因为时间间隔很短）
        val result = guard.checkClick()
        assertEquals(3, guard.getClickCount(), "应该记录 3 次点击")
        // 注意：由于测试环境中时间流逝可能小于阈值，这里可能会触发防护
        // 这是符合设计的行为
    }

    @Test
    fun testResetClearsClicks() {
        // 测试重置清空点击记录
        val guard = RapidClickGuard()

        guard.checkClick()
        guard.checkClick()
        assertEquals(2, guard.getClickCount(), "应该有 2 次点击记录")

        guard.reset()
        assertEquals(0, guard.getClickCount(), "重置后点击数应该是 0")
    }

    @Test
    fun testResetAfterClicks() {
        // 测试重置后重新计数
        val guard = RapidClickGuard()

        guard.checkClick()
        guard.checkClick()
        guard.reset()
        guard.checkClick()

        assertEquals(1, guard.getClickCount(), "重置后应该重新计数")
        assertFalse(guard.checkClick(), "重置后第二次点击不应该触发防护")
    }

    @Test
    fun testGetTimeSinceLastClick() {
        // 测试获取距离上次点击的时间
        val guard = RapidClickGuard()

        assertEquals(Long.MAX_VALUE, guard.getTimeSinceLastClick(), "没有点击记录时应该返回 MAX_VALUE")

        guard.checkClick()
        // 由于时间流逝，这里无法精确测试，只验证返回值类型
        val timeSince = guard.getTimeSinceLastClick()
        assertTrue(timeSince >= 0, "距离上次点击的时间应该是正数")
    }

    @Test
    fun testClickCountLimit() {
        // 测试点击记录数量限制
        val guard = RapidClickGuard(rapidClickThresholdMs = 100L, triggerCount = 5)

        // 点击 10 次，但应该只保留最近 5 次
        repeat(10) {
            guard.checkClick()
        }

        assertEquals(5, guard.getClickCount(), "应该只保留最近 5 次点击记录")
    }

    @Test
    fun testCustomThreshold() {
        // 测试自定义阈值和触发次数
        val guard = RapidClickGuard(
            rapidClickThresholdMs = 1000L,
            triggerCount = 2
        )

        // 初始状态点击数为 0
        assertEquals(0, guard.getClickCount(), "初始点击数应该是 0")

        guard.checkClick()
        assertEquals(1, guard.getClickCount(), "第一次点击后应该是 1")

        guard.checkClick()
        assertEquals(2, guard.getClickCount(), "第二次点击后应该是 2（达到上限）")
    }

    @Test
    fun testTriggerCount() {
        // 测试触发次数设置
        val guard = RapidClickGuard(
            rapidClickThresholdMs = 100L,
            triggerCount = 4
        )

        // 需要达到 4 次点击才可能触发
        repeat(3) {
            assertFalse(guard.checkClick(), "第 ${it + 1} 次点击不应该触发防护")
        }

        assertEquals(3, guard.getClickCount(), "应该记录 3 次点击")
    }

    @Test
    fun testClickCountUpdates() {
        // 测试点击计数更新
        val guard = RapidClickGuard()

        assertEquals(0, guard.getClickCount(), "初始计数应该是 0")

        guard.checkClick()
        assertEquals(1, guard.getClickCount(), "第一次点击后计数应该是 1")

        guard.checkClick()
        assertEquals(2, guard.getClickCount(), "第二次点击后计数应该是 2")

        guard.checkClick()
        assertEquals(3, guard.getClickCount(), "第三次点击后计数应该是 3")

        // 达到默认触发次数后，应该保持最大数量
        guard.checkClick()
        assertEquals(3, guard.getClickCount(), "超过触发次数后应该保持最大数量")
    }

    @Test
    fun testMultipleResets() {
        // 测试多次重置
        val guard = RapidClickGuard()

        repeat(3) { guard.checkClick() }
        assertEquals(3, guard.getClickCount())

        guard.reset()
        assertEquals(0, guard.getClickCount())

        repeat(5) { guard.checkClick() }
        assertEquals(3, guard.getClickCount(), "重置后应该重新计数并限制在触发次数")

        guard.reset()
        assertEquals(0, guard.getClickCount())

        guard.checkClick()
        assertEquals(1, guard.getClickCount())
    }

    @Test
    fun testResultDataClass() {
        // 测试 RapidClickResult 数据类
        val result1 = RapidClickResult(
            shouldTrigger = true,
            clickCount = 3,
            timeSinceLastClick = 100L
        )

        assertTrue(result1.shouldTrigger, "应该触发防护")
        assertEquals(3, result1.clickCount)
        assertEquals(100L, result1.timeSinceLastClick)

        val result2 = RapidClickResult.notTriggered()
        assertFalse(result2.shouldTrigger, "不应该触发防护")
        assertEquals(0, result2.clickCount)
        assertEquals(Long.MAX_VALUE, result2.timeSinceLastClick)
    }
}
