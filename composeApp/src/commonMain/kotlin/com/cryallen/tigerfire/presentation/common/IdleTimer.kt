package com.cryallen.tigerfire.presentation.common

import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.delay

/**
 * 空闲计时器状态
 *
 * 表示空闲检测器的当前运行状态
 */
enum class IdleTimerState {
    /**
     * 未启动
     */
    IDLE,

    /**
     * 正在检测
     */
    RUNNING,

    /**
     * 已暂停
     */
    PAUSED,

    /**
     * 空闲超时已触发
     */
    TRIGGERED
}

/**
 * 空闲计时器
 *
 * 用于检测用户无操作超时。当用户在指定时间内没有任何交互时，
 * 触发回调函数（如显示小火提示："需要帮忙吗？"）
 *
 * 设计原则：
 * - 使用协程延迟实现非阻塞检测
 * - 任何用户活动都会重置计时器
 * - 支持启动/停止/暂停/恢复
 * - 适用于儿童友好设计（30秒无操作提供引导）
 *
 * @param scope 协程作用域
 */
class IdleTimer(
    private val scope: CoroutineScope
) {
    // ==================== 内部状态 ====================

    /**
     * 检测是否正在运行的标志
     */
    private var isDetectionRunning = false

    /**
     * 空闲超时时间（毫秒）
     */
    private var timeoutMillis: Long = 0L

    /**
     * 空闲触发回调
     */
    private var onIdleCallback: (() -> Unit)? = null

    /**
     * 最后一次活动时间戳（用于重置计时）
     */
    private var lastActivityTime: Long = 0L

    // ==================== 公开状态 ====================

    private var _state = IdleTimerState.IDLE
    /**
     * 计时器当前状态
     */
    val state: IdleTimerState
        get() = _state

    // ==================== 公开方法 ====================

    /**
     * 启动空闲检测
     *
     * 开始监听用户活动，如果在指定时间内无任何操作，则触发回调。
     * 每次用户调用 [reportActivity] 都会重置计时器。
     *
     * @param timeoutMillis 超时时间（毫秒），默认 30 秒
     * @param onIdle 空闲超时时的回调函数
     */
    fun startIdleDetection(
        timeoutMillis: Long = DEFAULT_IDLE_TIMEOUT_MS,
        onIdle: () -> Unit
    ) {
        // 如果已有运行中的检测，先停止
        stopIdleDetection()

        this.timeoutMillis = timeoutMillis
        this.onIdleCallback = onIdle
        this.isDetectionRunning = true
        this._state = IdleTimerState.RUNNING

        // 记录初始活动时间
        lastActivityTime = getCurrentTimeMillis()

        // 启动检测协程
        startDetectionJob()
    }

    /**
     * 停止空闲检测
     *
     * 取消检测协程，不再监听用户活动
     */
    fun stopIdleDetection() {
        isDetectionRunning = false
        _state = IdleTimerState.IDLE
        onIdleCallback = null
    }

    /**
     * 暂停空闲检测
     *
     * 暂停计时，但不停止检测器。
     * 调用 [resumeIdleDetection] 可恢复检测。
     */
    fun pauseIdleDetection() {
        if (_state != IdleTimerState.RUNNING) return

        isDetectionRunning = false
        _state = IdleTimerState.PAUSED
    }

    /**
     * 恢复空闲检测
     *
     * 从暂停状态恢复检测，重置计时器
     */
    fun resumeIdleDetection() {
        if (_state != IdleTimerState.PAUSED) return

        isDetectionRunning = true
        _state = IdleTimerState.RUNNING
        lastActivityTime = getCurrentTimeMillis()
        startDetectionJob()
    }

    /**
     * 报告用户活动
     *
     * 当检测到任何用户交互时调用此方法，会重置空闲计时器。
     *
     * 典型调用场景：
     * - 屏幕点击/触摸
     * - 按钮按下
     * - 手势操作（拖拽、滑动）
     * - 键盘输入
     */
    fun reportActivity() {
        if (!isDetectionRunning) return

        // 更新最后活动时间
        lastActivityTime = getCurrentTimeMillis()

        // 检测器会自动在下一次循环中检测到新的活动时间
    }

    /**
     * 获取距离空闲触发还剩多少时间
     *
     * @return 剩余毫秒数，如果未启动返回 Long.MAX_VALUE
     */
    fun getTimeUntilIdle(): Long {
        if (!isDetectionRunning || timeoutMillis == 0L) {
            return Long.MAX_VALUE
        }

        val elapsed = getCurrentTimeMillis() - lastActivityTime
        return (timeoutMillis - elapsed).coerceAtLeast(0)
    }

    // ==================== 私有方法 ====================

    /**
     * 启动检测协程
     */
    private fun startDetectionJob() {
        scope.launch {
            while (isDetectionRunning) {
                val now = getCurrentTimeMillis()
                val elapsedSinceActivity = now - lastActivityTime

                // 检查是否超时
                if (elapsedSinceActivity >= timeoutMillis) {
                    handleIdleTimeout()
                    break
                }

                // 计算下次检查的延迟时间
                // 每秒检查一次，或直到超时时间
                val checkInterval = minOf(1000L, timeoutMillis - elapsedSinceActivity)
                delay(checkInterval.coerceAtLeast(100))
            }
        }
    }

    /**
     * 处理空闲超时
     */
    private fun handleIdleTimeout() {
        isDetectionRunning = false
        _state = IdleTimerState.TRIGGERED

        // 触发回调
        onIdleCallback?.invoke()
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private fun getCurrentTimeMillis(): Long {
        return PlatformDateTime.getCurrentTimeMillis()
    }

    companion object {
        /**
         * 默认空闲超时时间：30 秒
         *
         * 根据 spec.md，无操作 30 秒后小火弹出提示："需要帮忙吗？"
         */
        const val DEFAULT_IDLE_TIMEOUT_MS = 30_000L

        /**
         * 最小超时时间：5 秒
         *
         * 防止设置过短导致频繁触发
         */
        const val MIN_IDLE_TIMEOUT_MS = 5_000L

        /**
         * 最大超时时间：5 分钟
         *
         * 防止设置过长失去提示意义
         */
        const val MAX_IDLE_TIMEOUT_MS = 300_000L
    }
}
