package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页状态
 *
 * 表示启动页的 UI 状态
 */
data class WelcomeState(
    /**
     * 卡车入场动画是否已完成
     */
    val isTruckAnimationCompleted: Boolean = false,

    /**
     * 是否显示小火挥手动画
     */
    val showWaveAnimation: Boolean = false,

    /**
     * 语音是否正在播放
     */
    val isVoicePlaying: Boolean = false,

    /**
     * 是否启用点击响应（语音播放完成后才启用）
     */
    val isClickEnabled: Boolean = false
)
