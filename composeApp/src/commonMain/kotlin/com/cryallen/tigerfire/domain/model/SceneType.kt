package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 场景类型枚举
 *
 * 定义应用中的三个主要学习场景
 */
@Serializable
enum class SceneType {
    /** 消防站场景 - 设备教学互动 */
    FIRE_STATION,

    /** 学校场景 - 剧情动画播放 */
    SCHOOL,

    /** 森林场景 - 手势救援互动 */
    FOREST
}
