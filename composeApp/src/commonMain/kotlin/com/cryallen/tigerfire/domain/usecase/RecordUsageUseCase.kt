package com.cryallen.tigerfire.domain.usecase

import com.cryallen.tigerfire.domain.model.GameProgress
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.common.PlatformDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 记录使用时长用例
 *
 * 负责处理使用时长记录和统计的业务逻辑
 *
 * 功能包括：
 * - 记录单次使用时长
 * - 获取今日/本周/本月使用统计
 * - 计算平均使用时长
 * - 生成使用统计报告
 *
 * @param repository 进度仓储接口
 */
class RecordUsageUseCase(
    private val repository: ProgressRepository
) {
    /**
     * 记录使用时长
     *
     * @param durationMillis 使用时长（毫秒）
     * @return Result<Unit> 操作结果
     */
    suspend operator fun invoke(durationMillis: Long): Result<Unit> {
        return try {
            // 记录到今日统计
            val todayDate = PlatformDateTime.getTodayDate()
            repository.recordUsage(todayDate, durationMillis)

            // ✅ 修复：只更新总时长字段，避免覆盖fireStationCompletedItems等其他字段
            repository.addTotalPlayTime(durationMillis)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取今日使用时长
     *
     * @return Flow<Long> 今日使用时长（毫秒）
     */
    fun getTodayUsage(): Flow<Long> {
        return repository.getUsageForDate(PlatformDateTime.getTodayDate())
    }

    /**
     * 获取本周使用统计
     *
     * 返回最近7天的使用时长统计
     *
     * @return Flow<WeeklyStats> 本周统计数据
     */
    fun getWeeklyStats(): Flow<WeeklyStats> {
        return repository.getDailyUsageStats()
            .map { stats ->
                // 计算本周总时长
                val weeklyTotalMillis = stats.values.sum()

                // 计算每日平均时长
                val dailyAverageMillis = if (stats.isNotEmpty()) {
                    weeklyTotalMillis / stats.size
                } else {
                    0L
                }

                // 获取最近7天的数据
                val last7Days = getLast7DaysStats(stats)

                WeeklyStats(
                    totalMillis = weeklyTotalMillis,
                    dailyAverageMillis = dailyAverageMillis,
                    last7Days = last7Days,
                    activeDays = stats.size
                )
            }
    }

    /**
     * 获取使用统计报告
     *
     * 生成包含今日、本周、总计的统计报告
     *
     * @return Flow<UsageReport> 使用统计报告
     */
    fun getUsageReport(): Flow<UsageReport> {
        return repository.getGameProgress()
            .map { progress ->
                // 总游玩时长
                val totalPlayTimeMillis = progress.totalPlayTime

                UsageReport(
                    totalPlayTimeMillis = totalPlayTimeMillis,
                    totalPlayTimeMinutes = totalPlayTimeMillis / 1000 / 60,
                    totalPlayTimeHours = totalPlayTimeMillis / 1000 / 60 / 60
                )
            }
    }

    /**
     * 获取每日使用时长列表（用于图表展示）
     *
     * @param days 天数（默认7天）
     * @return Flow<List<DailyUsage>> 每日使用时长列表
     */
    fun getDailyUsageList(days: Int = 7): Flow<List<DailyUsage>> {
        return repository.getDailyUsageStats()
            .map { stats ->
                // 获取最近N天的数据
                val recentStats = stats.entries
                    .sortedByDescending { it.key }
                    .take(days)
                    .reversed()

                recentStats.map { (date, duration) ->
                    DailyUsage(
                        date = date,
                        durationMillis = duration,
                        durationMinutes = duration / 1000 / 60
                    )
                }
            }
    }

    /**
     * 获取最长单次使用时长
     *
     * @return Flow<Long> 最长单次使用时长（毫秒）
     */
    fun getMaxSingleSessionDuration(): Flow<Long> {
        return repository.getDailyUsageStats()
            .map { stats ->
                // 单次使用时长的最大值
                stats.values.maxOrNull() ?: 0L
            }
    }

    /**
     * 检查今日是否已使用
     *
     * @return Flow<Boolean> 今日是否有使用记录
     */
    fun hasUsedToday(): Flow<Boolean> {
        return getTodayUsage()
            .map { duration -> duration > 0 }
    }

    /**
     * 获取使用趋势
     *
     * 比较最近7天与之前7天的平均使用时长
     *
     * @return Flow<UsageTrend> 使用趋势
     */
    fun getUsageTrend(): Flow<UsageTrend> {
        return repository.getDailyUsageStats()
            .map { stats ->
                // 简化处理，使用最近7天的数据
                val recent7Days = stats.values.toList().takeLast(7)
                if (recent7Days.size < 2) {
                    return@map UsageTrend(UsageTrendDirection.STABLE, 0f)
                }

                val recentAverage = recent7Days.average()
                val previousAverage = recent7Days
                    .dropLast(recent7Days.size / 2)
                    .average()

                val changePercent = if (previousAverage > 0) {
                    ((recentAverage - previousAverage) / previousAverage * 100).toFloat()
                } else {
                    0f
                }

                val direction = when {
                    changePercent > 10 -> UsageTrendDirection.INCREASING
                    changePercent < -10 -> UsageTrendDirection.DECREASING
                    else -> UsageTrendDirection.STABLE
                }

                UsageTrend(direction, changePercent)
            }
    }

    /**
     * 获取最近7天的统计数据
     *
     * @param stats 所有统计数据
     * @return List<DailyUsage> 最近7天数据
     */
    private fun getLast7DaysStats(stats: Map<String, Long>): List<DailyUsage> {
        val today = PlatformDateTime.getTodayDate()

        return (0..6).map { dayOffset ->
            // 简化日期计算，实际可能需要更复杂的日期处理
            val date = getDateDaysAgo(dayOffset)
            val duration = stats[date] ?: 0L

            DailyUsage(
                date = date,
                durationMillis = duration,
                durationMinutes = duration / 1000 / 60
            )
        }.reversed()
    }

    /**
     * 获取N天前的日期字符串
     *
     * @param days 天数
     * @return String 日期字符串 "yyyy-MM-dd"
     */
    private fun getDateDaysAgo(days: Int): String {
        // 简化处理，实际应使用平台相关的日期计算
        // 这里返回占位符，实际应使用 expect/actual 实现
        return PlatformDateTime.getTodayDate() // TODO: 实现日期减法
    }
}

/**
 * 本周统计数据
 *
 * @property totalMillis 本周总使用时长（毫秒）
 * @property dailyAverageMillis 每日平均使用时长（毫秒）
 * @property last7Days 最近7天数据
 * @property activeDays 活跃天数
 */
data class WeeklyStats(
    val totalMillis: Long,
    val dailyAverageMillis: Long,
    val last7Days: List<DailyUsage>,
    val activeDays: Int
) {
    /**
     * 总使用时长（分钟）
     */
    val totalMinutes: Int
        get() = (totalMillis / 1000 / 60).toInt()

    /**
     * 每日平均时长（分钟）
     */
    val dailyAverageMinutes: Int
        get() = (dailyAverageMillis / 1000 / 60).toInt()
}

/**
 * 每日使用数据
 *
 * @property date 日期 "yyyy-MM-dd"
 * @property durationMillis 使用时长（毫秒）
 * @property durationMinutes 使用时长（分钟）
 */
data class DailyUsage(
    val date: String,
    val durationMillis: Long,
    val durationMinutes: Long
)

/**
 * 使用统计报告
 *
 * @property totalPlayTimeMillis 总游玩时长（毫秒）
 * @property totalPlayTimeMinutes 总游玩时长（分钟）
 * @property totalPlayTimeHours 总游玩时长（小时）
 */
data class UsageReport(
    val totalPlayTimeMillis: Long,
    val totalPlayTimeMinutes: Long,
    val totalPlayTimeHours: Long
) {
    /**
     * 格式化总时长显示
     */
    fun getFormattedTotalTime(): String {
        return when {
            totalPlayTimeHours > 0 -> "${totalPlayTimeHours}小时${totalPlayTimeMinutes % 60}分钟"
            totalPlayTimeMinutes > 0 -> "${totalPlayTimeMinutes}分钟"
            else -> "0分钟"
        }
    }
}

/**
 * 使用趋势方向
 */
enum class UsageTrendDirection {
    /** 增加 */
    INCREASING,

    /** 减少 */
    DECREASING,

    /** 稳定 */
    STABLE
}

/**
 * 使用趋势
 *
 * @property direction 趋势方向
 * @property changePercent 变化百分比
 */
data class UsageTrend(
    val direction: UsageTrendDirection,
    val changePercent: Float
) {
    /**
     * 获取趋势描述文本
     */
    fun getDescription(): String {
        return when (direction) {
            UsageTrendDirection.INCREASING -> "比上周增加 ${changePercent.toInt()}%"
            UsageTrendDirection.DECREASING -> "比上周减少 ${kotlin.math.abs(changePercent).toInt()}%"
            UsageTrendDirection.STABLE -> "与上周持平"
        }
    }
}
