package com.cryallen.tigerfire.presentation.parent

/**
 * 家长模式事件
 *
 *表示用户在家长模式页面的操作动作
 */
sealed class ParentEvent {
    /**
     * 返回主地图按钮点击
     */
    data object BackToMapClicked : ParentEvent()

    /**
     * 更新每次使用时长限制
     *
     * @property minutes 时长（分钟）
     */
    data class UpdateSessionTimeLimit(val minutes: Int) : ParentEvent()

    /**
     * 更新每日总时长限制
     *
     * @property minutes 时长（分钟）
     */
    data class UpdateDailyTimeLimit(val minutes: Int) : ParentEvent()

    /**
     * 更新提前提醒时间
     *
     * @property minutes 提前提醒时长（分钟），0表示关闭提醒
     */
    data class UpdateReminderTime(val minutes: Int) : ParentEvent()

    /**
     * 点击重置游戏进度按钮
     */
    data object ResetProgressClicked : ParentEvent()

    /**
     * 确认重置游戏进度
     */
    data object ConfirmResetProgress : ParentEvent()

    /**
     * 取消重置游戏进度
     */
    data object CancelResetProgress : ParentEvent()

    /**
     * 提交重新验证答案
     *
     * @property answer 用户输入的答案
     */
    data class SubmitReverificationAnswer(val answer: Int) : ParentEvent()

    /**
     * 取消重新验证
     */
    data object CancelReverification : ParentEvent()

    /**
     * 显示时间设置对话框
     */
    data object ShowTimeSettingsDialog : ParentEvent()

    /**
     * 关闭时间设置对话框
     */
    data object DismissTimeSettingsDialog : ParentEvent()

    /**
     * 切换每次使用时长限制
     *
     * @property enabled 是否启用
     */
    data class ToggleSessionTimeLimit(val enabled: Boolean) : ParentEvent()

    /**
     * 切换每日总时长限制
     *
     * @property enabled 是否启用
     */
    data class ToggleDailyTimeLimit(val enabled: Boolean) : ParentEvent()

    /**
     * 保存时间设置
     */
    data object SaveTimeSettings : ParentEvent()
}
