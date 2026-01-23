package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 游戏进度模型
 *
 * 表示用户的整体游戏进度状态，是整个应用的单一数据源（Single Source of Truth）
 *
 * @property sceneStatuses 各场景的解锁/完成状态
 * @property badges 用户已获得的所有徽章
 * @property totalPlayTime 累计游玩时长（毫秒）
 * @property fireStationCompletedItems 消防站已完成学习的设备集合
 * @property forestRescuedSheep 森林场景已救援的小羊数量
 */
@Serializable
data class GameProgress(
    val sceneStatuses: Map<SceneType, SceneStatus> = defaultSceneStatuses(),
    val badges: List<Badge> = emptyList(),
    val totalPlayTime: Long = 0L,
    val fireStationCompletedItems: Set<String> = emptySet(),
    val forestRescuedSheep: Int = 0
) {
    companion object {
        /** 消防站设备总数 */
        const val FIRE_STATION_TOTAL_ITEMS = 4

        /** 森林小羊总数 */
        const val FOREST_TOTAL_SHEEP = 2

        /** 基础徽章总数（消防站4 + 学校1 + 森林2） */
        const val TOTAL_UNIQUE_BADGES = 7

        /**
         * 默认场景状态
         *
         * 测试模式：所有场景默认解锁，方便测试所有功能
         * 生产模式：消防站解锁，学校和森林锁定
         */
        fun defaultSceneStatuses(): Map<SceneType, SceneStatus> {
            // TODO: 生产环境应该使用渐进式解锁逻辑
            // 当前为测试模式，所有场景都解锁
            return mapOf(
                SceneType.FIRE_STATION to SceneStatus.UNLOCKED,
                SceneType.SCHOOL to SceneStatus.UNLOCKED,
                SceneType.FOREST to SceneStatus.UNLOCKED
            )
        }

        /**
         * 创建初始进度
         */
        fun initial(): GameProgress {
            return GameProgress(
                sceneStatuses = defaultSceneStatuses(),
                badges = emptyList(),
                totalPlayTime = 0L,
                fireStationCompletedItems = emptySet(),
                forestRescuedSheep = 0
            )
        }
    }

    /**
     * 获取指定场景的状态
     */
    fun getSceneStatus(scene: SceneType): SceneStatus {
        return sceneStatuses[scene] ?: SceneStatus.LOCKED
    }

    /**
     * 检查场景是否已解锁
     */
    fun isSceneUnlocked(scene: SceneType): Boolean {
        val status = getSceneStatus(scene)
        return status == SceneStatus.UNLOCKED || status == SceneStatus.COMPLETED
    }

    /**
     * 检查场景是否已完成
     */
    fun isSceneCompleted(scene: SceneType): Boolean {
        return getSceneStatus(scene) == SceneStatus.COMPLETED
    }

    /**
     * 消防站是否全部完成
     */
    fun isFireStationCompleted(): Boolean {
        return fireStationCompletedItems.size == FIRE_STATION_TOTAL_ITEMS
    }

    /**
     * 森林场景是否全部完成
     */
    fun isForestCompleted(): Boolean {
        return forestRescuedSheep >= FOREST_TOTAL_SHEEP
    }

    /**
     * 获取已收集的徽章总数（包含变体）
     */
    fun getTotalBadgeCount(): Int = badges.size

    /**
     * 获取不同基础类型的徽章数量（不包含变体）
     */
    fun getUniqueBadgeCount(): Int = badges.distinctBy { it.baseType }.size

    /**
     * 是否集齐所有基础徽章
     */
    fun hasCollectedAllBadges(): Boolean {
        return getUniqueBadgeCount() >= TOTAL_UNIQUE_BADGES
    }

    /**
     * 添加游玩时长
     */
    fun addPlayTime(milliseconds: Long): GameProgress {
        return copy(totalPlayTime = totalPlayTime + milliseconds)
    }

    /**
     * 添加徽章
     */
    fun addBadge(badge: Badge): GameProgress {
        return copy(badges = badges + badge)
    }

    /**
     * 更新场景状态
     */
    fun updateSceneStatus(scene: SceneType, status: SceneStatus): GameProgress {
        return copy(sceneStatuses = sceneStatuses + (scene to status))
    }

    /**
     * 添加消防站已完成设备
     */
    fun addFireStationCompletedItem(deviceId: String): GameProgress {
        return copy(fireStationCompletedItems = fireStationCompletedItems + deviceId)
    }

    /**
     * 增加森林救援小羊数量
     */
    fun incrementForestRescuedSheep(): GameProgress {
        return copy(forestRescuedSheep = forestRescuedSheep + 1)
    }
}
