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

    // 颜色规范 - 儿童友好的温暖色调
    val gradientColors = listOf(
        Color(0xFFFF6B6B),  // 珊瑚红
        Color(0xFFFF8E72),  // 橙红色
        Color(0xFFFFAA64)   // 暖橙色
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
        // 装饰性背景元素
        DecorativeBackgroundElements()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.15f),
                            ambientColor = Color.Black.copy(alpha = 0.1f)
                        )
                        .background(Color.White, CircleShape)
                ) {
                    Text(
                        text = "←",
                        fontSize = 26.sp,
                        color = Color(0xFFFF6B6B)
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
                // 标题区域带阴影效果
                Text(
                    text = "🔥 消防站 🔥",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "点击设备学习消防知识",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )

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

                // 完成进度提示卡片
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "已完成: ${state.completedDevices.size}/4",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // 全部完成提示
                        if (state.isAllCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🎉 太棒了！消防站场景已解锁！",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD93D)
                            )
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
 * 装饰性背景元素组件
 * 添加浮动的云朵和星星装饰
 */
@Composable
private fun DecorativeBackgroundElements() {
    // 云朵浮动动画
    val cloudFloatAnimation = rememberInfiniteTransition(label = "cloudFloat")
    val cloud1Offset by cloudFloatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1"
    )
    val cloud2Offset by cloudFloatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2"
    )

    // 星星闪烁动画
    val starTwinkleAnimation = rememberInfiniteTransition(label = "starTwinkle")
    val starAlpha by starTwinkleAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 装饰云朵 - 左上
        Text(
            text = "☁️",
            fontSize = 48.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = 80.dp + cloud1Offset.dp)
                .alpha(0.25f)
        )

        // 装饰云朵 - 右上
        Text(
            text = "☁️",
            fontSize = 64.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 120.dp + cloud2Offset.dp)
                .alpha(0.2f)
        )

        // 装饰云朵 - 左下
        Text(
            text = "☁️",
            fontSize = 56.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = (-200).dp + cloud2Offset.dp)
                .alpha(0.15f)
        )

        // 装饰星星 - 散落分布
        Text(
            text = "⭐",
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-60).dp, y = 250.dp)
                .alpha(starAlpha * 0.3f)
        )

        Text(
            text = "✨",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 50.dp, y = 180.dp)
                .alpha(starAlpha * 0.25f)
        )

        Text(
            text = "⭐",
            fontSize = 28.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-280).dp)
                .alpha(starAlpha * 0.2f)
        )

        // 底部装饰波浪效果（使用emoji模拟）
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-30).dp)
                .alpha(0.15f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(8) {
                Text(
                    text = "🔥",
                    fontSize = 32.sp,
                    modifier = Modifier.scale(0.8f)
                )
            }
        }
    }
}

/**
 * 设备网格（2x2）
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

    // 按钮浮动动画
    val floatAnimation = rememberInfiniteTransition(label = "buttonFloat")
    val floatOffset by floatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        // 左列
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            devices.slice(0..1).forEachIndexed { index, device ->
                DeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) },
                    floatOffset = if (index == 0) floatOffset else 0f
                )
            }
        }

        // 右列
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            devices.slice(2..3).forEachIndexed { index, device ->
                DeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) },
                    floatOffset = if (index == 0) floatOffset * 0.7f else 0f
                )
            }
        }
    }
}

/**
 * 设备卡片组件
 *
 * @param device 设备类型
 * @param isCompleted 是否已完成
 * @param isEnabled 是否可点击
 * @param onClick 点击回调
 * @param floatOffset 浮动偏移量
 */
@Composable
private fun DeviceCard(
    device: FireStationDevice,
    isCompleted: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    floatOffset: Float = 0f
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 完成状态：轻微放大 + 按下状态：轻微缩小
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isCompleted -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // 旋转动画（完成时）
    val rotation by animateFloatAsState(
        targetValue = if (isCompleted) 5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    // 按下时背景色稍微变暗
    val backgroundColor by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(),
        label = "backgroundColor"
    )

    // 卡片颜色 - 每个设备有不同颜色
    val cardColor = when (device) {
        FireStationDevice.FIRE_HYDRANT -> Color(0xFFFFEE94)
        FireStationDevice.LADDER_TRUCK -> Color(0xFFFF94B5)
        FireStationDevice.FIRE_EXTINGUISHER -> Color(0xFF94FFD7)
        FireStationDevice.WATER_HOSE -> Color(0xFFFFB794)
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .offset(y = floatOffset.dp)
            .shadow(
                elevation = if (isPressed) 6.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isCompleted) Color(0xFFFFD93D) else Color.Black.copy(alpha = 0.15f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                cardColor.copy(alpha = backgroundColor)
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
        Box(
            modifier = Modifier
                .scale(scale)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp)
            ) {
                // 设备图标 - 更大的卡通emoji
                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getDeviceIcon(device),
                        fontSize = 52.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 设备名称
                Text(
                    text = device.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                // 完成标记（星星）
                if (isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "完成",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
            }
        }

        // 完成状态的边框高亮
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFD93D).copy(alpha = 0.5f),
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
    // 徽章缩放动画
    val badgeScale by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "badgeScale"
    )

    // 星星旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "starRotation")
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B6B).copy(alpha = 0.85f),
                            Color(0xFFFFAA64).copy(alpha = 0.85f)
                        )
                    )
                )
                .clickable(onClick = onAnimationComplete),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 旋转的星星装饰
                Box(
                    modifier = Modifier.scale(badgeScale)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 80.sp,
                        modifier = Modifier
                            .rotate(starRotation)
                            .alpha(0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 徽章图标
                Text(
                    text = "🏅",
                    fontSize = 140.sp,
                    modifier = Modifier.scale(badgeScale)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "太棒了！",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "你获得了新徽章！",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 设备名称卡片
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = device?.displayName ?: "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD93D)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 继续按钮
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .background(Color.White, CircleShape)
                        .size(140.dp, 56.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAnimationComplete),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "继续 →",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "或点击任意处继续",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
