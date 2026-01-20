package com.cryallen.tigerfire.presentation.common

/**
 * 快速点击防护器
 *
 * 用于防止儿童疯狂点击同一按钮/元素。
 * 记录最近 3 次点击时间，如果连续 3 次点击间隔都小于阈值，
 * 则判定为快速点击行为，返回 true 触发防护。
 *
 * 设计原则：
 * - 无状态依赖，可独立使用
 * - 纯时间窗口检测，不依赖具体业务逻辑
 * - 自动维护固定长度的点击历史
 *
 * @property rapidClickThresholdMs 快速点击阈值（毫秒），默认 500ms
 * @property triggerCount 触发防护所需的连续快速点击次数，默认 3 次
 */
class RapidClickGuard(
    private val rapidClickThresholdMs: Long = DEFAULT_RAPID_CLICK_THRESHOLD_MS,
    private val triggerCount: Int = DEFAULT_TRIGGER_COUNT
) {
    // ==================== 内部状态 ====================

    /**
     * 最近 N 次点击的时间戳记录（队列结构）
     *
     * 只保留最近 triggerCount 次点击，超出部分自动丢弃
     */
    private val clickTimestamps = ArrayDeque<Long>()

    // ==================== 公开方法 ====================

    /**
     * 检查本次点击是否应触发防护
     *
     * 工作流程：
     * 1. 记录当前点击时间
     * 2. 检查最近 triggerCount 次点击是否都满足快速点击条件
     * 3. 返回是否应该触发防护
     *
     * @return true 如果应该触发防护（连续快速点击），false 否则
     */
    fun checkClick(): Boolean {
        val now = getCurrentTimeMillis()

        // 添加当前点击时间戳
        clickTimestamps.addLast(now)

        // 只保留最近 triggerCount 次点击记录
        if (clickTimestamps.size > triggerCount) {
            clickTimestamps.removeFirst()
        }

        // 如果记录数不足 triggerCount 次，不触发防护
        if (clickTimestamps.size < triggerCount) {
            return false
        }

        // 检查最近 triggerCount 次点击是否都满足快速点击条件
        return isRapidClickSequence()
    }

    /**
     * 重置防护状态
     *
     * 清空所有点击时间记录，恢复初始状态
     *
     * 使用场景：
     * - 场景切换时
     * - 用户完成正常操作后
     * - 超过一定时间无操作时
     */
    fun reset() {
        clickTimestamps.clear()
    }

    /**
     * 获取当前记录的点击次数
     *
     * @return 当前记录的点击时间戳数量
     */
    fun getClickCount(): Int {
        return clickTimestamps.size
    }

    /**
     * 获取最近一次点击距离现在的时间（毫秒）
     *
     * @return 距离上次点击的毫秒数，如果没有点击记录返回 Long.MAX_VALUE
     */
    fun getTimeSinceLastClick(): Long {
        if (clickTimestamps.isEmpty()) {
            return Long.MAX_VALUE
        }
        return getCurrentTimeMillis() - clickTimestamps.last()
    }

    // ==================== 私有方法 ====================

    /**
     * 检查最近 triggerCount 次点击是否构成快速点击序列
     *
     * 条件：相邻两次点击的间隔都必须小于 rapidClickThresholdMs
     *
     * @return true 如果构成快速点击序列
     */
    private fun isRapidClickSequence(): Boolean {
        if (clickTimestamps.size < triggerCount) {
            return false
        }

        // 检查相邻点击间隔
        for (i in 0 until clickTimestamps.size - 1) {
            val current = clickTimestamps[i]
            val next = clickTimestamps[i + 1]
            val interval = next - current

            // 如果任意两次间隔 >= 阈值，则不构成快速点击序列
            if (interval >= rapidClickThresholdMs) {
                return false
            }
        }

        // 所有点击间隔都小于阈值，构成快速点击序列
        return true
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private fun getCurrentTimeMillis(): Long {
        return PlatformDateTime.getCurrentTimeMillis()
    }

    companion object {
        /**
         * 默认快速点击阈值：500 毫秒
         *
         * 即：如果相邻两次点击间隔小于 500ms，视为快速点击
         */
        const val DEFAULT_RAPID_CLICK_THRESHOLD_MS = 500L

        /**
         * 默认触发次数：3 次
         *
         * 即：连续 3 次快速点击触发防护
         */
        const val DEFAULT_TRIGGER_COUNT = 3
    }
}

/**
 * 快速点击检测结果
 *
 * 表示快速点击防护的检测结果，包含详细信息
 */
data class RapidClickResult(
    /**
     * 是否触发防护
     */
    val shouldTrigger: Boolean,

    /**
     * 当前记录的点击次数
     */
    val clickCount: Int,

    /**
     * 距离上次点击的毫秒数
     */
    val timeSinceLastClick: Long
) {
    companion object {
        /**
         * 创建不触发防护的结果
         */
        fun notTriggered(): RapidClickResult {
            return RapidClickResult(
                shouldTrigger = false,
                clickCount = 0,
                timeSinceLastClick = Long.MAX_VALUE
            )
        }
    }
}
