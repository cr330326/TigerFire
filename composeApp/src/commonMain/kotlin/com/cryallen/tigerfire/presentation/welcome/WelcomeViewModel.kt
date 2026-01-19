package com.cryallen.tigerfire.presentation.welcome

import kotlinx.coroutines.channels.Channel
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

    private val _effect = Channel<WelcomeEffect>()
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
            is WelcomeEvent.ScreenClicked -> handleScreenClicked()
        }
    }

    /**
     * 处理卡车入场动画完成
     */
    private fun handleTruckAnimationCompleted() {
        _state.value = _state.value.copy(
            isAnimationCompleted = true,
            showWaveAnimation = true
        )
        sendEffect(WelcomeEffect.PlayWaveAnimation)
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
     * 处理屏幕点击
     */
    private fun handleScreenClicked() {
        // 无论动画是否完成，点击屏幕都导航到主地图
        sendEffect(WelcomeEffect.NavigateToMap)
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
 * 协程作用域接口
 *
 * 用于解耦 ViewModel 与具体的协程作用域实现
 */
interface CoroutineScope {
    /**
     * 在协程作用域中执行代码块
     *
     * @param block 要执行的代码块
     */
    fun launch(block: suspend () -> Unit)
}
