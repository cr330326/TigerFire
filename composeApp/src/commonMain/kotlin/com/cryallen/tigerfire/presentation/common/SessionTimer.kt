package com.cryallen.tigerfire.presentation.common

import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 平台相关的日期/时间工具（expect/actual 模式）
 */
expect object PlatformDateTime {
    /**
     * 获取当前时间戳（毫秒）
     */
    fun getCurrentTimeMillis(): Long

    /**
     * 获取今日日期字符串（格式 "yyyy-MM-dd"）
     */
    fun getTodayDate(): String
}

/**
 * 会话计时器状态
 *
 * 表示计时器的当前运行状态
 */
enum class SessionTimerState {
    /**
     * 未启动
     */
    IDLE,

    /**
     * 正在计时
     */
    RUNNING,

    /**
     * 已暂停
     */
    PAUSED,

    /**
     * 时间到
     */
    TIME_LIMIT_REACHED
}

/**
 * 会话计时器
 *
 * 管理单次 App 使用时长，支持暂停/恢复，在时间到前发送提醒，
 * 时间到达时记录使用时长并触发状态变化
 *
 * 设计原则：
 * - 使用协程进行计时，避免阻塞主线程
 * - 通过 Flow 暴露状态，支持响应式更新
 * - 支持暂停/恢复，适应 App 生命周期变化
 * - 自动记录使用时长到数据库
 *
 * @param scope 协程作用域
 * @param progressRepository 进度仓储接口，用于记录使用时长
 */
class SessionTimer(
    private val scope: CoroutineScope,
    private val progressRepository: ProgressRepository
) {
    // ==================== 内部状态 ====================

    /**
     * 计时器是否正在运行的标志
     */
    private var isTimerRunning = false

    /**
     * 会话总时长（毫秒）
     */
    private var sessionDurationMillis: Long = 0L

    /**
     * 已计时时长（毫秒）
     */
    private var elapsedTimeMillis: Long = 0L

    /**
     * 提前提醒时间（毫秒）
     */
    private var reminderMillis: Long = 0L

    /**
     * 已发送提醒标志
     */
    private var reminderSent: Boolean = false

    /**
     * 会话开始时间戳（用于计算实际经过时间）
     */
    private var sessionStartTime: Long = 0L

    /**
     * 上次暂停时间戳（用于恢复时计算暂停时长）
     */
    private var pauseStartTime: Long = 0L

    /**
     * 总暂停时长（毫秒）
     */
    private var totalPausedMillis: Long = 0L

    // ==================== 公开状态流 ====================

    private val _timerState = MutableStateFlow(SessionTimerState.IDLE)
    /**
     * 计时器状态流
     */
    val timerState: StateFlow<SessionTimerState> = _timerState.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    /**
     * 已用时长流（毫秒）
     */
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _timeRemaining = MutableStateFlow(Long.MAX_VALUE)
    /**
     * 剩余时长流（毫秒）
     */
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    // ==================== 公开方法 ====================

    /**
     * 启动会话计时
     *
     * @param durationMinutes 会话时长（分钟）
     * @param reminderMinutesBefore 提前提醒时间（分钟），默认 2 分钟
     */
    fun startSession(
        durationMinutes: Int,
        reminderMinutesBefore: Int = 2
    ) {
        // 如果已有运行中的会话，先停止
        stopSession()

        // 重置所有状态
        sessionDurationMillis = durationMinutes * 60 * 1000L
        reminderMillis = reminderMinutesBefore * 60 * 1000L
        elapsedTimeMillis = 0L
        reminderSent = false
        totalPausedMillis = 0L
        sessionStartTime = PlatformDateTime.getCurrentTimeMillis()

        // 更新状态为运行中
        _timerState.value = SessionTimerState.RUNNING
        _elapsedTime.value = 0L
        _timeRemaining.value = sessionDurationMillis

        // 启动计时协程
        startTimerJob()
    }

    /**
     * 暂停会话计时
     *
     * 通常在 App 进入后台时调用
     */
    fun pauseSession() {
        if (_timerState.value != SessionTimerState.RUNNING) return

        isTimerRunning = false
        pauseStartTime = PlatformDateTime.getCurrentTimeMillis()
        _timerState.value = SessionTimerState.PAUSED
    }

    /**
     * 恢复会话计时
     *
     * 通常在 App 从后台恢复时调用
     */
    fun resumeSession() {
        if (_timerState.value != SessionTimerState.PAUSED) return

        // 累加暂停时长
        totalPausedMillis += (PlatformDateTime.getCurrentTimeMillis() - pauseStartTime)

        // 重新启动计时
        _timerState.value = SessionTimerState.RUNNING
        startTimerJob()
    }

    /**
     * 停止会话计时
     *
     * 取消计时器，不记录使用时长
     */
    fun stopSession() {
        isTimerRunning = false
        _timerState.value = SessionTimerState.IDLE
    }

    /**
     * 强制结束会话并记录使用时长
     *
     * 通常在用户主动退出或时间到时调用
     */
    fun endSessionAndRecord() {
        val finalElapsed = _elapsedTime.value
        stopSession()

        // 记录使用时长到数据库
        if (finalElapsed > 0) {
            scope.launch {
                val todayDate = PlatformDateTime.getTodayDate()
                progressRepository.recordUsage(todayDate, finalElapsed)
            }
        }

        _timerState.value = SessionTimerState.IDLE
    }

    /**
     * 报告用户活动
     *
     * 在某些实现中可用于重置空闲检测计时器
     * 当前实现中为保留接口，暂不实现具体逻辑
     */
    fun reportActivity() {
        // 预留接口，可用于未来扩展空闲检测功能
    }

    // ==================== 私有方法 ====================

    /**
     * 启动计时协程
     */
    private fun startTimerJob() {
        isTimerRunning = true
        scope.launch {
            val tickInterval = 1000L // 每秒更新一次

            while (isTimerRunning) {
                // 计算实际经过时间（考虑暂停时长）
                val currentElapsed = PlatformDateTime.getCurrentTimeMillis() - sessionStartTime - totalPausedMillis
                elapsedTimeMillis = currentElapsed.coerceAtLeast(0)

                // 更新状态流
                _elapsedTime.value = elapsedTimeMillis
                _timeRemaining.value = (sessionDurationMillis - elapsedTimeMillis).coerceAtLeast(0)

                // 检查是否需要发送提醒
                checkAndSendReminder()

                // 检查是否时间到
                if (elapsedTimeMillis >= sessionDurationMillis) {
                    handleTimeLimitReached()
                    break
                }

                delay(tickInterval)
            }
        }
    }

    /**
     * 检查并发送提前提醒
     */
    private fun checkAndSendReminder() {
        val remaining = sessionDurationMillis - elapsedTimeMillis

        // 如果剩余时间 <= 提前提醒时间，且尚未发送提醒
        if (remaining <= reminderMillis && !reminderSent) {
            reminderSent = true
            // 注意：这里不直接发送事件，而是通过状态变化让外部订阅者处理
            // 实际使用时，UI 层应订阅 timeRemaining 流并自行判断显示提醒
        }
    }

    /**
     * 处理时间到
     */
    private fun handleTimeLimitReached() {
        _timerState.value = SessionTimerState.TIME_LIMIT_REACHED

        // 记录本次会话使用时长
        endSessionAndRecord()
    }

    /**
     * 获取剩余分钟数
     *
     * @return 剩余分钟数（向上取整），0 表示时间已到
     */
    fun getRemainingMinutes(): Int {
        val remaining = _timeRemaining.value
        if (remaining <= 0) return 0
        return ((remaining + 60_000 - 1) / 60_000).toInt()
    }

    /**
     * 检查是否应该显示提醒
     *
     * @return true 如果应该显示提醒
     */
    fun shouldShowReminder(): Boolean {
        return reminderSent && _timerState.value == SessionTimerState.RUNNING
    }
}
