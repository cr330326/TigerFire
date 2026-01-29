package com.cryallen.tigerfire.presentation.parent

import com.cryallen.tigerfire.domain.model.ParentSettings
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 家长模式状态
 *
 * 表示家长模式页面的 UI 状态
 */
data class ParentState(
    /**
     * 家长设置（时间管理、使用限制等）
     */
    val settings: ParentSettings = ParentSettings(),

    /**
     * 今日使用时长（毫秒）
     */
    val todayPlayTime: Long = 0L,

    /**
     * 总使用时长（毫秒）
     */
    val totalPlayTime: Long = 0L,

    /**
     * 各场景的完成状态
     */
    val sceneStatuses: Map<SceneType, SceneStatus> = emptyMap(),

    /**
     * 已收集的徽章总数
     */
    val totalBadgeCount: Int = 0,

    /**
     * 是否显示重置确认对话框
     */
    val showResetConfirmation: Boolean = false,

    /**
     * 是否显示重新验证界面（敏感操作前）
     */
    val showReverification: Boolean = false,

    /**
     * 重新验证的数学题（问题文本 + 答案）
     */
    val reverificationQuestion: Pair<String, Int>? = null,

    /**
     * 当前正在执行的操作（需要重新验证）
     */
    val pendingAction: ParentAction? = null,

    /**
     * 是否显示时间设置对话框
     */
    val showTimeSettingsDialog: Boolean = false,

    /**
     * 每次使用时长限制开关状态
     */
    val sessionTimeLimitEnabled: Boolean = false,

    /**
     * 每日总时长限制开关状态
     */
    val dailyTimeLimitEnabled: Boolean = false,

    /**
     * 待应用的每次使用时长（分钟）
     * 用户选择的时间，验证通过后才会实际应用
     */
    val pendingSessionTimeLimit: Int? = null,

    /**
     * 是否显示设置保存成功提示
     */
    val showSettingsSavedHint: Boolean = false,

    /**
     * 是否显示重置成功提示
     */
    val showResetSuccessHint: Boolean = false,

    /**
     * 是否显示验证失败提示
     */
    val showVerificationFailedHint: Boolean = false
)

/**
 * 家长模式敏感操作
 *
 * 需要数学验证才能执行的操作
 */
enum class ParentAction {
    /**
     * 修改时间设置
     */
    UPDATE_TIME_SETTINGS,

    /**
     * 重置游戏进度
     */
    RESET_PROGRESS,

    /**
     * 清除使用统计
     */
    CLEAR_STATISTICS
}
