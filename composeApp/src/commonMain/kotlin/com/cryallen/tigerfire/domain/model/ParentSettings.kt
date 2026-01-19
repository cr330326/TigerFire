package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 家长设置模型
 *
 * 表示家长模式中的配置项，包括时间控制、使用统计等
 *
 * @property sessionDurationMinutes 单次使用时长（分钟）
 * @property reminderMinutesBefore 提前提醒时间（分钟）
 * @property dailyUsageStats 每日使用统计（日期格式 "yyyy-MM-dd" -> 时长毫秒）
 */
@Serializable
data class ParentSettings(
    val sessionDurationMinutes: Int = DEFAULT_SESSION_DURATION,
    val reminderMinutesBefore: Int = DEFAULT_REMINDER_MINUTES,
    val dailyUsageStats: Map<String, Long> = emptyMap()
) {
    companion object {
        /** 默认单次使用时长：15 分钟 */
        const val DEFAULT_SESSION_DURATION = 15

        /** 默认提前提醒时间：2 分钟 */
        const val DEFAULT_REMINDER_MINUTES = 2

        /** 可选的使用时长选项（分钟） */
        val AVAILABLE_DURATIONS = listOf(5, 10, 15, 30)

        /**
         * 创建默认设置
         */
        fun default(): ParentSettings {
            return ParentSettings(
                sessionDurationMinutes = DEFAULT_SESSION_DURATION,
                reminderMinutesBefore = DEFAULT_REMINDER_MINUTES,
                dailyUsageStats = emptyMap()
            )
        }
    }

    /**
     * 获取会话总时长（毫秒）
     */
    fun getSessionDurationMillis(): Long {
        return sessionDurationMinutes * 60 * 1000L
    }

    /**
     * 获取提醒时间（毫秒）
     */
    fun getReminderMillis(): Long {
        return reminderMinutesBefore * 60 * 1000L
    }

    /**
     * 更新使用时长
     */
    fun updateSessionDuration(minutes: Int): ParentSettings {
        require(minutes in AVAILABLE_DURATIONS) {
            "Duration must be one of: $AVAILABLE_DURATIONS"
        }
        return copy(sessionDurationMinutes = minutes)
    }

    /**
     * 记录今日使用时长
     *
     * @param date 日期字符串（格式 "yyyy-MM-dd"）
     * @param durationMillis 使用时长（毫秒）
     */
    fun recordUsage(date: String, durationMillis: Long): ParentSettings {
        val currentDuration = dailyUsageStats[date] ?: 0L
        return copy(
            dailyUsageStats = dailyUsageStats + (date to (currentDuration + durationMillis))
        )
    }

    /**
     * 获取指定日期的使用时长
     *
     * @param date 日期字符串（格式 "yyyy-MM-dd"）
     * @return 使用时长（毫秒），无记录返回 0
     */
    fun getUsageForDate(date: String): Long {
        return dailyUsageStats[date] ?: 0L
    }

    /**
     * 获取本周总使用时长
     *
     * @param weekDays 本周日期列表（格式 "yyyy-MM-dd"）
     * @return 总时长（毫秒）
     */
    fun getWeeklyUsage(weekDays: List<String>): Long {
        return weekDays.sumOf { date -> dailyUsageStats[date] ?: 0L }
    }

    /**
     * 清除使用统计数据
     */
    fun clearUsageStats(): ParentSettings {
        return copy(dailyUsageStats = emptyMap())
    }

    /**
     * 获取使用统计中所有日期（按时间倒序）
     */
    fun getSortedDates(): List<String> {
        return dailyUsageStats.keys.sortedDescending()
    }
}
