package com.cryallen.tigerfire.presentation.firestation

import com.cryallen.tigerfire.data.repository.ProgressRepositoryImpl
import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.model.calculateNextVariant
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.common.IdleTimer
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

/**
 * 消防站场景 ViewModel
 *
 * 管理消防站场景页面的状态和事件处理
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 * @param progressRepository 进度仓储接口
 * @param resourcePathProvider 资源路径提供者
 */
class FireStationViewModel(
    private val viewModelScope: CoroutineScope,
    private val progressRepository: ProgressRepository,
    private val resourcePathProvider: ResourcePathProvider
) {
    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(FireStationState())
    val state: StateFlow<FireStationState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<FireStationEffect>(capacity = Channel.UNLIMITED)
    val effect: Flow<FireStationEffect> = _effect.receiveAsFlow()

    // ==================== 辅助功能 ====================

    /**
     * 快速点击防护器
     *
     * 防止儿童疯狂点击设备按钮
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
        // ✅ 修复：订阅游戏进度流，持续监听数据库变化
        // 这样当数据库更新时，UI 状态也会自动更新
        viewModelScope.launch {
            progressRepository.getGameProgress().collect { progress ->
                val completedDevices = progress.fireStationCompletedItems
                    .mapNotNull { deviceId ->
                        FireStationDevice.entries.find { it.deviceId == deviceId }
                    }
                    .toSet()

                _state.value = _state.value.copy(
                    completedDevices = completedDevices,
                    isAllCompleted = completedDevices.size == FireStationDevice.ALL_DEVICES.size
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
     * 处理消防站场景事件
     *
     * @param event 消防站场景事件
     */
    fun onEvent(event: FireStationEvent) {
        when (event) {
            is FireStationEvent.ScreenEntered -> {
                // 页面进入事件，重新启动空闲计时器
                idleTimer.stopIdleDetection()
                idleTimer.startIdleDetection {
                    onIdleTimeout()
                }
            }
            is FireStationEvent.DeviceClicked -> handleDeviceClicked(event.device)
            is FireStationEvent.VideoPlaybackCompleted -> handleVideoCompleted(event.device)
            is FireStationEvent.BackToMapClicked -> handleBackToMap()
            is FireStationEvent.BadgeAnimationCompleted -> handleBadgeAnimationCompleted()
        }
    }

    /**
     * 处理设备图标点击
     *
     * @param device 设备类型
     */
    private fun handleDeviceClicked(device: FireStationDevice) {
        // ✅ 修复：报告用户活动并暂停空闲检测（视频播放期间不需要空闲提示）
        idleTimer.reportActivity()
        idleTimer.pauseIdleDetection()  // ✅ 暂停空闲计时器

        // 检测快速点击
        if (rapidClickGuard.checkClick()) {
            // 触发防护：播放语音提示
            sendEffect(FireStationEffect.PlaySlowDownVoice)
            rapidClickGuard.reset()
            return
        }

        val currentState = _state.value

        // 无论是否已完成，都可以重新观看视频
        sendEffect(FireStationEffect.PlayClickSound)

        // 获取视频资源路径
        val videoPath = resourcePathProvider.getVideoPath("firestation/${device.deviceId}.mp4")

        // 更新状态为正在播放
        _state.value = currentState.copy(
            isPlayingVideo = true,
            currentPlayingDevice = device
        )

        // 发送播放视频副作用
        sendEffect(FireStationEffect.PlayVideo(device, videoPath))
    }

    /**
     * 处理视频播放完成
     *
     * @param device 设备类型
     */
    private fun handleVideoCompleted(device: FireStationDevice) {
        val currentState = _state.value

        // ✅ 修复：视频完成时恢复空闲检测
        idleTimer.resumeIdleDetection()

        viewModelScope.launch {
            // 使用同步方法直接从数据库获取最新进度
            val repository = progressRepository as? ProgressRepositoryImpl
            val progress = repository?.getGameProgressNow() ?: progressRepository.getGameProgress().first()

            // 使用数据库状态作为基准，合并所有已完成的设备
            val dbCompletedDevices = progress.fireStationCompletedItems
                .mapNotNull { deviceId ->
                    FireStationDevice.entries.find { it.deviceId == deviceId }
                }
                .toSet()

            // 检查设备是否已完成
            val alreadyCompletedInDB = device.deviceId in progress.fireStationCompletedItems
            val alreadyCompletedLocal = device in currentState.completedDevices
            val alreadyCompleted = alreadyCompletedInDB || alreadyCompletedLocal

            if (alreadyCompleted) {
                // 已完成，只更新UI状态，不保存数据库
                _state.value = currentState.copy(
                    isPlayingVideo = false,
                    currentPlayingDevice = null,
                    completedDevices = dbCompletedDevices,
                    isAllCompleted = progress.isFireStationCompleted(),
                    showBadgeAnimation = false,
                    earnedBadgeDevice = null
                )
                return@launch
            }

            // 首次完成，更新进度
            var updatedProgress = progress.addFireStationCompletedItem(device.deviceId)
            val newCompletedDevices = dbCompletedDevices + device
            val isAllCompleted = updatedProgress.isFireStationCompleted()

            // 🔍 调试日志：打印即将保存的进度数据
            println("DEBUG handleVideoCompleted: device = ${device.deviceId}")
            println("DEBUG handleVideoCompleted: progress.fireStationCompletedItems = ${progress.fireStationCompletedItems}")
            println("DEBUG handleVideoCompleted: updatedProgress.fireStationCompletedItems = ${updatedProgress.fireStationCompletedItems}")
            println("DEBUG handleVideoCompleted: isAllCompleted = $isAllCompleted")

            // ✅ 关键修复：从数据库查询实际徽章来计算变体（而不是使用progress.badges，因为它总是空的）
            val existingBadges = progressRepository.getAllBadges().firstOrNull() ?: emptyList()
            val nextVariant = existingBadges.calculateNextVariant(device.deviceId)
            val deviceBadge = Badge(
                id = "${device.deviceId}_v${nextVariant}_${com.cryallen.tigerfire.presentation.common.PlatformDateTime.getCurrentTimeMillis()}",
                baseType = device.deviceId,  // "extinguisher", "hydrant", "ladder", "hose"
                scene = SceneType.FIRE_STATION,
                variant = nextVariant,
                earnedAt = com.cryallen.tigerfire.presentation.common.PlatformDateTime.getCurrentTimeMillis()
            )
            // 不需要添加到 updatedProgress.badges（因为updateGameProgress会单独处理徽章表）
            // updatedProgress = updatedProgress.addBadge(deviceBadge)

            // 检查是否全部完成，如果是则解锁学校场景
            val finalProgress = if (isAllCompleted) {
                updatedProgress.updateSceneStatus(SceneType.SCHOOL, SceneStatus.UNLOCKED)
            } else {
                updatedProgress
            }

            // 保存到数据库（先保存GameProgress）
            progressRepository.updateGameProgress(finalProgress)
            // ✅ 单独保存徽章到Badge表
            progressRepository.addBadge(deviceBadge)

            // 发送音效副作用
            sendEffect(FireStationEffect.PlayBadgeSound)

            // 如果全部完成，发送解锁学校场景和成功音效
            if (isAllCompleted) {
                sendEffect(FireStationEffect.UnlockSchoolScene)
                sendEffect(FireStationEffect.PlayAllCompletedSound)
            }

            // 更新UI状态
            _state.value = currentState.copy(
                isPlayingVideo = false,
                currentPlayingDevice = null,
                completedDevices = newCompletedDevices,
                isAllCompleted = isAllCompleted,
                showBadgeAnimation = true,
                earnedBadgeDevice = device
            )
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

        sendEffect(FireStationEffect.PlayClickSound)
        sendEffect(FireStationEffect.NavigateToMap)
    }

    /**
     * 处理空闲超时
     *
     * 无操作 30 秒后触发，显示小火提示
     */
    private fun onIdleTimeout() {
        // 更新状态显示空闲提示
        _state.value = _state.value.copy(showIdleHint = true)
        sendEffect(FireStationEffect.ShowIdleHint)
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
            earnedBadgeDevice = null
        )
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送副作用到 Effect 通道
     *
     * @param effect 要发送的副作用
     */
    private fun sendEffect(effect: FireStationEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
