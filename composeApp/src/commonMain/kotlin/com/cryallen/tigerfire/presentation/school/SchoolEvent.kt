package com.cryallen.tigerfire.presentation.school

/**
 * 学校场景事件
 *
 * 表示用户或系统在学校场景页面的操作动作
 *
 * 交互流程：
 * 1. ScreenEntered → 启动警报效果
 * 2. PlayButtonClicked → 停止警报，开始播放视频
 * 3. VideoPlaybackCompleted → 显示徽章动画
 * 4. BadgeAnimationCompleted → 准备导航
 * 5. BackToMapClicked → 返回主地图
 */
sealed class SchoolEvent {
    /**
     * 页面进入/初始化
     *
     * 触发警报效果（音效 + 红光闪烁）和语音提示
     */
    data object ScreenEntered : SchoolEvent()

    /**
     * 用户点击播放按钮
     *
     * 停止警报效果，开始播放视频
     */
    data object PlayButtonClicked : SchoolEvent()

    /**
     * 视频播放完成
     *
     * 颁发徽章，解锁森林场景，显示完成动画
     */
    data object VideoPlaybackCompleted : SchoolEvent()

    /**
     * 语音播放完成
     *
     * 赞美语音播放完毕，准备导航
     */
    data object VoicePlaybackCompleted : SchoolEvent()

    /**
     * 徽章收集动画完成
     *
     * 用户点击继续后触发
     */
    data object BadgeAnimationCompleted : SchoolEvent()

    /**
     * 返回主地图按钮点击
     *
     * 仅在视频未播放时允许返回
     */
    data object BackToMapClicked : SchoolEvent()
}
