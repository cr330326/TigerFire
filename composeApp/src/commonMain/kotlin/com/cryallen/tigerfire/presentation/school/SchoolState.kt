package com.cryallen.tigerfire.presentation.school

/**
 * 学校场景状态
 *
 * 表示学校场景页面的 UI 状态
 *
 * 交互流程：
 * 1. 进入场景 → showAlarmEffect=true, showPlayButton=true
 * 2. 用户点击播放 → showAlarmEffect=false, isVideoPlaying=true
 * 3. 视频完成 → showBadgeAnimation=true
 * 4. 动画完成 → 导航回主地图
 */
data class SchoolState(
    /**
     * 是否显示警报效果（红光闪烁 + 警报音效）
     */
    val showAlarmEffect: Boolean = true,

    /**
     * 是否显示播放按钮
     */
    val showPlayButton: Boolean = true,

    /**
     * 视频是否正在播放
     */
    val isVideoPlaying: Boolean = false,

    /**
     * 视频是否暂停
     */
    val isVideoPaused: Boolean = false,

    /**
     * 是否显示视频控制按钮
     */
    val showVideoControls: Boolean = true,

    /**
     * 是否显示徽章收集动画
     */
    val showBadgeAnimation: Boolean = false,

    /**
     * 是否已完成观看（首次播放完成后为 true）
     */
    val isCompleted: Boolean = false,

    /**
     * 警报音效是否正在播放
     */
    val isAlarmPlaying: Boolean = false,

    /**
     * 当前视频路径（从 ResourcePathProvider 获取）
     */
    val currentVideoPath: String = "",

    /**
     * 是否显示空闲提示（无操作30秒后显示）
     */
    val showIdleHint: Boolean = false
)
