package com.cryallen.tigerfire.presentation.welcome

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 欢迎页 ViewModel
 *
 * 管理启动页的状态和事件处理
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 */
class WelcomeViewModel(
    private val viewModelScope: CoroutineScope
) {
    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state

    // ==================== 副作用通道 ====================

    private val _effect = Channel<WelcomeEffect>(capacity = Channel.UNLIMITED)
    val effect: Flow<WelcomeEffect> = _effect.receiveAsFlow()

    // ==================== 事件处理 ====================

    /**
     * 处理欢迎页事件
     *
     * @param event 欢迎页事件
     */
    fun onEvent(event: WelcomeEvent) {
        when (event) {
            is WelcomeEvent.TruckAnimationCompleted -> handleTruckAnimationCompleted()
            is WelcomeEvent.WaveAnimationCompleted -> handleWaveAnimationCompleted()
            is WelcomeEvent.VoicePlaybackCompleted -> handleVoicePlaybackCompleted()
        }
    }

    /**
     * 处理卡车入场动画完成
     *
     * 触发小火挥手动画和播放欢迎语音
     */
    private fun handleTruckAnimationCompleted() {
        _state.value = _state.value.copy(
            isTruckAnimationCompleted = true,
            showWaveAnimation = true,
            isVoicePlaying = true
        )
        // 触发挥手动画
        sendEffect(WelcomeEffect.PlayWaveAnimation)
        // 播放欢迎语音："HI！今天和我一起救火吧！"
        sendEffect(WelcomeEffect.PlayVoice("audio/voices/welcome_greeting.mp3"))
    }

    /**
     * 处理小火挥手动画完成
     */
    private fun handleWaveAnimationCompleted() {
        _state.value = _state.value.copy(
            showWaveAnimation = false
        )
    }

    /**
     * 处理语音播放完成
     *
     * 延迟100ms后自动导航到主地图（无需用户交互）
     */
    private fun handleVoicePlaybackCompleted() {
        _state.value = _state.value.copy(
            isVoicePlaying = false,
            shouldNavigate = true
        )

        // 延迟2s后自动导航到主地图
        viewModelScope.launch {
            delay(2000)
            sendEffect(WelcomeEffect.NavigateToMap)
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送副作用到 Effect 通道
     *
     * @param effect 要发送的副作用
     */
    private fun sendEffect(effect: WelcomeEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

/**
 * 协程作用域
 *
 * 用于解耦 ViewModel 与具体的协程作用域实现
 * 使用 expect/actual 机制在各平台实现
 */
expect class CoroutineScope {
    /**
     * 在协程作用域中执行代码块
     *
     * @param block 要执行的代码块
     */
    fun launch(block: suspend () -> Unit)

    /**
     * 取消协程作用域中的所有协程
     *
     * 应在 ViewModel 不再需要时调用，防止内存泄漏
     */
    fun cancel()
}
