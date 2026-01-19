package com.cryallen.tigerfire.ui.firestation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FireStationEffect.PlayVideo -> {
                    // TODO: Task 4.10 实现 VideoPlayer 后播放视频
                    // videoPlayer.play(effect.videoResource)
                }
                is FireStationEffect.NavigateToMap -> onNavigateBack()
                is FireStationEffect.ShowBadgeAnimation -> {
                    // 徽章动画在 showBadgeAnimation 状态中处理
                }
                is FireStationEffect.PlayClickSound,
                is FireStationEffect.PlayBadgeSound,
                is FireStationEffect.PlayAllCompletedSound -> {
                    // TODO: Task 4.9/4.10 中集成音效播放
                }
                is FireStationEffect.UnlockSchoolScene -> {
                    // 学校场景已解锁，在进度中自动处理
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF6B6B)) // 消防站红背景
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮）
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Text(
                        text = "←",
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }
            }

            // 中央设备区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 标题
                Text(
                    text = "消防站",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "点击设备学习消防知识",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 设备网格（2x2）
                DeviceGrid(
                    completedDevices = state.completedDevices,
                    isPlayingVideo = state.isPlayingVideo,
                    onDeviceClick = { device ->
                        viewModel.onEvent(FireStationEvent.DeviceClicked(device))
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 完成进度提示
                Text(
                    text = "已完成: ${state.completedDevices.size}/4",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // 全部完成提示
                if (state.isAllCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "🎉 全部完成！学校场景已解锁！",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        // 左列
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            devices.slice(0..1).forEach { device ->
                DeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) }
                )
            }
        }

        // 右列
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            devices.slice(2..3).forEach { device ->
                DeviceCard(
                    device = device,
                    isCompleted = device in completedDevices,
                    isEnabled = !isPlayingVideo,
                    onClick = { onDeviceClick(device) }
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
 */
@Composable
private fun DeviceCard(
    device: FireStationDevice,
    isCompleted: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .shadow(
                elevation = if (isCompleted) 12.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isCompleted) Color.Yellow else Color.Gray
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isCompleted) Color(0xFFFFD700) else Color.White
            )
            .then(
                if (isEnabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.size(120.dp)
        ) {
            // 设备图标
            Text(
                text = getDeviceIcon(device),
                fontSize = 48.sp,
                modifier = Modifier.size(80.dp, 80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 设备名称
            Text(
                text = device.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            // 完成标记
            if (isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐",
                    fontSize = 24.sp
                )
            }
        }
    }
}

/**
 * 获取设备图标
 */
private fun getDeviceIcon(device: FireStationDevice): String {
    return when (device) {
        FireStationDevice.FIRE_HYDRANT -> "🚿"
        FireStationDevice.LADDER_TRUCK -> "🪜"
        FireStationDevice.FIRE_EXTINGUISHER -> "🧯"
        FireStationDevice.WATER_HOSE -> "💧"
    }
}

/**
 * 视频播放覆盖层（占位符）
 *
 * @param device 当前播放的设备
 * @param onPlaybackComplete 播放完成回调
 */
@Composable
private fun VideoPlayerOverlay(
    device: FireStationDevice?,
    onPlaybackComplete: (FireStationDevice) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp, 200.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📹",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "正在播放教学视频...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = device?.displayName ?: "",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                // 模拟播放完成（实际视频完成后会自动调用）
                Text(
                    text = "播放中...",
                    fontSize = 14.sp,
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        device?.let { onPlaybackComplete(it) }
                    }
                )
            }
        }
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
    AnimatedVisibility(
        visible = show,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 徽章图标
                Text(
                    text = "🏅",
                    fontSize = 120.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "获得徽章！",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = device?.displayName ?: "",
                    fontSize = 24.sp,
                    color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 点击关闭
                Text(
                    text = "点击继续",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.clickable(onClick = onAnimationComplete)
                )
            }
        }
    }
}
