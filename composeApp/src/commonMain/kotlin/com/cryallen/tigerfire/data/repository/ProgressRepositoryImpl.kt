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
            .onStart {
                emit(GameProgress.initial())
            }
    }

    override suspend fun updateGameProgress(progress: GameProgress) {
        database.gameProgressQueries.updateSceneStatuses(
            json.encodeToString(progress.sceneStatuses)
        )
        database.gameProgressQueries.updateFireStationCompletedItems(
            json.encodeToString(progress.fireStationCompletedItems.toList())
        )
        database.gameProgressQueries.updateForestRescuedSheep(
            progress.forestRescuedSheep.toLong()
        )
        database.gameProgressQueries.updateTotalPlayTime(
            progress.totalPlayTime
        )
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
    fun getAllBadges(): Flow<List<Badge>> {
        return database.badgeQueries.selectAllBadges()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { badgeEntities ->
                badgeEntities.map { it.toDomainModel() }
            }
    }

    /**
     * 添加徽章到数据库
     */
    suspend fun addBadge(badge: Badge) {
        database.badgeQueries.insertBadge(
            id = badge.id,
            scene = badge.scene.name,
            baseType = badge.baseType,
            variant = badge.variant.toLong(),
            earnedAt = badge.earnedAt
        )
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
