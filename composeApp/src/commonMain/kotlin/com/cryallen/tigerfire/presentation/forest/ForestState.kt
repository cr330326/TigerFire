package com.cryallen.tigerfire.presentation.forest

/**
 * 森林场景状态（点击交互版本）
 *
 * 表示森林场景页面的 UI 状态
 * 采用点击小羊 → 直升机自动飞行 → 显示播放按钮 → 观看视频的交互方式
 */
data class ForestState(
    /**
     * 直升机位置（X坐标，0.0-1.0 为屏幕比例）
     * 初始位置在屏幕左侧（20%）
     */
    val helicopterX: Float = 0.2f,

    /**
     * 直升机位置（Y坐标，0.0-1.0 为屏幕比例）
     * 初始位置垂直居中（50%）
     */
    val helicopterY: Float = 0.5f,

    /**
     * 直升机目标位置（X坐标，用于飞行动画）
     * null 表示没有目标
     */
    val targetHelicopterX: Float? = null,

    /**
     * 直升机目标位置（Y坐标，用于飞行动画）
     * null 表示没有目标
     */
    val targetHelicopterY: Float? = null,

    /**
     * 是否正在飞行到目标小羊
     */
    val isHelicopterFlying: Boolean = false,

    /**
     * 当前目标小羊索引（0 或 1），null 表示没有目标
     */
    val targetSheepIndex: Int? = null,

    /**
     * 已救援的小羊索引集合
     */
    val rescuedSheep: Set<Int> = emptySet(),

    /**
     * 是否显示"播放视频"按钮（直升机到达目标后显示）
     */
    val showPlayVideoButton: Boolean = false,

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
    val isAllCompleted: Boolean = false,

    /**
     * 是否显示空闲提示（无操作30秒后显示）
     */
    val showIdleHint: Boolean = false,

    /**
     * 当前飞行的唯一标识（用于防止竞态条件）
     * 每次开始新的飞行时递增，用于确保 handleHelicopterFlightCompleted 只处理最新的飞行
     */
    val currentFlightId: Int? = null
)
