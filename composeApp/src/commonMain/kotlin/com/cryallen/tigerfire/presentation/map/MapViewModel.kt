package com.cryallen.tigerfire.presentation.map

import androidx.compose.ui.geometry.Offset
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.GameProgress
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.domain.repository.ProgressRepository
import com.cryallen.tigerfire.presentation.welcome.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 主地图 ViewModel
 *
 * 管理主地图页面的状态和事件处理
 *
 * @param viewModelScope 协程作用域（由平台层注入）
 * @param progressRepository 进度仓储接口
 */
class MapViewModel(
    private val viewModelScope: CoroutineScope,
    private val progressRepository: ProgressRepository
) {
    // ==================== 状态管理 ====================

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    // 单独管理选中的场景（用于 Avatar 位置记忆，不受 progressRepository 更新影响）
    private val _selectedScene = MutableStateFlow(SceneType.FIRE_STATION)
    val selectedScene: StateFlow<SceneType> = _selectedScene

    // 单独管理动画触发器（不受 progressRepository 更新影响）
    private val _animationTrigger = MutableStateFlow(0)
    val animationTrigger: StateFlow<Int> = _animationTrigger

    // 单独管理场景图标位置（不受 progressRepository 更新影响）
    private val _scenePositions = MutableStateFlow<Map<SceneType, Offset>>(emptyMap())
    val scenePositions: StateFlow<Map<SceneType, Offset>> = _scenePositions

    // ==================== 副作用通道 ====================

    private val _effect = Channel<MapEffect>()
    val effect: Flow<MapEffect> = _effect.receiveAsFlow()

    // ==================== 初始化 ====================

    init {
        // 订阅游戏进度和徽章数据
        viewModelScope.launch {
            combine<GameProgress, List<Badge>, MapState>(
                progressRepository.getGameProgress(),
                // TODO: 添加徽章数据源（当前 ProgressRepository 暂未提供徽章 Flow）
                kotlinx.coroutines.flow.flowOf(emptyList<Badge>())
            ) { progress, badges ->
                MapState(
                    sceneStatuses = progress.sceneStatuses,
                    badges = badges
                )
            }.onStart {
                emit(MapState()) // 初始状态
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    // ==================== 事件处理 ====================

    /**
     * 处理主地图事件
     *
     * @param event 主地图事件
     */
    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.SceneClicked -> handleSceneClicked(event.scene)
            is MapEvent.UpdateSelectedScene -> handleUpdateSelectedScene(event.scene)
            is MapEvent.UpdateScenePosition -> handleUpdateScenePosition(event.scene, event.offset)
            is MapEvent.CollectionClicked -> handleCollectionClicked()
            is MapEvent.ParentModeClicked -> handleParentModeClicked()
            is MapEvent.SubmitParentAnswer -> handleSubmitAnswer(event.answer)
            is MapEvent.CancelParentVerification -> handleCancelVerification()
        }
    }

    /**
     * 处理场景图标点击
     *
     * @param scene 场景类型
     */
    private fun handleSceneClicked(scene: SceneType) {
        val currentState = _state.value
        val sceneStatus = currentState.sceneStatuses[scene]

        when (sceneStatus) {
            SceneStatus.LOCKED -> {
                // 场景锁定，播放提示音效/语音
                sendEffect(MapEffect.PlayLockedHint)
            }
            SceneStatus.UNLOCKED, SceneStatus.COMPLETED -> {
                // 场景已解锁，播放点击音效并导航
                sendEffect(MapEffect.PlaySceneSound(scene))
                sendEffect(MapEffect.NavigateToScene(scene))
            }
            null -> {
                // 场景未找到（数据不一致），默认视为锁定
                sendEffect(MapEffect.PlayLockedHint)
            }
        }
    }

    /**
     * 处理更新选中的场景（不触发导航，仅用于状态记忆）
     *
     * @param scene 场景类型
     */
    private fun handleUpdateSelectedScene(scene: SceneType) {
        _selectedScene.value = scene
        _animationTrigger.value++
    }

    /**
     * 处理更新场景图标位置
     *
     * @param scene 场景类型
     * @param offset 场景图标位置
     */
    private fun handleUpdateScenePosition(scene: SceneType, offset: Offset) {
        _scenePositions.value = _scenePositions.value + (scene to offset)
    }

    /**
     * 处理"我的收藏"按钮点击
     */
    private fun handleCollectionClicked() {
        // 播放点击音效并导航到收藏页面
        sendEffect(MapEffect.NavigateToCollection)
    }

    /**
     * 处理家长模式入口点击
     */
    private fun handleParentModeClicked() {
        // 生成随机数学题（一位数加法，结果 ≤ 10）
        val (question, answer) = generateMathQuestion()

        // 更新状态显示验证界面
        _state.value = _state.value.copy(
            showParentVerification = true,
            mathQuestion = question to answer
        )
    }

    /**
     * 处理家长模式验证 - 提交答案
     *
     * @param answer 用户输入的答案
     */
    private fun handleSubmitAnswer(answer: Int) {
        val currentState = _state.value
        val correctAnswer = currentState.mathQuestion?.second

        if (answer == correctAnswer) {
            // 验证通过，隐藏验证界面并导航到家长模式
            _state.value = currentState.copy(
                showParentVerification = false,
                mathQuestion = null
            )
            sendEffect(MapEffect.NavigateToParent)
        } else {
            // 验证失败，生成新题目
            val (newQuestion, newAnswer) = generateMathQuestion()
            _state.value = currentState.copy(
                mathQuestion = newQuestion to newAnswer
            )
            // TODO: 播放错误提示音效
        }
    }

    /**
     * 处理家长模式验证 - 取消
     */
    private fun handleCancelVerification() {
        _state.value = _state.value.copy(
            showParentVerification = false,
            mathQuestion = null
        )
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
    private fun sendEffect(effect: MapEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
