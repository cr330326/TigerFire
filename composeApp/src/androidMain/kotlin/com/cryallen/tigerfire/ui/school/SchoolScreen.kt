package com.cryallen.tigerfire.ui.school

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.school.SchoolEffect
import com.cryallen.tigerfire.presentation.school.SchoolEvent
import com.cryallen.tigerfire.presentation.school.SchoolViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 学校场景 Screen
 *
 * 自动播放剧情动画，展示火灾应急流程
 * 动画播放完毕后颁发徽章并解锁森林场景
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

    // 警报红光闪烁动画透明度
    var alertAlpha by remember { mutableFloatStateOf(0f) }

    // 自动触发页面进入事件
    LaunchedEffect(Unit) {
        viewModel.onEvent(SchoolEvent.ScreenEntered)

        // 播放警报音效
        audioManager.playAlertSound()

        // 警报红光闪烁动画（柔和脉冲）
        while (true) {
            // 渐入
            for (i in 0..10) {
                alertAlpha = i * 0.03f  // 最大 0.3，避免刺眼
                delay(50)
            }
            // 渐出
            for (i in 10 downTo 0) {
                alertAlpha = i * 0.03f
                delay(50)
            }
            delay(500)  // 停顿
        }
    }

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SchoolEffect.PlayAnimation -> {
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
                    audioManager.stopAlertSound()
                    onNavigateBack()
                }
                is SchoolEffect.PlaySlowDownVoice -> {
                    // 播放"慢一点"语音提示
                    // TODO: 添加语音资源文件并取消注释
                    // audioManager.playVoice("voice/slow_down.mp3")
                }
                is SchoolEffect.ShowIdleHint -> {
                    // 显示空闲提示：小火"需要帮忙吗？"
                    // TODO: 实现 UI 提示显示逻辑
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF457B9D)) // 学校蓝背景
    ) {
        // 警报红光闪烁效果（屏幕边缘）
        if (state.isPlayingAnimation) {
            AlertFlashOverlay(alpha = alertAlpha)
        }

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
                    onClick = {
                        viewModel.onEvent(SchoolEvent.BackToMapClicked)
                    },
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

            // 中央内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 场景标题
                Text(
                    text = "学校",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "消防安全情景动画",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 动画播放器区域
                AnimationPlayerArea(
                    isPlaying = state.isPlayingAnimation,
                    isCompleted = state.isCompleted,
                    onPlaybackComplete = {
                        viewModel.onEvent(SchoolEvent.AnimationPlaybackCompleted)
                    }
                )
            }
        }

        // 徽章收集动画覆盖层
        BadgeAnimationOverlay(
            show = state.showBadgeAnimation,
            onAnimationComplete = {
                viewModel.onEvent(SchoolEvent.BadgeAnimationCompleted)
            }
        )
    }
}

/**
 * 警报红光闪烁覆盖层
 *
 * 柔和的红色脉冲效果，不刺眼
 *
 * @param alpha 透明度（0-1）
 */
@Composable
private fun AlertFlashOverlay(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = alpha))
    )
}

/**
 * 动画播放器区域
 *
 * 使用 VideoPlayer 组件播放学校消防安全动画
 *
 * @param isPlaying 是否正在播放
 * @param isCompleted 是否已完成
 * @param onPlaybackComplete 播放完成回调
 */
@Composable
private fun AnimationPlayerArea(
    isPlaying: Boolean,
    isCompleted: Boolean,
    onPlaybackComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(240.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFFF6B6B)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            // 播放学校消防安全动画
            VideoPlayer(
                videoPath = "videos/School_Fire_Safety_Knowledge.mp4",
                modifier = Modifier.fillMaxSize(),
                onPlaybackCompleted = onPlaybackComplete,
                autoPlay = true,
                showControls = false
            )
        } else if (isCompleted) {
            // 已完成状态
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "✅",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "动画已观看完成",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A9D8F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "你获得了学校徽章！",
                    fontSize = 16.sp,
                    color = Color(0xFF2A9D8F)
                )
            }
        }
    }
}

/**
 * 徽章收集动画覆盖层
 *
 * 显示小火点赞动画 + 徽章获得提示
 *
 * @param show 是否显示
 * @param onAnimationComplete 动画完成回调
 */
@Composable
private fun BadgeAnimationOverlay(
    show: Boolean,
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
                // 小火点赞动画（占位符）
                Text(
                    text = "🐯",
                    fontSize = 100.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 点赞手势
                Text(
                    text = "👍",
                    fontSize = 60.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 徽章图标
                Text(
                    text = "🏅",
                    fontSize = 80.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "你真棒！",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "记住，着火要找大人帮忙！",
                    fontSize = 20.sp,
                    color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "获得学校徽章！",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 点击继续
                Text(
                    text = "点击继续",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(
                            Color(0xFF457B9D),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clickable(onClick = onAnimationComplete)
                )
            }
        }
    }
}
