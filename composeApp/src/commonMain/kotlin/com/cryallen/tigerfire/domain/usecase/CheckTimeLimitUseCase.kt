package com.cryallen.tigerfire.domain.usecase

import com.cryallen.tigerfire.domain.model.ParentSettings
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.common.PlatformDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 检查时间限制用例
 *
 * 负责处理家长时间控制相关的业务逻辑
 *
 * 功能包括：
 * - 检查是否已达到使用时长限制
 * - 检查是否需要显示提醒（提前2分钟）
 * - 计算剩余使用时间
 * - 检查是否可以延长使用时间
 *
 * @param repository 进度仓储接口
 */
class CheckTimeLimitUseCase(
    private val repository: ProgressRepository
) {
    /**
     * 时间限制检查结果
     *
     * @property hasReachedLimit 是否已达到时间限制
     * @property shouldShowReminder 是否应该显示提醒（提前2分钟）
     * @property remainingTimeMillis 剩余时间（毫秒）
     * @property canExtend 是否可以延长使用时间
     */
    data class TimeLimitResult(
        val hasReachedLimit: Boolean,
        val shouldShowReminder: Boolean,
        val remainingTimeMillis: Long,
        val canExtend: Boolean
    ) {
        /**
         * 获取剩余时间（分钟）
         */
        val remainingMinutes: Int
            get() = (remainingTimeMillis / 1000 / 60).toInt()

        /**
         * 格式化剩余时间文本
         */
        fun getRemainingTimeText(): String {
            val minutes = remainingTimeMillis / 1000 / 60
            val seconds = (remainingTimeMillis / 1000 % 60).toInt()
            return "${minutes}分${seconds}秒"
        }
    }

    /**
     * 检查时间限制
     *
     * @param elapsedTimeMs 已使用时长（毫秒）
     * @return TimeLimitResult 时间限制检查结果
     */
    suspend operator fun invoke(elapsedTimeMs: Long): TimeLimitResult {
        return try {
            // 获取家长设置
            val settings = repository.getParentSettings().first()
            val sessionDurationMs = settings.sessionDurationMinutes * 60 * 1000L
            val reminderMs = settings.reminderMinutesBefore * 60 * 1000L

            // 计算剩余时间
            val remainingTimeMs = (sessionDurationMs - elapsedTimeMs).coerceAtLeast(0)

            // 判断是否达到限制
            val hasReachedLimit = elapsedTimeMs >= sessionDurationMs

            // 判断是否需要显示提醒（提前2分钟）
            val shouldShowReminder = !hasReachedLimit &&
                    remainingTimeMs <= reminderMs &&
                    remainingTimeMs > 0

            // 判断是否可以延长（默认可以延长5分钟）
            val canExtend = hasReachedLimit

            TimeLimitResult(
                hasReachedLimit = hasReachedLimit,
                shouldShowReminder = shouldShowReminder,
                remainingTimeMillis = remainingTimeMs,
                canExtend = canExtend
            )
        } catch (e: Exception) {
            // 出错时返回默认值（不限制）
            TimeLimitResult(
                hasReachedLimit = false,
                shouldShowReminder = false,
                remainingTimeMillis = Long.MAX_VALUE,
                canExtend = false
            )
        }
    }

    /**
     * 检查是否需要显示提醒
     *
     * @param elapsedTimeMs 已使用时长（毫秒）
     * @return Flow<Boolean> 是否需要显示提醒
     */
    fun shouldShowReminder(elapsedTimeMs: Long): Flow<Boolean> {
        return repository.getParentSettings().map { settings ->
            val sessionDurationMs = settings.sessionDurationMinutes * 60 * 1000L
            val reminderMs = settings.reminderMinutesBefore * 60 * 1000L
            val remainingTimeMs = sessionDurationMs - elapsedTimeMs

            // 在提醒时间窗口内显示提醒
            remainingTimeMs in 1..reminderMs
        }
    }

    /**
     * 检查是否已达到时间限制
     *
     * @param elapsedTimeMs 已使用时长（毫秒）
     * @return Flow<Boolean> 是否已达到限制
     */
    fun hasReachedLimit(elapsedTimeMs: Long): Flow<Boolean> {
        return repository.getParentSettings().map { settings ->
            val sessionDurationMs = settings.sessionDurationMinutes * 60 * 1000L
            elapsedTimeMs >= sessionDurationMs
        }
    }

    /**
     * 获取剩余时间
     *
     * @param elapsedTimeMs 已使用时长（毫秒）
     * @return Flow<Long> 剩余时间（毫秒）
     */
    fun getRemainingTime(elapsedTimeMs: Long): Flow<Long> {
        return repository.getParentSettings().map { settings ->
            val sessionDurationMs = settings.sessionDurationMinutes * 60 * 1000L
            (sessionDurationMs - elapsedTimeMs).coerceAtLeast(0)
        }
    }

    /**
     * 获取今日使用时长
     *
     * @return Flow<Long> 今日使用时长（毫秒）
     */
    fun getTodayUsage(): Flow<Long> {
        return repository.getUsageForDate(getTodayDate())
    }

    /**
     * 获取今日剩余可用时间
     *
     * @return Flow<Long> 今日剩余时间（毫秒）
     */
    fun getTodayRemainingTime(): Flow<Long> {
        return repository.getParentSettings().map { settings ->
            val sessionDurationMs = settings.sessionDurationMinutes * 60 * 1000L
            val todayDate = getTodayDate()

            // 这里简化处理，假设每天有独立的额度
            // 实际可能需要更复杂的逻辑来处理每日使用次数
            sessionDurationMs
        }
    }

    /**
     * 延长使用时间
     *
     * 默认延长5分钟，可通过参数自定义
     *
     * @param additionalMinutes 额外分钟数（默认5分钟）
     * @return Result<ParentSettings> 更新后的设置
     */
    suspend fun extendTime(additionalMinutes: Int = 5): Result<ParentSettings> {
        return try {
            val currentSettings = repository.getParentSettings().first()
            val updatedSettings = currentSettings.copy(
                sessionDurationMinutes = currentSettings.sessionDurationMinutes + additionalMinutes
            )
            repository.updateParentSettings(updatedSettings)
            Result.success(updatedSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取时间限制设置
     *
     * @return Flow<ParentSettings> 家长设置流
     */
    fun getTimeLimitSettings(): Flow<ParentSettings> {
        return repository.getParentSettings()
    }

    /**
     * 更新时间限制设置
     *
     * @param minutes 单次使用时长（分钟）
     * @return Result<ParentSettings> 更新后的设置
     */
    suspend fun updateTimeLimit(minutes: Int): Result<ParentSettings> {
        return try {
            // 验证输入范围
            val validMinutes = minutes.coerceIn(5, 120) // 5分钟到2小时

            val currentSettings = repository.getParentSettings().first()
            val updatedSettings = currentSettings.copy(
                sessionDurationMinutes = validMinutes
            )
            repository.updateParentSettings(updatedSettings)
            Result.success(updatedSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取今日日期字符串
     *
     * @return String 格式 "yyyy-MM-dd"
     */
    private fun getTodayDate(): String {
        return PlatformDateTime.getTodayDate()
    }
}
