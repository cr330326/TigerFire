package com.cryallen.tigerfire.domain.repository

import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.GameProgress
import com.cryallen.tigerfire.domain.model.ParentSettings
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.model.SceneStatus
import kotlinx.coroutines.flow.Flow

/**
 * 进度仓储接口
 *
 * 定义游戏进度和家长设置的数据访问抽象
 * 这是 Domain 层与 Data 层的边界，由 Data 层实现具体逻辑
 */
interface ProgressRepository {

    /**
     * 获取游戏进度流
     *
     * @return Flow<GameProgress> 游戏进度数据流，自动响应数据变化
     */
    fun getGameProgress(): Flow<GameProgress>

    /**
     * 获取所有徽章流
     *
     * @return Flow<List<Badge>> 徽章列表数据流，自动响应数据变化
     */
    fun getAllBadges(): Flow<List<Badge>>

    /**
     * 添加徽章
     *
     * @param badge 要添加的徽章
     */
    suspend fun addBadge(badge: Badge)

    /**
     * 更新游戏进度
     *
     * @param progress 新的游戏进度状态
     */
    suspend fun updateGameProgress(progress: GameProgress)

    /**
     * 原子性地保存游戏进度和徽章
     *
     * 使用数据库事务确保两个操作要么全部成功，要么全部失败，
     * 避免出现进度已保存但徽章未保存的不一致状态
     *
     * @param progress 新的游戏进度状态
     * @param badge 要添加的徽章
     */
    suspend fun saveProgressWithBadge(progress: GameProgress, badge: Badge)

    /**
     * 只更新累计游玩时长
     *
     * 避免覆盖其他字段（如fireStationCompletedItems）
     *
     * @param additionalTime 要增加的时长（毫秒）
     */
    suspend fun addTotalPlayTime(additionalTime: Long)

    /**
     * 只更新单个场景的状态
     *
     * 避免覆盖其他字段
     *
     * @param scene 场景类型
     * @param status 新的状态
     */
    suspend fun updateSingleSceneStatus(scene: SceneType, status: SceneStatus)

    /**
     * 重置进度到初始状态
     *
     * 清空所有徽章、重置场景状态、清空统计数据
     */
    suspend fun resetProgress()

    /**
     * 获取家长设置流
     *
     * @return Flow<ParentSettings> 家长设置数据流，自动响应设置变化
     */
    fun getParentSettings(): Flow<ParentSettings>

    /**
     * 更新家长设置
     *
     * @param settings 新的家长设置
     */
    suspend fun updateParentSettings(settings: ParentSettings)

    /**
     * 获取每日使用统计流
     *
     * @return Flow<Map<String, Long>> 日期 -> 时长（毫秒）
     */
    fun getDailyUsageStats(): Flow<Map<String, Long>>

    /**
     * 记录使用时长
     *
     * @param date 日期字符串（格式 "yyyy-MM-dd"）
     * @param durationMillis 使用时长（毫秒）
     */
    suspend fun recordUsage(date: String, durationMillis: Long)

    /**
     * 获取今日使用时长
     *
     * @param date 日期字符串（格式 "yyyy-MM-dd"）
     * @return Flow<Long> 今日使用时长（毫秒）
     */
    fun getUsageForDate(date: String): Flow<Long>

    /**
     * 清除使用统计数据
     */
    suspend fun clearUsageStats()
}
