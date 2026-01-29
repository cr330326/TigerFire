package com.cryallen.tigerfire.ui.school

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.school.SchoolEffect
import com.cryallen.tigerfire.presentation.school.SchoolEvent
import com.cryallen.tigerfire.presentation.school.SchoolViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 学校场景 Screen
 *
 * 适合 3-6 岁儿童的消防安全教育场景
 *
 * 交互流程：
 * 1. 进入场景 → 警报音效 + 红光闪烁 + 小火语音提示
 * 2. 显示超大播放按钮（≥150pt）引导点击
 * 3. 点击播放按钮 → 停止警报 + 播放视频
 * 4. 视频完成 → 小火点赞动画 + 语音 + 徽章奖励
 * 5. 点击继续 → 返回主地图
 *
 * @param viewModel SchoolViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun SchoolScreen(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val coroutineScope = rememberCoroutineScope()

    // 警报红光闪烁动画透明度
    var alertAlpha by remember { mutableFloatStateOf(0f) }

    // 自动触发页面进入事件
    LaunchedEffect(Unit) {
        // 触发页面进入事件
        viewModel.onEvent(SchoolEvent.ScreenEntered)

        // 启动警报红光闪烁动画（柔和脉冲）
        while (true) {
            // 渐入
            for (i in 0..10) {
                if (!state.showAlarmEffect) break
                alertAlpha = i * 0.025f  // 最大 0.25，避免刺眼
                delay(50)
            }
            // 渐出
            for (i in 10 downTo 0) {
                if (!state.showAlarmEffect) break
                alertAlpha = i * 0.025f
                delay(50)
            }
            if (!state.showAlarmEffect) {
                alertAlpha = 0f
            }
            delay(500)  // 停顿
        }
    }

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SchoolEffect.StartAlarmEffects -> {
                    // 启动警报音效
                    audioManager.playAlertSound()
                }
                is SchoolEffect.StopAlarmEffects -> {
                    // 停止警报音效
                    audioManager.stopAlertSound()
                }
                is SchoolEffect.PlayVoice -> {
                    // 播放语音
                    audioManager.playVoice(effect.voicePath)
                }
                is SchoolEffect.PlayVideo -> {
                    // VideoPlayer 由状态驱动，无需额外处理
                }
                is SchoolEffect.ShowBadgeAnimation -> {
                    // 徽章动画在 showBadgeAnimation 状态中处理
                }
                is SchoolEffect.PlayBadgeSound -> {
                    audioManager.playBadgeSound()
                }
                is SchoolEffect.PlayCompletedSound -> {
                    audioManager.playSuccessSound()
                }
                is SchoolEffect.UnlockForestScene -> {
                    // 森林场景已解锁，在进度中自动处理
                }
                is SchoolEffect.NavigateToMap -> {
                    // 导航回主地图
                    audioManager.stopAlertSound()
                    audioManager.playClickSound(SceneType.SCHOOL)
                    onNavigateBack()
                }
                is SchoolEffect.PlaySlowDownVoice -> {
                    // 播放"慢一点"语音提示
                    audioManager.playVoice("audio/voices/slow_down.mp3")
                }
                is SchoolEffect.ShowIdleHint -> {
                    // 显示空闲提示：小火"需要帮忙吗？"
                    audioManager.playVoice("audio/voices/hint_idle.mp3")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF457B9D),  // 学校蓝
                        Color(0xFF5CA0C3),  // 天蓝色
                        Color(0xFF87CEEB)   // 天空蓝
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // 学校场景装饰性背景元素
        SchoolBackgroundEnhanced()

        // 警报红光闪烁效果（屏幕边缘）
        if (state.showAlarmEffect && alertAlpha > 0f) {
            AlertFlashOverlay(alpha = alertAlpha)
        }

        // 主内容区域
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮 + 场景标题）
            TopBar(
                onBackClick = {
                    viewModel.onEvent(SchoolEvent.BackToMapClicked)
                },
                isVideoPlaying = state.isVideoPlaying
            )

            // 中央内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 根据状态显示不同内容
                when {
                    state.showPlayButton -> {
                        // 显示播放按钮区域
                        PlayButtonArea(
                            onPlayClick = {
                                viewModel.onEvent(SchoolEvent.PlayButtonClicked)
                            }
                        )
                    }
                    state.isVideoPlaying -> {
                        // 视频播放中提示
                        VideoPlayingIndicator()
                    }
                    state.isCompleted && !state.showBadgeAnimation && !state.isVideoPlaying -> {
                        // 已完成状态
                        CompletedIndicator()
                    }
                }
            }
        }

        // 视频播放全屏覆盖层
        if (state.isVideoPlaying) {
            VideoPlayerOverlay(
                videoPath = state.currentVideoPath,
                isPaused = state.isVideoPaused,
                showControls = state.showVideoControls,
                onPauseToggle = {
                    if (state.isVideoPaused) {
                        viewModel.onEvent(SchoolEvent.ResumeVideoClicked)
                    } else {
                        viewModel.onEvent(SchoolEvent.PauseVideoClicked)
                    }
                },
                onExit = {
                    viewModel.onEvent(SchoolEvent.ExitVideoClicked)
                },
                onPlaybackComplete = {
                    viewModel.onEvent(SchoolEvent.VideoPlaybackCompleted)
                }
            )
        }

        // 徽章收集动画覆盖层
        BadgeAnimationOverlay(
            show = state.showBadgeAnimation,
            onAnimationComplete = {
                viewModel.onEvent(SchoolEvent.BadgeAnimationCompleted)
            },
            onClose = {
                viewModel.onEvent(SchoolEvent.CloseBadgeAnimation)
            }
        )

        // 空闲提示覆盖层
        IdleHintOverlay(
            show = state.showIdleHint,
            onDismiss = {
                viewModel.dismissIdleHint()
            }
        )
    }
}

/**
 * 顶部工具栏
 *
 * 包含返回按钮和场景标题
 */
@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    isVideoPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮（视频播放中隐藏或禁用）
        if (!isVideoPlaying) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(56.dp)  // 增大点击区域
                    .shadow(6.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Text(
                    text = "←",
                    fontSize = 28.sp,
                    color = Color(0xFF457B9D)
                )
            }
        } else {
            // 占位，保持布局一致
            Spacer(modifier = Modifier.size(56.dp))
        }

        // 场景标题
        Text(
            text = "🏫",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 占位，保持标题居中
        Spacer(modifier = Modifier.size(56.dp))
    }
}

/**
 * 播放按钮区域
 *
 * 超大播放按钮，适合 3-6 岁儿童点击
 */
@Composable
private fun PlayButtonArea(
    onPlayClick: () -> Unit
) {
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "playButtonPulse")

    // 按钮缩放动画（呼吸效果）
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    // 外圈光晕扩散动画
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloScale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )

    // 星星闪烁
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 场景说明文字
            Text(
                text = "学校消防安全知识",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "小朋友发现火灾后应该怎么做？",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 超大播放按钮容器
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // 外圈光晕效果
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(haloScale)
                        .alpha(haloAlpha)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // 星星装饰
                listOf(
                    Offset(-80f, -80f),
                    Offset(80f, -80f),
                    Offset(-80f, 80f),
                    Offset(80f, 80f),
                    Offset(0f, -95f),
                    Offset(0f, 95f),
                    Offset(-95f, 0f),
                    Offset(95f, 0f)
                ).forEach { offset ->
                    Text(
                        text = "⭐",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .offset(x = offset.x.dp, y = offset.y.dp)
                            .alpha(starAlpha * 0.5f)
                    )
                }

                // 主播放按钮
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(buttonScale)
                        .shadow(
                            elevation = 16.dp,
                            spotColor = Color(0xFFFF6B6B).copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B),  // 红色
                                    Color(0xFFFF8E53)   // 橙红色
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPlayClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 播放图标（三角形）
                    Text(
                        text = "▶",
                        fontSize = 64.sp,
                        color = Color.White,
                        modifier = Modifier.offset(x = 8.dp)  // 视觉居中
                    )
                }

                // 按钮外圈装饰
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(buttonScale)
                        .drawBehind {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.3f),
                                style = Stroke(width = 4.dp.toPx()),
                                cornerRadius = CornerRadius(85.dp.toPx(), 85.dp.toPx())  // 半径 = 尺寸的一半
                            )
                        }
                )
            }

            // 提示文字
            Text(
                text = "👆 点击播放动画",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // 小提示
            Text(
                text = "和小火一起学习消防安全知识吧！",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 视频播放中指示器
 */
@Composable
private fun VideoPlayingIndicator() {
    // 加载动画
    val infiniteTransition = rememberInfiniteTransition(label = "loadingIndicator")

    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎬",
            fontSize = 80.sp
        )

        Text(
            text = "正在播放动画...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 加载点动画
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val delay = index * 100L
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = delay.toInt(),
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot$index"
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(scale)
                        .background(
                            Color.White,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * 完成状态指示器
 */
@Composable
private fun CompletedIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "✅",
            fontSize = 80.sp
        )

        Text(
            text = "已完成观看",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "你获得了学校徽章！",
            fontSize = 22.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

/**
 * 警报红光闪烁覆盖层
 *
 * 柔和的红色脉冲效果，不刺眼
 */
@Composable
private fun AlertFlashOverlay(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // 绘制屏幕边缘红色边框
                val strokeWidth = 32.dp.toPx()
                drawRoundRect(
                    color = Color.Red.copy(alpha = alpha),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(0f)
                )
            }
    )
}

/**
 * 增强版学校场景装饰性背景
 *
 * 包含更多动态元素和儿童友好的装饰
 */
@Composable
private fun SchoolBackgroundEnhanced() {
    val infiniteTransition = rememberInfiniteTransition(label = "schoolBgEnhanced")

    // 云朵浮动
    val cloud1X by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1X"
    )

    // 消防车移动（小火提示）
    val fireTruckX by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fireTruckX"
    )

    // 星星闪烁
    val starAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景云朵层
        Text(
            text = "☁️",
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-60 + cloud1X * 0.5f).dp, y = 40.dp)
                .alpha(0.1f)
        )
        Text(
            text = "☁️",
            fontSize = 100.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (30 + cloud1X * 0.3f).dp, y = 100.dp)
                .alpha(0.08f)
        )

        // 消防车装饰（底部移动）
        Text(
            text = "🚒",
            fontSize = 60.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = fireTruckX.dp, y = (-20).dp)
                .alpha(0.12f)
        )

        // 学校建筑
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-10).dp)
                .alpha(0.1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "🏫",
                fontSize = 120.sp
            )
        }

        // 安全相关装饰元素
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = (-40).dp)
                .alpha(0.1f),
            horizontalArrangement = Arrangement.spacedBy((-20).dp)
        ) {
            Text(
                text = "🔥",
                fontSize = 50.sp
            )
            Text(
                text = "🧯",
                fontSize = 50.sp
            )
        }

        // 星星装饰（多个位置）
        val starPositions = listOf(
            Pair(Alignment.TopEnd, Pair((-80).dp, 200.dp)),
            Pair(Alignment.TopStart, Pair(60.dp, 150.dp)),
            Pair(Alignment.CenterEnd, Pair((-50).dp, (-100).dp)),
            Pair(Alignment.CenterStart, Pair(50.dp, 50.dp)),
        )

        starPositions.forEach { (alignment, offset) ->
            Text(
                text = "✨",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(alignment)
                    .offset(x = offset.first, y = offset.second)
                    .alpha(starAlpha1 * 0.15f)
            )
        }
    }
}

/**
 * 视频播放全屏覆盖层
 */
@Composable
private fun VideoPlayerOverlay(
    videoPath: String,
    isPaused: Boolean,
    showControls: Boolean,
    onPauseToggle: () -> Unit,
    onExit: () -> Unit,
    onPlaybackComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        // 视频播放器居中
        VideoPlayer(
            videoPath = videoPath,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            onPlaybackCompleted = onPlaybackComplete,
            autoPlay = true,
            showControls = false,
            isPaused = isPaused
        )

        // 视频控制栏在底部
        if (showControls) {
            VideoControlsBar(
                isPaused = isPaused,
                onPauseToggle = onPauseToggle,
                onExit = onExit,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * 视频控制栏
 *
 * 提供暂停/播放和退出按钮
 */
@Composable
private fun VideoControlsBar(
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 播放/暂停按钮
        ControlButton(
            icon = if (isPaused) "▶" else "⏸",
            contentDescription = if (isPaused) "播放" else "暂停",
            onClick = onPauseToggle
        )

        Spacer(modifier = Modifier.weight(1f))

        // 退出按钮
        ControlButton(
            icon = "✕",
            contentDescription = "退出",
            onClick = onExit
        )
    }
}

/**
 * 控制按钮
 *
 * 大尺寸圆形按钮，适合儿童点击
 */
@Composable
private fun ControlButton(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "controlButton")

    // 脉冲动画
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonPulse"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(buttonScale)
            .shadow(
                elevation = 8.dp,
                spotColor = Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF457B9D),
                        Color(0xFF5CA0C3)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 32.sp,
            color = Color.White
        )
    }
}

/**
 * 徽章收集动画覆盖层
 *
 * 参考消防站场景的精美设计，添加：
 * - 渐变背景（红→橙→黄）
 * - 弹性缩放动画
 * - 星星和彩带装饰
 * - 脉冲按钮效果
 */
@Composable
private fun BadgeAnimationOverlay(
    show: Boolean,
    onAnimationComplete: () -> Unit,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = show,
        enter = expandIn(expandFrom = Alignment.Center) + fadeIn(),
        exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE63946).copy(alpha = 0.9f),  // 红
                            Color(0xFFF77F00).copy(alpha = 0.9f),  // 橙
                            Color(0xFFFCBF49).copy(alpha = 0.9f)   // 黄
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 弹性缩放动画
            val badgeScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "badgeScale"
            )

            // 无限旋转动画
            val infiniteTransition = rememberInfiniteTransition(label = "badgeAnimations")

            val starRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "starRotation"
            )

            val confettiRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "confettiRotation"
            )

            val starAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "starAlpha"
            )

            // 按钮脉冲动画
            val buttonPulse by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "buttonPulse"
            )

            // 背景装饰层
            Box(modifier = Modifier.fillMaxSize()) {
                // 星星装饰（4个角落）
                listOf(
                    Pair(Alignment.TopStart, Pair((-80).dp, (-80).dp)),
                    Pair(Alignment.TopEnd, Pair(80.dp, (-80).dp)),
                    Pair(Alignment.BottomStart, Pair((-80).dp, 80.dp)),
                    Pair(Alignment.BottomEnd, Pair(80.dp, 80.dp)),
                ).forEach { (alignment, offset) ->
                    Text(
                        text = "⭐",
                        fontSize = 36.sp,
                        modifier = Modifier
                            .align(alignment)
                            .offset(x = offset.component1(), y = offset.component2())
                            .rotate(starRotation)
                            .alpha(starAlpha)
                    )
                }

                // 彩带装饰
                listOf(
                    Pair((-150).dp, (-100).dp),
                    Pair(150.dp, (-120).dp),
                    Pair((-120).dp, 100.dp),
                    Pair(140.dp, 120.dp),
                ).forEach { offset ->
                    Text(
                        text = "🎊",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offset.component1(), y = offset.component2())
                            .rotate(confettiRotation)
                            .alpha(0.6f)
                    )
                }
            }

            // 主内容层
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                // 小火老虎
                Text(
                    text = "🐯",
                    fontSize = 100.sp
                )

                // 点赞手势
                Text(
                    text = "👍",
                    fontSize = 70.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 徽章（弹性缩放动画）
                Text(
                    text = "🏅",
                    fontSize = 140.sp,
                    modifier = Modifier.scale(badgeScale)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 赞美文字
                Text(
                    text = "你真棒！",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "记住，着火要找大人帮忙！",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFD93D),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "获得学校徽章！",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 点击继续按钮
                Box(
                    modifier = Modifier
                        .scale(buttonPulse)
                        .shadow(
                            elevation = 12.dp,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .background(Color.White, shape = RoundedCornerShape(32.dp))
                        .padding(horizontal = 56.dp, vertical = 20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                // 先关闭徽章动画，然后导航
                                onClose()
                                onAnimationComplete()
                            }
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "点击继续",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE63946)
                        )
                        Text(
                            text = "▶",
                            fontSize = 20.sp,
                            color = Color(0xFFE63946)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空闲提示覆盖层
 *
 * 当用户30秒无操作时显示小火提示："需要帮忙吗？"
 *
 * @param show 是否显示提示
 * @param onDismiss 关闭提示回调
 */
@Composable
private fun IdleHintOverlay(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "schoolIdleHintPulse")

    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintScale"
    )

    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha"
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
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF457B9D).copy(alpha = 0.95f),  // 学校蓝
                            Color(0xFF5CA0C3).copy(alpha = 0.95f)
                        )
                    ),
                    RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 56.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 小火头像
                Text(
                    text = "🐯",
                    fontSize = 80.sp,
                    modifier = Modifier.scale(hintScale)
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

                // 装饰星星
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(hintAlpha)
                ) {
                    repeat(3) {
                        Text(
                            text = "⭐",
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}
