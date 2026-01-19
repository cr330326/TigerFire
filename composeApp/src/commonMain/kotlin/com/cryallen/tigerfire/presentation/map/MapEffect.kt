package com.cryallen.tigerfire.presentation.map

import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 主地图副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class MapEffect {
    /**
     * 导航到指定场景
     *
     * @property scene 目标场景类型
     */
    data class NavigateToScene(val scene: SceneType) : MapEffect()

    /**
     * 导航到"我的收藏"页面
     */
    data object NavigateToCollection : MapEffect()

    /**
     * 导航到家长模式
     */
    data object NavigateToParent : MapEffect()

    /**
     * 播放场景点击音效
     *
     * @property scene 场景类型（用于差异化音效）
     */
    data class PlaySceneSound(val scene: SceneType) : MapEffect()

    /**
     * 播放场景锁定提示音效/语音
     */
    data object PlayLockedHint : MapEffect()

    /**
     * 显示成功音效（徽章收集等）
     */
    data object PlaySuccessSound : MapEffect()
}
