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
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.forest.ForestEffect
import com.cryallen.tigerfire.presentation.forest.ForestEvent
import com.cryallen.tigerfire.presentation.forest.ForestViewModel
import kotlinx.coroutines.launch

/**
 * 森林场景 Screen（点击交互版本）
 *
 * 交互方式：点击小羊 → 直升机自动飞行 → 显示救援按钮 → 观看视频
 * 适合3-6岁儿童：大触控目标、明亮色彩、即时反馈、清晰引导
 *
 * @param viewModel ForestViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun ForestScreen(
    viewModel: ForestViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val coroutineScope = rememberCoroutineScope()

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ForestEffect.PlayRescueVideo -> {
                    // 视频播放由状态驱动，不需要额外处理
                }
                is ForestEffect.ShowBadgeAnimation -> {
                    // 徽章动画在 showBadgeAnimation 状态中处理
                }
                is ForestEffect.ShowCompletionHint -> {
                    // 完成提示由状态驱动
                }
                is ForestEffect.PlayClickSound -> {
                    audioManager.playClickSound(com.cryallen.tigerfire.domain.model.SceneType.FOREST)
                }
                is ForestEffect.PlayFlyingSound -> {
                    // 播放直升机飞行动画音效
                    audioManager.playVoice("audio/sfx_flying.mp3")
                }
                is ForestEffect.PlayBadgeSound -> {
                    audioManager.playBadgeSound()
                }
                is ForestEffect.PlayAllCompletedSound -> {
                    audioManager.playAllCompletedSound()
                }
                is ForestEffect.NavigateToMap -> onNavigateBack()
                is ForestEffect.PlaySlowDownVoice -> {
                    audioManager.playVoice("audio/voices/slow_down.mp3")
                }
                is ForestEffect.ShowIdleHint -> {
                    // 显示空闲提示：小火"需要帮忙吗？"
                    // TODO: 实现 UI 提示显示逻辑
                    audioManager.playVoice("audio/voices/hint_ idle.mp3")
                }
                is ForestEffect.PlayStartVoice -> {
                    // 播放开始语音："小羊被困啦！快开直升机救它们！"
                    audioManager.playVoice("audio/voices/forest_start.mp3")
                }
                is ForestEffect.PlayCompleteVoice -> {
                    // 播放完成语音："直升机能从天上救人，真厉害！"
                    audioManager.playVoice("audio/voices/forest_complete.mp3")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 优化的森林火灾背景
        ForestFireBackgroundEnhanced()

        // 游戏区域（小羊和直升机）- 全屏显示
        ForestGameArea(
            state = state,
            onSheepClick = { sheepIndex ->
                viewModel.onEvent(ForestEvent.SheepClicked(sheepIndex))
            },
            onFlightComplete = {
                viewModel.onEvent(ForestEvent.HelicopterFlightCompleted)
            },
            onPlayVideoClick = { sheepIndex ->
                viewModel.onEvent(ForestEvent.PlayVideoClicked(sheepIndex))
            }
        )

        // 顶部信息栏（绝对定位）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 顶部行：返回按钮 + 场景标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                IconButton(
                    onClick = {
                        viewModel.onEvent(ForestEvent.BackToMapClicked)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Text(
                        text = "←",
                        fontSize = 28.sp,
                        color = Color.Black
                    )
                }

                // 场景标题
                Text(
                    text = "🌲 森林救援",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.drawBehind {
                        drawRoundRect(
                            color = Color(0xFF2A9D8F),
                            style = Stroke(width = 4.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )
                    }
                )

                // 进度徽章（大而醒目）- 更卡通化的配色
                Box(
                    modifier = Modifier
                        .size(72.dp)
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

            // 提示文字（大而清晰）
            Text(
                text = "点击小羊救援它们！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f))
            )

            // 全部完成提示（更醒目）
            if (state.isAllCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎉 太棒了！全部救援完成！",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .shadow(8.dp, CircleShape, ambientColor = Color(0xFFF4A261).copy(alpha = 0.5f), spotColor = Color(0xFFF4A261).copy(alpha = 0.5f))
                )
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

        // 徽章收集动画覆盖层
        BadgeAnimationOverlay(
            show = state.showBadgeAnimation,
            sheepIndex = state.earnedBadgeSheepIndex,
            onAnimationComplete = {
                viewModel.onEvent(ForestEvent.BadgeAnimationCompleted)
            }
        )
    }
}

/**
 * 森林游戏区域（点击交互版本）
 *
 * 包含小羊（可点击）、直升机（自动飞行）和播放视频按钮
 */
@Composable
private fun ForestGameArea(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
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

            SheepClickable(
                xRatio = xRatio,
                yRatio = yRatio,
                isRescued = isRescued,
                isTarget = isTarget,
                isFlying = state.isHelicopterFlying,
                sheepIndex = index,
                screenWidth = containerWidthPx,
                screenHeight = containerHeightPx,
                onClick = { onSheepClick(index) }
            )
        }

        // 直升机（自动飞行动画）
        HelicopterAnimated(
            state = state,
            screenWidth = containerWidthPx,
            screenHeight = containerHeightPx,
            onFlightComplete = onFlightComplete
        )

        // "播放视频"按钮（当直升机到达目标后显示）
        if (state.showPlayVideoButton && state.targetSheepIndex != null) {
            val sheepIndex = state.targetSheepIndex
            PlayVideoButton(
                state = state,
                screenWidth = containerWidthPx,
                screenHeight = containerHeightPx,
                onClick = { onPlayVideoClick(sheepIndex) }
            )
        }
    }
}

/**
 * 可点击的小羊组件
 *
 * 设计要点：
 * - 超大触控区域（≥120pt）
 * - 悬浮动画吸引注意
 * - 火苗环绕营造紧迫感
 * - 点击缩放反馈
 */
@Composable
private fun SheepClickable(
    xRatio: Float,
    yRatio: Float,
    isRescued: Boolean,
    isTarget: Boolean,
    isFlying: Boolean,
    sheepIndex: Int,
    screenWidth: Float,
    screenHeight: Float,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 交互源（用于检测按下状态）
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下时的缩放动画
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.85f  // 按下时：缩小
            isTarget -> 1.15f  // 飞行目标：放大
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "sheep_scale"
    )

    // 悬浮动画（呼吸效果）
    val infiniteTransition = rememberInfiniteTransition(label = "sheep_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // 求救摇晃动画（未救援时）
    val shakeTransition = rememberInfiniteTransition(label = "sheep_shake")
    val shakeAngle by shakeTransition.animateFloat(
        initialValue = if (isRescued) 0f else -5f,
        targetValue = if (isRescued) 0f else 5f,
        animationSpec = infiniteRepeatable(
            animation = if (isRescued) tween(1) else tween(300, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "shake_angle"
    )

    // 计算屏幕位置（像素）- 缩小小羊尺寸以匹配缩小的直升机
    val sheepSize = 110.dp
    val sheepSizePx = with(LocalDensity.current) { sheepSize.toPx() }
    val xPosPx = (xRatio * screenWidth) - sheepSizePx / 2
    val finalYPos = if (!isRescued) {
        (yRatio * screenHeight) - sheepSizePx / 2 + floatOffset
    } else {
        (yRatio * screenHeight) - sheepSizePx / 2
    }

    // 光晕脉冲效果（目标小羊）
    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_alpha"
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
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    // 允许重复点击观看，只要直升机不在飞行中
                    if (!isFlying) {
                        coroutineScope.launch {
                            // 播放点击反馈动画
                            onClick()
                        }
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // 目标小羊的光晕效果
        if (isTarget) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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

        // 火苗效果（未救援的小羊周围）
        if (!isRescued) {
            val fireTransition = rememberInfiniteTransition(label = "fire_flicker")
            val fireAlpha by fireTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(200, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "fire_alpha"
            )

            // 火苗位置（环绕小羊）
            val fireOffsets = listOf(
                -50.dp to -40.dp,
                50.dp to -30.dp,
                -40.dp to 45.dp,
                45.dp to 40.dp
            )

            fireOffsets.forEach { ( xOffset, yOffset ) ->
                Box(
                    modifier = Modifier
                        .offset(xOffset, yOffset)
                        .size(28.dp)  // 略微缩小火苗以匹配更小的小羊
                        .graphicsLayer {
                            rotationZ = shakeAngle
                            alpha = fireAlpha
                        }
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 22.sp  // 调整字体大小以匹配新的容器尺寸
                    )
                }
            }
        }

        // 小羊本体
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = shakeAngle
                }
                .shadow(
                    elevation = if (isTarget) 20.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = if (isTarget) Color.Yellow else Color.Transparent
                )
        ) {
            Text(
                text = "🐑",
                fontSize = 65.sp,  // 调整字体大小以匹配新的容器尺寸
                color = Color.White  // 保持正常颜色，允许重复观看
            )
        }

        // 救援成功的标记
        if (isRescued) {
            Box(
                modifier = Modifier
                    .offset(y = (-50).dp)
                    .size(45.dp)
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
        }
    }
}

/**
 * 自动飞行的直升机组件
 *
 * 设计要点：
 * - 超大尺寸（≥150pt）
 * - 平滑飞行动画（1-1.5秒）
 * - 螺旋桨旋转动画
 * - 飞行轨迹效果
 */
@Composable
private fun HelicopterAnimated(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
    screenWidth: Float,
    screenHeight: Float,
    onFlightComplete: () -> Unit
) {
    // 当前显示的位置
    val displayX = state.targetHelicopterX ?: state.helicopterX
    val displayY = state.targetHelicopterY ?: state.helicopterY

    // 飞行动画
    val animatedX by animateFloatAsState(
        targetValue = displayX,
        animationSpec = if (state.isHelicopterFlying) {
            tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            )
        } else {
            spring()
        },
        label = "helicopter_x",
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
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            )
        } else {
            spring()
        },
        label = "helicopter_y"
    )

    // 直升机尺寸 - 缩小到1/2以获得更好的平衡 (原180.dp -> 90.dp)
    val helicopterSize = 90.dp
    val helicopterSizePx = with(LocalDensity.current) { helicopterSize.toPx() }

    // 计算屏幕位置
    val xPosPx = (animatedX * screenWidth) - helicopterSizePx / 2
    val yPosPx = (animatedY * screenHeight) - helicopterSizePx / 2

    // 螺旋桨旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "helicopter_anim")
    val propellerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "propeller_rotation"
    )

    // 悬浮动画（待机时）
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "hover_offset"
    )

    // 飞行时的倾斜角度
    val targetTilt = if (state.isHelicopterFlying) {
        // 根据飞行方向计算倾斜角度
        val deltaX = (state.targetHelicopterX ?: state.helicopterX) - state.helicopterX
        when {
            deltaX > 0.1f -> 15f  // 向右飞，向右倾斜
            deltaX < -0.1f -> -15f  // 向左飞，向左倾斜
            else -> 0f
        }
    } else {
        0f
    }
    val tiltAngle by animateFloatAsState(
        targetValue = targetTilt,
        animationSpec = tween(300),
        label = "tilt_angle"
    )

    // 计算最终位置（加上悬浮效果）
    val finalYPos = if (!state.isHelicopterFlying) {
        yPosPx + hoverOffset
    } else {
        yPosPx
    }

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
                elevation = if (state.isHelicopterFlying) 20.dp else 12.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                spotColor = Color(0xFF4DABF7),  // 蓝色阴影 - 更卡通化
                ambientColor = Color(0xFF74C0FC)
            )
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF74C0FC).copy(alpha = 0.95f),  // 明亮天蓝
                        Color(0xFF339AF0).copy(alpha = 0.9f),   // 深蓝色
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 飞行时的尾迹效果
        if (state.isHelicopterFlying) {
            val trailTransition = rememberInfiniteTransition(label = "trail")
            val trailAlpha by trailTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "trail_alpha"
            )

            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .offset(x = (-60 - i * 15).dp, y = 0.dp)
                        .size((12 - i * 2).dp)
                        .alpha(trailAlpha * (1f - i * 0.2f))
                        .background(
                            Color.White.copy(alpha = 0.7f),
                            CircleShape
                        )
                )
            }
        }

        // 直升机主体
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = propellerRotation * 0.1f
                    scaleX = if (state.isHelicopterFlying) 1.1f else 1f
                    scaleY = if (state.isHelicopterFlying) 1.1f else 1f
                }
        ) {
            Text(
                text = "🚁",
                fontSize = 50.sp  // 缩小以匹配新的容器尺寸
            )
        }

        // 飞行时的光晕效果
        if (state.isHelicopterFlying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
            )
        }
    }
}

/**
 * 播放视频按钮组件
 *
 * 设计要点：
 * - 超大圆形按钮（≥100pt）
 * - 醒目的渐变色彩
 * - 脉冲动画吸引注意
 * - 播放图标清晰
 */
@Composable
private fun PlayVideoButton(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
    screenWidth: Float,
    screenHeight: Float,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 交互源
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下时的缩放
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(),
        label = "button_scale"
    )

    // 脉冲动画 - 优化为更柔和、更适合儿童的呼吸效果
    val infiniteTransition = rememberInfiniteTransition(label = "button_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,  // 缩小缩放幅度，更温和 (原1.2f -> 1.1f)
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),  // 放慢动画速度，更柔和 (原600ms -> 1000ms)
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),  // 同步速度
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // 计算按钮位置（在直升机下方）- 缩小到1/2以获得更好的视觉平衡 (原120.dp -> 60.dp)
    val buttonSize = 60.dp
    val buttonSizePx = with(LocalDensity.current) { buttonSize.toPx() }
    // 调整间距：由于按钮缩小，间距也需要相应调整以保持视觉平衡
    val yOffsetPx = with(LocalDensity.current) { 55.dp.toPx() }  // 原80.dp -> 55.dp
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
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = Color(0xFFFFE066),  // 明亮黄色阴影
                ambientColor = Color(0xFFFFD93D)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD93D),  // 明亮黄色
                        Color(0xFFFF922B),  // 橙色
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = pulseAlpha * 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 救援图标和文字 - 调整字体大小以匹配新的容器尺寸
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "▶️",
                fontSize = 18.sp  // 缩小以匹配新的按钮尺寸 (原36.sp -> 18.sp)
            )
            Text(
                text = "救援",
                fontSize = 11.sp,  // 缩小以匹配新的按钮尺寸 (原18.sp -> 11.sp)
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 救援视频播放覆盖层
 *
 * @param sheepIndex 小羊索引
 * @param onPlaybackComplete 播放完成回调
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
 * 徽章收集动画覆盖层（含庆祝动画）
 *
 * @param show 是否显示
 * @param sheepIndex 获得徽章的小羊索引
 * @param onAnimationComplete 动画完成回调
 */
@Composable
private fun BadgeAnimationOverlay(
    show: Boolean,
    sheepIndex: Int?,
    onAnimationComplete: () -> Unit
) {
    // 徽章动画显示后自动消失
    LaunchedEffect(show) {
        if (show) {
            kotlinx.coroutines.delay(3000) // 显示3秒后自动消失
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
            // 庆祝动画效果（烟花粒子）
            val infiniteTransition = rememberInfiniteTransition(label = "celebration")
            val particleRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                ),
                label = "particle_rotation"
            )

            // 烟花粒子背景
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // 不拦截点击事件，让点击穿透到父元素
                    }
            ) {
                val centerX = size.center.x
                val centerY = size.center.y
                val colors = listOf(
                    Color(0xFFFFD700), // 金色
                    Color(0xFFFF6B6B), // 红色
                    Color(0xFF4ECDC4), // 青色
                    Color(0xFFFFA07A), // 橙色
                    Color(0xFF98D8C8), // 薄荷绿
                    Color(0xFFF7DC6F), // 黄色
                )

                // 绘制烟花粒子
                repeat(12) { i ->
                    val angle = Math.toRadians((particleRotation + i * 30f).toDouble())
                    val distance = 220f + kotlin.math.sin(Math.toRadians((particleRotation * 2 + i * 45f).toDouble())).toFloat() * 60f
                    val x = centerX + kotlin.math.cos(angle).toFloat() * distance
                    val y = centerY + kotlin.math.sin(angle).toFloat() * distance
                    val color = colors[i % colors.size]

                    drawCircle(
                        color = color.copy(alpha = 0.7f),
                        radius = 10.dp.toPx() * (1 + kotlin.math.sin(Math.toRadians((particleRotation * 3).toDouble())).toFloat() * 0.4f),
                        center = Offset(x = x, y = y)
                    )

                    // 星星闪烁
                    val starAngle = Math.toRadians((particleRotation * 1.5f + i * 60f).toDouble())
                    val starX = centerX + kotlin.math.cos(starAngle).toFloat() * (distance + 100f)
                    val starY = centerY + kotlin.math.sin(starAngle).toFloat() * (distance + 100f)
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.9f),
                        radius = 6.dp.toPx(),
                        center = Offset(x = starX, y = starY)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 徽章旋转缩放动画
                val badgeScale by rememberInfiniteTransition(label = "badge_scale").animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "badge_scale"
                )

                // 小羊（浮动动画）
                val sheepFloat by rememberInfiniteTransition(label = "sheep_float").animateFloat(
                    initialValue = 0f,
                    targetValue = -12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = FastOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "sheep_float"
                )

                // 小羊
                Box(modifier = Modifier.offset(y = sheepFloat.dp)) {
                    Text(
                        text = "🐑",
                        fontSize = 90.sp,
                        modifier = Modifier.shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            spotColor = Color.Yellow
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 徽章
                Box(modifier = Modifier.scale(badgeScale)) {
                    Text(
                        text = "🏅",
                        fontSize = 110.sp,
                        modifier = Modifier.shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFF4A261),
                            spotColor = Color(0xFFF4A261)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // 赞美文字
                Text(
                    text = "你真棒！",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.shadow(10.dp, CircleShape, ambientColor = Color.Yellow, spotColor = Color.Yellow)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "成功救出第 ${sheepIndex?.plus(1)} 只小羊！",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(56.dp))

                // 确认按钮
                androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 16.dp,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                            spotColor = Color(0xFF2A9D8F)
                        )
                        .background(
                            Color(0xFF2A9D8F),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                        .clickable(onClick = onAnimationComplete)
                ) {
                    Text(
                        text = "确认 ▶",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 优化的森林火灾背景组件
 *
 * 增强视觉效果：
 * - 更丰富的色彩层次
 * - 动态火焰效果
 * - 飘动的烟雾
 * - 森林树木剪影
 */
@Composable
private fun ForestFireBackgroundEnhanced() {
    val infiniteTransition = rememberInfiniteTransition(label = "fire_animation")

    // 烟雾飘动动画
    val smokeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "smoke_offset"
    )

    // 火焰闪烁动画
    val fireAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "fire_alpha"
    )

    // 火焰上升动画
    val fireRise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "fire_rise"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF87CEEB),  // 天蓝色 - 更明亮的天空
                        Color(0xFF98D8C8),  // 薄荷绿 - 卡通感
                        Color(0xFF52B788),  // 清新绿
                        Color(0xFF40916C),  // 森林绿
                        Color(0xFF2D6A4F),  // 深绿
                        Color(0xFF1B4332),  // 最深绿
                    )
                )
            )
    ) {
        // 绘制火焰和烟雾效果
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 火焰颜色渐变 - 更卡通化的明亮色彩
            val fireColors = listOf(
                Color(0xFFFF6B6B).copy(alpha = fireAlpha),        // 明亮橙红
                Color(0xFFFFA07A).copy(alpha = fireAlpha * 0.9f), // 柔和橙色
                Color(0xFFFFD93D).copy(alpha = fireAlpha * 0.8f), // 明亮黄色
                Color(0xFFFFEE52).copy(alpha = fireAlpha * 0.6f), // 金黄色
            )

            // 绘制多层火焰（从底部升起）
            val flameHeight = canvasHeight * 0.35f
            val flameBaseY = canvasHeight

            fireColors.forEachIndexed { index, color ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(
                        x = 0f,
                        y = flameBaseY - flameHeight - (fireRise * (index + 1) / 4f) - (index * 25f)
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = canvasWidth,
                        height = flameHeight + (index * 35f)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(120f)
                )
            }

            // 绘制烟雾粒子
            val smokeCount = 12
            repeat(smokeCount) { index ->
                val angle = (index.toFloat() / smokeCount) * 2 * kotlin.math.PI.toFloat()
                val smokeX = canvasWidth * 0.5f + kotlin.math.sin(angle + smokeOffset * 0.01f) * canvasWidth * 0.35f
                val smokeY = canvasHeight * 0.65f + kotlin.math.sin(angle * 2 + smokeOffset * 0.02f) * 120f - (smokeOffset % 250f)

                // 烟雾透明度变化
                val smokeAlpha = 0.08f + kotlin.math.sin(angle * 3 + smokeOffset * 0.015f).toFloat() * 0.04f

                drawCircle(
                    color = Color.White.copy(alpha = smokeAlpha.coerceIn(0f, 0.15f)),
                    radius = 70f * (1 + (index % 3) * 0.4f),
                    center = Offset(x = smokeX, y = smokeY)
                )
            }

            // 绘制火星粒子
            val sparkCount = 20
            repeat(sparkCount) { index ->
                val sparkAngle = smokeOffset * 0.02f + index * 0.3f
                val sparkDistance = (smokeOffset * 0.5f + index * 50f) % (canvasHeight * 0.4f)
                val sparkX = canvasWidth * (0.1f + index * 0.04f)
                val sparkY = canvasHeight - sparkDistance

                val sparkAlpha = ((kotlin.math.sin(sparkAngle * 2) + 1) * 0.5f * 0.6f).toFloat()

                drawCircle(
                    color = Color(0xFFFFCC00).copy(alpha = sparkAlpha),
                    radius = 4.dp.toPx() + kotlin.math.sin(sparkAngle * 3).toFloat() * 2.dp.toPx(),
                    center = Offset(x = sparkX, y = sparkY)
                )
            }
        }

        // 绘制树木剪影（背景层）
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 树木颜色
            val treeColor = Color(0xFF0d2618).copy(alpha = 0.6f)
            val treePositions = listOf(0.08f, 0.22f, 0.36f, 0.56f, 0.74f, 0.88f)

            treePositions.forEachIndexed { index, xPos ->
                val x = canvasWidth * xPos
                val treeHeight = canvasHeight * 0.28f + (index % 3) * 20f
                val treeWidth = canvasHeight * 0.09f

                // 树干
                drawRoundRect(
                    color = Color(0xFF2d1810).copy(alpha = 0.5f),
                    topLeft = Offset(
                        x = x - treeWidth * 0.12f,
                        y = canvasHeight - treeHeight * 0.4f
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = treeWidth * 0.24f,
                        height = treeHeight * 0.4f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )

                // 树冠（多层三角形）
                repeat(3) { layer ->
                    val layerY = canvasHeight - treeHeight * (0.35f + layer * 0.15f)
                    val layerWidth = treeWidth * (1f - layer * 0.2f)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(x, layerY - treeHeight * 0.3f)
                        lineTo(x - layerWidth / 2, layerY)
                        lineTo(x + layerWidth / 2, layerY)
                        close()
                    }

                    drawPath(
                        path = path,
                        color = treeColor,
                        style = Stroke(width = (4 - layer).dp.toPx())
                    )
                }
            }

            // 绘制地面（橙色渐变）
            drawRoundRect(
                color = Color(0xFFD4A373).copy(alpha = 0.3f),
                topLeft = Offset(x = 0f, y = canvasHeight * 0.85f),
                size = androidx.compose.ui.geometry.Size(
                    width = canvasWidth,
                    height = canvasHeight * 0.15f
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f)
            )

            // 绘制卡通云朵 - 儿童友好的装饰元素
            val cloudPositions = listOf(
                0.15f to 0.12f,  // 云朵1
                0.55f to 0.08f,  // 云朵2
                0.85f to 0.15f,  // 云朵3
            )

            cloudPositions.forEach { (xRatio, yRatio) ->
                val cloudX = canvasWidth * xRatio
                val cloudY = canvasHeight * yRatio

                // 云朵主体（圆形组合）
                val cloudColor = Color.White.copy(alpha = 0.85f)
                drawCircle(color = cloudColor, radius = 45f, center = Offset(cloudX, cloudY))
                drawCircle(color = cloudColor, radius = 35f, center = Offset(cloudX - 40f, cloudY + 10f))
                drawCircle(color = cloudColor, radius = 38f, center = Offset(cloudX + 40f, cloudY + 10f))
                drawCircle(color = cloudColor, radius = 30f, center = Offset(cloudX - 25f, cloudY - 20f))
                drawCircle(color = cloudColor, radius = 32f, center = Offset(cloudX + 25f, cloudY - 20f))
            }
        }
    }
}
