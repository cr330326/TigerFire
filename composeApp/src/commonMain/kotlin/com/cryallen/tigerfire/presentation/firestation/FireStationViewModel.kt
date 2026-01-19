package com.cryallen.tigerfire.presentation.firestation

import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    private val _effect = Channel<FireStationEffect>()
    val effect: Flow<FireStationEffect> = _effect.receiveAsFlow()

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度，加载已完成的设备
        viewModelScope.launch {
            progressRepository.getGameProgress()
                .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                .collect { progress ->
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
    }

    // ==================== 事件处理 ====================

    /**
     * 处理消防站场景事件
     *
     * @param event 消防站场景事件
     */
    fun onEvent(event: FireStationEvent) {
        when (event) {
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

        // 更新状态：结束播放
        _state.value = currentState.copy(
            isPlayingVideo = false,
            currentPlayingDevice = null
        )

        viewModelScope.launch {
            // 获取当前进度
            val progress = progressRepository.getGameProgress()
                .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                .first()

            // 检查设备是否已完成
            if (!progress.fireStationCompletedItems.contains(device.deviceId)) {
                // 首次完成，使用 GameProgress 辅助方法更新进度
                var updatedProgress = progress.addFireStationCompletedItem(device.deviceId)

                // 检查是否全部完成，如果是则解锁学校场景
                if (updatedProgress.isFireStationCompleted()) {
                    updatedProgress = updatedProgress.updateSceneStatus(SceneType.SCHOOL, SceneStatus.UNLOCKED)
                }

                progressRepository.updateGameProgress(updatedProgress)

                // 更新本地状态
                val newCompletedDevices = currentState.completedDevices + device
                val isAllCompleted = updatedProgress.isFireStationCompleted()

                _state.value = currentState.copy(
                    completedDevices = newCompletedDevices,
                    isAllCompleted = isAllCompleted
                )

                // 发送徽章动画副作用
                sendEffect(FireStationEffect.ShowBadgeAnimation(device))
                sendEffect(FireStationEffect.PlayBadgeSound)

                // 如果全部完成，发送解锁学校场景和成功音效
                if (isAllCompleted) {
                    sendEffect(FireStationEffect.UnlockSchoolScene)
                    sendEffect(FireStationEffect.PlayAllCompletedSound)
                }
            } else {
                // 重复观看，不重复发放徽章
                // 可以播放普通完成音效
            }
        }
    }

    /**
     * 处理返回主地图按钮点击
     */
    private fun handleBackToMap() {
        sendEffect(FireStationEffect.NavigateToMap)
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
