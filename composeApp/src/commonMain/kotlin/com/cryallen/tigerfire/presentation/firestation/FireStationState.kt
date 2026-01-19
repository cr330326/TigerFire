package com.cryallen.tigerfire.presentation.firestation

import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 消防站设备类型
 *
 * @property deviceId 设备唯一标识
 * @property displayName 设备显示名称
 */
enum class FireStationDevice(val deviceId: String, val displayName: String) {
    FIRE_HYDRANT("fire_hydrant", "消防栓"),
    LADDER_TRUCK("ladder_truck", "云梯"),
    FIRE_EXTINGUISHER("fire_extinguisher", "灭火器"),
    WATER_HOSE("water_hose", "水枪");

    companion object {
        /**
         * 获取所有设备列表
         */
        val ALL_DEVICES = entries.toSet()
    }
}

/**
 * 消防站状态
 *
 * 表示消防站场景页面的 UI 状态
 */
data class FireStationState(
    /**
     * 各设备的学习完成状态
     */
    val completedDevices: Set<FireStationDevice> = emptySet(),

    /**
     * 当前是否正在播放视频
     */
    val isPlayingVideo: Boolean = false,

    /**
     * 当前播放的设备（仅当 isPlayingVideo = true 时有效）
     */
    val currentPlayingDevice: FireStationDevice? = null,

    /**
     * 是否显示徽章收集动画
     */
    val showBadgeAnimation: Boolean = false,

    /**
     * 当前获得的徽章设备（仅当 showBadgeAnimation = true 时有效）
     */
    val earnedBadgeDevice: FireStationDevice? = null,

    /**
     * 是否已完成全部4个设备学习
     */
    val isAllCompleted: Boolean = false
)
