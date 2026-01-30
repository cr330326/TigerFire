package com.cryallen.tigerfire.ui.welcome

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.welcome.WelcomeEffect
import com.cryallen.tigerfire.presentation.welcome.WelcomeEvent
import com.cryallen.tigerfire.presentation.welcome.WelcomeViewModel
import com.cryallen.tigerfire.ui.theme.ThemeGradients
import com.cryallen.tigerfire.ui.theme.createVerticalGradient
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import com.cryallen.tigerfire.R

/**
 * 欢迎页/启动页 Screen（自动导航版）
 *
 * 功能说明：
 * 1. 显示背景图（淡入动画）
 * 2. 自动播放卡车入场 Lottie 动画（2-3秒）+ 鸣笛音效
 * 3. 自动播放小火挥手 Lottie 动画（3秒）
 * 4. 自动播放欢迎语音："HI！今天和我一起救火吧！"
 * 5. 语音播放完毕后延迟100ms自动导航到主地图（无需用户交互）
 *
 * 设计理念：
 * - 零操作启动 - 完全自动化流程
 * - 沉浸式体验 - 连续动画营造"出发"代入感
 * - 节奏紧凑 - 总时长5-6秒快速进入内容
 *
 * @param viewModel WelcomeViewModel
 * @param onNavigateToMap 导航到主地图回调
 */
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onNavigateToMap: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }

    // 背景图淡入动画
    var backgroundAlpha by remember { mutableStateOf(0f) }
    val backgroundAlphaAnimated by animateFloatAsState(
        targetValue = backgroundAlpha,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "background_fade"
    )

    // 卡车入场动画（从底部滑入 + 淡入）
    var truckAlpha by remember { mutableStateOf(0f) }
    var truckOffsetY by remember { mutableStateOf(1f) }
    val truckAlphaAnimated by animateFloatAsState(
        targetValue = truckAlpha,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "truck_fade"
    )

    // 小火挥手动画（缩放 + 淡入）
    var waveAlpha by remember { mutableStateOf(0f) }
    var waveScale by remember { mutableStateOf(0.5f) }
    val waveAlphaAnimated by animateFloatAsState(
        targetValue = waveAlpha,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "wave_fade"
    )
    val waveScaleAnimated by animateFloatAsState(
        targetValue = waveScale,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "wave_scale"
    )

    // 欢迎文字淡入动画
    var textAlpha by remember { mutableStateOf(0f) }
    val textAlphaAnimated by animateFloatAsState(
        targetValue = textAlpha,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "text_fade"
    )

    // 加载 Lottie 动画资源
    val truckComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/anim_truck_enter.json")
    )
    val waveComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/anim_xiaohuo_wave.json")
    )

    // 卡车动画进度
    val truckProgress by animateLottieCompositionAsState(
        composition = truckComposition,
        isPlaying = true,
        iterations = 1
    )

    // 小火挥手动画进度
    val waveProgress by animateLottieCompositionAsState(
        composition = waveComposition,
        isPlaying = true,
        iterations = 1
    )

    // 启动时的动画序列
    LaunchedEffect(Unit) {
        // 1. 背景图淡入
        backgroundAlpha = 1f
        delay(200)

        // 2. 卡车入场动画开始（从底部滑入 + 淡入）
        truckAlpha = 1f
        truckOffsetY = 0f
    }

    // 卡车动画完成后触发事件
    LaunchedEffect(truckProgress) {
        if (truckProgress == 1f && !state.isTruckAnimationCompleted) {
            viewModel.onEvent(WelcomeEvent.TruckAnimationCompleted)
        }
    }

    // 小火挥手动画开始（卡车完成后）
    LaunchedEffect(state.showWaveAnimation) {
        if (state.showWaveAnimation) {
            // 延迟后显示小火挥手动画（缩放 + 淡入）
            delay(200)
            waveAlpha = 1f
            waveScale = 1f

            // 文字淡入
            delay(300)
            textAlpha = 1f
        }
    }

    // 小火挥手动画完成后触发事件
    LaunchedEffect(waveProgress) {
        if (waveProgress >= 0.95f && state.showWaveAnimation && !state.isVoicePlaying) {
            delay(300)
            viewModel.onEvent(WelcomeEvent.WaveAnimationCompleted)
        }
    }

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToMap -> {
                    // 延迟100ms后自动导航（已在ViewModel中处理）
                    onNavigateToMap()
                }
                is WelcomeEffect.PlayWaveAnimation -> {
                    // 小火挥手动画开始
                }
                is WelcomeEffect.PlayVoice -> {
                    // 播放欢迎语音
                    audioManager.playVoice(effect.audioPath)

                    // 模拟语音播放时长（3秒）
                    delay(3000)

                    // 语音播放完成，触发自动导航
                    viewModel.onEvent(WelcomeEvent.VoicePlaybackCompleted)
                }
            }
        }
    }

    // 无限呼吸动画（用于状态提示）
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // 全屏容器（无点击交互，完全自动化）
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景层（使用 drawable 资源，裁剪填充屏幕）
        Image(
            painter = painterResource(R.drawable.bg_welcome),
            contentDescription = "启动页背景",
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundAlphaAnimated),
            contentScale = ContentScale.Crop
        )

        // 内容层
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // 卡车入场 Lottie 动画（从底部滑入 + 淡入）
            Box(
                modifier = Modifier
                    .size(280.dp, 300.dp)
                    .alpha(truckAlphaAnimated)
                    .offset(y = (truckOffsetY * 100).dp)
            ) {
                LottieAnimation(
                    composition = truckComposition,
                    progress = { truckProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 小火挥手 Lottie 动画（缩放 + 淡入）
            Box(
                modifier = Modifier
                    .size(200.dp, 200.dp)
                    .scale(waveScaleAnimated)
                    .alpha(waveAlphaAnimated)
            ) {
                LottieAnimation(
                    composition = waveComposition,
                    progress = { waveProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 欢迎文字（淡入效果）
            Text(
                text = "HI！今天和我一起救火吧！",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .alpha(textAlphaAnimated)
                    .offset(x = 0.dp, y = (-60).dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // 底部状态提示
            when {
                state.isVoicePlaying -> {
                    // 语音播放中提示（呼吸效果）
                    val pulseAlpha = infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )

                    Text(
                        text = "🔊 语音播放中...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = pulseAlpha.value),
                        textAlign = TextAlign.Center
                    )
                }

                state.shouldNavigate -> {
                    // 导航中提示
                    Text(
                        text = "🚀 正在进入冒险场景中...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                !state.isTruckAnimationCompleted -> {
                    // 卡车入场中提示
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_firetruck),
                            contentDescription = "消防车图标",
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "消防车出发中...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    // 准备就绪提示（短暂显示）
                    Text(
                        text = "✨ 准备就绪",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
