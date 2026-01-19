package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class WelcomeEffect {
    /**
     * 播放小火挥手动画
     */
    data object PlayWaveAnimation : WelcomeEffect()

    /**
     * 导航到主地图
     */
    data object NavigateToMap : WelcomeEffect()
}
