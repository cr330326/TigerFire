package com.cryallen.tigerfire.presentation.firestation

import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 消防站副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class FireStationEffect {
    /**
     * 播放设备教学视频
     *
     * @property device 设备类型
     * @property videoResource 视频资源路径
     */
    data class PlayVideo(
        val device: FireStationDevice,
        val videoResource: String
    ) : FireStationEffect()

    /**
     * 播放设备点击音效
     */
    data object PlayClickSound : FireStationEffect()

    /**
     * 播放徽章收集成功音效
     */
    data object PlayBadgeSound : FireStationEffect()

    /**
     * 播放全部完成成功音效 + 小火欢呼语音
     */
    data object PlayAllCompletedSound : FireStationEffect()

    /**
     * 显示徽章收集动画
     *
     * @property device 刚获得徽章的设备类型
     */
    data class ShowBadgeAnimation(val device: FireStationDevice) : FireStationEffect()

    /**
     * 导航返回主地图
     */
    data object NavigateToMap : FireStationEffect()

    /**
     * 解锁学校场景
     */
    data object UnlockSchoolScene : FireStationEffect()
}
