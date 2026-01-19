package com.cryallen.tigerfire.presentation.forest

/**
 * 森林场景事件
 *
 * 表示用户在森林场景页面的操作动作
 */
sealed class ForestEvent {
    /**
     * 开始拖拽直升机
     */
    data object DragStarted : ForestEvent()

    /**
     * 拖拽直升机位置更新
     *
     * @property x X坐标（0.0-1.0 屏幕比例）
     * @property y Y坐标（0.0-1.0 屏幕比例）
     */
    data class DragUpdated(val x: Float, val y: Float) : ForestEvent()

    /**
     * 结束拖拽直升机
     */
    data object DragEnded : ForestEvent()

    /**
     * 点击"放下梯子"按钮
     *
     * @property sheepIndex 小羊索引（0 或 1）
     */
    data class LowerLadderClicked(val sheepIndex: Int) : ForestEvent()

    /**
     * 救援视频播放完成
     *
     * @property sheepIndex 小羊索引（0 或 1）
     */
    data class RescueVideoCompleted(val sheepIndex: Int) : ForestEvent()

    /**
     * 返回主地图按钮点击
     */
    data object BackToMapClicked : ForestEvent()

    /**
     * 徽章收集动画完成
     */
    data object BadgeAnimationCompleted : ForestEvent()
}
