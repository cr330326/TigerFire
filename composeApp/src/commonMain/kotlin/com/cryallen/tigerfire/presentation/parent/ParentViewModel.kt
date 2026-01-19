package com.cryallen.tigerfire.presentation.parent

import com.cryallen.tigerfire.domain.model.ParentSettings
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
 * 家长模式 ViewModel
 *
 * 管理家长模式页面的状态和事件处理
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 * @param progressRepository 进度仓储接口
 */
class ParentViewModel(
    private val viewModelScope: CoroutineScope,
    private val progressRepository: ProgressRepository
) {
    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(ParentState())
    val state: StateFlow<ParentState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<ParentEffect>()
    val effect: Flow<ParentEffect> = _effect.receiveAsFlow()

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度和家长设置
        viewModelScope.launch {
            combine<com.cryallen.tigerfire.domain.model.GameProgress, ParentSettings, Pair<com.cryallen.tigerfire.domain.model.GameProgress, ParentSettings>>(
                progressRepository.getGameProgress(),
                progressRepository.getParentSettings()
            ) { progress, settings ->
                progress to settings
            }.collect { (progress, settings) ->
                _state.value = ParentState(
                    settings = settings,
                    todayPlayTime = 0L, // TODO: 从使用统计服务获取今日时长
                    totalPlayTime = progress.totalPlayTime,
                    sceneStatuses = progress.sceneStatuses,
                    totalBadgeCount = progress.getTotalBadgeCount()
                )
            }
        }
    }

    // ==================== 事件处理 ====================

    /**
     * 处理家长模式事件
     *
     * @param event 家长模式事件
     */
    fun onEvent(event: ParentEvent) {
        when (event) {
            is ParentEvent.BackToMapClicked -> handleBackToMap()
            is ParentEvent.UpdateSessionTimeLimit -> handleUpdateSessionTimeLimit(event.minutes)
            is ParentEvent.UpdateDailyTimeLimit -> handleUpdateDailyTimeLimit(event.minutes)
            is ParentEvent.ResetProgressClicked -> handleResetProgressClicked()
            is ParentEvent.ConfirmResetProgress -> handleConfirmResetProgress()
            is ParentEvent.CancelResetProgress -> handleCancelResetProgress()
            is ParentEvent.SubmitReverificationAnswer -> handleSubmitReverification(event.answer)
            is ParentEvent.CancelReverification -> handleCancelReverification()
        }
    }

    /**
     * 处理返回主地图按钮点击
     */
    private fun handleBackToMap() {
        sendEffect(ParentEffect.PlayClickSound)
        sendEffect(ParentEffect.NavigateToMap)
    }

    /**
     * 处理更新每次使用时长限制
     *
     * @param minutes 时长（分钟）
     */
    private fun handleUpdateSessionTimeLimit(minutes: Int) {
        // 对于修改时间设置等敏感操作，先显示重新验证界面
        _state.value = _state.value.copy(
            showReverification = true,
            reverificationQuestion = generateMathQuestion(),
            pendingAction = ParentAction.UPDATE_TIME_SETTINGS
        )
    }

    /**
     * 处理更新每日总时长限制
     *
     * @param minutes 时长（分钟）
     */
    private fun handleUpdateDailyTimeLimit(minutes: Int) {
        // 对于修改时间设置等敏感操作，先显示重新验证界面
        _state.value = _state.value.copy(
            showReverification = true,
            reverificationQuestion = generateMathQuestion(),
            pendingAction = ParentAction.UPDATE_TIME_SETTINGS
        )
    }

    /**
     * 处理点击重置游戏进度按钮
     */
    private fun handleResetProgressClicked() {
        // 显示重置确认对话框
        _state.value = _state.value.copy(
            showResetConfirmation = true
        )
    }

    /**
     * 处理确认重置游戏进度
     */
    private fun handleConfirmResetProgress() {
        // 隐藏确认对话框，显示重新验证界面
        _state.value = _state.value.copy(
            showResetConfirmation = false,
            showReverification = true,
            reverificationQuestion = generateMathQuestion(),
            pendingAction = ParentAction.RESET_PROGRESS
        )
    }

    /**
     * 处理取消重置游戏进度
     */
    private fun handleCancelResetProgress() {
        _state.value = _state.value.copy(
            showResetConfirmation = false
        )
    }

    /**
     * 处理提交重新验证答案
     *
     * @param answer 用户输入的答案
     */
    private fun handleSubmitReverification(answer: Int) {
        val currentState = _state.value
        val correctAnswer = currentState.reverificationQuestion?.second
        val action = currentState.pendingAction

        if (answer == correctAnswer) {
            // 验证通过，执行待处理的操作
            when (action) {
                ParentAction.RESET_PROGRESS -> executeResetProgress()
                ParentAction.UPDATE_TIME_SETTINGS -> {
                    // 这里简化处理，实际应该保存新的时间设置
                    sendEffect(ParentEffect.ShowSettingsSavedHint)
                }
                ParentAction.CLEAR_STATISTICS -> {
                    // 清除使用统计
                    sendEffect(ParentEffect.ShowSettingsSavedHint)
                }
                null -> {
                    // 不应该发生
                }
            }

            // 隐藏重新验证界面
            _state.value = currentState.copy(
                showReverification = false,
                reverificationQuestion = null,
                pendingAction = null
            )
        } else {
            // 验证失败，生成新题目
            _state.value = currentState.copy(
                reverificationQuestion = generateMathQuestion()
            )
            sendEffect(ParentEffect.ShowVerificationFailedHint)
        }
    }

    /**
     * 处理取消重新验证
     */
    private fun handleCancelReverification() {
        _state.value = _state.value.copy(
            showReverification = false,
            reverificationQuestion = null,
            pendingAction = null
        )
    }

    // ==================== 敏感操作执行 ====================

    /**
     * 执行重置游戏进度
     */
    private fun executeResetProgress() {
        viewModelScope.launch {
            // 重置为初始进度
            val initialProgress = com.cryallen.tigerfire.domain.model.GameProgress.initial()

            // 更新进度
            progressRepository.updateGameProgress(initialProgress)

            // 更新本地状态
            _state.value = _state.value.copy(
                sceneStatuses = initialProgress.sceneStatuses,
                totalBadgeCount = 0,
                totalPlayTime = 0L
            )

            // 发送成功提示
            sendEffect(ParentEffect.ShowResetSuccessHint)
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成随机数学题（一位数加法，结果 ≤ 10）
     *
     * @return Pair(问题文本, 答案)
     */
    private fun generateMathQuestion(): Pair<String, Int> {
        val a = (1..5).random()
        val b = (1..5).random()
        val answer = a + b

        return "$a + $b = ?" to answer
    }

    /**
     * 发送副作用到 Effect 通道
     *
     * @param effect 要发送的副作用
     */
    private fun sendEffect(effect: ParentEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
