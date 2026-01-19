package com.cryallen.tigerfire.presentation.firestation

/**
 * 消防站事件
 *
 * 表示用户在消防站场景页面的操作动作
 */
sealed class FireStationEvent {
    /**
     * 点击设备图标
     *
     * @property device 设备类型
     */
    data class DeviceClicked(val device: FireStationDevice) : FireStationEvent()

    /**
     * 视频播放完成
     *
     * @property device 设备类型
     */
    data class VideoPlaybackCompleted(val device: FireStationDevice) : FireStationEvent()

    /**
     * 返回主地图按钮点击
     */
    data object BackToMapClicked : FireStationEvent()

    /**
     * 徽章收集动画完成
     */
    data object BadgeAnimationCompleted : FireStationEvent()
}
