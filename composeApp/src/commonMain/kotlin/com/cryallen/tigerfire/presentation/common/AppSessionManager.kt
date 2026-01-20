package com.cryallen.tigerfire.presentation.common

import com.cryallen.tigerfire.domain.model.ParentSettings
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * 应用会话管理器
 *
 * 全局单例，负责管理 App 使用时长限制。
 * 在应用启动时启动计时，在时间到前提醒，时间到时记录使用时长。
 *
 * 功能：
 * - 自动启动会话计时
 * - 暂停/恢复计时（App 后台/前台切换）
 * - 时间到前 2 分钟触发提醒
 * - 时间到时自动记录使用时长
 * - 提供实时状态查询
 *
 * @property scope 协程作用域
 * @property progressRepository 进度仓储接口
 */
class AppSessionManager(
    private val scope: CoroutineScope,
    private val progressRepository: ProgressRepository
) {
    // ==================== 内部状态 ====================

    /**
     * 会话计时器
     */
    private val sessionTimer = SessionTimer(scope, progressRepository)

    /**
     * 家长设置
     */
    private var parentSettings: ParentSettings = ParentSettings.default()

    /**
     * 会话是否已启动
     */
    private var isSessionStarted = false

    /**
     * 是否已触发时间到事件
     */
    private var timeLimitReachedTriggered = false

    // ==================== 公开状态 ====================

    /**
     * 计时器状态流
     */
    val timerState: StateFlow<SessionTimerState>
        get() = sessionTimer.timerState

    /**
     * 已用时长流（毫秒）
     */
    val elapsedTime: StateFlow<Long>
        get() = sessionTimer.elapsedTime

    /**
     * 剩余时长流（毫秒）
     */
    val timeRemaining: StateFlow<Long>
        get() = sessionTimer.timeRemaining

    // ==================== 公开方法 ====================

    /**
     * 初始化会话管理器
     *
     * 从数据库加载家长设置，然后启动会话计时
     * 应在应用启动时调用
     */
    fun initialize() {
        if (isSessionStarted) return

        // 加载家长设置（使用默认值，首次启动）
        parentSettings = ParentSettings.default()

        // 启动会话计时
        startSession()
        isSessionStarted = true
    }

    /**
     * 更新家长设置
     *
     * 当用户在家长模式中修改时间设置时调用
     *
     * @param settings 新的家长设置
     */
    fun updateSettings(settings: ParentSettings) {
        parentSettings = settings

        // 如果会话正在运行，使用新设置重新启动
        if (isSessionStarted && sessionTimer.timerState.value != SessionTimerState.IDLE) {
            restartSession()
        }
    }

    /**
     * 启动会话计时
     */
    fun startSession() {
        timeLimitReachedTriggered = false
        sessionTimer.startSession(
            durationMinutes = parentSettings.sessionDurationMinutes,
            reminderMinutesBefore = parentSettings.reminderMinutesBefore
        )
    }

    /**
     * 重新启动会话计时
     *
     * 使用当前设置重新开始计时
     */
    fun restartSession() {
        sessionTimer.stopSession()
        startSession()
    }

    /**
     * 暂停会话计时
     *
     * App 进入后台时调用
     */
    fun pauseSession() {
        sessionTimer.pauseSession()
    }

    /**
     * 恢复会话计时
     *
     * App 从后台恢复时调用
     */
    fun resumeSession() {
        sessionTimer.resumeSession()
    }

    /**
     * 停止会话计时并记录使用时长
     *
     * 应用退出时调用
     */
    fun stopSession() {
        sessionTimer.endSessionAndRecord()
        isSessionStarted = false
    }

    /**
     * 检查是否应该显示时间提醒
     *
     * @return true 如果应该显示提醒（剩余时间 <= 2分钟）
     */
    fun shouldShowTimeReminder(): Boolean {
        return sessionTimer.shouldShowReminder() &&
               sessionTimer.timerState.value == SessionTimerState.RUNNING
    }

    /**
     * 检查时间是否已到
     *
     * @return true 如果时间已到
     */
    fun isTimeLimitReached(): Boolean {
        return sessionTimer.timerState.value == SessionTimerState.TIME_LIMIT_REACHED
    }

    /**
     * 获取剩余分钟数
     *
     * @return 剩余分钟数（向上取整），-1 表示计时器未运行
     */
    fun getRemainingMinutes(): Int {
        if (sessionTimer.timerState.value == SessionTimerState.IDLE) {
            return -1
        }
        return sessionTimer.getRemainingMinutes()
    }

    /**
     * 获取会话总时长（分钟）
     *
     * @return 会话总时长（分钟）
     */
    fun getSessionDurationMinutes(): Int {
        return parentSettings.sessionDurationMinutes
    }

    /**
     * 获取提醒时间（分钟）
     *
     * @return 提前提醒时间（分钟）
     */
    fun getReminderMinutesBefore(): Int {
        return parentSettings.reminderMinutesBefore
    }

    /**
     * 标记时间到事件已处理
     *
     * UI 层处理完时间到事件后调用，避免重复处理
     */
    fun markTimeLimitReachedHandled() {
        timeLimitReachedTriggered = true
    }

    /**
     * 重置会话状态
     *
     * 用于测试或特殊场景
     */
    fun reset() {
        sessionTimer.stopSession()
        timeLimitReachedTriggered = false
        isSessionStarted = false
    }

    companion object {
        /**
         * 全局单例实例
         *
         * 注意：在 KMM 中实现真正的线程安全单例需要平台特定代码。
         * 这里使用简单的延迟初始化方式。
         */
        private var instance: AppSessionManager? = null

        /**
         * 获取单例实例
         *
         * @param scope 协程作用域
         * @param progressRepository 进度仓储接口
         * @return AppSessionManager 实例
         */
        fun getInstance(
            scope: CoroutineScope,
            progressRepository: ProgressRepository
        ): AppSessionManager {
            return instance ?: AppSessionManager(scope, progressRepository).also {
                instance = it
            }
        }

        /**
         * 清除单例实例
         *
         * 主要用于测试
         */
        fun clearInstance() {
            instance = null
        }
    }
}
