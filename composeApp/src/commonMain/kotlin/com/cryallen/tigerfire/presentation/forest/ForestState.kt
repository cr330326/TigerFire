package com.cryallen.tigerfire.presentation.forest

/**
 * 森林场景状态
 *
 * 表示森林场景页面的 UI 状态
 */
data class ForestState(
    /**
     * 直升机位置（X坐标，0.0-1.0 为屏幕比例）
     */
    val helicopterX: Float = 0.1f,

    /**
     * 直升机位置（Y坐标，0.0-1.0 为屏幕比例）
     */
    val helicopterY: Float = 0.5f,

    /**
     * 是否正在拖拽直升机
     */
    val isDraggingHelicopter: Boolean = false,

    /**
     * 各小羊的救援状态（索引 0 和 1 分别代表两只小羊）
     */
    val rescuedSheep: Set<Int> = emptySet(),

    /**
     * 当前靠近的小羊索引（null 表示没有靠近任何小羊）
     */
    val nearbySheepIndex: Int? = null,

    /**
     * 是否显示"放下梯子"按钮
     */
    val showLowerLadderButton: Boolean = false,

    /**
     * 当前是否正在播放救援视频
     */
    val isPlayingRescueVideo: Boolean = false,

    /**
     * 当前播放救援视频的小羊索引（仅当 isPlayingRescueVideo = true 时有效）
     */
    val currentPlayingSheepIndex: Int? = null,

    /**
     * 是否显示徽章收集动画
     */
    val showBadgeAnimation: Boolean = false,

    /**
     * 当前获得徽章的小羊索引（仅当 showBadgeAnimation = true 时有效）
     */
    val earnedBadgeSheepIndex: Int? = null,

    /**
     * 是否已完成全部救援（2只小羊）
     */
    val isAllCompleted: Boolean = false
)
