package com.cryallen.tigerfire.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ParentSettings 单元测试
 */
class ParentSettingsTest {

    @Test
    fun testDefaultSettings() {
        // 测试默认设置
        val settings = ParentSettings.default()

        assertEquals(15, settings.sessionDurationMinutes, "默认时长应该是 15 分钟")
        assertEquals(2, settings.reminderMinutesBefore, "默认提醒应该是 2 分钟")
        assertTrue(settings.dailyUsageStats.isEmpty(), "默认使用统计应该为空")
    }

    @Test
    fun testDefaultConstants() {
        // 测试默认常量
        assertEquals(15, ParentSettings.DEFAULT_SESSION_DURATION, "默认会话时长应该是 15 分钟")
        assertEquals(2, ParentSettings.DEFAULT_REMINDER_MINUTES, "默认提醒时间应该是 2 分钟")

        val expectedDurations = listOf(5, 10, 15, 30)
        assertEquals(expectedDurations, ParentSettings.AVAILABLE_DURATIONS, "可选时长应该是 [5, 10, 15, 30]")
    }

    @Test
    fun testGetSessionDurationMillis() {
        // 测试获取会话时长（毫秒）
        val settings = ParentSettings.default()

        // 15 分钟 = 15 * 60 * 1000 = 900000 毫秒
        val expected = 15 * 60 * 1000L
        assertEquals(expected, settings.getSessionDurationMillis(), "15 分钟应该是 900000 毫秒")
    }

    @Test
    fun testGetReminderMillis() {
        // 测试获取提醒时间（毫秒）
        val settings = ParentSettings.default()

        // 2 分钟 = 2 * 60 * 1000 = 120000 毫秒
        val expected = 2 * 60 * 1000L
        assertEquals(expected, settings.getReminderMillis(), "2 分钟应该是 120000 毫秒")
    }

    @Test
    fun testUpdateSessionDuration() {
        // 测试更新会话时长
        val settings = ParentSettings.default()

        // 更新为 10 分钟
        val updated = settings.updateSessionDuration(10)
        assertEquals(10, updated.sessionDurationMinutes, "时长应该是 10 分钟")

        // 更新为 30 分钟
        val updated2 = updated.updateSessionDuration(30)
        assertEquals(30, updated2.sessionDurationMinutes, "时长应该是 30 分钟")

        // 原设置不受影响
        assertEquals(15, settings.sessionDurationMinutes, "原设置应该不变")
    }

    @Test
    fun testUpdateSessionDurationWithInvalidValue() {
        // 测试使用无效值更新会话时长
        val settings = ParentSettings.default()

        // 尝试使用无效值
        assertFailsWith<IllegalArgumentException> {
            settings.updateSessionDuration(20)
        }

        assertFailsWith<IllegalArgumentException> {
            settings.updateSessionDuration(0)
        }

        assertFailsWith<IllegalArgumentException> {
            settings.updateSessionDuration(-5)
        }
    }

    @Test
    fun testRecordUsage() {
        // 测试记录使用时长
        val settings = ParentSettings.default()
        val date = "2026-01-21"

        // 记录 30 分钟
        val recorded = settings.recordUsage(date, 30 * 60 * 1000L)
        assertEquals(30 * 60 * 1000L, recorded.getUsageForDate(date), "应该记录 30 分钟")

        // 再记录 15 分钟
        val recorded2 = recorded.recordUsage(date, 15 * 60 * 1000L)
        assertEquals(45 * 60 * 1000L, recorded2.getUsageForDate(date), "总共应该记录 45 分钟")

        // 记录不同日期
        val date2 = "2026-01-22"
        val recorded3 = recorded2.recordUsage(date2, 20 * 60 * 1000L)
        assertEquals(45 * 60 * 1000L, recorded3.getUsageForDate(date), "第一天的记录应该不变")
        assertEquals(20 * 60 * 1000L, recorded3.getUsageForDate(date2), "第二天应该记录 20 分钟")
    }

    @Test
    fun testGetUsageForDate() {
        // 测试获取指定日期的使用时长
        val settings = ParentSettings.default()
        val date = "2026-01-21"

        // 没有记录时返回 0
        assertEquals(0L, settings.getUsageForDate(date), "没有记录时应该返回 0")

        // 记录后返回正确值
        val recorded = settings.recordUsage(date, 60 * 60 * 1000L) // 1 小时
        assertEquals(60 * 60 * 1000L, recorded.getUsageForDate(date), "应该返回记录的时长")
    }

    @Test
    fun testGetWeeklyUsage() {
        // 测试获取本周总使用时长
        val settings = ParentSettings.default()

        val weekDays = listOf(
            "2026-01-20", "2026-01-21", "2026-01-22",
            "2026-01-23", "2026-01-24", "2026-01-25", "2026-01-26"
        )

        // 记录不同日期的使用时长
        val withUsage = settings
            .recordUsage("2026-01-20", 30 * 60 * 1000L) // 30 分钟
            .recordUsage("2026-01-21", 60 * 60 * 1000L) // 1 小时
            .recordUsage("2026-01-22", 45 * 60 * 1000L) // 45 分钟
            .recordUsage("2026-01-26", 15 * 60 * 1000L) // 15 分钟

        // 本周总计：30 + 60 + 45 + 15 = 150 分钟 = 2.5 小时
        val expectedWeekly = (30 + 60 + 45 + 15) * 60 * 1000L
        assertEquals(expectedWeekly, withUsage.getWeeklyUsage(weekDays), "本周总时长应该是 150 分钟")

        // 计算包含未记录日期的使用时长（未记录的日期返回 0）
        val anotherWeek = listOf("2026-01-15", "2026-01-16")
        val anotherWeekUsage = withUsage.getWeeklyUsage(anotherWeek)
        assertEquals(0L, anotherWeekUsage, "没有记录的周应该返回 0")
    }

    @Test
    fun testClearUsageStats() {
        // 测试清除使用统计
        val settings = ParentSettings.default()

        // 记录一些使用数据
        val withUsage = settings
            .recordUsage("2026-01-20", 30 * 60 * 1000L)
            .recordUsage("2026-01-21", 60 * 60 * 1000L)

        assertFalse(withUsage.dailyUsageStats.isEmpty(), "应该有使用记录")

        // 清除统计数据
        val cleared = withUsage.clearUsageStats()
        assertTrue(cleared.dailyUsageStats.isEmpty(), "清除后应该没有记录")
        assertEquals(0L, cleared.getUsageForDate("2026-01-20"), "清除后应该返回 0")
    }

    @Test
    fun testGetSortedDates() {
        // 测试获取排序后的日期列表
        val settings = ParentSettings.default()

        // 没有记录时返回空列表
        assertTrue(settings.getSortedDates().isEmpty(), "没有记录时应该返回空列表")

        // 记录不同日期的使用
        val withUsage = settings
            .recordUsage("2026-01-20", 30 * 60 * 1000L)
            .recordUsage("2026-01-22", 60 * 60 * 1000L)
            .recordUsage("2026-01-21", 45 * 60 * 1000L)

        // 获取排序后的日期（倒序）
        val sortedDates = withUsage.getSortedDates()
        assertEquals(3, sortedDates.size, "应该有 3 个日期")
        assertEquals("2026-01-22", sortedDates[0], "第一个应该是最新日期")
        assertEquals("2026-01-21", sortedDates[1], "第二个应该是中间日期")
        assertEquals("2026-01-20", sortedDates[2], "第三个应该是最早日期")
    }

    @Test
    fun testAvailableDurations() {
        // 测试可选时长
        val settings = ParentSettings.default()

        ParentSettings.AVAILABLE_DURATIONS.forEach { duration ->
            val updated = settings.updateSessionDuration(duration)
            assertEquals(duration, updated.sessionDurationMinutes, "时长应该是 $duration 分钟")
        }
    }

    @Test
    fun testSettingsImmutability() {
        // 测试设置的不可变性
        val settings = ParentSettings.default()
        val originalDuration = settings.sessionDurationMinutes
        val originalStats = settings.dailyUsageStats.size

        // 更新设置
        val updated = settings
            .updateSessionDuration(30)
            .recordUsage("2026-01-21", 60 * 60 * 1000L)

        // 验证原设置未改变
        assertEquals(originalDuration, settings.sessionDurationMinutes, "原时长应该不变")
        assertEquals(originalStats, settings.dailyUsageStats.size, "原统计数量应该不变")

        // 验证新设置已改变
        assertEquals(30, updated.sessionDurationMinutes, "新时长应该改变")
        assertEquals(1, updated.dailyUsageStats.size, "新统计数量应该改变")
    }

    @Test
    fun testCumulativeUsage() {
        // 测试累计使用时长
        val settings = ParentSettings.default()
        val date = "2026-01-21"

        // 多次记录同一天的使用
        val recorded = settings
            .recordUsage(date, 10 * 60 * 1000L)
            .recordUsage(date, 15 * 60 * 1000L)
            .recordUsage(date, 20 * 60 * 1000L)

        // 累计应该是 45 分钟
        assertEquals(45 * 60 * 1000L, recorded.getUsageForDate(date), "累计应该是 45 分钟")
    }

    @Test
    fun testMultipleDatesUsage() {
        // 测试多日期使用统计
        val settings = ParentSettings.default()

        val dates = listOf(
            "2026-01-18" to 30,
            "2026-01-19" to 45,
            "2026-01-20" to 60,
            "2026-01-21" to 15
        )

        val withUsage = dates.fold(settings) { acc, (date, minutes) ->
            acc.recordUsage(date, minutes * 60 * 1000L)
        }

        // 验证每个日期的记录
        dates.forEach { (date, minutes) ->
            assertEquals(minutes * 60 * 1000L, withUsage.getUsageForDate(date), "日期 $date 的记录应该正确")
        }

        // 验证总记录数
        assertEquals(4, withUsage.dailyUsageStats.size, "应该有 4 条记录")
    }
}
