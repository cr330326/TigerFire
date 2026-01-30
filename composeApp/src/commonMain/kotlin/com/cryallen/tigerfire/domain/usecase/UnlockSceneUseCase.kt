package com.cryallen.tigerfire.domain.usecase

import com.cryallen.tigerfire.domain.model.GameProgress
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 解锁场景用例
 *
 * 负责处理场景解锁的业务逻辑，确保解锁条件符合游戏规则
 *
 * 业务规则：
 * - FIRE_STATION: 始终解锁（初始场景）
 * - SCHOOL: 消防站全部完成（4个设备学习完毕）后解锁
 * - FOREST: 学校场景完成后解锁
 *
 * @param repository 进度仓储接口
 */
class UnlockSceneUseCase(
    private val repository: ProgressRepository
) {
    /**
     * 执行解锁场景操作
     *
     * @param scene 要解锁的场景类型
     * @return Result<GameProgress> 操作结果，包含更新后的进度或错误信息
     */
    suspend operator fun invoke(scene: SceneType): Result<GameProgress> {
        return try {
            // 获取当前进度
            val currentProgress = repository.getGameProgress().first()

            // 检查是否已解锁
            if (currentProgress.isSceneUnlocked(scene)) {
                return Result.success(currentProgress)
            }

            // 验证解锁条件
            val canUnlock = checkUnlockCondition(currentProgress, scene)
            if (!canUnlock) {
                return Result.failure(
                    IllegalStateException("Cannot unlock scene: $scene, prerequisites not met")
                )
            }

            // ✅ 修复：只更新场景状态字段，避免覆盖fireStationCompletedItems等其他字段
            repository.updateSingleSceneStatus(scene, SceneStatus.UNLOCKED)

            // 重新获取更新后的进度
            val updatedProgress = repository.getGameProgress().first()
            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查场景是否可以解锁
     *
     * @param progress 当前游戏进度
     * @param scene 要检查的场景类型
     * @return Boolean 是否满足解锁条件
     */
    private fun checkUnlockCondition(progress: GameProgress, scene: SceneType): Boolean {
        return when (scene) {
            SceneType.FIRE_STATION -> {
                // 消防站始终解锁（初始场景）
                true
            }
            SceneType.SCHOOL -> {
                // 学校需要消防站全部完成
                progress.isFireStationCompleted()
            }
            SceneType.FOREST -> {
                // 森林需要学校完成
                progress.isSceneCompleted(SceneType.SCHOOL)
            }
        }
    }

    /**
     * 检查场景是否已解锁
     *
     * @param scene 要检查的场景类型
     * @return Flow<Boolean> 实时解锁状态流
     */
    fun isSceneUnlocked(scene: SceneType): Flow<Boolean> {
        return repository.getGameProgress()
            .map { progress -> progress.isSceneUnlocked(scene) }
    }

    /**
     * 获取场景可解锁的提示信息
     *
     * @param scene 要检查的场景类型
     * @return String 解锁条件提示文本
     */
    fun getUnlockHint(scene: SceneType): String {
        return when (scene) {
            SceneType.FIRE_STATION -> "消防站随时可以进入学习！"
            SceneType.SCHOOL -> "完成消防站所有设备学习后解锁"
            SceneType.FOREST -> "完成学校场景后解锁"
        }
    }
}

