package com.cryallen.tigerfire.presentation.collection

import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 我的收藏页面 ViewModel
 *
 * 管理我的收藏页面的状态和事件处理
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 * @param progressRepository 进度仓储接口
 */
class CollectionViewModel(
    private val viewModelScope: CoroutineScope,
    private val progressRepository: ProgressRepository
) {
    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(CollectionState())
    val state: StateFlow<CollectionState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<CollectionEffect>(capacity = Channel.UNLIMITED)
    val effect: Flow<CollectionEffect> = _effect.receiveAsFlow()

    // ==================== 初始化 ====================

    init {
        // ✅ 修复：同时订阅 GameProgress 和 Badge，验证数据一致性
        viewModelScope.launch {
            combine(
                progressRepository.getGameProgress(),
                progressRepository.getAllBadges()
            ) { progress, badges ->
                // 验证徽章与实际进度的一致性
                val validatedBadges = validateBadgesWithProgress(badges, progress)

                // 按场景分组徽章
                val badgesByScene = validatedBadges.groupBy { it.scene }

                // 计算统计信息（基于验证后的徽章）
                val totalBadgeCount = validatedBadges.size
                val uniqueBadgeCount = validatedBadges.distinctBy { it.baseType }.size
                val hasCollectedAllBadges = uniqueBadgeCount >= com.cryallen.tigerfire.domain.model.GameProgress.TOTAL_UNIQUE_BADGES

                _state.value = CollectionState(
                    badges = validatedBadges,
                    badgesByScene = badgesByScene,
                    totalBadgeCount = totalBadgeCount,
                    uniqueBadgeCount = uniqueBadgeCount,
                    hasCollectedAllBadges = hasCollectedAllBadges
                )
            }.collect { /* 持续收集 */ }
        }
    }

    /**
     * 验证徽章与实际进度的一致性
     *
     * 确保只有真正完成的项目对应的徽章才会被显示
     *
     * @param badges 数据库中的所有徽章
     * @param progress 当前游戏进度
     * @return 验证后的徽章列表
     */
    private fun validateBadgesWithProgress(
        badges: List<com.cryallen.tigerfire.domain.model.Badge>,
        progress: com.cryallen.tigerfire.domain.model.GameProgress
    ): List<com.cryallen.tigerfire.domain.model.Badge> {
        val validatedBadges = mutableListOf<com.cryallen.tigerfire.domain.model.Badge>()

        for (badge in badges) {
            val isValid = when (badge.scene) {
                com.cryallen.tigerfire.domain.model.SceneType.FIRE_STATION -> {
                    // 检查设备是否在 fireStationCompletedItems 中
                    badge.baseType in progress.fireStationCompletedItems
                }
                com.cryallen.tigerfire.domain.model.SceneType.SCHOOL -> {
                    // 学校场景完成检查
                    progress.getSceneStatus(com.cryallen.tigerfire.domain.model.SceneType.SCHOOL) == com.cryallen.tigerfire.domain.model.SceneStatus.COMPLETED
                }
                com.cryallen.tigerfire.domain.model.SceneType.FOREST -> {
                    // 森林场景：检查小羊是否被救援
                    // baseType 格式: "forest_sheep1" 或 "forest_sheep2"
                    val sheepIndex = when (badge.baseType) {
                        "forest_sheep1" -> 1
                        "forest_sheep2" -> 2
                        else -> 0
                    }
                    sheepIndex > 0 && sheepIndex <= progress.forestRescuedSheep
                }
            }

            if (isValid) {
                validatedBadges.add(badge)
            }
        }

        return validatedBadges
    }

    // ==================== 事件处理 ====================

    /**
     * 处理我的收藏页面事件
     *
     * @param event 我的收藏页面事件
     */
    fun onEvent(event: CollectionEvent) {
        when (event) {
            is CollectionEvent.BackToMapClicked -> handleBackToMap()
            is CollectionEvent.BadgeClicked -> handleBadgeClicked(event.badge)
            is CollectionEvent.CloseBadgeDetail -> handleCloseBadgeDetail()
        }
    }

    /**
     * 处理返回主地图按钮点击
     */
    private fun handleBackToMap() {
        sendEffect(CollectionEffect.PlayClickSound)
        sendEffect(CollectionEffect.NavigateToMap)
    }

    /**
     * 处理徽章点击
     *
     * @param badge 被点击的徽章
     */
    private fun handleBadgeClicked(badge: com.cryallen.tigerfire.domain.model.Badge) {
        sendEffect(CollectionEffect.PlayBadgeSound)
        sendEffect(CollectionEffect.ShowBadgeDetail(badge))
    }

    /**
     * 处理关闭徽章详情
     */
    private fun handleCloseBadgeDetail() {
        // UI 层自行处理详情关闭，无需副作用
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送副作用到 Effect 通道
     *
     * @param effect 要发送的副作用
     */
    private fun sendEffect(effect: CollectionEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * 获取指定场景的徽章列表
     *
     * @param scene 场景类型
     * @return 该场景的徽章列表
     */
    fun getBadgesForScene(scene: SceneType): List<com.cryallen.tigerfire.domain.model.Badge> {
        return _state.value.badgesByScene[scene] ?: emptyList()
    }

    /**
     * 检查指定场景是否有徽章
     *
     * @param scene 场景类型
     * @return 是否有徽章
     */
    fun hasBadgesForScene(scene: SceneType): Boolean {
        return getBadgesForScene(scene).isNotEmpty()
    }
}
