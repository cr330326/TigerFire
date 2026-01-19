package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页状态
 *
 * 表示启动页的 UI 状态
 */
data class WelcomeState(
    /**
     * Lottie 动画是否已完成
     */
    val isAnimationCompleted: Boolean = false,

    /**
     * 是否显示小火挥手动画
     */
    val showWaveAnimation: Boolean = false
)
