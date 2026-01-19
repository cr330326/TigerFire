package com.cryallen.tigerfire.presentation.school

/**
 * 学校场景副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class SchoolEffect {
    /**
     * 播放剧情动画
     *
     * @property videoResource 视频资源路径
     */
    data class PlayAnimation(
        val videoResource: String
    ) : SchoolEffect()

    /**
     * 播放徽章收集成功音效
     */
    data object PlayBadgeSound : SchoolEffect()

    /**
     * 播放完成音效 + 小火欢呼语音
     */
    data object PlayCompletedSound : SchoolEffect()

    /**
     * 显示徽章收集动画
     */
    data object ShowBadgeAnimation : SchoolEffect()

    /**
     * 导航返回主地图
     */
    data object NavigateToMap : SchoolEffect()

    /**
     * 解锁森林场景
     */
    data object UnlockForestScene : SchoolEffect()
}
