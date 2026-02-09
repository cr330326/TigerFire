package com.cryallen.tigerfire.ui.school

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.school.SchoolEffect
import com.cryallen.tigerfire.presentation.school.SchoolEvent
import com.cryallen.tigerfire.presentation.school.SchoolViewModel
import com.cryallen.tigerfire.ui.components.CartoonPlayButton
import com.cryallen.tigerfire.ui.components.KidsBackButton
import com.cryallen.tigerfire.ui.theme.AlertConfig
import com.cryallen.tigerfire.ui.theme.ThemeGradients
import com.cryallen.tigerfire.ui.theme.createVerticalGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 学校场景 Screen 优化版本
 *
 * 优化内容：
 * 1. 触觉反馈 - 所有交互都带震动反馈
 * 2. 增强动画 - 叙事动画场景增强、角色入场动画、进度指示器动画
 * 3. 粒子背景 - 漂浮书本/知识元素效果
 * 4. 微交互 - 按钮缩放反馈
 * 5. 性能优化 - 动画资源预加载
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
fun SchoolScreenOptimized(
    viewModel: SchoolViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 警报红光闪烁动画透明度
    var alertAlpha by remember { mutableFloatStateOf(0f) }

    // 页面进入动画 - 优化版
    var contentVisible by remember { mutableStateOf(false) }
    var titleAlpha by remember { mutableFloatStateOf(0f) }
    var titleScale by remember { mutableFloatStateOf(0.9f) }

    val titleAlphaAnimated by animateFloatAsState(
        targetValue = titleAlpha,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "title_fade"
    )
    val titleScaleAnimated by animateFloatAsState(
        targetValue = titleScale,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "title_scale"
    )

    // 自动触发页面进入事件
    LaunchedEffect(Unit) {
        // 触发页面进入事件
        viewModel.onEvent(SchoolEvent.ScreenEntered)

        // 页面淡入动画
        delay(100)
        contentVisible = true
        titleAlpha = 1f
        titleScale = 1f
        delay(200)

        // 启动警报红光闪烁动画（使用AlertConfig柔和配置）
        while (true) {
            // 渐入
            for (i in 0..AlertConfig.FadeSteps) {
                if (!state.showAlarmEffect) break
                alertAlpha = i * (AlertConfig.MaxAlpha / AlertConfig.FadeSteps)  // 最大 0.15，更柔和
                delay(AlertConfig.StepDelay)
            }
            // 渐出
            for (i in AlertConfig.FadeSteps downTo 0) {
                if (!state.showAlarmEffect) break
                alertAlpha = i * (AlertConfig.MaxAlpha / AlertConfig.FadeSteps)
                delay(AlertConfig.StepDelay)
            }
            if (!state.showAlarmEffect) {
                alertAlpha = 0f
            }
            delay(AlertConfig.FlashPeriod / 6)  // 停顿时间（周期的1/6）
        }
    }

    // 订阅副作用（Effect）- 优化版：带触觉反馈
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    audioManager.playVoice("audio/voices/hint_idle.mp3")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = createVerticalGradient(ThemeGradients.School)
            )
    ) {
        // 优化的学校场景装饰性背景 - 漂浮书本/知识元素
        SchoolBackgroundOptimized()

        // 警报红光闪烁效果（屏幕边缘）
        if (state.showAlarmEffect && alertAlpha > 0f) {
            AlertFlashOverlayOptimized(alpha = alertAlpha)
        }

        // 主内容区域
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮 + 场景标题）
            TopBarOptimized(
                onBackClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(SchoolEvent.BackToMapClicked)
                },
                isVideoPlaying = state.isVideoPlaying,
                titleAlpha = titleAlphaAnimated,
                titleScale = titleScaleAnimated
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
                        // 显示播放按钮区域 - 优化版
                        PlayButtonAreaOptimized(
                            onPlayClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onEvent(SchoolEvent.PlayButtonClicked)
                            }
                        )
                    }
                    state.isVideoPlaying -> {
                        // 视频播放中提示 - 优化版
                        VideoPlayingIndicatorOptimized()
                    }
                    state.isCompleted && !state.showBadgeAnimation && !state.isVideoPlaying -> {
                        // 已完成状态 - 优化版
                        CompletedIndicatorOptimized()
                    }
                }
            }
        }

        // 视频播放全屏覆盖层
        if (state.isVideoPlaying) {
            VideoPlayerOverlayOptimized(
                videoPath = state.currentVideoPath,
                isPaused = state.isVideoPaused,
                showControls = state.showVideoControls,
                onPauseToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (state.isVideoPaused) {
                        viewModel.onEvent(SchoolEvent.ResumeVideoClicked)
                    } else {
                        viewModel.onEvent(SchoolEvent.PauseVideoClicked)
                    }
                },
                onExit = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(SchoolEvent.ExitVideoClicked)
                },
                onPlaybackComplete = {
                    viewModel.onEvent(SchoolEvent.VideoPlaybackCompleted)
                }
            )
        }

        // 徽章收集动画覆盖层 - 优化版
        BadgeAnimationOverlayOptimized(
            show = state.showBadgeAnimation,
            onAnimationComplete = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onEvent(SchoolEvent.BadgeAnimationCompleted)
            },
            onClose = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onEvent(SchoolEvent.CloseBadgeAnimation)
            }
        )

        // 空闲提示覆盖层 - 优化版
        IdleHintOverlayOptimized(
            show = state.showIdleHint,
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.dismissIdleHint()
            }
        )
    }
}

/**
 * 优化的顶部工具栏 - 带触觉反馈
 */
@Composable
private fun TopBarOptimized(
    onBackClick: () -> Unit,
    isVideoPlaying: Boolean,
    titleAlpha: Float,
    titleScale: Float
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "title_bar_pulse")

    // 装饰元素脉冲动画
    val decorativeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "decorative_pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮（视频播放中隐藏）
        if (!isVideoPlaying) {
            var backButtonScale by remember { mutableStateOf(1f) }

            Box(
                modifier = Modifier
                    .scale(backButtonScale)
            ) {
                KidsBackButton(
                    onClick = {
                        backButtonScale = 0.9f
                        onBackClick()
                    }
                )
            }

            LaunchedEffect(backButtonScale) {
                if (backButtonScale != 1f) {
                    delay(100)
                    backButtonScale = 1f
                }
            }
        } else {
            // 占位，保持布局一致
            Spacer(modifier = Modifier.size(64.dp))
        }

        // 场景标题 - 带增强动画
        Box(
            modifier = Modifier
                .scale(titleScale * decorativeScale)
                .alpha(titleAlpha)
                .shadow(
                    elevation = 8.dp,
                    spotColor = Color(0xFF457B9D).copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFF457B9D).copy(alpha = 0.2f)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(12.dp)
        ) {
            Text(
                text = "🏫",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 占位，保持标题居中
        Spacer(modifier = Modifier.size(64.dp))
    }
}

/**
 * 优化的播放按钮区域 - 带触觉反馈和增强动画
 */
@Composable
private fun PlayButtonAreaOptimized(
    onPlayClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "play_button_area")

    // 背景元素漂浮动画
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_float"
    )

    // 文字脉冲动画
    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_pulse"
    )

    var buttonScale by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 场景说明文字 - 带增强效果
            Box(
                modifier = Modifier
                    .scale(textPulse)
                    .offset(y = (-floatOffset / 2).dp)
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0xFF457B9D).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .drawBehind {
                        val strokeWidth = 3.dp.toPx()
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF457B9D),
                                    Color(0xFFA8DADC),
                                    Color(0xFF457B9D)
                                )
                            ),
                            style = Stroke(width = strokeWidth),
                            cornerRadius = CornerRadius(20.dp.toPx())
                        )
                    }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "🚨 学校消防安全知识",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF457B9D),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "小朋友发现火灾后应该怎么做？",
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 使用CartoonPlayButton组件 - 带缩放反馈
            Box(
                modifier = Modifier
                    .scale(buttonScale)
            ) {
                CartoonPlayButton(
                    onClick = {
                        buttonScale = 0.95f
                        onPlayClick()
                    },
                    text = "点我观看"
                )
            }

            LaunchedEffect(buttonScale) {
                if (buttonScale != 1f) {
                    delay(100)
                    buttonScale = 1f
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 小提示 - 带闪烁动画
            val hintAlpha by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hint_alpha"
            )

            Text(
                text = "👦 和小火一起学习消防安全知识吧！",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = hintAlpha),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 优化的视频播放中指示器 - 带增强动画
 */
@Composable
private fun VideoPlayingIndicatorOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_indicator")

    // 多层点动画
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    // 外圈旋转动画
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 脉冲缩放
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.scale(pulseScale)
    ) {
        // 电影图标 - 带旋转装饰
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(
                    elevation = 12.dp,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                    shape = CircleShape
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFF457B9D).copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
                .drawBehind {
                    // 旋转装饰圈
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF457B9D).copy(alpha = 0.6f),
                                Color(0xFFA8DADC).copy(alpha = 0.6f)
                            )
                        ),
                        style = Stroke(width = 4.dp.toPx()),
                        cornerRadius = CornerRadius(size.minDimension / 2)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎬",
                fontSize = 60.sp,
                modifier = Modifier.rotate(rotationAnim * 0.1f)
            )
        }

        Text(
            text = "正在播放动画...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 加载点动画 - 三阶段延迟
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val delay = index * 100L
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = delay.toInt(),
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = delay.toInt(),
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_alpha_$index"
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .scale(scale)
                        .alpha(dotAlpha)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFF457B9D).copy(alpha = 0.5f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * 优化的完成状态指示器 - 带增强动画
 */
@Composable
private fun CompletedIndicatorOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = "completed")

    // 徽章脉冲动画
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_pulse"
    )

    // 星星闪烁动画
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    // 星星旋转
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 装饰星星 - 旋转
        Box(
            modifier = Modifier
                .size(90.dp)
                .rotate(starRotation)
        ) {
            Text(
                text = "✨",
                fontSize = 80.sp,
                modifier = Modifier.alpha(starAlpha)
            )
        }

        // 完成图标
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

        Box(
            modifier = Modifier
                .scale(badgeScale)
                .shadow(
                    elevation = 10.dp,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                )
                .drawBehind {
                    val strokeWidth = 3.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFF6B6B),
                                Color(0xFFFFD700)
                            )
                        ),
                        style = Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "你获得了学校徽章！",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF457B9D)
            )
        }
    }
}

/**
 * 优化的警报红光闪烁覆盖层 - 带增强效果
 */
@Composable
private fun AlertFlashOverlayOptimized(alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "alert_flash")

    // 内圈脉冲
    val innerPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "inner_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // 绘制多层红色边框，增强警报效果
                val outerStrokeWidth = 32.dp.toPx()
                val innerStrokeWidth = 24.dp.toPx()
                val centerStrokeWidth = 16.dp.toPx()

                // 外圈
                drawRoundRect(
                    color = Color.Red.copy(alpha = alpha),
                    style = Stroke(width = outerStrokeWidth),
                    cornerRadius = CornerRadius(0f)
                )

                // 内圈 - 带脉冲
                drawRoundRect(
                    color = Color.Red.copy(alpha = alpha * innerPulse * 0.7f),
                    style = Stroke(width = innerStrokeWidth),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width - 48.dp.toPx(),
                        height = size.height - 48.dp.toPx()
                    ),
                    topLeft = Offset(24.dp.toPx(), 24.dp.toPx())
                )

                // 中心圈
                drawRoundRect(
                    color = Color.Red.copy(alpha = alpha * innerPulse * 0.5f),
                    style = Stroke(width = centerStrokeWidth),
                    cornerRadius = CornerRadius(48.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width - 96.dp.toPx(),
                        height = size.height - 96.dp.toPx()
                    ),
                    topLeft = Offset(48.dp.toPx(), 48.dp.toPx())
                )
            }
    )
}

/**
 * 优化的学校场景装饰性背景 - 漂浮书本/知识元素
 */
@Composable
private fun SchoolBackgroundOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = "school_bg_optimized")

    // 多层知识元素以不同速度移动，创造深度感
    val layer1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "knowledge_layer_1"
    )

    val layer2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "knowledge_layer_2"
    )

    // 漂浮动画
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "knowledge_float"
    )

    // 知识元素闪烁
    val knowledgeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "knowledge_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 第一层知识元素 - 小而快（书本、铅笔、尺子）
        val knowledgeIcons1 = listOf("📚", "✏️", "📏", "🎒")
        repeat(8) { index ->
            val x = ((layer1Offset * 0.5 + index * 60) % 500).dp
            val y = ((index * 80) % 400).dp + floatAnim.dp * ((index % 2) * 2 - 1).toFloat()
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(20.dp)
                    .alpha(knowledgeAlpha * 0.4f)
            ) {
                Text(
                    text = knowledgeIcons1[index % knowledgeIcons1.size],
                    fontSize = 16.dp.value.sp
                )
            }
        }

        // 第二层知识元素 - 中等速度
        val knowledgeIcons2 = listOf("📖", "📝", "🔢", "🔤")
        repeat(6) { index ->
            val x = ((layer2Offset * 0.3 + index * 100) % 500).dp
            val y = ((index * 120 + 50) % 400).dp - floatAnim.dp * 0.6f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(24.dp)
                    .alpha(knowledgeAlpha * 0.5f)
            ) {
                Text(
                    text = knowledgeIcons2[index % knowledgeIcons2.size],
                    fontSize = 20.dp.value.sp
                )
            }
        }

        // 云朵层
        Text(
            text = "☁️",
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-60 + layer1Offset * 0.5f).dp, y = 40.dp)
                .alpha(0.08f)
        )
        Text(
            text = "☁️",
            fontSize = 100.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (30 + layer1Offset * 0.3f).dp, y = 100.dp)
                .alpha(0.06f)
        )

        // 学校建筑 - 装饰
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

        // 消防车装饰（底部移动）
        val fireTruckX by infiniteTransition.animateFloat(
            initialValue = -100f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "firetruck_move"
        )

        Text(
            text = "🚒",
            fontSize = 60.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = fireTruckX.dp, y = (-20).dp)
                .alpha(0.1f)
        )

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
        val starAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_decorate_alpha"
        )

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
                    .alpha(starAlpha * 0.2f)
            )
        }
    }
}

/**
 * 优化的视频播放全屏覆盖层 - 带触觉反馈
 */
@Composable
private fun VideoPlayerOverlayOptimized(
    videoPath: String,
    isPaused: Boolean,
    showControls: Boolean,
    onPauseToggle: () -> Unit,
    onExit: () -> Unit,
    onPlaybackComplete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "video_controls")

    // 控制栏脉冲动画
    val controlsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "controls_alpha"
    )

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
            VideoControlsBarOptimized(
                isPaused = isPaused,
                onPauseToggle = onPauseToggle,
                onExit = onExit,
                modifier = Modifier.align(Alignment.BottomCenter),
                controlsAlpha = controlsAlpha
            )
        }
    }
}

/**
 * 优化的视频控制栏 - 带触觉反馈
 */
@Composable
private fun VideoControlsBarOptimized(
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    controlsAlpha: Float
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "video_control_buttons")

    // 按钮脉冲动画
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_pulse"
    )

    var playPauseScale by remember { mutableStateOf(1f) }
    var exitScale by remember { mutableStateOf(1f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 播放/暂停按钮 - 优化版
        ControlButtonOptimized(
            icon = if (isPaused) "▶" else "⏸",
            contentDescription = if (isPaused) "播放" else "暂停",
            onClick = {
                playPauseScale = 0.9f
                onPauseToggle()
            },
            buttonScale = playPauseScale * buttonPulse,
            buttonAlpha = controlsAlpha
        )

        Spacer(modifier = Modifier.weight(1f))

        // 退出按钮 - 优化版
        ControlButtonOptimized(
            icon = "✕",
            contentDescription = "退出",
            onClick = {
                exitScale = 0.9f
                onExit()
            },
            buttonScale = exitScale * buttonPulse,
            buttonAlpha = controlsAlpha
        )
    }

    LaunchedEffect(playPauseScale) {
        if (playPauseScale != 1f) {
            delay(100)
            playPauseScale = 1f
        }
    }

    LaunchedEffect(exitScale) {
        if (exitScale != 1f) {
            delay(100)
            exitScale = 1f
        }
    }
}

/**
 * 优化的控制按钮 - 带触觉反馈
 */
@Composable
private fun ControlButtonOptimized(
    icon: String,
    contentDescription: String,
    onClick: () -> Unit,
    buttonScale: Float,
    buttonAlpha: Float
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "control_button_optimized")

    // 光晕脉冲动画
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(buttonScale)
            .alpha(buttonAlpha)
            .shadow(
                elevation = 10.dp,
                spotColor = Color(0xFF457B9D).copy(alpha = glowAlpha),
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
            .drawBehind {
                // 添加光晕效果
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2 - 4.dp.toPx()
                )
            }
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
 * 优化的徽章收集动画覆盖层 - 带触觉反馈和增强效果
 */
@Composable
private fun BadgeAnimationOverlayOptimized(
    show: Boolean,
    onAnimationComplete: () -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

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
                label = "badge_scale"
            )

            // 无限旋转动画
            val infiniteTransition = rememberInfiniteTransition(label = "badge_animations")

            val starRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "star_rotation"
            )

            val confettiRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "confetti_rotation"
            )

            val starAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "star_alpha"
            )

            // 按钮脉冲动画 - 增强版
            val buttonPulse by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "button_pulse"
            )

            // 文字闪烁动画
            val textGlow by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "text_glow"
            )

            var buttonScale by remember { mutableStateOf(1f) }

            // 背景装饰层
            Box(modifier = Modifier.fillMaxSize()) {
                // 星星装饰（4个角落）- 增强版
                listOf(
                    Pair(Alignment.TopStart, Pair((-80).dp, (-80).dp)),
                    Pair(Alignment.TopEnd, Pair(80.dp, (-80).dp)),
                    Pair(Alignment.BottomStart, Pair((-80).dp, 80.dp)),
                    Pair(Alignment.BottomEnd, Pair(80.dp, 80.dp)),
                ).forEach { (alignment, offset) ->
                    Text(
                        text = "⭐",
                        fontSize = 40.sp,
                        modifier = Modifier
                            .align(alignment)
                            .offset(x = offset.component1(), y = offset.component2())
                            .rotate(starRotation)
                            .alpha(starAlpha)
                            .shadow(
                                elevation = 8.dp,
                                spotColor = Color(0xFFFFD700).copy(alpha = 0.6f)
                            )
                    )
                }

                // 彩带装饰 - 增强版
                listOf(
                    Pair((-150).dp, (-120).dp),
                    Pair(150.dp, (-140).dp),
                    Pair((-130).dp, 110.dp),
                    Pair(160.dp, 130.dp),
                ).forEach { offset ->
                    Text(
                        text = "🎊",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offset.component1(), y = offset.component2())
                            .rotate(confettiRotation)
                            .alpha(0.7f)
                    )
                }
            }

            // 主内容层
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                // 小火老虎 - 带脉冲效果
                val xiaohuoPulse by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "xiaohuo_pulse"
                )

                Text(
                    text = "🐯",
                    fontSize = 110.sp,
                    modifier = Modifier.scale(xiaohuoPulse)
                )

                // 点赞手势 - 带旋转动画
                val thumbsUpRotation by infiniteTransition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "thumbsup_rotation"
                )

                Text(
                    text = "👍",
                    fontSize = 75.sp,
                    modifier = Modifier.rotate(thumbsUpRotation)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 徽章（弹性缩放动画）- 增强版
                Box(
                    modifier = Modifier
                        .scale(badgeScale)
                        .shadow(
                            elevation = 16.dp,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                        .drawBehind {
                            // 徽章光晕效果
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700).copy(alpha = textGlow),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 + 10.dp.toPx()
                            )
                        }
                ) {
                    Text(
                        text = "🏅",
                        fontSize = 150.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 赞美文字 - 带发光效果
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            spotColor = Color(0xFFFFD700).copy(alpha = textGlow),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700).copy(alpha = textGlow * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                cornerRadius = CornerRadius(20.dp.toPx())
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "你真棒！",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "记住，着火要找大人帮忙！",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFD93D),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "获得学校徽章！",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF457B9D)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 点击继续按钮 - 优化版
                Box(
                    modifier = Modifier
                        .scale(buttonScale * buttonPulse)
                        .shadow(
                            elevation = 16.dp,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .background(Color.White, shape = RoundedCornerShape(32.dp))
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE63946),
                                        Color(0xFFFFD700),
                                        Color(0xFFE63946)
                                    )
                                ),
                                style = Stroke(width = strokeWidth),
                                cornerRadius = CornerRadius(32.dp.toPx())
                            )
                        }
                        .padding(horizontal = 56.dp, vertical = 20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                buttonScale = 0.95f
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
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE63946)
                        )
                        Text(
                            text = "▶",
                            fontSize = 22.sp,
                            color = Color(0xFFE63946)
                        )
                    }
                }

                LaunchedEffect(buttonScale) {
                    if (buttonScale != 1f) {
                        delay(100)
                        buttonScale = 1f
                    }
                }
            }
        }
    }
}

/**
 * 优化的空闲提示覆盖层 - 带触觉反馈和增强效果
 */
@Composable
private fun IdleHintOverlayOptimized(
    show: Boolean,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    if (!show) return

    // 脉冲动画 - 增强版
    val infiniteTransition = rememberInfiniteTransition(label = "idle_hint_pulse")

    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_scale"
    )

    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    // 装饰元素动画
    val decorRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "decor_rotation"
    )

    // 星星闪烁
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(hintScale)
                .alpha(hintAlpha)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0xFF457B9D).copy(alpha = 0.4f),
                    ambientColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF457B9D).copy(alpha = 0.97f),  // 学校蓝
                            Color(0xFF5CA0C3).copy(alpha = 0.97f)
                        )
                    ),
                    RoundedCornerShape(32.dp)
                )
                .drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.6f),
                                Color(0xFF457B9D).copy(alpha = 0.6f),
                                Color(0xFFFFD700).copy(alpha = 0.6f)
                            )
                        ),
                        style = Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(32.dp.toPx())
                    )
                }
                .padding(horizontal = 56.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 小火头像 - 带旋转装饰
                Box(
                    modifier = Modifier
                        .scale(hintScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    Color(0xFFFFF8DC)
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🐯",
                        fontSize = 85.sp,
                        modifier = Modifier.rotate(decorRotation * 0.2f)
                    )
                }

                // 提示文字 - 带阴影效果
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "需要帮忙吗？",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF457B9D)
                    )
                }

                Text(
                    text = "点击屏幕任意位置继续",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )

                // 装饰星星 - 增强版
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.alpha(starAlpha)
                ) {
                    repeat(4) { index ->
                        val starRotation = infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000 + index * 200, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "star_rotation_$index"
                        ).value

                        Text(
                            text = "⭐",
                            fontSize = 28.sp,
                            modifier = Modifier.rotate(starRotation)
                        )
                    }
                }
            }
        }
    }
}
