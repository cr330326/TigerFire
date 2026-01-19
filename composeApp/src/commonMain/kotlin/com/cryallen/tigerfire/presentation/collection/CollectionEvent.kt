package com.cryallen.tigerfire.presentation.collection

import com.cryallen.tigerfire.domain.model.Badge

/**
 * 我的收藏页面事件
 *
 * 表示用户在我的收藏页面的操作动作
 */
sealed class CollectionEvent {
    /**
     * 返回主地图按钮点击
     */
    data object BackToMapClicked : CollectionEvent()

    /**
     * 点击徽章查看详情
     *
     * @property badge 被点击的徽章
     */
    data class BadgeClicked(val badge: Badge) : CollectionEvent()

    /**
     * 关闭徽章详情
     */
    data object CloseBadgeDetail : CollectionEvent()
}
