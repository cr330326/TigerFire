package com.cryallen.tigerfire.domain.model

/**
 * 游戏进度扩展函数
 *
 * 提供场景解锁条件的验证逻辑
 */

/**
 * 检查场景是否已解锁（基于完成条件判定）
 *
 * 此扩展函数提供基于游戏完成条件的解锁状态判定，
 * 与 GameProgress.isSceneUnlocked() 配合使用
 *
 * @return 场景是否已解锁
 */
fun GameProgress.isSceneUnlockedByCompletion(scene: SceneType): Boolean {
    return when (scene) {
        // 消防站始终解锁（第一个场景）
        SceneType.FIRE_STATION -> true

        // 学校解锁条件：消防站 4 个设备全部完成
        SceneType.SCHOOL -> {
            fireStationCompletedItems.size >= GameProgress.FIRE_STATION_TOTAL_ITEMS
        }

        // 森林解锁条件：学校场景已完成
        SceneType.FOREST -> {
            getSceneStatus(SceneType.SCHOOL) == SceneStatus.COMPLETED
        }
    }
}

/**
 * 获取场景的解锁进度描述
 *
 * @return 解锁进度描述文本
 */
fun GameProgress.getUnlockProgressDescription(scene: SceneType): String {
    return when (scene) {
        SceneType.FIRE_STATION -> "已解锁"
        SceneType.SCHOOL -> {
            val completed = fireStationCompletedItems.size
            val total = GameProgress.FIRE_STATION_TOTAL_ITEMS
            "完成消防站 $completed/$total 个设备后解锁"
        }
        SceneType.FOREST -> {
            if (getSceneStatus(SceneType.SCHOOL) == SceneStatus.COMPLETED) {
                "已解锁"
            } else {
                "完成学校动画后解锁"
            }
        }
    }
}

/**
 * 检查是否可以自动解锁下一个场景
 *
 * 当当前场景完成后，检查是否满足解锁下一个场景的条件
 *
 * @return 可解锁的下一个场景，若无则返回 null
 */
fun GameProgress.getNextUnlockableScene(): SceneType? {
    // 检查学校是否可解锁
    if (getSceneStatus(SceneType.FIRE_STATION) != SceneStatus.COMPLETED &&
        fireStationCompletedItems.size >= GameProgress.FIRE_STATION_TOTAL_ITEMS) {
        return SceneType.SCHOOL
    }

    // 检查森林是否可解锁
    if (getSceneStatus(SceneType.SCHOOL) == SceneStatus.COMPLETED &&
        getSceneStatus(SceneType.FOREST) == SceneStatus.LOCKED) {
        return SceneType.FOREST
    }

    return null
}

/**
 * 获取下一个推荐进入的场景
 *
 * 根据当前进度，推荐用户应该进入的场景
 *
 * @return 推荐场景
 */
fun GameProgress.getRecommendedScene(): SceneType {
    // 优先返回未完成的已解锁场景
    val unlockedUncompleted = SceneType.entries
        .filter { isSceneUnlockedByCompletion(it) }
        .filter { getSceneStatus(it) != SceneStatus.COMPLETED }

    if (unlockedUncompleted.isNotEmpty()) {
        return unlockedUncompleted.first()
    }

    // 所有场景都已完成，返回消防站（可重复游玩）
    return SceneType.FIRE_STATION
}

/**
 * 计算总体完成进度
 *
 * @return 完成进度（0.0 ~ 1.0）
 */
fun GameProgress.getOverallProgress(): Float {
    var completedScenes = 0

    if (getSceneStatus(SceneType.FIRE_STATION) == SceneStatus.COMPLETED) completedScenes++
    if (getSceneStatus(SceneType.SCHOOL) == SceneStatus.COMPLETED) completedScenes++
    if (getSceneStatus(SceneType.FOREST) == SceneStatus.COMPLETED) completedScenes++

    return completedScenes.toFloat() / SceneType.entries.size.toFloat()
}

/**
 * 检查是否有场景可解锁
 *
 * @return 是否有场景满足解锁条件但尚未解锁
 */
fun GameProgress.hasUnlockableScene(): Boolean {
    return getNextUnlockableScene() != null
}

/**
 * 获取消防站的完成进度
 *
 * @return Pair(已完成数量, 总数)
 */
fun GameProgress.getFireStationProgress(): Pair<Int, Int> {
    return Pair(
        fireStationCompletedItems.size,
        GameProgress.FIRE_STATION_TOTAL_ITEMS
    )
}

/**
 * 获取森林的救援进度
 *
 * @return Pair(已救援数量, 总数)
 */
fun GameProgress.getForestProgress(): Pair<Int, Int> {
    return Pair(
        forestRescuedSheep,
        GameProgress.FOREST_TOTAL_SHEEP
    )
}

// ==================== 徽章变体系统扩展 ====================

/**
 * 获取指定基础类型的最大变体数量
 *
 * 根据徽章类型返回其支持的最大变体数量：
 * - 消防站设备（fire_hydrant, ladder_truck, fire_extinguisher, water_hose）：4 种变体（红/黄/蓝/绿）
 * - 学校（school）：3 种变体（不同边框颜色）
 * - 森林小羊（forest_sheep1, forest_sheep2）：2 种变体（不同小羊表情）
 * - 其他类型：1 种（无变体）
 *
 * @param baseType 基础类型
 * @return 最大变体数量
 */
fun getMaxVariantsForBaseType(baseType: String): Int {
    return when (baseType) {
        "fire_hydrant", "ladder_truck", "fire_extinguisher", "water_hose" -> 4
        "school" -> 3
        "forest_sheep1", "forest_sheep2" -> 2
        else -> 1
    }
}

/**
 * 计算指定基础类型的下一个变体编号
 *
 * @param baseType 基础类型
 * @return 下一个变体编号（0 到 maxVariants-1）
 */
fun GameProgress.calculateNextVariant(baseType: String): Int {
    val maxVariants = getMaxVariantsForBaseType(baseType)
    val existingCount = badges.count { it.baseType == baseType }
    return existingCount % maxVariants
}

/**
 * 检查是否应颁发变体徽章（即是否已获得过该基础类型的徽章）
 *
 * @param baseType 基础类型
 * @return true 如果已获得过该类型的徽章（本次应颁发变体）
 */
fun GameProgress.shouldAwardVariantBadge(baseType: String): Boolean {
    return badges.any { it.baseType == baseType }
}

/**
 * 获取指定基础类型的已获得变体数量
 *
 * @param baseType 基础类型
 * @return 该类型的徽章数量（包含所有变体）
 */
fun GameProgress.getBadgeVariantCount(baseType: String): Int {
    return badges.count { it.baseType == baseType }
}

/**
 * 获取指定基础类型的所有变体徽章
 *
 * @param baseType 基础类型
 * @return 该类型的所有徽章列表
 */
fun GameProgress.getBadgesByBaseType(baseType: String): List<Badge> {
    return badges.filter { it.baseType == baseType }
}

/**
 * 检查指定基础类型是否已收集所有变体
 *
 * @param baseType 基础类型
 * @return true 如果已收集该类型的所有变体
 */
fun GameProgress.hasAllVariantsForBaseType(baseType: String): Boolean {
    val maxVariants = getMaxVariantsForBaseType(baseType)
    val collectedCount = badges.count { it.baseType == baseType }
    return collectedCount >= maxVariants
}

/**
 * 获取指定场景的徽章收集数量（包含变体）
 *
 * @param scene 场景类型
 * @return 该场景的徽章总数
 */
fun GameProgress.getBadgeCountForScene(scene: SceneType): Int {
    return badges.count { it.scene == scene }
}

/**
 * 获取指定场景的不同类型徽章数量（不包含变体）
 *
 * @param scene 场景类型
 * @return 该场景的不同类型徽章数量
 */
fun GameProgress.getUniqueBadgeCountForScene(scene: SceneType): Int {
    return badges.filter { it.scene == scene }
        .distinctBy { it.baseType }
        .size
}
