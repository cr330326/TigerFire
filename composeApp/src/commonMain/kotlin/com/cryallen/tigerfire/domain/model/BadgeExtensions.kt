package com.cryallen.tigerfire.domain.model

/**
 * 徽章扩展函数
 *
 * 提供徽章相关的计算和操作方法
 */

/**
 * 计算下一个徽章的变体编号
 *
 * 根据已有的徽章列表，计算指定基础类型的下一个变体编号。
 * 变体编号按顺序循环：0 -> 1 -> 2 -> 3 -> 0 -> ...
 *
 * @param baseType 基础类型（如 "extinguisher"、"hydrant" 等）
 * @return 下一个变体编号（0 ~ MAX_VARIANTS_PER_TYPE - 1）
 */
fun List<Badge>.calculateNextVariant(baseType: String): Int {
    val existingCount = this.count { it.baseType == baseType }
    return existingCount % Badge.MAX_VARIANTS_PER_TYPE
}

/**
 * 获取指定基础类型的所有徽章（包含所有变体）
 *
 * @param baseType 基础类型
 * @return 该基础类型的所有徽章列表
 */
fun List<Badge>.getBadgesByBaseType(baseType: String): List<Badge> {
    return this.filter { it.baseType == baseType }
}

/**
 * 获取指定场景的所有徽章
 *
 * @param scene 场景类型
 * @return 该场景的所有徽章列表
 */
fun List<Badge>.getBadgesByScene(scene: SceneType): List<Badge> {
    return this.filter { it.scene == scene }
}

/**
 * 检查是否已获得指定基础类型的徽章
 *
 * @param baseType 基础类型
 * @return 是否已获得至少一枚该类型的徽章
 */
fun List<Badge>.hasBadgeType(baseType: String): Boolean {
    return this.any { it.baseType == baseType }
}

/**
 * 获取指定基础类型的最新徽章（按获得时间倒序）
 *
 * @param baseType 基础类型
 * @return 最新获得的徽章，若无则返回 null
 */
fun List<Badge>.getLatestBadgeByBaseType(baseType: String): Badge? {
    return this
        .filter { it.baseType == baseType }
        .maxByOrNull { it.earnedAt }
}

/**
 * 获取指定基础类型的最高变体编号
 *
 * @param baseType 基础类型
 * @return 最高变体编号，若无徽章则返回 -1
 */
fun List<Badge>.getMaxVariant(baseType: String): Int {
    val badges = this.getBadgesByBaseType(baseType)
    if (badges.isEmpty()) return -1
    return badges.maxOfOrNull { it.variant } ?: -1
}

/**
 * 统计指定场景的徽章数量（包含变体）
 *
 * @param scene 场景类型
 * @return 该场景的徽章总数
 */
fun List<Badge>.countByScene(scene: SceneType): Int {
    return this.count { it.scene == scene }
}

/**
 * 按场景分组徽章
 *
 * @return Map<SceneType, List<Badge>> 按场景分组的徽章映射
 */
fun List<Badge>.groupByScene(): Map<SceneType, List<Badge>> {
    return this.groupBy { it.scene }
}

/**
 * 按基础类型分组徽章
 *
 * @return Map<String, List<Badge>> 按基础类型分组的徽章映射
 */
fun List<Badge>.groupByBaseType(): Map<String, List<Badge>> {
    return this.groupBy { it.baseType }
}

/**
 * 检查是否集齐所有基础徽章类型
 *
 * 基础徽章类型：
 * - 消防站：extinguisher, hydrant, ladder, hose（4 种）
 * - 学校：school（1 种）
 * - 森林：forest_sheep_sheep0, forest_sheep_sheep1（2 种）
 *
 * @return 是否集齐全部 7 种基础徽章
 */
fun List<Badge>.hasAllUniqueBadges(): Boolean {
    val requiredBaseTypes = setOf(
        "extinguisher", "hydrant", "ladder", "hose",
        "school",
        "forest_sheep_sheep0", "forest_sheep_sheep1"
    )
    val collectedTypes = this.map { it.baseType }.toSet()
    return requiredBaseTypes.all { it in collectedTypes }
}

/**
 * 获取收集进度百分比
 *
 * @return 收集进度（0.0 ~ 1.0）
 */
fun List<Badge>.getCollectionProgress(): Float {
    val requiredBaseTypes = setOf(
        "extinguisher", "hydrant", "ladder", "hose",
        "school",
        "forest_sheep_sheep0", "forest_sheep_sheep1"
    )
    val collectedTypes = this.map { it.baseType }.toSet()
    val collectedCount = requiredBaseTypes.count { it in collectedTypes }
    return collectedCount.toFloat() / requiredBaseTypes.size.toFloat()
}
