package com.cryallen.tigerfire.presentation.map

import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 主地图状态
 *
 * 表示主地图页面的 UI 状态
 */
data class MapState(
    /**
     * 各场景的状态（锁定/解锁/完成）
     */
    val sceneStatuses: Map<SceneType, SceneStatus> = mapOf(
        SceneType.FIRE_STATION to SceneStatus.UNLOCKED,
        SceneType.SCHOOL to SceneStatus.LOCKED,
        SceneType.FOREST to SceneStatus.LOCKED
    ),

    /**
     * 用户已获得的所有徽章
     */
    val badges: List<Badge> = emptyList(),

    /**
     * 是否显示家长模式验证界面
     */
    val showParentVerification: Boolean = false,

    /**
     * 家长模式数学题（问题文本 + 答案）
     */
    val mathQuestion: Pair<String, Int>? = null
)
