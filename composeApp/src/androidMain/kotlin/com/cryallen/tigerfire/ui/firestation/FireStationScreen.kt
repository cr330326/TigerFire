package com.cryallen.tigerfire.ui.firestation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.firestation.FireStationDevice
import com.cryallen.tigerfire.presentation.firestation.FireStationEffect
import com.cryallen.tigerfire.presentation.firestation.FireStationEvent
import com.cryallen.tigerfire.presentation.firestation.FireStationViewModel

/**
 * 消防站场景 Screen
 *
 * 显示4个设备图标，点击播放教学视频，完成后点亮星星
 *
 * @param viewModel FireStationViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun FireStationScreen(
    viewModel: FireStationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FireStationEffect.PlayVideo -> {
                    // VideoPlayer 由状态驱动，无需额外处理
                }
                is FireStationEffect.NavigateToMap -> onNavigateBack()
                is FireStationEffect.ShowBadgeAnimation -> {
                    // 徽章动画在 showBadgeAnimation 状态中处理
                }
                is FireStationEffect.PlayClickSound -> {
                    audioManager.playClickSound(com.cryallen.tigerfire.domain.model.SceneType.FIRE_STATION)
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
                    // TODO: 添加语音资源文件并取消注释
                    // audioManager.playVoice("voice/slow_down.mp3")
                }
                is FireStationEffect.ShowIdleHint -> {
                    // 显示空闲提示：小火"需要帮忙吗？"
                    // TODO: 实现 UI 提示显示逻辑
                }
            }
        }
    }

    // 消防站主题配色 - 更丰富的层次
    val gradientColors = listOf(
        Color(0xFFE63946),  // 消防红
        Color(0xFFF77F00),  // 橙色
        Color(0xFFFCBF49),  // 暖黄色
        Color(0xFFEAE2B7)   // 米黄色底部
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // 消防站装饰性背景元素
        FireStationBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮）- 卡通风格
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val returnScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "returnScale"
                )

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .scale(returnScale)
                        .size(56.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.18f),
                            ambientColor = Color.Black.copy(alpha = 0.12f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.95f)
                                )
                            ),
                            CircleShape
                        )
                ) {
                    Text(
                        text = "←",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE63946)
                    )
                }
            }

            // 中央设备区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 标题区域 - 卡通风格
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 装饰火焰图标
                    val flameAnimation = rememberInfiniteTransition(label = "titleFlame")
                    val flameScale by flameAnimation.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "flameScale"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 48.sp,
                            modifier = Modifier.scale(flameScale)
                        )
                        Text(
                            text = "消防站",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.shadow(
                                elevation = 6.dp,
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            )
                        )
                        Text(
                            text = "🔥",
                            fontSize = 48.sp,
                            modifier = Modifier.scale(flameScale)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 副标题
                    Text(
                        text = "点击设备学习消防知识",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.shadow(
                            elevation = 3.dp,
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 设备网格（2x2）
                DeviceGrid(
                    completedDevices = state.completedDevices,
                    isPlayingVideo = state.isPlayingVideo,
                    onDeviceClick = { device ->
                        viewModel.onEvent(FireStationEvent.DeviceClicked(device))
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // 完成进度提示卡片 - 增强设计
                val progressAnimation = rememberInfiniteTransition(label = "progressPulse")
                val progressScale by progressAnimation.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "progressScale"
                )

                Box(
                    modifier = Modifier
                        .scale(progressScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color.Black.copy(alpha = 0.18f),
                            ambientColor = Color.Black.copy(alpha = 0.1f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 36.dp, vertical = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 进度文本
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐",
                                fontSize = 24.sp
                            )
                            Text(
                                text = "已完成: ${state.completedDevices.size}/4",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.shadow(
                                    elevation = 3.dp,
                                    spotColor = Color.Black.copy(alpha = 0.2f)
                                )
                            )
                            Text(
                                text = "⭐",
                                fontSize = 24.sp
                            )
                        }

                        // 全部完成提示 - 更醒目的效果
                        if (state.isAllCompleted) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // 庆祝动画
                            val celebrateScale by progressAnimation.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "celebrateScale"
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.scale(celebrateScale)
                            ) {
                                Text(
                                    text = "🎉",
                                    fontSize = 22.sp
                                )
                                Text(
                                    text = "太棒了！全部完成！",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD93D),
                                    modifier = Modifier.shadow(
                                        elevation = 3.dp,
                                        spotColor = Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                                Text(
                                    text = "🎉",
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }
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

        // 徽章收集动画
        BadgeAnimationOverlay(
            show = state.showBadgeAnimation,
            device = state.earnedBadgeDevice,
            onAnimationComplete = {
                viewModel.onEvent(FireStationEvent.BadgeAnimationCompleted)
            }
        )
    }
}

/**
 * 消防站装饰性背景组件
 * 卡通风格的消防站场景装饰
 */
@Composable
private fun FireStationBackground() {
    // 多层动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnimations")

    // 云朵浮动 - 更自然的多层移动
    val cloud1X by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1X"
    )
    val cloud2X by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2X"
    )

    // 烟雾上升动画
    val smokeY1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "smokeY1"
    )
    val smokeAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smokeAlpha1"
    )

    // 星星闪烁
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    // 火焰跳动
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景云朵层 - 柔和的氛围
        Text(
            text = "☁️",
            fontSize = 72.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-50 + cloud1X).dp, y = 60.dp)
                .alpha(0.12f)
        )
        Text(
            text = "☁️",
            fontSize = 88.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (30 + cloud2X).dp, y = 100.dp)
                .alpha(0.1f)
        )
        Text(
            text = "☁️",
            fontSize = 56.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-30 + cloud2X * 0.5f).dp, y = (-180).dp)
                .alpha(0.08f)
        )

        // 消防站建筑剪影 - 使用emoji组合
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .alpha(0.08f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "🚒",
                fontSize = 120.sp,
                modifier = Modifier.offset(x = 20.dp)
            )
            Text(
                text = "🏢",
                fontSize = 80.sp
            )
        }

        // 左下角装饰 - 消防栓
        Text(
            text = "🔥",
            fontSize = 90.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = (-30).dp)
                .alpha(0.08f)
        )

        // 星星和闪光装饰 - 分布在四角
        val starPositions = listOf(
            Triple(Alignment.TopEnd, (-80).dp, 200.dp),
            Triple(Alignment.TopStart, 60.dp, 150.dp),
            Triple(Alignment.BottomEnd, (-50).dp, (-260).dp),
            Triple(Alignment.CenterStart, 30.dp, 0.dp),
        )

        starPositions.forEach { (alignment, xOffset, yOffset) ->
            Text(
                text = "⭐",
                fontSize = (20..32).random().sp,
                modifier = Modifier
                    .align(alignment)
                    .offset(x = xOffset, y = yOffset)
                    .alpha(starAlpha * 0.25f)
            )
        }

        // 闪光效果
        Text(
            text = "✨",
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 100.dp, y = 220.dp)
                .alpha(starAlpha * 0.2f)
        )

        // 底部火焰装饰条 - 卡通风格
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-25).dp)
                .alpha(0.12f),
            horizontalArrangement = Arrangement.spacedBy((-8).dp)
        ) {
            repeat(10) { index ->
                val delayOffset = index * 50
                val localFlameScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 400 + delayOffset,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flame$index"
                )
                Text(
                    text = "🔥",
                    fontSize = 28.sp,
                    modifier = Modifier.scale(localFlameScale)
                )
            }
        }

        // 右上角烟雾效果 - 模拟消防站场景
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-60).dp, y = 140.dp)
                .alpha(smokeAlpha1 * 0.15f)
        ) {
            Text(
                text = "💨",
                fontSize = 40.sp,
                modifier = Modifier.offset(y = smokeY1.dp)
            )
        }
    }
}

/**
 * 设备网格（2x2）
 * 增强的动画效果和布局
 *
 * @param completedDevices 已完成的设备集合
 * @param isPlayingVideo 是否正在播放视频
 * @param onDeviceClick 设备点击回调
 */
@Composable
private fun DeviceGrid(
    completedDevices: Set<FireStationDevice>,
    isPlayingVideo: Boolean,
    onDeviceClick: (FireStationDevice) -> Unit
) {
    val devices = FireStationDevice.entries

    // 入场动画 - 依次出现
    val enterTransition = rememberInfiniteTransition(label = "deviceEntry")
    val animatedIndices = listOf(0, 1, 2, 3).map { index ->
        index to remember { androidx.compose.animation.core.Animatable(0f) }
    }

    // 触发入场动画
    LaunchedEffect(Unit) {
        animatedIndices.forEachIndexed { i, (index, anim) ->
            kotlinx.coroutines.delay(i * 100L)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // 按钮呼吸浮动动画
    val floatAnimation = rememberInfiniteTransition(label = "buttonFloat")
    val floatOffset by floatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // 按钮缩放呼吸效果
    val pulseScale by floatAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        // 左列
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            devices.slice(0..1).forEachIndexed { index, device ->
                val (_, anim) = animatedIndices[index]
                DeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) },
                    floatOffset = floatOffset,
                    pulseScale = pulseScale,
                    enterProgress = anim.value,
                    index = index
                )
            }
        }

        // 右列
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            devices.slice(2..3).forEachIndexed { index, device ->
                val (_, anim) = animatedIndices[index + 2]
                DeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) },
                    floatOffset = floatOffset * 0.8f,
                    pulseScale = pulseScale,
                    enterProgress = anim.value,
                    index = index + 2
                )
            }
        }
    }
}

/**
 * 设备卡片组件
 * 增强的视觉效果、动画和交互反馈
 *
 * @param device 设备类型
 * @param isCompleted 是否已完成
 * @param isEnabled 是否可点击
 * @param onClick 点击回调
 * @param floatOffset 浮动偏移量
 * @param pulseScale 呼吸缩放
 * @param enterProgress 入场动画进度
 * @param index 索引（用于动画延迟）
 */
@Composable
private fun DeviceCard(
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

    // 图标旋转动画（持续的轻微旋转）
    val infiniteTransition = rememberInfiniteTransition(label = "iconRotate")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + index * 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation$index"
    )

    // 按下时的缩放和旋转
    val pressScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isCompleted -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // 完成状态的旋转效果
    val completionRotation by animateFloatAsState(
        targetValue = if (isCompleted) 8f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "completionRotation"
    )

    // 背景亮度变化
    val backgroundBrightness by animateFloatAsState(
        targetValue = when {
            !isEnabled -> 0.6f
            isPressed -> 0.9f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "backgroundBrightness"
    )

    // 卡片颜色 - 更丰富的渐变色调
    val cardGradient = when (device) {
        FireStationDevice.FIRE_HYDRANT -> listOf(
            Color(0xFFFFE066), // 金黄色
            Color(0xFFFFB347)  // 橙色
        )
        FireStationDevice.LADDER_TRUCK -> listOf(
            Color(0xFFFF6B9D), // 粉红色
            Color(0xFFC44569)  // 深粉色
        )
        FireStationDevice.FIRE_EXTINGUISHER -> listOf(
            Color(0xFF6BCB77), // 绿色
            Color(0xFF4D8076)  // 深绿色
        )
        FireStationDevice.WATER_HOSE -> listOf(
            Color(0xFF4ECDC4), // 青色
            Color(0xFF44A08D)  // 深青色
        )
    }

    // 完成状态的光晕颜色
    val glowColor = when (device) {
        FireStationDevice.FIRE_HYDRANT -> Color(0xFFFFD700)
        FireStationDevice.LADDER_TRUCK -> Color(0xFFFF69B4)
        FireStationDevice.FIRE_EXTINGUISHER -> Color(0xFF00FF7F)
        FireStationDevice.WATER_HOSE -> Color(0xFF00CED1)
    }

    // 入场动画的缩放和透明度
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
            .size(160.dp)
            .scale(enterScale * pulseScale)
            .alpha(enterProgress)
            .offset(y = floatOffset.dp)
            .shadow(
                elevation = if (isPressed) 4.dp else if (isCompleted) 16.dp else 10.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (isCompleted) glowColor.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.12f),
                ambientColor = if (isCompleted) glowColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = cardGradient.map { it.copy(alpha = backgroundBrightness) },
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
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
        // 内部内容容器
        Box(
            modifier = Modifier
                .scale(pressScale)
                .rotate(completionRotation),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                // 图标容器 - 添加光晕效果
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 图标背景圆圈
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            glowColor.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // 设备图标
                    Text(
                        text = getDeviceIcon(device),
                        fontSize = 56.sp,
                        modifier = Modifier.rotate(iconRotation)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 设备名称 - 更大的字体
                Text(
                    text = device.displayName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.shadow(
                        elevation = 2.dp,
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                )

                // 完成标记 - 更醒目的星星和文字
                if (isCompleted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 旋转的星星
                        val starRotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "starRotation"
                        )
                        Text(
                            text = "⭐",
                            fontSize = 22.sp,
                            modifier = Modifier.rotate(starRotation)
                        )
                        Text(
                            text = "已完成",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = glowColor,
                            modifier = Modifier.shadow(
                                elevation = 2.dp,
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        // 完成状态的旋转边框高亮
        if (isCompleted) {
            val borderRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "borderRotation"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .rotate(borderRotation)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                glowColor.copy(alpha = 0.6f),
                                Color.Transparent,
                                glowColor.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 按下时的波纹效果
        if (isPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 获取设备图标 - 使用更生动的emoji
 */
private fun getDeviceIcon(device: FireStationDevice): String {
    return when (device) {
        FireStationDevice.FIRE_HYDRANT -> "🚒"      // 消防车
        FireStationDevice.LADDER_TRUCK -> "🪜"     // 梯子
        FireStationDevice.FIRE_EXTINGUISHER -> "🔥" // 火焰（灭火器场景）
        FireStationDevice.WATER_HOSE -> "💦"       // 水花
    }
}

/**
 * 视频播放覆盖层
 *
 * 使用 VideoPlayer 组件播放教学视频
 *
 * @param device 当前播放的设备
 * @param onPlaybackComplete 播放完成回调
 */
@Composable
private fun VideoPlayerOverlay(
    device: FireStationDevice?,
    onPlaybackComplete: (FireStationDevice) -> Unit
) {
    // 设备对应的视频文件路径
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
                onPlaybackComplete(device)
            },
            autoPlay = true,
            showControls = false
        )
    }
}

/**
 * 徽章收集动画覆盖层
 * 增强的卡通风格效果
 *
 * @param show 是否显示
 * @param device 获得徽章的设备
 * @param onAnimationComplete 动画完成回调
 */
@Composable
private fun BadgeAnimationOverlay(
    show: Boolean,
    device: FireStationDevice?,
    onAnimationComplete: () -> Unit
) {
    // 徽章缩放动画 - 弹性效果
    val badgeScale by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "badgeScale"
    )

    // 无限旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "badgeAnimations")

    // 星星旋转 - 内层
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )

    // 星星闪烁
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    // 彩带效果
    val confettiRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiRotation"
    )

    // 徽章浮动
    val badgeFloat by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeFloat"
    )

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE63946).copy(alpha = 0.92f),
                            Color(0xFFF77F00).copy(alpha = 0.92f),
                            Color(0xFFFCBF49).copy(alpha = 0.92f)
                        )
                    )
                )
                .clickable(onClick = onAnimationComplete),
            contentAlignment = Alignment.Center
        ) {
            // 背景装饰元素
            Box(modifier = Modifier.fillMaxSize()) {
                // 旋转的星星装饰
                listOf(
                    Pair(0f, 0f),
                    Pair(120f, 1f),
                    Pair(240f, 0.5f)
                ).forEach { (offset, scale) ->
                    Text(
                        text = "⭐",
                        fontSize = 60.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 0.dp, y = (-180).dp)
                            .scale(scale * badgeScale)
                            .rotate(starRotation + offset)
                            .alpha(starAlpha * 0.25f)
                    )
                }

                // 彩带效果
                val confetti = listOf("🎉", "🎊", "✨", "⭐", "🌟")
                confetti.forEachIndexed { index, emoji ->
                    val angle = (index * 72f)
                    val distance = 200.dp
                    val radius = distance * badgeScale

                    Text(
                        text = emoji,
                        fontSize = 36.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(
                                x = (kotlin.math.sin(Math.toRadians(angle.toDouble())) * radius.value).dp,
                                y = (kotlin.math.cos(Math.toRadians(angle.toDouble())) * radius.value).dp
                            )
                            .rotate(confettiRotation + angle)
                            .alpha(starAlpha * 0.4f)
                    )
                }
            }

            // 主内容区域
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(badgeScale)
                    .offset(y = badgeFloat.dp)
            ) {
                // 大徽章图标
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 光晕效果
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 徽章
                    Text(
                        text = "🏅",
                        fontSize = 160.sp,
                        modifier = Modifier
                            .rotate(starRotation * 0.1f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 标题动画
                val titleScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "titleScale"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.scale(titleScale)
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "太棒了！",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.shadow(
                            elevation = 6.dp,
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = "🎉",
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "你获得了新徽章！",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.98f),
                    modifier = Modifier.shadow(
                        elevation = 4.dp,
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 设备名称卡片 - 渐变背景
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color.Black.copy(alpha = 0.25f),
                            ambientColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.25f)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 40.dp, vertical = 20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getDeviceIcon(device ?: FireStationDevice.FIRE_HYDRANT),
                            fontSize = 32.sp
                        )
                        Text(
                            text = device?.displayName ?: "",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD93D),
                            modifier = Modifier.shadow(
                                elevation = 3.dp,
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))

                // 继续按钮 - 卡通风格
                val buttonPulse by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "buttonPulse"
                )

                Box(
                    modifier = Modifier
                        .scale(buttonPulse)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(30.dp),
                            spotColor = Color.Black.copy(alpha = 0.25f)
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.95f)
                                )
                            ),
                            RoundedCornerShape(30.dp)
                        )
                        .size(160.dp, 64.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .clickable(onClick = onAnimationComplete),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "继续",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE63946)
                        )
                        Text(
                            text = "→",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE63946)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "或点击任意处继续",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
