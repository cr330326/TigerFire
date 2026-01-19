package com.cryallen.tigerfire.presentation.school

/**
 * 学校场景事件
 *
 * 表示用户或系统在学校场景页面的操作动作
 */
sealed class SchoolEvent {
    /**
     * 页面进入/初始化
     */
    data object ScreenEntered : SchoolEvent()

    /**
     * 动画播放完成
     */
    data object AnimationPlaybackCompleted : SchoolEvent()

    /**
     * 返回主地图按钮点击
     */
    data object BackToMapClicked : SchoolEvent()

    /**
     * 徽章收集动画完成
     */
    data object BadgeAnimationCompleted : SchoolEvent()
}
