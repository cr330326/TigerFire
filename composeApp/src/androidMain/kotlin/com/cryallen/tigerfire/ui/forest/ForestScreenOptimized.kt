package com.cryallen.tigerfire.ui.forest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.forest.ForestEffect
import com.cryallen.tigerfire.presentation.forest.ForestEvent
import com.cryallen.tigerfire.presentation.forest.ForestViewModel
import com.cryallen.tigerfire.ui.components.CartoonFlame
import com.cryallen.tigerfire.ui.components.KidsBackButton
import com.cryallen.tigerfire.ui.theme.ThemeGradients
import com.cryallen.tigerfire.ui.theme.createVerticalGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ForestScreen 优化版本
 *
 * 优化内容：
 * 1. 触觉反馈 - 所有交互都带震动反馈
 * 2. 增强动画 - 直升机救援动画增强、羊只被救动画
 * 3. 粒子背景 - 漂浮树叶/云朵效果
 * 4. 微交互 - 拖拽缩放反馈增强
 * 5. 性能优化 - 动画资源预加载
 */
@Composable
fun ForestScreenOptimized(
    viewModel: ForestViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // 页面进入淡入动画
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
        viewModel.onEvent(ForestEvent.ScreenEntered)
    }

    // 预加载动画资源（性能优化）
    LaunchedEffect(Unit) {
        // 预加载音效和动画资源
        audioManager.preloadSounds()
    }

    // 订阅副作用（Effect）- 带触觉反馈
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ForestEffect.PlayRescueVideo -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is ForestEffect.ShowBadgeAnimation -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is ForestEffect.ShowCompletionHint -> {
                    // 完成提示由状态驱动
                }
                is ForestEffect.PlayClickSound -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    audioManager.playClickSound(com.cryallen.tigerfire.domain.model.SceneType.FOREST)
                }
                is ForestEffect.PlayFlyingSound -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    audioManager.playVoice("audio/sfx_flying.mp3")
                }
                is ForestEffect.PlayBadgeSound -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    audioManager.playBadgeSound()
                }
                is ForestEffect.PlayAllCompletedSound -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    audioManager.playAllCompletedSound()
                }
                is ForestEffect.NavigateToMap -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateBack()
                }
                is ForestEffect.PlaySlowDownVoice -> {
                    audioManager.playVoice("audio/voices/slow_down.mp3")
                }
                is ForestEffect.ShowIdleHint -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    audioManager.playVoice("audio/voices/hint_idle.mp3")
                }
                is ForestEffect.PlayStartVoice -> {
                    audioManager.playVoice("audio/voices/forest_start.mp3")
                }
                is ForestEffect.PlayCompleteVoice -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    audioManager.playVoice("audio/voices/forest_complete.mp3")
                }
                is ForestEffect.PlayCompletedSound -> {
                    audioManager.playVoice("audio/sfx_complete.mp3")
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "forest_bg")

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 优化的森林主题渐变背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = createVerticalGradient(ThemeGradients.Forest)
                )
        )

        // 优化的粒子背景 - 漂浮树叶和云朵
        FloatingLeavesAndClouds(infiniteTransition)

        // Q版火焰装饰（在屏幕底部）- 带增强动画
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 5个火焰emoji装饰 - 带脉冲动画
            repeat(5) { index ->
                val flameScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800 + index * 100, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flame_pulse_$index"
                )
                CartoonFlame(
                    modifier = Modifier
                        .offset(y = (index % 2 * 10).dp)
                        .scale(flameScale),
                    size = (36 + index * 4).dp
                )
            }
        }

        // 游戏区域（小羊和直升机）- 全屏显示
        ForestGameAreaOptimized(
            state = state,
            infiniteTransition = infiniteTransition,
            onSheepClick = { sheepIndex ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onEvent(ForestEvent.SheepClicked(sheepIndex))
            },
            onFlightComplete = {
                viewModel.onEvent(ForestEvent.HelicopterFlightCompleted)
            },
            onPlayVideoClick = { sheepIndex ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onEvent(ForestEvent.PlayVideoClicked(sheepIndex))
            }
        )

        // 顶部信息栏（绝对定位）- 带进入动画
        androidx.compose.animation.AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                initialScale = 0.95f
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 顶部行：返回按钮 + 场景标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 返回按钮 - 带触觉反馈
                    KidsBackButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onEvent(ForestEvent.BackToMapClicked)
                        }
                    )

                    // 场景标题 - 带发光效果
                    val titleGlowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "title_glow"
                    )

                    Text(
                        text = "🌲 森林救援",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .drawBehind {
                                drawRoundRect(
                                    color = Color(0xFF2A9D8F),
                                    style = Stroke(width = 4.dp.toPx()),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                                )
                                drawRoundRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF2A9D8F).copy(alpha = titleGlowAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                                )
                            }
                    )

                    // 进度徽章（大而醒目）- 带脉冲动画
                    val badgePulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "badge_pulse"
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(badgePulseScale)
                            .shadow(10.dp, CircleShape, spotColor = Color(0xFFFFE066), ambientColor = Color(0xFFFFD93D))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD93D),  // 明亮黄色
                                        Color(0xFFFF922B),  // 橙色
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🐑",
                                fontSize = 24.sp
                            )
                            Text(
                                text = "${state.rescuedSheep.size}/2",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 提示文字（大而清晰）- 带闪烁动画
                val hintAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "hint_alpha"
                )

                Text(
                    text = "点击小羊救援它们！",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = hintAlpha),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f))
                )

                // 全部完成提示（更醒目）
                if (state.isAllCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val celebrationScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "celebration_scale"
                    )

                    Text(
                        text = "🎉 太棒了！全部救援完成！",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .scale(celebrationScale)
                            .shadow(8.dp, CircleShape, ambientColor = Color(0xFFF4A261).copy(alpha = 0.5f), spotColor = Color(0xFFF4A261).copy(alpha = 0.5f))
                    )
                }
            }
        }

        // 救援视频播放覆盖层
        if (state.isPlayingRescueVideo && state.currentPlayingSheepIndex != null) {
            RescueVideoOverlay(
                sheepIndex = state.currentPlayingSheepIndex!!,
                onPlaybackComplete = { sheepIndex ->
                    viewModel.onEvent(ForestEvent.RescueVideoCompleted(sheepIndex))
                }
            )
        }

        // 徽章收集动画覆盖层 - 优化版
        BadgeAnimationOverlayOptimized(
            show = state.showBadgeAnimation,
            sheepIndex = state.earnedBadgeSheepIndex,
            infiniteTransition = infiniteTransition,
            onAnimationComplete = {
                viewModel.onEvent(ForestEvent.BadgeAnimationCompleted)
            }
        )

        // 空闲提示覆盖层 - 优化版
        IdleHintOverlayOptimized(
            show = state.showIdleHint,
            infiniteTransition = infiniteTransition,
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.dismissIdleHint()
            }
        )
    }
}

/**
 * 优化的漂浮树叶和云朵背景效果
 */
@Composable
private fun FloatingLeavesAndClouds(
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition
) {
    val density = LocalDensity.current

    // 树叶飘动动画
    val leafOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_x"
    )

    val leafOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_y"
    )

    val leafRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "leaf_rotation"
    )

    // 云朵飘动动画
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_1"
    )

    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 绘制云朵
        repeat(3) { index ->
            val xBase = when (index) {
                0 -> 0.1f
                1 -> 0.5f
                else -> 0.8f
            }
            val yBase = when (index) {
                0 -> 0.08f
                1 -> 0.12f
                else -> 0.06f
            }
            val offset = when (index) {
                0 -> cloudOffset1
                1 -> cloudOffset2
                else -> cloudOffset1 * 0.7f
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) {
                            val xPos = (xBase * 400 + offset % 100) / 400
                            xPos.dp
                        },
                        y = with(density) {
                            (yBase * 600).dp
                        }
                    )
                    .alpha(0.25f)
            ) {
                Text("☁️", fontSize = (48 + index * 8).sp)
            }
        }

        // 绘制树叶
        val leafPositions = listOf(
            0.15f to 0.2f,
            0.25f to 0.35f,
            0.75f to 0.25f,
            0.85f to 0.4f,
            0.45f to 0.15f
        )

        leafPositions.forEachIndexed { index, (xRatio, yRatio) ->
            val individualOffsetX = leafOffsetX * (1 + index * 0.2f) * if (index % 2 == 0) 1f else -1f
            val individualOffsetY = leafOffsetY * (1 + index * 0.15f)

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (xRatio * 400).dp + individualOffsetX.dp },
                        y = with(density) { (yRatio * 600).dp + individualOffsetY.dp }
                    )
                    .graphicsLayer { rotationZ = leafRotation * (0.5f + index * 0.1f) }
                    .alpha(0.4f)
            ) {
                Text(
                    text = listOf("🍃", "🌿", "🍂")[index % 3],
                    fontSize = (20 + index * 4).sp
                )
            }
        }
    }
}

/**
 * 森林游戏区域优化版（点击交互版本）
 */
@Composable
private fun ForestGameAreaOptimized(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onSheepClick: (Int) -> Unit,
    onFlightComplete: () -> Unit,
    onPlayVideoClick: (Int) -> Unit
) {
    // 使用 BoxWithConstraints 获取实际屏幕尺寸
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        // 小羊位置（屏幕比例）
        val sheepPositions = listOf(
            0.7f to 0.3f,   // 小羊 1 - 右上
            0.75f to 0.65f  // 小羊 2 - 右下
        )

        // 绘制小羊（可点击）
        sheepPositions.forEachIndexed { index, (xRatio, yRatio) ->
            val isRescued = index in state.rescuedSheep
            val isTarget = state.targetSheepIndex == index && state.isHelicopterFlying

            SheepClickableOptimized(
                xRatio = xRatio,
                yRatio = yRatio,
                isRescued = isRescued,
                isTarget = isTarget,
                isFlying = state.isHelicopterFlying,
                sheepIndex = index,
                screenWidth = containerWidthPx,
                screenHeight = containerHeightPx,
                infiniteTransition = infiniteTransition,
                onClick = { onSheepClick(index) }
            )
        }

        // 直升机（自动飞行动画）- 优化版
        HelicopterAnimatedOptimized(
            state = state,
            infiniteTransition = infiniteTransition,
            screenWidth = containerWidthPx,
            screenHeight = containerHeightPx,
            onFlightComplete = onFlightComplete
        )

        // "播放视频"按钮（当直升机到达目标后显示）- 优化版
        if (state.showPlayVideoButton && state.targetSheepIndex != null) {
            val sheepIndex = state.targetSheepIndex
            PlayVideoButtonOptimized(
                state = state,
                infiniteTransition = infiniteTransition,
                screenWidth = containerWidthPx,
                screenHeight = containerHeightPx,
                onClick = { onPlayVideoClick(sheepIndex) }
            )
        }
    }
}

/**
 * 优化的可点击小羊组件 - 带增强动画和触觉反馈
 */
@Composable
private fun SheepClickableOptimized(
    xRatio: Float,
    yRatio: Float,
    isRescued: Boolean,
    isTarget: Boolean,
    isFlying: Boolean,
    sheepIndex: Int,
    screenWidth: Float,
    screenHeight: Float,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // 交互源（用于检测按下状态）
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下时的缩放动画 - 增强反馈
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.82f  // 按下时：更明显的缩小
            isTarget -> 1.18f  // 飞行目标：更明显的放大
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "sheep_scale_optimized"
    )

    // 悬浮动画（呼吸效果）- 更平滑
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset_optimized"
    )

    // 求救摇晃动画（未救援时）- 增强
    val shakeAngle by infiniteTransition.animateFloat(
        initialValue = if (isRescued) 0f else -6f,
        targetValue = if (isRescued) 0f else 6f,
        animationSpec = infiniteRepeatable(
            animation = if (isRescued) tween(1) else tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_angle_optimized"
    )

    // 计算屏幕位置（像素）
    val sheepSize = 110.dp
    val sheepSizePx = with(LocalDensity.current) { sheepSize.toPx() }
    val xPosPx = (xRatio * screenWidth) - sheepSizePx / 2
    val finalYPos = if (!isRescued) {
        (yRatio * screenHeight) - sheepSizePx / 2 + floatOffset
    } else {
        (yRatio * screenHeight) - sheepSizePx / 2
    }

    // 光晕脉冲效果（目标小羊）- 增强
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_optimized"
    )

    // 救援成功的庆祝动画 - 新增
    val rescueCelebrationScale by animateFloatAsState(
        targetValue = if (isRescued) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 400f
        ),
        label = "rescue_celebration"
    )

    Box(
        modifier = Modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    x = xPosPx.toInt(),
                    y = finalYPos.toInt()
                )
            }
            .size(sheepSize)
            .scale(scale * rescueCelebrationScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!isFlying) {
                        coroutineScope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClick()
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // 目标小羊的增强光晕效果 - 多层
        if (isTarget) {
            repeat(2) { layer ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(
                            x = (layer * 8).dp,
                            y = (layer * 8).dp
                        )
                        .alpha(pulseAlpha * (1f - layer * 0.3f))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Yellow.copy(alpha = pulseAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }

        // 火苗效果（未救援的小羊周围）- 增强
        if (!isRescued) {
            val fireAlpha by infiniteTransition.animateFloat(
                initialValue = 0.55f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(180, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "fire_alpha_optimized"
            )

            // 火苗位置（环绕小羊）- 更多火苗
            val fireOffsets = listOf(
                -55.dp to -45.dp,
                55.dp to -35.dp,
                -45.dp to 50.dp,
                50.dp to 45.dp,
                -30.dp to -55.dp,  // 新增
                35.dp to -50.dp,   // 新增
            )

            fireOffsets.forEach { ( xOffset, yOffset ) ->
                val fireScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "fire_scale_${xOffset.value}_${yOffset.value}"
                )

                Box(
                    modifier = Modifier
                        .offset(xOffset, yOffset)
                        .size(28.dp)
                        .scale(fireScale)
                        .graphicsLayer {
                            rotationZ = shakeAngle
                            alpha = fireAlpha
                        }
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 22.sp
                    )
                }
            }
        }

        // 小羊本体 - 增强
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = shakeAngle
                }
                .shadow(
                    elevation = if (isTarget) 24.dp else 10.dp,
                    shape = CircleShape,
                    spotColor = if (isTarget) Color.Yellow else Color.Transparent
                )
        ) {
            Text(
                text = "🐑",
                fontSize = 65.sp,
                color = Color.White
            )
        }

        // 救援成功的标记 - 增强动画
        if (isRescued) {
            val checkmarkScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "checkmark_pulse"
            )

            val sparkleRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sparkle_rotation"
            )

            Box(
                modifier = Modifier
                    .offset(y = (-50).dp)
                    .size(45.dp)
                    .scale(checkmarkScale)
                    .background(
                        Color(0xFF2A9D8F),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 添加闪光效果 - 新增
            repeat(4) { index ->
                val angle = (index * 90f + sparkleRotation) * (Math.PI / 180).toFloat()
                val radius = 40.dp
                Box(
                    modifier = Modifier
                        .offset(
                            x = (kotlin.math.cos(angle) * radius.value).dp,
                            y = (kotlin.math.sin(angle) * radius.value - 50).dp
                        )
                        .size(12.dp)
                        .alpha(0.8f)
                ) {
                    Text("✨", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 优化的自动飞行直升机组件 - 带增强动画
 */
@Composable
private fun HelicopterAnimatedOptimized(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    screenWidth: Float,
    screenHeight: Float,
    onFlightComplete: () -> Unit
) {
    // 当前显示的位置
    val displayX = state.targetHelicopterX ?: state.helicopterX
    val displayY = state.targetHelicopterY ?: state.helicopterY

    // 飞行动画 - 更平滑的缓动
    val animatedX by animateFloatAsState(
        targetValue = displayX,
        animationSpec = if (state.isHelicopterFlying) {
            tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        } else {
            spring()
        },
        label = "helicopter_x_optimized",
        finishedListener = {
            if (state.isHelicopterFlying) {
                onFlightComplete()
            }
        }
    )

    val animatedY by animateFloatAsState(
        targetValue = displayY,
        animationSpec = if (state.isHelicopterFlying) {
            tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        } else {
            spring()
        },
        label = "helicopter_y_optimized"
    )

    // 直升机尺寸
    val helicopterSize = 90.dp
    val helicopterSizePx = with(LocalDensity.current) { helicopterSize.toPx() }

    // 计算屏幕位置
    val xPosPx = (animatedX * screenWidth) - helicopterSizePx / 2
    val yPosPx = (animatedY * screenHeight) - helicopterSizePx / 2

    // 螺旋桨旋转动画 - 更快更流畅
    val propellerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "propeller_rotation_optimized"
    )

    // 悬浮动画（待机时）- 更平滑
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_offset_optimized"
    )

    // 飞行时的倾斜角度 - 增强反馈
    val targetTilt = if (state.isHelicopterFlying) {
        val deltaX = (state.targetHelicopterX ?: state.helicopterX) - state.helicopterX
        when {
            deltaX > 0.1f -> 18f  // 向右飞，增加倾斜角度
            deltaX < -0.1f -> -18f  // 向左飞，增加倾斜角度
            else -> 0f
        }
    } else {
        0f
    }
    val tiltAngle by animateFloatAsState(
        targetValue = targetTilt,
        animationSpec = tween(250),
        label = "tilt_angle_optimized"
    )

    // 计算最终位置（加上悬浮效果）
    val finalYPos = if (!state.isHelicopterFlying) {
        yPosPx + hoverOffset
    } else {
        yPosPx
    }

    // 飞行时的尾迹效果 - 增强
    val trailAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trail_alpha_optimized"
    )

    Box(
        modifier = Modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    x = xPosPx.toInt(),
                    y = finalYPos.toInt()
                )
            }
            .size(helicopterSize)
            .graphicsLayer {
                rotationZ = tiltAngle
            }
            .shadow(
                elevation = if (state.isHelicopterFlying) 24.dp else 14.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                spotColor = Color(0xFF4DABF7),
                ambientColor = Color(0xFF74C0FC)
            )
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF74C0FC).copy(alpha = 0.98f),
                        Color(0xFF339AF0).copy(alpha = 0.95f),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 飞行时的增强尾迹效果 - 更多粒子
        if (state.isHelicopterFlying) {
            repeat(6) { i ->
                val trailOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(300 + i * 50, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "trail_offset_$i"
                )

                Box(
                    modifier = Modifier
                        .offset(x = (-65 - i * 18).dp, y = trailOffset.dp)
                        .size((14 - i * 2).dp)
                        .alpha(trailAlpha * (1f - i * 0.15f))
                        .background(
                            Color.White.copy(alpha = 0.8f),
                            CircleShape
                        )
                )
            }
        }

        // 直升机主体 - 增强缩放反馈
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = propellerRotation * 0.1f
                    val scaleFactor = if (state.isHelicopterFlying) 1.15f else 1f
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
        ) {
            Text(
                text = "🚁",
                fontSize = 50.sp
            )
        }

        // 飞行时的增强光晕效果 - 多层
        if (state.isHelicopterFlying) {
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )

            repeat(2) { layer ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(
                            x = (layer * 4).dp,
                            y = (layer * 4).dp
                        )
                        .alpha(glowAlpha * (1f - layer * 0.4f))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                )
            }
        }
    }
}

/**
 * 优化的播放视频按钮组件 - 带增强动画
 */
@Composable
private fun PlayVideoButtonOptimized(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    screenWidth: Float,
    screenHeight: Float,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // 交互源
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下时的缩放 - 更明显
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(),
        label = "button_scale_optimized"
    )

    // 增强的脉冲动画 - 更吸引注意
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale_optimized"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_optimized"
    )

    // 闪光效果 - 新增
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // 计算按钮位置（在直升机下方）
    val buttonSize = 60.dp
    val buttonSizePx = with(LocalDensity.current) { buttonSize.toPx() }
    val yOffsetPx = with(LocalDensity.current) { 55.dp.toPx() }
    val xPosPx = (state.helicopterX * screenWidth) - buttonSizePx / 2
    val yPosPx = (state.helicopterY * screenHeight) + yOffsetPx

    Box(
        modifier = Modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    x = xPosPx.toInt(),
                    y = yPosPx.toInt()
                )
            }
            .size(buttonSize)
            .scale(scale * pulseScale)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                spotColor = Color(0xFFFFE066),
                ambientColor = Color(0xFFFFD93D)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD93D),
                        Color(0xFFFF922B),
                    )
                )
            )
            .drawBehind {
                // 闪光效果
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        startX = shimmerOffset - 500f,
                        endX = shimmerOffset + 500f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width / 2)
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // 外层增强光晕
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = pulseAlpha * 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 救援图标和文字
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_scale"
            )

            Text(
                text = "▶️",
                fontSize = 18.sp,
                modifier = Modifier.scale(iconScale)
            )
            Text(
                text = "救援",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 救援视频播放覆盖层（保持原有实现）
 */
@Composable
private fun RescueVideoOverlay(
    sheepIndex: Int,
    onPlaybackComplete: (Int) -> Unit
) {
    val videoPath = when (sheepIndex) {
        0 -> "videos/rescue_sheep_1.mp4"
        1 -> "videos/rescue_sheep_2.mp4"
        else -> "videos/rescue_sheep_1.mp4"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // 视频播放器
        VideoPlayer(
            videoPath = videoPath,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(32.dp),
            onPlaybackCompleted = {
                onPlaybackComplete(sheepIndex)
            },
            autoPlay = true,
            showControls = false
        )
    }
}

/**
 * 徽章收集动画覆盖层优化版（含庆祝动画）
 */
@Composable
private fun BadgeAnimationOverlayOptimized(
    show: Boolean,
    sheepIndex: Int?,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onAnimationComplete: () -> Unit
) {
    // 徽章动画显示后自动消失
    LaunchedEffect(show) {
        if (show) {
            kotlinx.coroutines.delay(3000)
            onAnimationComplete()
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onAnimationComplete),
            contentAlignment = Alignment.Center
        ) {
            // 增强的庆祝动画效果（烟花粒子）
            val particleRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(12000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particle_rotation_optimized"
            )

            // 烟花粒子背景 - 更多粒子
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {}
            ) {
                val centerX = size.center.x
                val centerY = size.center.y
                val colors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFFFFA07A),
                    Color(0xFF98D8C8),
                    Color(0xFFF7DC6F),
                    Color(0xFFDDA0DD),  // 新增
                    Color(0xFF87CEEB),  // 新增
                )

                // 绘制更多烟花粒子 - 从12个增加到20个
                repeat(20) { i ->
                    val angle = Math.toRadians((particleRotation + i * 18f).toDouble())
                    val distance = 240f + kotlin.math.sin(Math.toRadians((particleRotation * 2 + i * 36f).toDouble())).toFloat() * 80f
                    val x = centerX + kotlin.math.cos(angle).toFloat() * distance
                    val y = centerY + kotlin.math.sin(angle).toFloat() * distance
                    val color = colors[i % colors.size]

                    val pulseRadius = 12.dp.toPx() * (1 + kotlin.math.sin(Math.toRadians((particleRotation * 3 + i * 20f).toDouble())).toFloat() * 0.5f)
                    drawCircle(
                        color = color.copy(alpha = 0.8f),
                        radius = pulseRadius,
                        center = Offset(x = x, y = y)
                    )

                    // 星星闪烁 - 增加
                    val starAngle = Math.toRadians((particleRotation * 1.8f + i * 72f).toDouble())
                    val starX = centerX + kotlin.math.cos(starAngle).toFloat() * (distance + 120f)
                    val starY = centerY + kotlin.math.sin(starAngle).toFloat() * (distance + 120f)
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.95f),
                        radius = 8.dp.toPx(),
                        center = Offset(x = starX, y = starY)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 增强的徽章旋转缩放动画
                val badgeScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "badge_scale_optimized"
                )

                // 小羊（浮动动画）- 增强
                val sheepFloat by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sheep_float_optimized"
                )

                val sheepRotation by infiniteTransition.animateFloat(
                    initialValue = -3f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sheep_rotation"
                )

                // 小羊带旋转效果
                Box(
                    modifier = Modifier
                        .offset(y = sheepFloat.dp)
                        .graphicsLayer { rotationZ = sheepRotation }
                ) {
                    Text(
                        text = "🐑",
                        fontSize = 90.sp,
                        modifier = Modifier.shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color.Yellow
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 徽章带发光效果
                Box(modifier = Modifier.scale(badgeScale)) {
                    val badgeGlowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "badge_glow"
                    )

                    Text(
                        text = "🏅",
                        fontSize = 110.sp,
                        modifier = Modifier
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFFF4A261),
                                spotColor = Color(0xFFF4A261)
                            )
                            .drawBehind {
                                // 发光效果
                                drawCircle(
                                    color = Color(0xFFFFD700).copy(alpha = badgeGlowAlpha),
                                    radius = size.minDimension / 2 + 20.dp.toPx()
                                )
                            }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // 赞美文字 - 带脉冲效果
                val praiseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "praise_scale"
                )

                Text(
                    text = "你真棒！",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .scale(praiseScale)
                        .shadow(12.dp, CircleShape, ambientColor = Color.Yellow, spotColor = Color.Yellow)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "成功救出第 ${sheepIndex?.plus(1)} 只小羊！",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(56.dp))

                // 确认按钮 - 带增强动画
                var buttonScale by remember { mutableStateOf(1f) }
                val haptic = LocalHapticFeedback.current

                androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                Box(
                    modifier = Modifier
                        .scale(buttonScale)
                        .shadow(
                            elevation = 20.dp,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                            spotColor = Color(0xFF2A9D8F)
                        )
                        .background(
                            Color(0xFF2A9D8F),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                        )
                        .drawBehind {
                            // 按钮发光效果
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                            )
                        }
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            buttonScale = 0.95f
                            onAnimationComplete()
                        }
                ) {
                    Text(
                        text = "确认 ▶",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                LaunchedEffect(buttonScale) {
                    if (buttonScale != 1f) {
                        kotlinx.coroutines.delay(100)
                        buttonScale = 1f
                    }
                }
            }
        }
    }
}

/**
 * 空闲提示覆盖层优化版
 */
@Composable
private fun IdleHintOverlayOptimized(
    show: Boolean,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onDismiss: () -> Unit
) {
    if (!show) return

    // 增强的脉冲动画
    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintScale_optimized"
    )

    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha_optimized"
    )

    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(hintScale)
                .alpha(hintAlpha)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.4f),
                    ambientColor = Color.Black.copy(alpha = 0.3f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A9D8F).copy(alpha = 0.98f),
                            Color(0xFF3CB9A3).copy(alpha = 0.98f)
                        )
                    ),
                    RoundedCornerShape(32.dp)
                )
                .drawBehind {
                    // 边框发光效果
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                    )
                }
                .padding(horizontal = 56.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 小火头像带旋转动画
                Text(
                    text = "🐯",
                    fontSize = 80.sp,
                    modifier = Modifier
                        .scale(hintScale)
                        .graphicsLayer { rotationZ = iconRotation }
                )

                // 提示文字
                Text(
                    text = "需要帮忙吗？",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "点击屏幕任意位置继续",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // 装饰星星带闪烁动画
                val starAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "star_alpha"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(starAlpha)
                ) {
                    repeat(3) { index ->
                        val starScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500 + index * 100, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "star_scale_$index"
                        )

                        Text(
                            text = "⭐",
                            fontSize = 24.sp,
                            modifier = Modifier.scale(starScale)
                        )
                    }
                }
            }
        }
    }
}
