package com.cryallen.tigerfire.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.GameProgress
import com.cryallen.tigerfire.domain.model.ParentSettings
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.database.TigerFireDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 进度仓储实现
 *
 * 使用 SQLDelight 实现数据持久化，提供响应式数据流
 *
 * @param database SQLDelight 数据库实例
 */
class ProgressRepositoryImpl(
    private val database: TigerFireDatabase
) : ProgressRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // ==================== 游戏进度相关 ====================

    override fun getGameProgress(): Flow<GameProgress> {
        return database.gameProgressQueries.selectAllGameProgress()
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { gameProgressEntity ->
                gameProgressEntity.toDomainModel()
            }
    }

    /**
     * 获取当前游戏进度（同步，不通过 Flow）
     * 用于需要立即获取最新数据的场景
     */
    fun getGameProgressNow(): GameProgress {
        return database.gameProgressQueries.selectAllGameProgress()
            .executeAsOne()
            .toDomainModel()
    }

    override suspend fun updateGameProgress(progress: GameProgress) {
        // ✅ 修复：直接更新所有字段，不再做复杂的合并逻辑
        // 调用者（ViewModel）负责确保传入完整的进度对象

        // 🔍 调试日志：打印即将保存的数据
        val completedItemsJson = json.encodeToString(progress.fireStationCompletedItems.toList())
        println("DEBUG updateGameProgress: fireStationCompletedItems = $completedItemsJson")
        println("DEBUG updateGameProgress: forestRescuedSheep = ${progress.forestRescuedSheep}")

        database.gameProgressQueries.updateSceneStatuses(
            json.encodeToString(progress.sceneStatuses)
        )
        database.gameProgressQueries.updateFireStationCompletedItems(
            completedItemsJson
        )
        database.gameProgressQueries.updateForestRescuedSheep(
            progress.forestRescuedSheep.toLong()
        )
        database.gameProgressQueries.updateTotalPlayTime(
            progress.totalPlayTime
        )

        // 同步保存徽章：获取现有徽章，只添加新的
        val existingBadges = getAllBadges().firstOrNull() ?: emptyList()
        val existingBadgeIds = existingBadges.map { it.id }.toSet()
        progress.badges.filterNot { it.id in existingBadgeIds }
            .forEach { badge ->
                addBadge(badge)
            }
    }

    /**
     * 只增加累计游玩时长
     *
     * 直接在SQL层面做增量更新，避免读-改-写的竞态条件
     */
    override suspend fun addTotalPlayTime(additionalTime: Long) {
        // 先获取当前时长
        val current = database.gameProgressQueries.selectAllGameProgress()
            .executeAsOne()
            .totalPlayTime

        // 增量更新
        database.gameProgressQueries.updateTotalPlayTime(
            current + additionalTime
        )

        println("DEBUG addTotalPlayTime: added $additionalTime ms, new total = ${current + additionalTime}")
    }

    /**
     * 只更新单个场景的状态
     *
     * 只修改sceneStatuses字段，不影响其他字段
     */
    override suspend fun updateSingleSceneStatus(scene: SceneType, status: SceneStatus) {
        // 获取当前场景状态
        val currentStatuses = parseSceneStatuses(
            database.gameProgressQueries.selectAllGameProgress()
                .executeAsOne()
                .sceneStatuses
        ).toMutableMap()

        // 更新指定场景
        currentStatuses[scene] = status

        // 保存回数据库
        database.gameProgressQueries.updateSceneStatuses(
            json.encodeToString(currentStatuses)
        )

        println("DEBUG updateSingleSceneStatus: scene=$scene, status=$status")
    }

    override suspend fun resetProgress() {
        database.gameProgressQueries.resetProgress()
        database.badgeQueries.deleteAllBadges()
        database.parentSettingsQueries.resetParentSettings()
    }

    // ==================== 家长设置相关 ====================

    override fun getParentSettings(): Flow<ParentSettings> {
        return database.parentSettingsQueries.selectAllParentSettings()
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toDomainModel() }
            .onStart { emit(ParentSettings.default()) }
    }

    override suspend fun updateParentSettings(settings: ParentSettings) {
        database.parentSettingsQueries.updateAllSettings(
            sessionDurationMinutes = settings.sessionDurationMinutes.toLong(),
            reminderMinutesBefore = settings.reminderMinutesBefore.toLong(),
            dailyUsageStats = json.encodeToString(settings.dailyUsageStats)
        )
    }

    // ==================== 使用统计相关 ====================

    override fun getDailyUsageStats(): Flow<Map<String, Long>> {
        return getParentSettings().map { it.dailyUsageStats }
    }

    override suspend fun recordUsage(date: String, durationMillis: Long) {
        val currentSettings = getParentSettings().first()
        val updatedSettings = currentSettings.recordUsage(date, durationMillis)
        updateParentSettings(updatedSettings)
    }

    override fun getUsageForDate(date: String): Flow<Long> {
        return getDailyUsageStats().map { stats -> stats[date] ?: 0L }
    }

    override suspend fun clearUsageStats() {
        val currentSettings = getParentSettings().first()
        val updatedSettings = currentSettings.clearUsageStats()
        updateParentSettings(updatedSettings)
    }

    // ==================== 辅助方法 ====================

    /**
     * 从数据库获取当前徽章列表
     */
    override fun getAllBadges(): Flow<List<Badge>> {
        return kotlinx.coroutines.flow.flow {
            // 立即发出当前数据
            val currentBadges = database.badgeQueries.selectAllBadges().executeAsList().map { it.toDomainModel() }
            emit(currentBadges)

            // 然后监听数据库变化
            database.badgeQueries.selectAllBadges()
                .asFlow()
                .mapToList(Dispatchers.Default)
                .collect { badgeEntities ->
                    val badges = badgeEntities.map { it.toDomainModel() }
                    emit(badges)
                }
        }
    }

    /**
     * 添加徽章到数据库
     */
    override suspend fun addBadge(badge: Badge) {
        database.badgeQueries.insertBadge(
            id = badge.id,
            scene = badge.scene.name,
            baseType = badge.baseType,
            variant = badge.variant.toLong(),
            earnedAt = badge.earnedAt
        )
    }

    /**
     * 原子性地保存游戏进度和徽章
     *
     * 使用数据库事务确保数据一致性
     */
    override suspend fun saveProgressWithBadge(progress: GameProgress, badge: Badge) {
        database.transaction {
            // 🔍 调试日志：打印即将保存的数据
            val completedItemsJson = json.encodeToString(progress.fireStationCompletedItems.toList())
            println("DEBUG saveProgressWithBadge: START TRANSACTION")
            println("DEBUG saveProgressWithBadge: badge.id = ${badge.id}")
            println("DEBUG saveProgressWithBadge: badge.baseType = ${badge.baseType}")
            println("DEBUG saveProgressWithBadge: fireStationCompletedItems = $completedItemsJson")
            println("DEBUG saveProgressWithBadge: forestRescuedSheep = ${progress.forestRescuedSheep}")

            // 更新游戏进度
            database.gameProgressQueries.updateSceneStatuses(
                json.encodeToString(progress.sceneStatuses)
            )
            database.gameProgressQueries.updateFireStationCompletedItems(
                completedItemsJson
            )
            database.gameProgressQueries.updateForestRescuedSheep(
                progress.forestRescuedSheep.toLong()
            )
            database.gameProgressQueries.updateTotalPlayTime(
                progress.totalPlayTime
            )

            // 添加徽章
            database.badgeQueries.insertBadge(
                id = badge.id,
                scene = badge.scene.name,
                baseType = badge.baseType,
                variant = badge.variant.toLong(),
                earnedAt = badge.earnedAt
            )

            println("DEBUG saveProgressWithBadge: COMMIT TRANSACTION")
        }
    }

    /**
     * 获取游戏进度和徽章的组合数据
     */
    fun getGameProgressWithBadges(): Flow<Pair<GameProgress, List<Badge>>> {
        return kotlinx.coroutines.flow.combine(
            getGameProgress(),
            getAllBadges()
        ) { progress, badges ->
            progress.copy(badges = badges) to badges
        }
    }
}

// ==================== 扩展函数：实体转换 ====================

/**
 * 将数据库实体转换为领域模型
 */
private fun com.cryallen.tigerfire.database.GameProgress.toDomainModel(): GameProgress {
    return GameProgress(
        sceneStatuses = parseSceneStatuses(sceneStatuses),
        badges = emptyList(), // 徽章由单独的表查询
        totalPlayTime = totalPlayTime,
        fireStationCompletedItems = parseCompletedItems(fireStationCompletedItems),
        forestRescuedSheep = forestRescuedSheep.toInt()
    )
}

/**
 * 将数据库实体转换为领域模型
 */
private fun com.cryallen.tigerfire.database.Badge.toDomainModel(): Badge {
    return Badge(
        id = id,
        scene = SceneType.valueOf(scene),
        baseType = baseType,
        variant = variant.toInt(),
        earnedAt = earnedAt
    )
}

/**
 * 解析场景状态 JSON 字符串
 */
private fun parseSceneStatuses(jsonString: String): Map<SceneType, SceneStatus> {
    val json = Json { ignoreUnknownKeys = true }
    return try {
        val map: Map<String, String> = json.decodeFromString(jsonString)
        map.mapKeys { (key, _) -> SceneType.valueOf(key) }
            .mapValues { (_, value) -> SceneStatus.valueOf(value) }
    } catch (e: Exception) {
        // 解析失败时返回默认状态
        mapOf(
            SceneType.FIRE_STATION to SceneStatus.UNLOCKED,
            SceneType.SCHOOL to SceneStatus.LOCKED,
            SceneType.FOREST to SceneStatus.LOCKED
        )
    }
}

/**
 * 解析已完成设备 JSON 数组字符串
 */
private fun parseCompletedItems(jsonString: String): Set<String> {
    val json = Json { ignoreUnknownKeys = true }
    return try {
        json.decodeFromString<List<String>>(jsonString).toSet()
    } catch (e: Exception) {
        emptySet()
    }
}

/**
 * 将数据库实体转换为领域模型
 */
private fun com.cryallen.tigerfire.database.ParentSettings.toDomainModel(): ParentSettings {
    return ParentSettings(
        sessionDurationMinutes = sessionDurationMinutes.toInt(),
        reminderMinutesBefore = reminderMinutesBefore.toInt(),
        dailyUsageStats = parseDailyUsageStats(dailyUsageStats)
    )
}

/**
 * 解析每日使用统计 JSON 字符串
 */
private fun parseDailyUsageStats(jsonString: String): Map<String, Long> {
    return try {
        Json { ignoreUnknownKeys = true }.decodeFromString<Map<String, Long>>(jsonString)
    } catch (e: Exception) {
        emptyMap()
    }
}
