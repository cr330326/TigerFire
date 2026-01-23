package com.cryallen.tigerfire.presentation.school

/**
 * 学校场景副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 *
 * Effect 流程：
 * 1. StartAlarmEffects → 启动警报（音效 + 红光）
 * 2. PlayVoice → 播放语音提示
 * 3. StopAlarmEffects → 停止警报
 * 4. PlayVideo → 播放视频
 * 5. ShowBadgeAnimation → 显示徽章动画
 * 6. PlayVoice → 播放赞美语音
 * 7. NavigateToMap → 导航回主地图
 */
sealed class SchoolEffect {
    /**
     * 启动警报效果
     *
     * 包括：
     * - 播放警报音效（循环）
     * - 屏幕边缘红光闪烁（柔和脉冲）
     */
    data object StartAlarmEffects : SchoolEffect()

    /**
     * 停止警报效果
     *
     * 包括：
     * - 停止警报音效
     * - 停止红光闪烁
     */
    data object StopAlarmEffects : SchoolEffect()

    /**
     * 播放语音
     *
     * @property voicePath 语音资源路径
     */
    data class PlayVoice(
        val voicePath: String
    ) : SchoolEffect()

    /**
     * 播放视频
     *
     * @property videoPath 视频资源路径
     */
    data class PlayVideo(
        val videoPath: String
    ) : SchoolEffect()

    /**
     * 显示徽章收集动画
     *
     * 显示小火点赞动画 + 徽章
     */
    data object ShowBadgeAnimation : SchoolEffect()

    /**
     * 播放徽章收集成功音效
     */
    data object PlayBadgeSound : SchoolEffect()

    /**
     * 播放完成音效
     */
    data object PlayCompletedSound : SchoolEffect()

    /**
     * 解锁森林场景
     */
    data object UnlockForestScene : SchoolEffect()

    /**
     * 导航返回主地图
     */
    data object NavigateToMap : SchoolEffect()

    /**
     * 播放慢下来语音提示
     *
     * 当检测到快速点击时触发
     */
    data object PlaySlowDownVoice : SchoolEffect()

    /**
     * 显示空闲提示
     *
     * 无操作 30 秒后显示小火提示："需要帮忙吗？"
     */
    data object ShowIdleHint : SchoolEffect()
}
