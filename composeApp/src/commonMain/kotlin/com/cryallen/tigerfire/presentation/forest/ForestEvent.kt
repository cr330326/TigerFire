package com.cryallen.tigerfire.presentation.forest

/**
 * 森林场景事件（点击交互版本）
 *
 * 表示用户在森林场景页面的操作动作
 * 采用点击小羊 → 直升机自动飞行 → 显示播放按钮 → 观看视频的交互方式
 */
sealed class ForestEvent {
    /**
     * 页面进入事件
     * 启动空闲计时器和播放开始语音
     */
    data object ScreenEntered : ForestEvent()

    /**
     * 点击小羊（触发直升机自动飞行）
     *
     * @property sheepIndex 小羊索引（0 或 1）
     */
    data class SheepClicked(val sheepIndex: Int) : ForestEvent()

    /**
     * 直升机飞行完成（到达目标小羊上方）
     */
    data object HelicopterFlightCompleted : ForestEvent()

    /**
     * 点击"播放视频"按钮
     *
     * @property sheepIndex 小羊索引（0 或 1）
     */
    data class PlayVideoClicked(val sheepIndex: Int) : ForestEvent()

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
