package com.cryallen.tigerfire.presentation.school

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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 学校场景 ViewModel
 *
 * 管理学校场景页面的状态和事件处理
 *
 * 交互流程：
 * 1. 进入场景 → 启动警报效果 + 播放语音提示
 * 2. 用户点击播放按钮 → 停止警报 + 播放视频
 * 3. 视频播放完成 → 颁发徽章 + 解锁森林 + 播放赞美语音
 * 4. 语音播放完成 → 导航回主地图
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 * @param progressRepository 进度仓储接口
 * @param resourcePathProvider 资源路径提供者
 */
class SchoolViewModel(
    private val viewModelScope: CoroutineScope,
    private val progressRepository: ProgressRepository,
    private val resourcePathProvider: ResourcePathProvider
) {
    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(SchoolState())
    val state: StateFlow<SchoolState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<SchoolEffect>()
    val effect: Flow<SchoolEffect> = _effect.receiveAsFlow()

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

    // ==================== 常量定义 ====================

    companion object {
        /** 学校场景徽章基础类型 */
        const val SCHOOL_BADGE_BASE_TYPE = "school"

        /** 视频资源名称 */
        const val VIDEO_NAME = "School_Fire_Safety_Knowledge"

        /** 警报语音路径 */
        const val VOICE_FIRE_ALERT = "audio/voices/school_fire.mp3"

        /** 赞美语音路径 */
        const val VOICE_PRAISE = "audio/voices/school_praise.mp3"
    }

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度，加载完成状态
        viewModelScope.launch {
            progressRepository.getGameProgress()
                .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                .collect { progress ->
                    val isCompleted = progress.getSceneStatus(SceneType.SCHOOL) == SceneStatus.COMPLETED

                    _state.value = _state.value.copy(
                        isCompleted = isCompleted
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
     * 处理学校场景事件
     *
     * @param event 学校场景事件
     */
    fun onEvent(event: SchoolEvent) {
        when (event) {
            is SchoolEvent.ScreenEntered -> handleScreenEntered()
            is SchoolEvent.PlayButtonClicked -> handlePlayButtonClicked()
            is SchoolEvent.VideoPlaybackCompleted -> handleVideoCompleted()
            is SchoolEvent.VoicePlaybackCompleted -> handleVoiceCompleted()
            is SchoolEvent.BackToMapClicked -> handleBackToMap()
            is SchoolEvent.BadgeAnimationCompleted -> handleBadgeAnimationCompleted()
            is SchoolEvent.CloseBadgeAnimation -> handleCloseBadgeAnimation()
            is SchoolEvent.PauseVideoClicked -> handlePauseVideo()
            is SchoolEvent.ResumeVideoClicked -> handleResumeVideo()
            is SchoolEvent.ExitVideoClicked -> handleExitVideo()
        }
    }

    /**
     * 处理页面进入
     *
     * 启动警报效果（音效 + 红光闪烁）+ 播放语音提示
     */
    private fun handleScreenEntered() {
        // 报告用户活动，重置空闲计时器
        idleTimer.reportActivity()

        // 检测快速点击
        if (rapidClickGuard.checkClick()) {
            // 触发防护：播放语音提示
            sendEffect(SchoolEffect.PlaySlowDownVoice)
            rapidClickGuard.reset()
            return
        }

        // 更新状态：显示警报效果和播放按钮
        _state.value = _state.value.copy(
            showAlarmEffect = true,
            showPlayButton = true,
            isAlarmPlaying = true
        )

        // 发送启动警报效果副作用
        sendEffect(SchoolEffect.StartAlarmEffects)

        // 发送播放语音副作用："学校着火啦！快叫消防车！"
        sendEffect(SchoolEffect.PlayVoice(VOICE_FIRE_ALERT))
    }

    /**
     * 处理播放按钮点击
     *
     * 停止警报效果，开始播放视频
     */
    private fun handlePlayButtonClicked() {
        val currentState = _state.value

        // 仅在显示播放按钮时允许点击
        if (!currentState.showPlayButton || currentState.isVideoPlaying) {
            return
        }

        // 报告用户活动
        idleTimer.reportActivity()

        // 检测快速点击
        if (rapidClickGuard.checkClick()) {
            sendEffect(SchoolEffect.PlaySlowDownVoice)
            rapidClickGuard.reset()
            return
        }

        // 获取视频资源路径
        val videoPath = resourcePathProvider.getVideoPath(VIDEO_NAME)

        // 更新状态：隐藏播放按钮，停止警报，开始播放视频，保存视频路径
        _state.value = currentState.copy(
            showPlayButton = false,
            showAlarmEffect = false,
            isVideoPlaying = true,
            isAlarmPlaying = false,
            currentVideoPath = videoPath  // 保存视频路径到状态中
        )

        // 发送停止警报效果副作用
        sendEffect(SchoolEffect.StopAlarmEffects)

        // 发送播放视频副作用
        sendEffect(SchoolEffect.PlayVideo(videoPath))
    }

    /**
     * 处理视频播放完成
     *
     * 颁发徽章，解锁森林场景，播放赞美语音
     */
    private fun handleVideoCompleted() {
        val currentState = _state.value

        // 更新状态：视频播放结束
        _state.value = currentState.copy(
            isVideoPlaying = false
        )

        viewModelScope.launch {
            // 获取当前进度
            val progress = progressRepository.getGameProgress()
                .onStart { emit(com.cryallen.tigerfire.domain.model.GameProgress.initial()) }
                .first()

            // 检查是否首次完成
            if (progress.getSceneStatus(SceneType.SCHOOL) != SceneStatus.COMPLETED) {
                // 首次完成，更新进度
                var updatedProgress = progress.updateSceneStatus(SceneType.SCHOOL, SceneStatus.COMPLETED)

                // 解锁森林场景
                updatedProgress = updatedProgress.updateSceneStatus(SceneType.FOREST, SceneStatus.UNLOCKED)

                // ✅ 关键修复：从数据库查询实际徽章来计算变体（而不是使用progress.badges，因为它总是空的）
                val existingBadges = progressRepository.getAllBadges().first()
                val nextVariant = existingBadges.calculateNextVariant(SCHOOL_BADGE_BASE_TYPE)

                // 添加学校徽章
                val schoolBadge = Badge(
                    id = "${SCHOOL_BADGE_BASE_TYPE}_v${nextVariant}_${PlatformDateTime.getCurrentTimeMillis()}",
                    baseType = SCHOOL_BADGE_BASE_TYPE,
                    scene = SceneType.SCHOOL,
                    variant = nextVariant,  // 使用计算出的变体编号
                    earnedAt = PlatformDateTime.getCurrentTimeMillis()
                )

                // 保存到数据库（先保存GameProgress）
                progressRepository.updateGameProgress(updatedProgress)
                // ✅ 单独保存徽章到Badge表
                progressRepository.addBadge(schoolBadge)

                // 更新本地状态
                _state.value = currentState.copy(
                    isCompleted = true,
                    showBadgeAnimation = true
                )

                // 发送徽章动画和音效副作用
                sendEffect(SchoolEffect.ShowBadgeAnimation)
                sendEffect(SchoolEffect.PlayBadgeSound)
                sendEffect(SchoolEffect.PlayCompletedSound)

                // 发送解锁森林场景副作用
                sendEffect(SchoolEffect.UnlockForestScene)

                // 发送播放赞美语音副作用："你真棒！记住，着火要找大人帮忙！"
                sendEffect(SchoolEffect.PlayVoice(VOICE_PRAISE))
            } else {
                // ✅ 关键修复：重复观看也颁发变体徽章（支持3种变体）
                val existingBadges = progressRepository.getAllBadges().first()
                val nextVariant = existingBadges.calculateNextVariant(SCHOOL_BADGE_BASE_TYPE)
                val maxVariants = com.cryallen.tigerfire.domain.model.getMaxVariantsForBaseType(SCHOOL_BADGE_BASE_TYPE)

                // 检查是否还有未收集的变体
                if (nextVariant < maxVariants) {
                    val schoolBadge = Badge(
                        id = "${SCHOOL_BADGE_BASE_TYPE}_v${nextVariant}_${PlatformDateTime.getCurrentTimeMillis()}",
                        baseType = SCHOOL_BADGE_BASE_TYPE,
                        scene = SceneType.SCHOOL,
                        variant = nextVariant,
                        earnedAt = PlatformDateTime.getCurrentTimeMillis()
                    )
                    // ✅ 单独保存徽章到Badge表
                    progressRepository.addBadge(schoolBadge)

                    // 显示获得了新变体徽章
                    sendEffect(SchoolEffect.ShowBadgeAnimation)
                    sendEffect(SchoolEffect.PlayBadgeSound)
                } else {
                    // 所有变体已收集完成，只播放普通完成音效
                    sendEffect(SchoolEffect.PlayCompletedSound)
                }
                sendEffect(SchoolEffect.PlayVoice(VOICE_PRAISE))

                _state.value = currentState.copy(
                    showBadgeAnimation = true
                )
            }
        }
    }

    /**
     * 处理语音播放完成
     *
     * 赞美语音播放完毕
     */
    private fun handleVoiceCompleted() {
        // 语音播放完成，等待用户点击徽章动画继续
        // 不做额外处理，等待 BadgeAnimationCompleted 事件
    }

    /**
     * 处理徽章动画完成
     *
     * 用户点击继续后触发，导航回主地图
     */
    private fun handleBadgeAnimationCompleted() {
        _state.value = _state.value.copy(
            showBadgeAnimation = false
        )

        // 延迟一小段时间后导航
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            sendEffect(SchoolEffect.NavigateToMap)
        }
    }

    /**
     * 处理返回主地图按钮点击
     *
     * 仅在视频未播放时允许返回
     */
    private fun handleBackToMap() {
        val currentState = _state.value

        // 视频播放中禁止返回
        if (currentState.isVideoPlaying) {
            return
        }

        // 报告用户活动
        idleTimer.reportActivity()

        // 停止空闲检测
        idleTimer.stopIdleDetection()

        // 停止警报效果
        if (currentState.isAlarmPlaying) {
            sendEffect(SchoolEffect.StopAlarmEffects)
        }

        sendEffect(SchoolEffect.NavigateToMap)
    }

    /**
     * 处理空闲超时
     *
     * 无操作 30 秒后触发，显示小火提示
     */
    private fun onIdleTimeout() {
        sendEffect(SchoolEffect.ShowIdleHint)
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送副作用到 Effect 通道
     *
     * @param effect 要发送的副作用
     */
    private fun sendEffect(effect: SchoolEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * 处理暂停视频
     */
    private fun handlePauseVideo() {
        val currentState = _state.value

        // 仅在视频正在播放时允许暂停
        if (!currentState.isVideoPlaying || currentState.isVideoPaused) {
            return
        }

        // 报告用户活动
        idleTimer.reportActivity()

        // 更新状态：暂停视频
        _state.value = currentState.copy(
            isVideoPaused = true
        )
    }

    /**
     * 处理恢复视频
     */
    private fun handleResumeVideo() {
        val currentState = _state.value

        // 仅在视频暂停时允许恢复
        if (!currentState.isVideoPlaying || !currentState.isVideoPaused) {
            return
        }

        // 报告用户活动
        idleTimer.reportActivity()

        // 更新状态：恢复视频播放
        _state.value = currentState.copy(
            isVideoPaused = false
        )
    }

    /**
     * 处理退出视频
     *
     * 中断视频播放，返回播放按钮界面
     */
    private fun handleExitVideo() {
        val currentState = _state.value

        // 仅在视频播放中允许退出
        if (!currentState.isVideoPlaying) {
            return
        }

        // 报告用户活动
        idleTimer.reportActivity()

        // 更新状态：停止视频，返回播放按钮界面
        _state.value = currentState.copy(
            isVideoPlaying = false,
            isVideoPaused = false,
            showPlayButton = true,
            showAlarmEffect = false,
            isAlarmPlaying = false
        )
    }

    /**
     * 处理关闭徽章动画
     *
     * 用户点击徽章动画背景关闭，返回播放按钮界面
     * 允许用户重新观看视频
     */
    private fun handleCloseBadgeAnimation() {
        val currentState = _state.value

        // 仅在徽章动画显示时允许关闭
        if (!currentState.showBadgeAnimation) {
            return
        }

        // 报告用户活动
        idleTimer.reportActivity()

        // 更新状态：关闭徽章动画，显示播放按钮
        _state.value = currentState.copy(
            showBadgeAnimation = false,
            showPlayButton = true,
            isVideoPlaying = false,
            isVideoPaused = false,
            showAlarmEffect = false,
            isAlarmPlaying = false
        )
    }
}
