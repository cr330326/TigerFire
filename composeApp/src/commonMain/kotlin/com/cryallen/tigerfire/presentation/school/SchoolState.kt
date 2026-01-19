package com.cryallen.tigerfire.presentation.school

/**
 * 学校场景状态
 *
 * 表示学校场景页面的 UI 状态
 */
data class SchoolState(
    /**
     * 当前是否正在播放动画
     */
    val isPlayingAnimation: Boolean = false,

    /**
     * 是否显示徽章收集动画
     */
    val showBadgeAnimation: Boolean = false,

    /**
     * 是否已完成观看（首次播放完成后为 true）
     */
    val isCompleted: Boolean = false
)
