package com.cryallen.tigerfire.domain.usecase

import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.GameProgress
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.domain.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 颁发徽章用例
 *
 * 负责处理徽章获得的业务逻辑，包括变体计算和重复获得规则
 *
 * 业务规则：
 * - 每个基础类型初次获得时为变体 0（默认）
 * - 重复通关同一项目时，按顺序获得变体 1, 2, 3...
 * - 变体总数由 MAX_VARIANTS_PER_TYPE 决定（默认为4）
 * - 变体编号循环使用：0 → 1 → 2 → 3 → 0 → 1 ...
 *
 * @param repository 进度仓储接口
 */
class AwardBadgeUseCase(
    private val repository: ProgressRepository
) {
    /**
     * 颁发徽章
     *
     * 自动计算变体编号并保存徽章
     *
     * @param scene 所属场景
     * @param baseType 基础类型（如 "extinguisher"、"hydrant"）
     * @return Result<Badge> 操作结果，包含新颁发的徽章或错误信息
     */
    suspend operator fun invoke(
        scene: SceneType,
        baseType: String
    ): Result<Badge> {
        return try {
            // 获取当前进度
            val currentProgress = repository.getGameProgress().first()

            // 计算变体编号
            val variant = calculateNextVariant(currentProgress.badges, baseType)

            // 创建徽章
            val badge = Badge(
                id = Badge.generateId(baseType, variant),
                scene = scene,
                baseType = baseType,
                variant = variant,
                earnedAt = TimeUtils.getCurrentTimeMillis()
            )

            // 保存徽章
            repository.addBadge(badge)

            Result.success(badge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 批量颁发徽章
     *
     * 用于一次获得多个徽章的场景（如集齐奖励）
     *
     * @param badges 要颁发的徽章列表
     * @return Result<List<Badge>> 操作结果
     */
    suspend fun awardMultiple(badges: List<Badge>): Result<List<Badge>> {
        return try {
            badges.forEach { badge ->
                repository.addBadge(badge)
            }
            Result.success(badges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 计算下一个变体编号
     *
     * 统计已有同 baseType 徽章数量，对 MAX_VARIANTS_PER_TYPE 取模
     *
     * @param badges 当前已获得的所有徽章
     * @param baseType 基础类型
     * @return Int 下一个变体编号（0 到 MAX_VARIANTS_PER_TYPE-1）
     */
    fun calculateNextVariant(badges: List<Badge>, baseType: String): Int {
        val existingCount = badges.count { it.baseType == baseType }
        return existingCount % Badge.MAX_VARIANTS_PER_TYPE
    }

    /**
     * 获取指定场景的徽章数量
     *
     * @param scene 场景类型
     * @return Flow<Int> 该场景的徽章数量
     */
    fun getBadgeCountForScene(scene: SceneType): Flow<Int> {
        return repository.getAllBadges()
            .map { badges -> badges.count { it.belongsToScene(scene) } }
    }

    /**
     * 检查是否已集齐所有基础徽章
     *
     * @return Flow<Boolean> 是否集齐7个基础徽章
     */
    fun hasCollectedAllBadges(): Flow<Boolean> {
        return repository.getGameProgress()
            .map { progress -> progress.hasCollectedAllBadges() }
    }

    /**
     * 获取徽章收集进度
     *
     * @return Flow<BadgeProgress> 收集进度信息
     */
    fun getCollectionProgress(): Flow<BadgeProgress> {
        return repository.getAllBadges()
            .map { badges ->
                val uniqueBadges = badges.distinctBy { it.baseType }
                BadgeProgress(
                    totalBadges = badges.size,
                    uniqueBadges = uniqueBadges.size,
                    fireStationBadges = uniqueBadges.count { it.scene == SceneType.FIRE_STATION },
                    schoolBadges = uniqueBadges.count { it.scene == SceneType.SCHOOL },
                    forestBadges = uniqueBadges.count { it.scene == SceneType.FOREST },
                    hasCollectedAll = uniqueBadges.size >= GameProgress.TOTAL_UNIQUE_BADGES
                )
            }
    }
}

/**
 * 徽章收集进度数据类
 *
 * @property totalBadges 总徽章数（包含变体）
 * @property uniqueBadges 不同类型的徽章数（不含变体）
 * @property fireStationBadges 消防站徽章数量
 * @property schoolBadges 学校徽章数量
 * @property forestBadges 森林徽章数量
 * @property hasCollectedAll 是否集齐所有基础徽章
 */
data class BadgeProgress(
    val totalBadges: Int,
    val uniqueBadges: Int,
    val fireStationBadges: Int,
    val schoolBadges: Int,
    val forestBadges: Int,
    val hasCollectedAll: Boolean
) {
    /**
     * 收集完成度百分比
     */
    val completionPercentage: Float
        get() = (uniqueBadges.toFloat() / GameProgress.TOTAL_UNIQUE_BADGES * 100)
            .coerceAtMost(100f)

    /**
     * 获取显示文本
     */
    fun getDisplayText(): String {
        return "已收集 $uniqueBadges / ${GameProgress.TOTAL_UNIQUE_BADGES} 枚徽章"
    }
}
