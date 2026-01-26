package com.cryallen.tigerfire.presentation.welcome

/**
 * 欢迎页状态
 *
 * 表示启动页的 UI 状态
 *
 * 动画播放流程：
 * 1. 卡车入场动画（2-3秒）
 * 2. 小火挥手动画（3秒）+ 语音播报
 * 3. 语音播放完毕后延迟100ms自动导航
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
     * 是否应该导航到主地图
     *
     * 语音播放完毕后设置为 true，触发延迟100ms后自动导航
     */
    val shouldNavigate: Boolean = false
)
