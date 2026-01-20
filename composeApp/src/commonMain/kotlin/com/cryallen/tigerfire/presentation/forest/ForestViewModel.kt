package com.cryallen.tigerfire.presentation.forest

import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.common.IdleTimer
import com.cryallen.tigerfire.presentation.common.PlatformDateTime
import com.cryallen.tigerfire.presentation.common.RapidClickGuard
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 森林场景 ViewModel
 *
 * 管理森林场景页面的状态和事件处理
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 * @param progressRepository 进度仓储接口
 * @param resourcePathProvider 资源路径提供者
 */
class ForestViewModel(
    private val viewModelScope: CoroutineScope,
    private val progressRepository: ProgressRepository,
    private val resourcePathProvider: ResourcePathProvider
) {
    // ==================== 常量定义 ====================

    companion object {
        /** 小羊总数 */
        const val TOTAL_SHEEP = 2

        /** 靠近检测阈值（屏幕比例，约 100pt 对应的比例） */
        const val NEARBY_THRESHOLD = 0.15f

        /** 森林场景徽章基础类型 */
        const val FOREST_BADGE_BASE_TYPE = "forest_sheep"

        /** 小羊位置（屏幕比例，x, y） */
        private val SHEEP_POSITIONS = listOf(
            0.6f to 0.3f,  // 小羊 1
            0.7f to 0.7f   // 小羊 2
        )
    }

    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(ForestState())
    val state: StateFlow<ForestState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<ForestEffect>()
    val effect: Flow<ForestEffect> = _effect.receiveAsFlow()

    // ==================== 辅助功能 ====================

    /**
     * 快速点击防护器
     *
     * 防止儿童疯狂点击按钮
     */
    private val rapidClickGuard = RapidClickGuard()

    /**
     * 空闲计时器
     *
     * 检测无操作超时，显示小火提示
     */
    private val idleTimer = IdleTimer(viewModelScope)

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度，加载已救援的小羊
        viewModelScope.launch {
            progressRepository.getGameProgress()
                .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                .collect { progress ->
                    val rescuedCount = progress.forestRescuedSheep
                    val rescuedSheep = (0 until rescuedCount).toSet()

                    _state.value = _state.value.copy(
                        rescuedSheep = rescuedSheep,
                        isAllCompleted = rescuedCount >= TOTAL_SHEEP
                    )
                }
        }

        // 启动空闲检测（30秒无操作显示小火提示）
        idleTimer.startIdleDetection {
            onIdleTimeout()
        }
    }

    // ==================== 事件处理 ====================

    /**
     * 处理森林场景事件
     *
     * @param event 森林场景事件
     */
    fun onEvent(event: ForestEvent) {
        when (event) {
            is ForestEvent.DragStarted -> handleDragStarted()
            is ForestEvent.DragUpdated -> handleDragUpdated(event.x, event.y)
            is ForestEvent.DragEnded -> handleDragEnded()
            is ForestEvent.LowerLadderClicked -> handleLowerLadderClicked(event.sheepIndex)
            is ForestEvent.RescueVideoCompleted -> handleRescueVideoCompleted(event.sheepIndex)
            is ForestEvent.BackToMapClicked -> handleBackToMap()
            is ForestEvent.BadgeAnimationCompleted -> handleBadgeAnimationCompleted()
        }
    }

    /**
     * 处理开始拖拽直升机
     */
    private fun handleDragStarted() {
        // 报告用户活动，重置空闲计时器
        idleTimer.reportActivity()

        _state.value = _state.value.copy(
            isDraggingHelicopter = true
        )
        sendEffect(ForestEffect.PlayDragSound)
    }

    /**
     * 处理拖拽位置更新
     *
     * @param x X坐标（0.0-1.0 屏幕比例）
     * @param y Y坐标（0.0-1.0 屏幕比例）
     */
    private fun handleDragUpdated(x: Float, y: Float) {
        val currentState = _state.value

        // 限制坐标在 0.0-1.0 范围内
        val clampedX = x.coerceIn(0f, 1f)
        val clampedY = y.coerceIn(0f, 1f)

        // 检测是否靠近任何未救援的小羊
        val nearbySheepIndex = findNearbySheep(clampedX, clampedY, currentState.rescuedSheep)

        // 更新状态
        _state.value = currentState.copy(
            helicopterX = clampedX,
            helicopterY = clampedY,
            nearbySheepIndex = nearbySheepIndex,
            showLowerLadderButton = nearbySheepIndex != null && !currentState.isPlayingRescueVideo
        )
    }

    /**
     * 处理结束拖拽直升机
     */
    private fun handleDragEnded() {
        val currentState = _state.value

        // 如果靠近小羊，吸附到小羊位置
        val nearbySheepIndex = currentState.nearbySheepIndex
        if (nearbySheepIndex != null) {
            val (sheepX, sheepY) = SHEEP_POSITIONS[nearbySheepIndex]
            _state.value = currentState.copy(
                helicopterX = sheepX,
                helicopterY = sheepY,
                isDraggingHelicopter = false
            )
            sendEffect(ForestEffect.PlaySnapSound)
        } else {
            _state.value = currentState.copy(
                isDraggingHelicopter = false
            )
        }
    }

    /**
     * 处理"放下梯子"按钮点击
     *
     * @param sheepIndex 小羊索引
     */
    private fun handleLowerLadderClicked(sheepIndex: Int) {
        // 报告用户活动，重置空闲计时器
        idleTimer.reportActivity()

        // 检测快速点击
        if (rapidClickGuard.checkClick()) {
            // 触发防护：播放语音提示
            sendEffect(ForestEffect.PlaySlowDownVoice)
            rapidClickGuard.reset()
            return
        }

        val currentState = _state.value

        // 获取救援视频路径
        val videoPath = resourcePathProvider.getVideoPath("forest/rescue_sheep_${sheepIndex + 1}.mp4")

        // 更新状态为正在播放视频
        _state.value = currentState.copy(
            isPlayingRescueVideo = true,
            currentPlayingSheepIndex = sheepIndex,
            showLowerLadderButton = false
        )

        // 发送播放视频副作用
        sendEffect(ForestEffect.PlayRescueVideo(sheepIndex, videoPath))
    }

    /**
     * 处理救援视频播放完成
     *
     * @param sheepIndex 小羊索引
     */
    private fun handleRescueVideoCompleted(sheepIndex: Int) {
        val currentState = _state.value

        // 更新状态：结束播放
        _state.value = currentState.copy(
            isPlayingRescueVideo = false,
            currentPlayingSheepIndex = null
        )

        viewModelScope.launch {
            // 获取当前进度
            val progress = progressRepository.getGameProgress()
                .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                .first()

            // 检查小羊是否已救援
            if (!currentState.rescuedSheep.contains(sheepIndex)) {
                // 首次救援，更新进度
                val updatedProgress = progress.incrementForestRescuedSheep()

                // 添加森林徽章
                val sheepBadge = Badge(
                    id = "${FOREST_BADGE_BASE_TYPE}_${sheepIndex}_${PlatformDateTime.getCurrentTimeMillis()}",
                    baseType = FOREST_BADGE_BASE_TYPE,
                    scene = SceneType.FOREST,
                    variant = sheepIndex,
                    earnedAt = PlatformDateTime.getCurrentTimeMillis()
                )

                val updatedProgressWithBadge = updatedProgress.addBadge(sheepBadge)

                // 检查是否全部完成
                val isAllCompleted = updatedProgressWithBadge.forestRescuedSheep >= TOTAL_SHEEP
                var finalProgress = updatedProgressWithBadge
                if (isAllCompleted) {
                    finalProgress = finalProgress.updateSceneStatus(SceneType.FOREST, SceneStatus.COMPLETED)
                }

                progressRepository.updateGameProgress(finalProgress)

                // 更新本地状态
                val newRescuedSheep = currentState.rescuedSheep + sheepIndex

                _state.value = currentState.copy(
                    rescuedSheep = newRescuedSheep,
                    isAllCompleted = isAllCompleted,
                    showBadgeAnimation = true,
                    earnedBadgeSheepIndex = sheepIndex
                )

                // 发送徽章动画和音效副作用
                sendEffect(ForestEffect.ShowBadgeAnimation(sheepIndex))
                sendEffect(ForestEffect.PlayBadgeSound)

                // 如果全部完成，发送完成提示和成功音效
                if (isAllCompleted) {
                    sendEffect(ForestEffect.ShowCompletionHint)
                    sendEffect(ForestEffect.PlayAllCompletedSound)
                }
            } else {
                // 重复救援，不重复发放徽章
            }
        }
    }

    /**
     * 处理返回主地图按钮点击
     */
    private fun handleBackToMap() {
        // 报告用户活动
        idleTimer.reportActivity()

        // 停止空闲检测
        idleTimer.stopIdleDetection()

        sendEffect(ForestEffect.NavigateToMap)
    }

    /**
     * 处理空闲超时
     *
     * 无操作 30 秒后触发，显示小火提示
     */
    private fun onIdleTimeout() {
        sendEffect(ForestEffect.ShowIdleHint)
    }

    /**
     * 处理徽章收集动画完成
     */
    private fun handleBadgeAnimationCompleted() {
        _state.value = _state.value.copy(
            showBadgeAnimation = false,
            earnedBadgeSheepIndex = null
        )
    }

    // ==================== 辅助方法 ====================

    /**
     * 查找附近的小羊
     *
     * @param x 直升机 X 坐标
     * @param y 直升机 Y 坐标
     * @param rescuedSheep 已救援的小羊集合
     * @return 附近的小羊索引，如果没有则返回 null
     */
    private fun findNearbySheep(x: Float, y: Float, rescuedSheep: Set<Int>): Int? {
        SHEEP_POSITIONS.forEachIndexed { index, (sheepX, sheepY) ->
            // 跳过已救援的小羊
            if (index in rescuedSheep) return@forEachIndexed

            // 计算距离
            val distance = sqrt((x - sheepX) * (x - sheepX) + (y - sheepY) * (y - sheepY))

            // 如果距离小于阈值，返回该小羊索引
            if (distance < NEARBY_THRESHOLD) {
                return index
            }
        }
        return null
    }

    /**
     * 发送副作用到 Effect 通道
     *
     * @param effect 要发送的副作用
     */
    private fun sendEffect(effect: ForestEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
