package com.cryallen.tigerfire.presentation.parent

import com.cryallen.tigerfire.domain.model.ParentSettings
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 获取当前日期字符串（格式：yyyy-MM-dd）
 * 平台相关实现
 */
expect fun getCurrentDate(): String

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

    private val _effect = Channel<ParentEffect>(capacity = Channel.UNLIMITED)
    val effect: Flow<ParentEffect> = _effect.receiveAsFlow()

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度、家长设置和徽章列表
        viewModelScope.launch {
            combine(
                progressRepository.getGameProgress(),
                progressRepository.getParentSettings(),
                progressRepository.getAllBadges()
            ) { progress, settings, badges ->
                Triple(progress, settings, badges)
            }.collect { (progress, settings, badges) ->
                // 获取今日日期（格式：yyyy-MM-dd）
                val today = getCurrentDate()

                // 计算今日使用时长
                val todayPlayTime = settings.dailyUsageStats[today] ?: 0L

                _state.value = ParentState(
                    settings = settings,
                    todayPlayTime = todayPlayTime,
                    totalPlayTime = progress.totalPlayTime,
                    sceneStatuses = progress.sceneStatuses,
                    totalBadgeCount = badges.size
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
            is ParentEvent.UpdateReminderTime -> handleUpdateReminderTime(event.minutes)
            is ParentEvent.ResetProgressClicked -> handleResetProgressClicked()
            is ParentEvent.ConfirmResetProgress -> handleConfirmResetProgress()
            is ParentEvent.CancelResetProgress -> handleCancelResetProgress()
            is ParentEvent.SubmitReverificationAnswer -> handleSubmitReverification(event.answer)
            is ParentEvent.CancelReverification -> handleCancelReverification()
            is ParentEvent.ShowTimeSettingsDialog -> handleShowTimeSettingsDialog()
            is ParentEvent.DismissTimeSettingsDialog -> handleDismissTimeSettingsDialog()
            is ParentEvent.ToggleSessionTimeLimit -> handleToggleSessionTimeLimit(event.enabled)
            is ParentEvent.ToggleDailyTimeLimit -> handleToggleDailyTimeLimit(event.enabled)
            is ParentEvent.SaveTimeSettings -> handleSaveTimeSettings()
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
        // 存储用户选择的时间，然后显示验证界面
        _state.value = _state.value.copy(
            pendingSessionTimeLimit = minutes,
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
     * 处理更新提前提醒时间
     *
     * @param minutes 提前提醒时长（分钟），0表示关闭提醒
     */
    private fun handleUpdateReminderTime(minutes: Int) {
        // 提前提醒设置不需要验证，直接更新
        viewModelScope.launch {
            // 获取当前settings并更新
            progressRepository.getParentSettings().first().let { currentSettings ->
                val updatedSettings = currentSettings.copy(reminderMinutesBefore = minutes)
                progressRepository.updateParentSettings(updatedSettings)
            }
            // 更新状态显示提示
            _state.value = _state.value.copy(showSettingsSavedHint = true)
            sendEffect(ParentEffect.ShowSettingsSavedHint)
        }
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
                    // 使用待处理的时间值来更新设置
                    val pendingMinutes = currentState.pendingSessionTimeLimit
                    if (pendingMinutes != null) {
                        executeUpdateTimeSettings(pendingMinutes)
                    }
                }
                ParentAction.CLEAR_STATISTICS -> {
                    // 清除使用统计
                    _state.value = _state.value.copy(showSettingsSavedHint = true)
                    sendEffect(ParentEffect.ShowSettingsSavedHint)
                }
                null -> {
                    // 不应该发生
                }
            }

            // 隐藏重新验证界面，清除待处理的状态
            _state.value = currentState.copy(
                showReverification = false,
                reverificationQuestion = null,
                pendingAction = null,
                pendingSessionTimeLimit = null
            )
        } else {
            // 验证失败，生成新题目
            _state.value = currentState.copy(
                reverificationQuestion = generateMathQuestion(),
                showVerificationFailedHint = true
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

    /**
     * 处理显示时间设置对话框
     */
    private fun handleShowTimeSettingsDialog() {
        // 从当前settings读取开关状态并显示对话框
        val currentSettings = _state.value.settings
        _state.value = _state.value.copy(
            showTimeSettingsDialog = true,
            sessionTimeLimitEnabled = currentSettings.sessionDurationMinutes > 0,
            dailyTimeLimitEnabled = false // 当前模型不支持每日限制，默认为false
        )
    }

    /**
     * 处理关闭时间设置对话框
     */
    private fun handleDismissTimeSettingsDialog() {
        _state.value = _state.value.copy(
            showTimeSettingsDialog = false
        )
    }

    /**
     * 处理切换每次使用时长限制
     */
    private fun handleToggleSessionTimeLimit(enabled: Boolean) {
        _state.value = _state.value.copy(
            sessionTimeLimitEnabled = enabled
        )
    }

    /**
     * 处理切换每日总时长限制（暂未实现）
     */
    private fun handleToggleDailyTimeLimit(enabled: Boolean) {
        // TODO: 需要先在ParentSettings模型中添加dailyLimitMinutes字段
        _state.value = _state.value.copy(
            dailyTimeLimitEnabled = enabled
        )
    }

    /**
     * 处理保存时间设置
     */
    private fun handleSaveTimeSettings() {
        viewModelScope.launch {
            val currentState = _state.value

            // 获取当前settings
            progressRepository.getParentSettings().first().let { currentSettings ->
                // 更新settings（如果开关关闭，则使用默认值15分钟）
                val updatedSettings = currentSettings.copy(
                    sessionDurationMinutes = if (currentState.sessionTimeLimitEnabled) {
                        if (currentSettings.sessionDurationMinutes > 0) {
                            currentSettings.sessionDurationMinutes
                        } else {
                            30 // 默认30分钟
                        }
                    } else {
                        15 // 禁用时使用默认值
                    }
                    // TODO: 添加dailyLimitMinutes字段后在此更新
                )
                progressRepository.updateParentSettings(updatedSettings)
            }

            // 关闭对话框
            _state.value = currentState.copy(
                showTimeSettingsDialog = false,
                showSettingsSavedHint = true
            )

            // 发送保存成功提示
            sendEffect(ParentEffect.ShowSettingsSavedHint)
        }
    }

    // ==================== 敏感操作执行 ====================

    /**
     * 执行更新时间设置
     *
     * @param minutes 每次使用时长（分钟）
     */
    private fun executeUpdateTimeSettings(minutes: Int) {
        viewModelScope.launch {
            // 获取当前settings并更新
            progressRepository.getParentSettings().first().let { currentSettings ->
                val updatedSettings = currentSettings.copy(
                    sessionDurationMinutes = minutes
                )
                progressRepository.updateParentSettings(updatedSettings)
            }
            // 更新状态显示提示
            _state.value = _state.value.copy(showSettingsSavedHint = true)
            // 发送保存成功提示
            sendEffect(ParentEffect.ShowSettingsSavedHint)
        }
    }

    /**
     * 执行重置游戏进度
     */
    private fun executeResetProgress() {
        viewModelScope.launch {
            // 使用 resetProgress() 方法，它会清空所有徽章、重置场景状态、清空统计数据
            progressRepository.resetProgress()

            // 获取重置后的进度数据更新本地状态
            val initialProgress = progressRepository.getGameProgress().first()

            // 更新本地状态
            _state.value = _state.value.copy(
                sceneStatuses = initialProgress.sceneStatuses,
                totalBadgeCount = 0,
                totalPlayTime = 0L,
                showResetSuccessHint = true
            )

            // 发送成功提示
            sendEffect(ParentEffect.ShowResetSuccessHint)
        }
    }

    /**
     * 关闭设置保存成功提示
     */
    fun dismissSettingsSavedHint() {
        _state.value = _state.value.copy(showSettingsSavedHint = false)
    }

    /**
     * 关闭重置成功提示
     */
    fun dismissResetSuccessHint() {
        _state.value = _state.value.copy(showResetSuccessHint = false)
    }

    /**
     * 关闭验证失败提示
     */
    fun dismissVerificationFailedHint() {
        _state.value = _state.value.copy(showVerificationFailedHint = false)
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
