package com.cryallen.tigerfire.presentation.forest

import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.model.calculateNextVariant
import com.cryallen.tigerfire.domain.model.getMaxVariantsForBaseType
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 森林场景 ViewModel（点击交互版本）
 *
 * 管理森林场景页面的状态和事件处理
 * 交互方式：点击小羊 → 直升机自动飞行 → 显示播放按钮 → 观看视频
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

        /** 森林场景徽章基础类型 */
        const val FOREST_BADGE_BASE_TYPE = "forest_sheep"

        /** 小羊位置（屏幕比例，x, y） - 与 UI 组件保持一致 */
        private val SHEEP_POSITIONS = listOf(
            0.7f to 0.3f,   // 小羊 1 - 右上
            0.75f to 0.65f  // 小羊 2 - 右下
        )

        /** 直升机初始位置 */
        private const val HELICOPTER_INITIAL_X = 0.15f  // 屏幕左侧 15%
        private const val HELICOPTER_INITIAL_Y = 0.5f   // 垂直居中
    }

    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(ForestState(
        helicopterX = HELICOPTER_INITIAL_X,
        helicopterY = HELICOPTER_INITIAL_Y
    ))
    val state: StateFlow<ForestState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<ForestEffect>(capacity = Channel.UNLIMITED)
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

    /**
     * 正在保存徽章的小羊索引集合
     *
     * 用于防止同一小羊的徽章被并发保存多次
     */
    private val savingSheepIndices = mutableSetOf<Int>()

    /**
     * 互斥锁，用于保护 savingSheepIndices 集合
     */
    private val savingMutex = Mutex()

    /**
     * 当前飞行的唯一标识（用于防止竞态条件）
     * 每次开始新的飞行时递增，用于确保 handleHelicopterFlightCompleted 只处理最新的飞行
     */
    private var currentFlightId: Int = 0

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度和徽章，加载已救援的小羊
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                progressRepository.getGameProgress()
                    .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) },
                progressRepository.getAllBadges()
                    .onStart { emit(emptyList()) }
            ) { progress, badges ->
                // 从徽章列表中反推已救援的小羊索引
                // baseType 格式: "forest_sheep1" 或 "forest_sheep2"
                // 需要从 baseType 中提取小羊索引并转换为 0 或 1
                val rescuedSheep = badges
                    .filter { it.scene == SceneType.FOREST }
                    .mapNotNull { badge ->
                        // 从 baseType (如 "forest_sheep1") 中提取小羊索引
                        // 提取的数字是 1 或 2，需要转换为 0 或 1
                        val match = Regex("sheep(\\d)$").find(badge.baseType)
                        match?.groupValues?.get(1)?.toIntOrNull()?.minus(1)
                    }
                    .toSet()

                _state.value = _state.value.copy(
                    rescuedSheep = rescuedSheep,
                    isAllCompleted = rescuedSheep.size >= TOTAL_SHEEP
                )
            }.collect { /* 收集即可，不需要处理 */ }
        }

        // 启动空闲检测（30秒无操作显示小火提示）
        idleTimer.startIdleDetection {
            onIdleTimeout()
        }

        // 延迟播放开始语音："小羊被困啦！快开直升机救它们！"
        viewModelScope.launch {
            kotlinx.coroutines.delay(500) // 等待 UI 渲染完成
            sendEffect(ForestEffect.PlayStartVoice)
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
            is ForestEvent.ScreenEntered -> {
                // 页面进入事件，重新启动空闲计时器
                idleTimer.stopIdleDetection()
                idleTimer.startIdleDetection {
                    onIdleTimeout()
                }
            }
            is ForestEvent.SheepClicked -> handleSheepClicked(event.sheepIndex)
            is ForestEvent.HelicopterFlightCompleted -> handleHelicopterFlightCompleted()
            is ForestEvent.PlayVideoClicked -> handlePlayVideoClicked(event.sheepIndex)
            is ForestEvent.RescueVideoCompleted -> handleRescueVideoCompleted(event.sheepIndex)
            is ForestEvent.BackToMapClicked -> handleBackToMap()
            is ForestEvent.BadgeAnimationCompleted -> handleBadgeAnimationCompleted()
        }
    }

    /**
     * 处理点击小羊事件
     *
     * @param sheepIndex 小羊索引（0 或 1）
     * 注意：用户可以重复观看视频学习，已救援的小羊也可以再次点击
     */
    private fun handleSheepClicked(sheepIndex: Int) {
        val currentState = _state.value

        // 报告用户活动，重置空闲计时器
        idleTimer.reportActivity()

        // 检查是否可以点击：
        // 1. 直升机不在飞行中
        // 2. 没有正在播放视频
        // 注意：允许重复点击已救援的小羊进行复习
        if (currentState.isHelicopterFlying || currentState.isPlayingRescueVideo) {
            // 正在飞行或播放视频，忽略点击
            return
        }

        // ✅ 新增：防止在保存徽章期间重复点击
        if (savingMutex.isLocked && sheepIndex in savingSheepIndices) {
            // 正在保存此小羊的徽章，忽略点击
            return
        }

        // 检测快速点击
        if (rapidClickGuard.checkClick()) {
            // 触发防护：播放语音提示
            sendEffect(ForestEffect.PlaySlowDownVoice)
            rapidClickGuard.reset()
            return
        }

        // 获取目标小羊位置
        val (targetX, targetY) = SHEEP_POSITIONS[sheepIndex]

        // 增加飞行ID，用于防止竞态条件
        currentFlightId++
        val flightId = currentFlightId

        // 更新状态：开始飞行
        _state.value = currentState.copy(
            targetSheepIndex = sheepIndex,
            targetHelicopterX = targetX,
            targetHelicopterY = targetY,
            isHelicopterFlying = true,
            showPlayVideoButton = false,
            currentFlightId = flightId  // 记录当前飞行ID
        )

        // 发送飞行动画音效
        sendEffect(ForestEffect.PlayFlyingSound)
    }

    /**
     * 处理直升机飞行完成事件
     *
     * 当 UI 完成飞行动画后调用
     * 通过验证 flightId 确保只处理最新的飞行请求，防止竞态条件
     */
    private fun handleHelicopterFlightCompleted() {
        val currentState = _state.value

        // 验证飞行ID，确保只处理最新的飞行
        // 如果 currentFlightId 为 null，说明这不是一次有效的飞行
        if (currentState.targetSheepIndex == null || currentState.currentFlightId == null) {
            return
        }

        val sheepIndex = currentState.targetSheepIndex
        val flightId = currentState.currentFlightId

        // 验证这个飞行完成事件是否对应最新的飞行请求
        // 如果用户在飞行期间点击了另一只小羊，currentFlightId 会递增
        // 旧的飞行完成事件应该被忽略
        if (flightId != currentFlightId) {
            // 这是一个过期的飞行完成事件，忽略它
            return
        }

        // 更新直升机位置到目标位置
        val targetX = currentState.targetHelicopterX ?: currentState.helicopterX
        val targetY = currentState.targetHelicopterY ?: currentState.helicopterY

        _state.value = currentState.copy(
            helicopterX = targetX,
            helicopterY = targetY,
            isHelicopterFlying = false,
            showPlayVideoButton = true,
            targetSheepIndex = sheepIndex  // 保持目标小羊索引
        )

        // 播放点击音效提示用户可以点击播放按钮
        sendEffect(ForestEffect.PlayClickSound)
    }

    /**
     * 处理点击"播放视频"按钮事件
     *
     * @param sheepIndex 小羊索引（0 或 1）
     */
    private fun handlePlayVideoClicked(sheepIndex: Int) {
        val currentState = _state.value

        // ✅ 修复：报告用户活动并暂停空闲检测（视频播放期间不需要空闲提示）
        idleTimer.reportActivity()
        idleTimer.pauseIdleDetection()  // ✅ 暂停空闲计时器

        // 检测快速点击
        if (rapidClickGuard.checkClick()) {
            sendEffect(ForestEffect.PlaySlowDownVoice)
            rapidClickGuard.reset()
            return
        }

        // 获取救援视频路径
        val videoPath = resourcePathProvider.getVideoPath("rescue_sheep_${sheepIndex + 1}")

        // 更新状态为正在播放视频
        _state.value = currentState.copy(
            isPlayingRescueVideo = true,
            currentPlayingSheepIndex = sheepIndex,
            showPlayVideoButton = false
        )

        // 发送播放视频副作用
        sendEffect(ForestEffect.PlayRescueVideo(sheepIndex, videoPath))
    }

    /**
     * 处理救援视频播放完成事件
     *
     * @param sheepIndex 小羊索引（0 或 1）
     */
    private fun handleRescueVideoCompleted(sheepIndex: Int) {
        val currentState = _state.value

        // ✅ 修复：视频完成时恢复空闲检测
        idleTimer.resumeIdleDetection()

        // 更新状态：结束播放
        _state.value = currentState.copy(
            isPlayingRescueVideo = false,
            currentPlayingSheepIndex = null,
            targetSheepIndex = null,
            targetHelicopterX = null,
            targetHelicopterY = null
        )

        viewModelScope.launch {
            // ✅ 新增：检查并添加保存锁
            val shouldSave = savingMutex.withLock {
                if (sheepIndex in savingSheepIndices) {
                    // 已经在保存中，跳过
                    false
                } else {
                    // 添加到保存集合
                    savingSheepIndices.add(sheepIndex)
                    true
                }
            }

            if (!shouldSave) {
                // 正在保存中，只更新UI状态
                return@launch
            }

            try {
                // 获取当前进度
                val progress = progressRepository.getGameProgress()
                    .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                    .first()

                // 检查小羊是否已救援
                if (!currentState.rescuedSheep.contains(sheepIndex)) {
                // 首次救援，更新进度
                var updatedProgress = progress.incrementForestRescuedSheep()

                // ✅ 关键修复：从数据库查询实际徽章来计算变体（而不是使用progress.badges，因为它总是空的）
                // 使用 "forest_sheep1" 或 "forest_sheep2" 作为基础类型（与 CollectionViewModel 验证逻辑保持一致）
                val sheepBaseType = "${FOREST_BADGE_BASE_TYPE}${sheepIndex + 1}"
                val existingBadges = progressRepository.getAllBadges().firstOrNull() ?: emptyList()
                val nextVariant = existingBadges.calculateNextVariant(sheepBaseType)

                // 添加森林徽章（带变体支持）
                val sheepBadge = Badge(
                    id = "${sheepBaseType}_v${nextVariant}_${PlatformDateTime.getCurrentTimeMillis()}",
                    baseType = sheepBaseType,  // 每只小羊有独立的徽章类型
                    scene = SceneType.FOREST,
                    variant = nextVariant,
                    earnedAt = PlatformDateTime.getCurrentTimeMillis()
                )

                // 检查是否全部完成
                val isAllCompleted = updatedProgress.forestRescuedSheep >= TOTAL_SHEEP
                var finalProgress = updatedProgress
                if (isAllCompleted) {
                    finalProgress = finalProgress.updateSceneStatus(SceneType.FOREST, SceneStatus.COMPLETED)
                }

                // ✅ 使用事务原子性地保存游戏进度和徽章
                progressRepository.saveProgressWithBadge(finalProgress, sheepBadge)

                // 更新本地状态
                val newRescuedSheep = _state.value.rescuedSheep.toMutableSet().apply {
                    add(sheepIndex)
                }

                _state.value = _state.value.copy(
                    rescuedSheep = newRescuedSheep,
                    isAllCompleted = isAllCompleted,
                    showBadgeAnimation = true,  // 显示徽章动画
                    earnedBadgeSheepIndex = sheepIndex
                )

                // 发送徽章动画和音效副作用
                sendEffect(ForestEffect.ShowBadgeAnimation(sheepIndex))
                sendEffect(ForestEffect.PlayBadgeSound)

                // 如果全部完成，发送完成提示和成功音效
                if (isAllCompleted) {
                    sendEffect(ForestEffect.ShowCompletionHint)
                    sendEffect(ForestEffect.PlayAllCompletedSound)
                    // 播放完成语音："直升机能从天上救人，真厉害！"
                    sendEffect(ForestEffect.PlayCompleteVoice)
                }
            } else {
                // ✅ 关键修复：重复救援同一个小羊，也颁发变体徽章（支持2种变体）
                val sheepBaseType = "${FOREST_BADGE_BASE_TYPE}${sheepIndex + 1}"  // ✅ 修复：与验证逻辑保持一致
                val existingBadges = progressRepository.getAllBadges().firstOrNull() ?: emptyList()
                val nextVariant = existingBadges.calculateNextVariant(sheepBaseType)
                val maxVariants = com.cryallen.tigerfire.domain.model.getMaxVariantsForBaseType(sheepBaseType)

                // 检查是否还有未收集的变体
                if (nextVariant < maxVariants) {
                    val sheepBadge = Badge(
                        id = "${sheepBaseType}_v${nextVariant}_${PlatformDateTime.getCurrentTimeMillis()}",
                        baseType = sheepBaseType,
                        scene = SceneType.FOREST,
                        variant = nextVariant,
                        earnedAt = PlatformDateTime.getCurrentTimeMillis()
                    )
                    // 重复救援只添加徽章，不更新进度
                    progressRepository.addBadge(sheepBadge)

                    // 显示获得了新变体徽章
                    sendEffect(ForestEffect.ShowBadgeAnimation(sheepIndex))
                    sendEffect(ForestEffect.PlayBadgeSound)
                } else {
                    // 所有变体已收集完成，只播放普通完成音效
                    sendEffect(ForestEffect.PlayCompletedSound)
                }
            }
            } finally {
                // ✅ 新增：保存完成后移除锁
                savingMutex.withLock {
                    savingSheepIndices.remove(sheepIndex)
                }
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

        sendEffect(ForestEffect.PlayClickSound)
        sendEffect(ForestEffect.NavigateToMap)
    }

    /**
     * 处理空闲超时
     *
     * 无操作 30 秒后触发，显示小火提示
     */
    private fun onIdleTimeout() {
        // 更新状态显示空闲提示
        _state.value = _state.value.copy(showIdleHint = true)
        sendEffect(ForestEffect.ShowIdleHint)
    }

    /**
     * 隐藏空闲提示
     *
     * 用户点击提示或进行任何操作后调用
     */
    fun dismissIdleHint() {
        _state.value = _state.value.copy(showIdleHint = false)
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
