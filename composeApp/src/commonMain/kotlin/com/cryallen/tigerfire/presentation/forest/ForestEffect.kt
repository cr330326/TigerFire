package com.cryallen.tigerfire.presentation.forest

/**
 * 森林场景副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class ForestEffect {
    /**
     * 播放救援视频
     *
     * @property sheepIndex 小羊索引（0 或 1）
     * @property videoResource 视频资源路径
     */
    data class PlayRescueVideo(
        val sheepIndex: Int,
        val videoResource: String
    ) : ForestEffect()

    /**
     * 播放点击音效
     */
    data object PlayClickSound : ForestEffect()

    /**
     * 播放拖拽音效
     */
    data object PlayDragSound : ForestEffect()

    /**
     * 播放吸附成功音效
     */
    data object PlaySnapSound : ForestEffect()

    /**
     * 播放徽章收集成功音效
     */
    data object PlayBadgeSound : ForestEffect()

    /**
     * 播放全部完成成功音效 + 小火欢呼语音
     */
    data object PlayAllCompletedSound : ForestEffect()

    /**
     * 显示徽章收集动画
     *
     * @property sheepIndex 刚获得徽章的小羊索引
     */
    data class ShowBadgeAnimation(val sheepIndex: Int) : ForestEffect()

    /**
     * 导航返回主地图
     */
    data object NavigateToMap : ForestEffect()

    /**
     * 显示完成提示（全部救援完成后）
     */
    data object ShowCompletionHint : ForestEffect()

    /**
     * 播放慢下来语音提示
     *
     * 当检测到快速点击时触发
     */
    data object PlaySlowDownVoice : ForestEffect()

    /**
     * 显示空闲提示
     *
     * 无操作 30 秒后显示小火提示："需要帮忙吗？"
     */
    data object ShowIdleHint : ForestEffect()
}
