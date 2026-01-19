package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 场景状态枚举
 *
 * 定义场景在游戏进度中的解锁与完成状态
 */
@Serializable
enum class SceneStatus {
    /** 锁定态 - 灰色半透明 + 锁图标 + 无交互 */
    LOCKED,

    /** 已解锁 - 彩色 + 光效 + 可点击 */
    UNLOCKED,

    /** 已完成 - 彩色 + 勾图标 + 可重复进入 */
    COMPLETED
}
