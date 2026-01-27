package com.cryallen.tigerfire.presentation.collection

import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _effect = Channel<CollectionEffect>()
    val effect: Flow<CollectionEffect> = _effect.receiveAsFlow()

    // ==================== 初始化 ====================

    init {
        // ✅ 修复：订阅独立的徽章数据流（而不是GameProgress中的空数组）
        viewModelScope.launch {
            progressRepository.getAllBadges()
                .collect { badges ->
                    // 按场景分组徽章
                    val badgesByScene = badges.groupBy { it.scene }

                    // 计算统计信息
                    val totalBadgeCount = badges.size
                    val uniqueBadgeCount = badges.distinctBy { it.baseType }.size
                    val hasCollectedAllBadges = uniqueBadgeCount >= com.cryallen.tigerfire.domain.model.GameProgress.TOTAL_UNIQUE_BADGES

                    _state.value = CollectionState(
                        badges = badges,
                        badgesByScene = badgesByScene,
                        totalBadgeCount = totalBadgeCount,
                        uniqueBadgeCount = uniqueBadgeCount,
                        hasCollectedAllBadges = hasCollectedAllBadges
                    )
                }
        }
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
