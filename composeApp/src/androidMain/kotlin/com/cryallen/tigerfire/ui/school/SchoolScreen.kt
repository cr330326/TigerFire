package com.cryallen.tigerfire.ui.school

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.Dp
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
        // 先播放警报音效，让用户看到背景装饰
        audioManager.playAlertSound()

        // 播放小火语音："学校着火啦！快叫消防车！"
        audioManager.playVoice("voice/school_fire.mp3")

        // 延迟 2 秒后才开始播放视频，让用户先欣赏背景
        delay(2000)

        // 然后触发页面进入事件，开始播放视频
        viewModel.onEvent(SchoolEvent.ScreenEntered)

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
                    // 播放小火语音："你真棒！记住，着火要找大人帮忙！"
                    audioManager.playVoice("voice/school_praise.mp3")
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
        SchoolBackground()

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

                // 播放提示/完成状态显示区域
                AnimationPlayerArea(
                    isPlaying = state.isPlayingAnimation,
                    isCompleted = state.isCompleted,
                    onPlaybackComplete = {
                        viewModel.onEvent(SchoolEvent.AnimationPlaybackCompleted)
                    }
                )
            }
        }

        // 视频播放覆盖层 - 全屏播放确保视频正常渲染
        if (state.isPlayingAnimation) {
            VideoPlayerOverlay(
                onPlaybackComplete = {
                    viewModel.onEvent(SchoolEvent.AnimationPlaybackCompleted)
                }
            )
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
 * 学校场景装饰性背景组件
 * 卡通风格的学校场景装饰
 */
@Composable
private fun SchoolBackground() {
    // 多层动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "schoolBgAnimations")

    // 云朵浮动
    val cloud1X by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1X"
    )
    val cloud2X by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2X"
    )

    // 星星闪烁
    val starAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha1"
    )
    val starAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha2"
    )

    // 书本跳动动画
    val bookScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bookScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景云朵层 - 柔和的氛围
        Text(
            text = "☁️",
            fontSize = 64.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-40 + cloud1X).dp, y = 50.dp)
                .alpha(0.15f)
        )
        Text(
            text = "☁️",
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (20 + cloud2X).dp, y = 80.dp)
                .alpha(0.12f)
        )
        Text(
            text = "☁️",
            fontSize = 56.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (20 + cloud1X * 0.5f).dp, y = (-150).dp)
                .alpha(0.1f)
        )

        // 左上角学校建筑剪影
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-10).dp, y = 40.dp)
                .alpha(0.08f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "🏫",
                fontSize = 100.sp
            )
            Text(
                text = "📚",
                fontSize = 60.sp,
                modifier = Modifier.offset(x = 20.dp, y = (-10).dp)
            )
        }

        // 右下角装饰 - 书本和铅笔
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-30).dp)
                .alpha(0.08f),
            horizontalArrangement = Arrangement.spacedBy((-10).dp)
        ) {
            Text(
                text = "✏️",
                fontSize = 50.sp
            )
            Text(
                text = "📖",
                fontSize = 70.sp,
                modifier = Modifier.scale(bookScale)
            )
        }

        // 星星和闪光装饰
        data class StarPos(
            val alignment: Alignment,
            val xOffset: Dp,
            val yOffset: Dp,
            val alpha: Float,
            val emoji: String = "⭐"
        )

        val starPositions = listOf(
            StarPos(Alignment.TopEnd, (-60).dp, 180.dp, starAlpha1),
            StarPos(Alignment.TopStart, 50.dp, 120.dp, starAlpha2),
            StarPos(Alignment.CenterEnd, (-30).dp, (-80).dp, starAlpha1, "✨"),
            StarPos(Alignment.CenterStart, 40.dp, 0.dp, starAlpha2, "✨"),
        )

        starPositions.forEach { (alignment, xOffset, yOffset, alpha, emoji) ->
            Text(
                text = emoji,
                fontSize = (18..26).random().sp,
                modifier = Modifier
                    .align(alignment)
                    .offset(x = xOffset, y = yOffset)
                    .alpha(alpha * 0.2f)
            )
        }

        // 左下角装饰
        Text(
            text = "🎒",
            fontSize = 70.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-20).dp)
                .alpha(0.08f)
        )

        // 顶部太阳装饰 - 温暖的感觉
        val sunRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sunRotation"
        )

        Text(
            text = "☀️",
            fontSize = 50.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 30.dp)
                .rotate(sunRotation)
                .alpha(0.12f)
        )
    }
}

/**
 * 动画播放器区域
 *
 * 显示播放状态或完成状态，实际视频播放由 VideoPlayerOverlay 全屏覆盖层处理
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
    // 未播放时的动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "animationArea")

    // 电影图标跳动
    val movieScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movieScale"
    )

    // 加载点闪烁
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(240.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFFFF6B6B)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            // 播放中提示 - 实际视频由全屏覆盖层播放
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎬",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "正在播放动画...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF457B9D)
                )
                // 加载动画点
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    Color(0xFF457B9D),
                                    shape = CircleShape
                                )
                                .alpha(dotAlpha)
                        )
                    }
                }
            }
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
        } else {
            // 未开始状态 - 增强视觉效果
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 跳动的电影图标
                Text(
                    text = "🎬",
                    fontSize = 56.sp,
                    modifier = Modifier.scale(movieScale)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "即将播放消防安全动画",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF457B9D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 描述文字
                Text(
                    text = "小朋友发现火 → 打119 → 消防车到达\n→ 喷水灭火 → 老师带大家离开",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 倒计时提示
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⏱️",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "即将开始...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE63946)
                    )
                }
            }
        }
    }
}

/**
 * 视频播放全屏覆盖层
 *
 * 使用与 FireStationScreen 相同的全屏覆盖模式播放视频
 * 确保视频画面正常显示
 *
 * @param onPlaybackComplete 播放完成回调
 */
@Composable
private fun VideoPlayerOverlay(
    onPlaybackComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        VideoPlayer(
            videoPath = "videos/School_Fire_Safety_Knowledge.mp4",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(32.dp),
            onPlaybackCompleted = onPlaybackComplete,
            autoPlay = true,
            showControls = false
        )
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
