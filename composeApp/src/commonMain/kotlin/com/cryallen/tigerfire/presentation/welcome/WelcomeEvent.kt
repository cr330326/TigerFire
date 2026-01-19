package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页事件
 *
 * 表示用户或系统触发的动作
 */
sealed class WelcomeEvent {
    /**
     * Lottie 卡车入场动画完成
     */
    data object TruckAnimationCompleted : WelcomeEvent()

    /**
     * 小火挥手动画完成
     */
    data object WaveAnimationCompleted : WelcomeEvent()

    /**
     * 用户点击屏幕
     */
    data object ScreenClicked : WelcomeEvent()
}
