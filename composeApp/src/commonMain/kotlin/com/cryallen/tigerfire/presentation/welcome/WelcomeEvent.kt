package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页事件
 *
 * 表示系统触发的动作（欢迎页无需用户交互，完全自动化）
 */
sealed class WelcomeEvent {
    /**
     * 卡车入场动画完成
     *
     * 触发后自动播放小火挥手动画和语音播报
     */
    data object TruckAnimationCompleted : WelcomeEvent()

    /**
     * 小火挥手动画完成
     */
    data object WaveAnimationCompleted : WelcomeEvent()

    /**
     * 语音播放完成
     *
     * 触发后延迟100ms自动导航到主地图
     */
    data object VoicePlaybackCompleted : WelcomeEvent()
}
