package com.cryallen.tigerfire.presentation.collection

import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 我的收藏页面状态
 *
 * 表示我的收藏页面的 UI 状态
 */
data class CollectionState(
    /**
     * 所有已获得的徽章
     */
    val badges: List<Badge> = emptyList(),

    /**
     * 按场景分组的徽章
     */
    val badgesByScene: Map<SceneType, List<Badge>> = emptyMap(),

    /**
     * 徽章总数（包含变体）
     */
    val totalBadgeCount: Int = 0,

    /**
     * 不同基础类型的徽章数量（不包含变体）
     */
    val uniqueBadgeCount: Int = 0,

    /**
     * 是否集齐所有基础徽章（7枚）
     */
    val hasCollectedAllBadges: Boolean = false
)
