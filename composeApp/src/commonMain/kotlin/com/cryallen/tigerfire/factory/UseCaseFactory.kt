package com.cryallen.tigerfire.factory

import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.domain.usecase.AwardBadgeUseCase
import com.cryallen.tigerfire.domain.usecase.CheckTimeLimitUseCase
import com.cryallen.tigerfire.domain.usecase.RecordUsageUseCase
import com.cryallen.tigerfire.domain.usecase.ResetProgressUseCase
import com.cryallen.tigerfire.domain.usecase.UnlockSceneUseCase

/**
 * UseCase 工厂
 *
 * 提供 UseCase 实例，用于封装业务逻辑
 *
 * UseCase 层职责：
 * - 封装单一业务用例
 * - 协调多个 Repository 调用
 * - 处理业务规则验证
 * - 提供清晰的业务操作接口
 *
 * @param progressRepository 进度仓储接口
 */
class UseCaseFactory(
    private val progressRepository: ProgressRepository
) {
    /**
     * 解锁场景用例
     *
     * 负责场景解锁的业务逻辑和条件验证
     */
    val unlockScene: UnlockSceneUseCase
        get() = UnlockSceneUseCase(progressRepository)

    /**
     * 颁发徽章用例
     *
     * 负责徽章获得和变体计算
     */
    val awardBadge: AwardBadgeUseCase
        get() = AwardBadgeUseCase(progressRepository)

    /**
     * 检查时间限制用例
     *
     * 负责家长时间控制的业务逻辑
     */
    val checkTimeLimit: CheckTimeLimitUseCase
        get() = CheckTimeLimitUseCase(progressRepository)

    /**
     * 重置进度用例
     *
     * 负责进度重置和数据清理
     */
    val resetProgress: ResetProgressUseCase
        get() = ResetProgressUseCase(progressRepository)

    /**
     * 记录使用时长用例
     *
     * 负责使用统计和时长记录
     */
    val recordUsage: RecordUsageUseCase
        get() = RecordUsageUseCase(progressRepository)

    /**
     * 创建自定义 UseCase 实例
     *
     * 当需要额外的配置时，可以使用此方法创建新实例
     *
     * @param creator UseCase 创建函数
     * @return T UseCase 实例
     */
    fun <T> createCustom(creator: (ProgressRepository) -> T): T {
        return creator(progressRepository)
    }
}

/**
 * UseCase 容器
 *
 * 提供对所有 UseCase 的便捷访问
 *
 * @property useCaseFactory UseCase 工厂实例
 */
class UseCases(
    useCaseFactory: UseCaseFactory
) {
    val unlockScene = useCaseFactory.unlockScene
    val awardBadge = useCaseFactory.awardBadge
    val checkTimeLimit = useCaseFactory.checkTimeLimit
    val resetProgress = useCaseFactory.resetProgress
    val recordUsage = useCaseFactory.recordUsage

    companion object {
        /**
         * 从 UseCaseFactory 创建 UseCases 容器
         *
         * @param factory UseCase 工厂实例
         * @return UseCases UseCase 容器实例
         */
        fun fromFactory(factory: UseCaseFactory): UseCases {
            return UseCases(factory)
        }
    }
}
