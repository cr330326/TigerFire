package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 徽章模型
 *
 * 表示用户在游戏中获得的徽章，支持变体系统
 *
 * @property id 唯一标识符（如 "firestation_extinguisher_red"）
 * @property scene 所属场景类型
 * @property baseType 基础类型（如 "extinguisher"、"hydrant"、"ladder"、"hose"）
 * @property variant 变体编号（0=默认, 1=红色, 2=黄色, 3=蓝色...）
 * @property earnedAt 获得时间戳（毫秒）
 */
@Serializable
data class Badge(
    val id: String,
    val scene: SceneType,
    val baseType: String,
    val variant: Int = 0,
    val earnedAt: Long
) {
    /**
     * 检查是否为指定场景的徽章
     */
    fun belongsToScene(sceneType: SceneType): Boolean = scene == sceneType

    /**
     * 获取徽章的显示名称（用于 UI 展示）
     * 格式："{baseType}_{variant}"
     */
    fun getDisplayName(): String = "${baseType}_v$variant"

    companion object {
        /**
         * 最大变体数量
         */
        const val MAX_VARIANTS_PER_TYPE = 4

        /**
         * 生成徽章 ID
         */
        fun generateId(baseType: String, variant: Int): String {
            return "${baseType}_v$variant"
        }
    }
}
