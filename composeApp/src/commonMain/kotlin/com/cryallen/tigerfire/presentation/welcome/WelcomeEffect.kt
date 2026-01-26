package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 *
 * 副作用流程：
 * 1. PlayWaveAnimation - 播放小火挥手动画
 * 2. PlayVoice - 播放欢迎语音
 * 3. NavigateToMap - 延迟100ms后自动导航到主地图
 */
sealed class WelcomeEffect {
    /**
     * 播放小火挥手动画
     */
    data object PlayWaveAnimation : WelcomeEffect()

    /**
     * 播放欢迎语音
     *
     * @param audioPath 语音文件路径（相对于 assets 目录）
     */
    data class PlayVoice(val audioPath: String) : WelcomeEffect()

    /**
     * 导航到主地图
     *
     * 发送后 UI 层延迟100ms再执行导航，确保动画和语音完全结束
     * 提供更好的用户体验过渡
     */
    data object NavigateToMap : WelcomeEffect()
}
