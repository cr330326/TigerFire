package com.cryallen.tigerfire.presentation.school

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

/**
 * 学校场景 ViewModel
 *
 * 管理学校场景页面的状态和事件处理
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
            is SchoolEvent.AnimationPlaybackCompleted -> handleAnimationCompleted()
            is SchoolEvent.BackToMapClicked -> handleBackToMap()
            is SchoolEvent.BadgeAnimationCompleted -> handleBadgeAnimationCompleted()
        }
    }

    /**
     * 处理页面进入
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

        // 获取视频资源路径
        val videoPath = resourcePathProvider.getVideoPath("school/school_story.mp4")

        // 更新状态为正在播放
        _state.value = _state.value.copy(
            isPlayingAnimation = true
        )

        // 发送播放动画副作用
        sendEffect(SchoolEffect.PlayAnimation(videoPath))
    }

    /**
     * 处理动画播放完成
     */
    private fun handleAnimationCompleted() {
        val currentState = _state.value

        // 更新状态：结束播放
        _state.value = currentState.copy(
            isPlayingAnimation = false
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

                // 添加学校徽章
                val schoolBadge = Badge(
                    id = "${SCHOOL_BADGE_BASE_TYPE}_${PlatformDateTime.getCurrentTimeMillis()}",
                    baseType = SCHOOL_BADGE_BASE_TYPE,
                    scene = SceneType.SCHOOL,
                    variant = 0,
                    earnedAt = PlatformDateTime.getCurrentTimeMillis()
                )
                updatedProgress = updatedProgress.addBadge(schoolBadge)

                progressRepository.updateGameProgress(updatedProgress)

                // 更新本地状态
                _state.value = currentState.copy(
                    isCompleted = true
                )

                // 发送徽章动画和音效副作用
                sendEffect(SchoolEffect.ShowBadgeAnimation)
                sendEffect(SchoolEffect.PlayBadgeSound)
                sendEffect(SchoolEffect.PlayCompletedSound)

                // 发送解锁森林场景副作用
                sendEffect(SchoolEffect.UnlockForestScene)
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
        // 报告用户活动
        idleTimer.reportActivity()

        // 停止空闲检测
        idleTimer.stopIdleDetection()

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

    /**
     * 处理徽章收集动画完成
     */
    private fun handleBadgeAnimationCompleted() {
        _state.value = _state.value.copy(
            showBadgeAnimation = false
        )
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
}
