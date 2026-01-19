package com.cryallen.tigerfire.presentation.collection

import com.cryallen.tigerfire.domain.model.Badge

/**
 * 我的收藏页面副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class CollectionEffect {
    /**
     * 播放点击音效
     */
    data object PlayClickSound : CollectionEffect()

    /**
     * 播放徽章音效
     */
    data object PlayBadgeSound : CollectionEffect()

    /**
     * 导航返回主地图
     */
    data object NavigateToMap : CollectionEffect()

    /**
     * 显示徽章详情
     *
     * @property badge 要显示详情的徽章
     */
    data class ShowBadgeDetail(val badge: Badge) : CollectionEffect()

    /**
     * 播放集齐所有徽章庆祝动画
     */
    data object PlayCompletionAnimation : CollectionEffect()
}
