package com.cryallen.tigerfire.ui.firestation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.firestation.FireStationDevice
import com.cryallen.tigerfire.presentation.firestation.FireStationEffect
import com.cryallen.tigerfire.presentation.firestation.FireStationEvent
import com.cryallen.tigerfire.presentation.firestation.FireStationViewModel
import com.cryallen.tigerfire.ui.components.KidsBackButton
import com.cryallen.tigerfire.ui.theme.ThemeGradients
import com.cryallen.tigerfire.ui.theme.createVerticalGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 消防站场景 Screen - 优化版本
 *
 * 增强特性:
 * - 触觉反馈 (HapticFeedback)
 * - 3D 翻转/缩放动画效果
 * - 脉冲光环效果
 * - 视差背景效果
 * - 粒子火焰背景
 * - 微交互按钮反馈
 * - 动画资源预加载
 *
 * @param viewModel FireStationViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun FireStationScreenOptimized(
    viewModel: FireStationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val hapticFeedback = LocalHapticFeedback.current

    // 防止重复导航
    var isNavigating by remember { mutableStateOf(false) }

    // 预加载动画资源
    LaunchedEffect(Unit) {
        viewModel.onEvent(FireStationEvent.ScreenEntered)
        // 预热动画系统
        delay(100)
    }

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FireStationEffect.PlayVideo -> {
                    // VideoPlayer 由状态驱动，无需额外处理
                }
                is FireStationEffect.NavigateToMap -> {
                    if (!isNavigating) {
                        isNavigating = true
                        onNavigateBack()
                    }
                }
                is FireStationEffect.ShowBadgeAnimation -> {
                    // 徽章动画在 showBadgeAnimation 状态中处理
                }
                is FireStationEffect.PlayClickSound -> {
                    audioManager.playClickSound(SceneType.FIRE_STATION)
                    // 触觉反馈
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is FireStationEffect.PlayBadgeSound -> {
                    audioManager.playBadgeSound()
                }
                is FireStationEffect.PlayAllCompletedSound -> {
                    audioManager.playAllCompletedSound()
                }
                is FireStationEffect.UnlockSchoolScene -> {
                    // 学校场景已解锁，在进度中自动处理
                }
                is FireStationEffect.PlaySlowDownVoice -> {
                    // 播放"慢一点"语音提示
                    audioManager.playVoice("audio/voices/slow_down.mp3")
                }
                is FireStationEffect.ShowIdleHint -> {
                    // 显示空闲提示：小火"需要帮忙吗？"
                    audioManager.playVoice("audio/voices/hint_idle.mp3")
                }
            }
        }
    }

    // 视差效果状态
    var parallaxOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = createVerticalGradient(ThemeGradients.FireStation)
            )
            .then(
                Modifier
                    .drawBehind {
                        // 粒子火焰背景效果
                        drawFireParticles(this, parallaxOffset, density)
                    }
            )
    ) {
        // 消防站装饰性背景元素 - 带视差效果
        FireStationBackgroundOptimized(
            parallaxOffset = parallaxOffset,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                KidsBackButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(FireStationEvent.BackToMapClicked)
                    }
                )
            }

            // 中央设备区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 标题区域 - 增强动画效果
                EnhancedTitle()

                Spacer(modifier = Modifier.height(40.dp))

                // 设备网格（2x2）- 带增强效果
                DeviceGridOptimized(
                    completedDevices = state.completedDevices,
                    isPlayingVideo = state.isPlayingVideo,
                    onDeviceClick = { device ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(FireStationEvent.DeviceClicked(device))
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 完成进度提示卡片 - 增强设计
                EnhancedProgressCard(
                    completedCount = state.completedDevices.size,
                    isAllCompleted = state.isAllCompleted
                )
            }
        }

        // 视频播放覆盖层
        if (state.isPlayingVideo) {
            VideoPlayerOverlay(
                device = state.currentPlayingDevice,
                onPlaybackComplete = { device ->
                    viewModel.onEvent(FireStationEvent.VideoPlaybackCompleted(device))
                }
            )
        }

        // 徽章收集动画 - 增强版本
        EnhancedBadgeAnimationOverlay(
            show = state.showBadgeAnimation,
            device = state.earnedBadgeDevice,
            onAnimationComplete = {
                viewModel.onEvent(FireStationEvent.BadgeAnimationCompleted)
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
 * 增强标题组件 - 带火焰粒子效果
 */
@Composable
private fun EnhancedTitle() {
    val infiniteTransition = rememberInfiniteTransition(label = "titleAnimations")

    // 火焰缩放动画
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    // 标题脉冲动画
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    // 标题旋转（微妙）
    val titleRotation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleRotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .scale(titleScale)
                .rotate(titleRotation)
        ) {
            Text(
                text = "🔥",
                fontSize = 52.sp,
                modifier = Modifier.scale(flameScale)
            )
            Text(
                text = "消防站",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
            )
            Text(
                text = "🔥",
                fontSize = 52.sp,
                modifier = Modifier.scale(flameScale)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 副标题 - 带闪烁效果
        val subtitleAlpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "subtitleAlpha"
        )

        Text(
            text = "点击设备学习消防知识",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = subtitleAlpha),
            modifier = Modifier.shadow(
                elevation = 4.dp,
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
        )
    }
}

/**
 * 增强进度卡片 - 带脉冲光环效果
 */
@Composable
private fun EnhancedProgressCard(
    completedCount: Int,
    isAllCompleted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progressAnimations")

    // 脉冲缩放
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 光环脉冲
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // 星星旋转
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )

    Box(
        modifier = Modifier
            .scale(pulseScale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = if (isAllCompleted) Color(0xFFFFD700).copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.18f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 40.dp, vertical = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 进度文本
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐",
                    fontSize = 28.sp,
                    modifier = Modifier.rotate(starRotation * 0.2f)
                )
                Text(
                    text = "已完成: $completedCount/4",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.shadow(
                        elevation = 4.dp,
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    )
                )
                Text(
                    text = "⭐",
                    fontSize = 28.sp,
                    modifier = Modifier.rotate(-starRotation * 0.2f)
                )
            }

            // 全部完成提示 - 超醒目效果
            if (isAllCompleted) {
                Spacer(modifier = Modifier.height(16.dp))

                // 光环背景
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val celebrateScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "celebrateScale"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.scale(celebrateScale)
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 26.sp
                        )
                        Text(
                            text = "太棒了！全部完成！",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD93D),
                            modifier = Modifier.shadow(
                                elevation = 4.dp,
                                spotColor = Color.Black.copy(alpha = 0.35f)
                            )
                        )
                        Text(
                            text = "🎉",
                            fontSize = 26.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 优化版设备网格（2x2）
 * 带增强的3D效果和微交互
 */
@Composable
private fun DeviceGridOptimized(
    completedDevices: Set<FireStationDevice>,
    isPlayingVideo: Boolean,
    onDeviceClick: (FireStationDevice) -> Unit
) {
    val devices = FireStationDevice.entries

    // 入场动画状态
    val coroutineScope = rememberCoroutineScope()
    val enterProgress = remember { mutableStateListOf(0f, 0f, 0f, 0f) }

    LaunchedEffect(Unit) {
        devices.forEachIndexed { index, _ ->
            delay(index * 80L)
            enterProgress[index] = 1f
        }
    }

    // 浮动动画
    val infiniteTransition = rememberInfiniteTransition(label = "deviceAnimations")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    // 脉冲缩放
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        // 左列
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            devices.slice(0..1).forEachIndexed { index, device ->
                EnhancedDeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) },
                    floatOffset = floatOffset,
                    pulseScale = pulseScale,
                    enterProgress = enterProgress[index],
                    index = index
                )
            }
        }

        // 右列
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            devices.slice(2..3).forEachIndexed { index, device ->
                EnhancedDeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) },
                    floatOffset = floatOffset * 0.7f,
                    pulseScale = pulseScale,
                    enterProgress = enterProgress[index + 2],
                    index = index + 2
                )
            }
        }
    }
}

/**
 * 增强版设备卡片 - 3D翻转效果和微交互
 */
@Composable
private fun EnhancedDeviceCard(
    device: FireStationDevice,
    isCompleted: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    floatOffset: Float = 0f,
    pulseScale: Float = 1f,
    enterProgress: Float = 1f,
    index: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 3D翻转动画
    val flipRotation by animateFloatAsState(
        targetValue = if (isCompleted) 360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "flipRotation"
    )

    // 按下缩放
    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.85f
            isCompleted -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // 背景亮度
    val backgroundBrightness by animateFloatAsState(
        targetValue = when {
            !isEnabled -> 0.5f
            isPressed -> 0.85f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "backgroundBrightness"
    )

    // 持续动画
    val infiniteTransition = rememberInfiniteTransition(label = "cardAnimations")
    val iconFloatY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500 + index * 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconFloatY"
    )

    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + index * 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    // 卡片渐变色
    val cardGradient = when (device) {
        FireStationDevice.FIRE_HYDRANT -> listOf(
            Color(0xFFFFE066),
            Color(0xFFFFB347),
            Color(0xFFFA8231)
        )
        FireStationDevice.LADDER_TRUCK -> listOf(
            Color(0xFFFF6B9D),
            Color(0xFFC44569),
            Color(0xFF8B3A62)
        )
        FireStationDevice.FIRE_EXTINGUISHER -> listOf(
            Color(0xFF6BCB77),
            Color(0xFF4D8076),
            Color(0xFF3A5F58)
        )
        FireStationDevice.WATER_HOSE -> listOf(
            Color(0xFF4ECDC4),
            Color(0xFF44A08D),
            Color(0xFF2E7D73)
        )
    }

    // 完成状态光晕颜色
    val glowColor = when (device) {
        FireStationDevice.FIRE_HYDRANT -> Color(0xFFFFD700)
        FireStationDevice.LADDER_TRUCK -> Color(0xFFFF69B4)
        FireStationDevice.FIRE_EXTINGUISHER -> Color(0xFF00FF7F)
        FireStationDevice.WATER_HOSE -> Color(0xFF00CED1)
    }

    // 入场缩放
    val enterScale by animateFloatAsState(
        targetValue = enterProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "enterScale"
    )

    Box(
        modifier = Modifier
            .size(170.dp)
            .graphicsLayer {
                // 3D透视效果
                cameraDistance = 12f * density
                rotationX = if (isPressed) 5f else 0f
                rotationY = flipRotation.coerceIn(0f, 180f)
            }
            .scale(enterScale * pulseScale)
            .alpha(enterProgress)
            .offset(y = floatOffset.dp)
            .shadow(
                elevation = if (isPressed) 6.dp else if (isCompleted) 20.dp else 14.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = if (isCompleted) glowColor.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f),
                ambientColor = if (isCompleted) glowColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = cardGradient.map { it.copy(alpha = backgroundBrightness) },
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .then(
                if (isEnabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // 内部内容
        Box(
            modifier = Modifier
                .scale(pressScale)
                .offset(y = iconFloatY.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 28.dp, horizontal = 20.dp)
            ) {
                // 图标容器 - 增强光晕
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 完成状态脉冲光晕
                    if (isCompleted) {
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 0.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            glowColor.copy(alpha = pulseAlpha),
                                            glowColor.copy(alpha = pulseAlpha * 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // 设备图标
                    Text(
                        text = getDeviceIcon(device),
                        fontSize = 62.sp,
                        modifier = Modifier.rotate(iconRotation)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 设备名称
                Text(
                    text = device.displayName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.98f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.shadow(
                        elevation = 3.dp,
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
                )

                // 完成标记 - 超醒目
                if (isCompleted) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val starRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "starRotation"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 24.sp,
                            modifier = Modifier.rotate(starRotation)
                        )
                        Text(
                            text = "已完成",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = glowColor,
                            modifier = Modifier.shadow(
                                elevation = 3.dp,
                                spotColor = Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }
        }

        // 旋转边框高亮
        if (isCompleted) {
            val borderRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(5000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "borderRotation"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .rotate(borderRotation)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                glowColor.copy(alpha = 0.7f),
                                Color.Transparent,
                                glowColor.copy(alpha = 0.7f),
                                Color.Transparent,
                                glowColor.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 按下波纹
        if (isPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 优化版消防站背景 - 带视差效果
 */
@Composable
private fun FireStationBackgroundOptimized(
    parallaxOffset: Offset,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnimations")

    // 云朵浮动
    val cloud1X by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1X"
    )
    val cloud2X by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2X"
    )

    // 烟雾上升
    val smokeY1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "smokeY1"
    )
    val smokeAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smokeAlpha1"
    )

    // 星星闪烁
    val starAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ),
        label = "starAlpha1"
    )
    val starAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(700)
        ),
        label = "starAlpha2"
    )
    val starAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1400)
        ),
        label = "starAlpha3"
    )

    // 火焰跳动
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    Box(modifier = modifier) {
        // 背景云朵 - 带视差
        val parallaxX = parallaxOffset.x * 30
        val parallaxY = parallaxOffset.y * 30

        Text(
            text = "☁️",
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-60 + cloud1X + parallaxX).dp, y = (70 + parallaxY).dp)
                .alpha(0.1f)
        )
        Text(
            text = "☁️",
            fontSize = 96.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (40 + cloud2X - parallaxX).dp, y = (120 - parallaxY).dp)
                .alpha(0.08f)
        )

        // 消防站建筑 - 带视差
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (50 - parallaxX * 0.5f).dp, y = (-30 + parallaxY * 0.5f).dp)
                .alpha(0.06f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "🚒",
                fontSize = 140.sp,
                modifier = Modifier.offset(x = 25.dp)
            )
            Text(
                text = "🏢",
                fontSize = 90.sp
            )
        }

        // 星星装饰
        data class StarInfo(
            val alignment: Alignment,
            val xOffset: Dp,
            val yOffset: Dp,
            val alpha: Float
        )

        val starPositions = listOf(
            StarInfo(Alignment.TopEnd, (-100).dp, 220.dp, starAlpha1),
            StarInfo(Alignment.TopStart, 80.dp, 180.dp, starAlpha2),
            StarInfo(Alignment.BottomEnd, (-70).dp, (-280).dp, starAlpha3),
        )

        starPositions.forEach { (alignment, xOffset, yOffset, alpha) ->
            Text(
                text = "⭐",
                fontSize = (24..36).random().sp,
                modifier = Modifier
                    .align(alignment)
                    .offset(x = xOffset, y = yOffset)
                    .alpha(alpha * 0.18f)
            )
        }

        // 底部火焰装饰
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-30).dp)
                .alpha(0.1f),
            horizontalArrangement = Arrangement.spacedBy((-10).dp)
        ) {
            repeat(12) { index ->
                val delayOffset = index * 40
                val localFlameScale by infiniteTransition.animateFloat(
                    initialValue = 0.75f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 350 + delayOffset,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flame$index"
                )
                Text(
                    text = "🔥",
                    fontSize = 32.sp,
                    modifier = Modifier.scale(localFlameScale)
                )
            }
        }

        // 烟雾效果
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-80).dp, y = 160.dp)
                .alpha(smokeAlpha1 * 0.12f)
        ) {
            Text(
                text = "💨",
                fontSize = 48.sp,
                modifier = Modifier.offset(y = smokeY1.dp)
            )
        }
    }
}

/**
 * 增强版徽章动画覆盖层
 */
@Composable
private fun EnhancedBadgeAnimationOverlay(
    show: Boolean,
    device: FireStationDevice?,
    onAnimationComplete: () -> Unit
) {
    val badgeScale by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "badgeScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "badgeAnimations")

    // 星星旋转
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )

    // 星星闪烁
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    // 彩带旋转
    val confettiRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiRotation"
    )

    // 徽章浮动
    val badgeFloat by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeFloat"
    )

    // 光环脉冲
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(350)),
        exit = fadeOut(animationSpec = tween(350))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE63946).copy(alpha = 0.94f),
                            Color(0xFFF77F00).copy(alpha = 0.94f),
                            Color(0xFFFCBF49).copy(alpha = 0.94f)
                        )
                    )
                )
                .clickable(onClick = onAnimationComplete),
            contentAlignment = Alignment.Center
        ) {
            // 背景装饰
            Box(modifier = Modifier.fillMaxSize()) {
                // 旋转星星
                listOf(0f, 120f, 240f).forEach { offset ->
                    Text(
                        text = "⭐",
                        fontSize = 72.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 0.dp, y = (-200).dp)
                            .scale(1f * badgeScale)
                            .rotate(starRotation + offset)
                            .alpha(starAlpha * 0.3f)
                    )
                }

                // 彩带效果
                val confetti = listOf("🎉", "🎊", "✨", "⭐", "🌟", "💫")
                confetti.forEachIndexed { index, emoji ->
                    val angle = (index * 60f)
                    val distance = 220.dp
                    val radius = distance * badgeScale

                    Text(
                        text = emoji,
                        fontSize = 42.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(
                                x = (sin(Math.toRadians(angle.toDouble())) * radius.value).dp,
                                y = (cos(Math.toRadians(angle.toDouble())) * radius.value).dp
                            )
                            .rotate(confettiRotation + angle)
                            .alpha(starAlpha * 0.45f)
                    )
                }
            }

            // 主内容
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(badgeScale)
                    .offset(y = badgeFloat.dp)
            ) {
                // 大徽章 - 带光环
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 脉冲光环
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(haloPulse)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 徽章
                    Text(
                        text = "🏅",
                        fontSize = 180.sp,
                        modifier = Modifier.rotate(starRotation * 0.08f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 标题 - 脉冲效果
                val titleScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "titleScale"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.scale(titleScale)
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 54.sp
                    )
                    Text(
                        text = "太棒了！",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.shadow(
                            elevation = 8.dp,
                            spotColor = Color.Black.copy(alpha = 0.35f)
                        )
                    )
                    Text(
                        text = "🎉",
                        fontSize = 54.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "你获得了新徽章！",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.98f),
                    modifier = Modifier.shadow(
                        elevation = 5.dp,
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 设备名称卡片
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color.Black.copy(alpha = 0.3f),
                            ambientColor = Color.Black.copy(alpha = 0.18f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.28f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getDeviceIcon(device ?: FireStationDevice.FIRE_HYDRANT),
                            fontSize = 38.sp
                        )
                        Text(
                            text = device?.displayName ?: "",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD93D),
                            modifier = Modifier.shadow(
                                elevation = 4.dp,
                                spotColor = Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                // 继续按钮
                val buttonPulse by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "buttonPulse"
                )

                Box(
                    modifier = Modifier
                        .scale(buttonPulse)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.92f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .size(180.dp, 72.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .clickable(onClick = onAnimationComplete),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "继续",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE63946)
                        )
                        Text(
                            text = "→",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE63946)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "或点击任意处继续",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * 粒子火焰背景效果
 */
private fun drawFireParticles(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    parallaxOffset: Offset,
    density: androidx.compose.ui.unit.Density
) {
    val particleCount = 15
    val time = System.currentTimeMillis() / 1000f

    with(drawScope) {
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }

            repeat(particleCount) { index ->
                val progress = (index.toFloat() / particleCount + time * 0.2f) % 1f
                val x = size.width * (0.1f + (index % 5) * 0.2f) + parallaxOffset.x * 20 * density.density
                val y = size.height * (1f - progress * 0.8f) + parallaxOffset.y * 10 * density.density

                val alpha = (1f - progress) * 0.15f
                val size = (12 + progress * 8) * density.density

                paint.color = android.graphics.Color.argb(
                    (alpha * 255).toInt(),
                    255,
                    (150 + progress * 50).toInt(),
                    50
                )

                canvas.nativeCanvas.drawCircle(
                    x,
                    y,
                    size,
                    paint
                )
            }
        }
    }
}

/**
 * 视频播放覆盖层
 */
@Composable
private fun VideoPlayerOverlay(
    device: FireStationDevice?,
    onPlaybackComplete: (FireStationDevice) -> Unit
) {
    val videoPath = when (device) {
        FireStationDevice.FIRE_HYDRANT -> "videos/firehydrant_cartoon.mp4"
        FireStationDevice.LADDER_TRUCK -> "videos/fireladder_truck_cartoon.mp4"
        FireStationDevice.FIRE_EXTINGUISHER -> "videos/firefighter_cartoon.mp4"
        FireStationDevice.WATER_HOSE -> "videos/firenozzle_cartoon.mp4"
        null -> return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        VideoPlayer(
            videoPath = videoPath,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(36.dp),
            onPlaybackCompleted = {
                onPlaybackComplete(device)
            },
            autoPlay = true,
            showControls = false
        )
    }
}

/**
 * 空闲提示覆盖层
 */
@Composable
private fun IdleHintOverlay(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    val infiniteTransition = rememberInfiniteTransition(label = "idleHintPulse")

    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintScale"
    )

    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
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
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(hintScale)
                .alpha(hintAlpha)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                    ambientColor = Color.Black.copy(alpha = 0.25f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE63946).copy(alpha = 0.96f),
                            Color(0xFFF77F00).copy(alpha = 0.96f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
                .padding(horizontal = 64.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "🐯",
                    fontSize = 96.sp,
                    modifier = Modifier.scale(hintScale)
                )

                Text(
                    text = "需要帮忙吗？",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "点击屏幕任意位置继续",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.alpha(hintAlpha)
                ) {
                    repeat(3) {
                        Text(
                            text = "⭐",
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取设备图标
 */
private fun getDeviceIcon(device: FireStationDevice): String {
    return when (device) {
        FireStationDevice.FIRE_HYDRANT -> "🚒"
        FireStationDevice.LADDER_TRUCK -> "🪜"
        FireStationDevice.FIRE_EXTINGUISHER -> "🔥"
        FireStationDevice.WATER_HOSE -> "💦"
    }
}

/**
 * 扩展函数：绘制背景内容
 */
private fun Modifier.drawBehind(onDraw: ContentDrawScope.() -> Unit): Modifier =
    this.then(
        Modifier.drawWithContent {
            drawContent()
            onDraw()
        }
    )
