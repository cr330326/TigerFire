package com.cryallen.tigerfire.presentation.map

import androidx.compose.ui.geometry.Offset
import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 主地图事件
 *
 * 表示用户在主地图页面的操作动作
 */
sealed class MapEvent {
    /**
     * 点击场景图标
     *
     * @property scene 场景类型
     */
    data class SceneClicked(val scene: SceneType) : MapEvent()

    /**
     * 更新选中的场景（用于 Avatar 位置记忆，不触发导航）
     *
     * @property scene 场景类型
     */
    data class UpdateSelectedScene(val scene: SceneType) : MapEvent()

    /**
     * 更新场景图标位置（用于 Avatar 定位）
     *
     * @property scene 场景类型
     * @property offset 场景图标的位置
     */
    data class UpdateScenePosition(val scene: SceneType, val offset: Offset) : MapEvent()

    /**
     * 点击"我的收藏"按钮
     */
    data object CollectionClicked : MapEvent()

    /**
     * 点击家长模式入口（齿轮图标）
     */
    data object ParentModeClicked : MapEvent()

    /**
     * 家长模式验证 - 提交答案
     *
     * @property answer 用户输入的答案
     */
    data class SubmitParentAnswer(val answer: Int) : MapEvent()

    /**
     * 家长模式验证 - 取消
     */
    data object CancelParentVerification : MapEvent()
}
