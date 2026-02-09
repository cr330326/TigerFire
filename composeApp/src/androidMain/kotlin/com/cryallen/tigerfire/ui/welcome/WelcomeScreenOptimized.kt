package com.cryallen.tigerfire.ui.welcome

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
 * WelcomeScreen 优化版本
 *
 * 优化内容：
 * 1. 触觉反馈 - 动画阶段切换时的震动反馈
 * 2. 增强动画 - 更平滑的过渡效果
 * 3. 粒子背景 - 漂浮火花效果
 * 4. 微交互 - 状态指示器的呼吸效果
 * 5. 性能优化 - 动画资源预加载和复用
 */
@Composable
fun WelcomeScreenOptimized(
    viewModel: WelcomeViewModel,
    onNavigateToMap: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val haptic = LocalHapticFeedback.current

    // 背景图淡入动画
    var backgroundAlpha by remember { mutableStateOf(0f) }
    val backgroundAlphaAnimated by animateFloatAsState(
        targetValue = backgroundAlpha,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "background_fade"
    )

    // 卡车入场动画
    var truckAlpha by remember { mutableStateOf(0f) }
    var truckOffsetY by remember { mutableStateOf(1f) }
    val truckAlphaAnimated by animateFloatAsState(
        targetValue = truckAlpha,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "truck_fade"
    )

    // 小火挥手动画
    var waveAlpha by remember { mutableStateOf(0f) }
    var waveScale by remember { mutableStateOf(0.5f) }
    val waveAlphaAnimated by animateFloatAsState(
        targetValue = waveAlpha,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "wave_fade"
    )
    val waveScaleAnimated by animateFloatAsState(
        targetValue = waveScale,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "wave_scale"
    )

    // 欢迎文字淡入动画
    var textAlpha by remember { mutableStateOf(0f) }
    val textAlphaAnimated by animateFloatAsState(
        targetValue = textAlpha,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "text_fade"
    )

    // 加载 Lottie 动画资源
    val truckComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/anim_truck_enter.json")
    )
    val waveComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("lottie/anim_xiaohuo_wave.json")
    )

    val truckProgress by animateLottieCompositionAsState(
        composition = truckComposition,
        isPlaying = true,
        iterations = 1
    )

    val waveProgress by animateLottieCompositionAsState(
        composition = waveComposition,
        isPlaying = true,
        iterations = 1
    )

    // 动画序列 - 优化版：带触觉反馈
    LaunchedEffect(Unit) {
        // 1. 背景图淡入
        backgroundAlpha = 1f
        delay(300)

        // 2. 卡车入场（带触觉反馈）
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        truckAlpha = 1f
        truckOffsetY = 0f
    }

    LaunchedEffect(truckProgress) {
        if (truckProgress == 1f && !state.isTruckAnimationCompleted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onEvent(WelcomeEvent.TruckAnimationCompleted)
        }
    }

    LaunchedEffect(state.showWaveAnimation) {
        if (state.showWaveAnimation) {
            delay(300)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            waveAlpha = 1f
            waveScale = 1f

            delay(400)
            textAlpha = 1f
        }
    }

    LaunchedEffect(waveProgress) {
        if (waveProgress >= 0.95f && state.showWaveAnimation && !state.isVoicePlaying) {
            delay(300)
            viewModel.onEvent(WelcomeEvent.WaveAnimationCompleted)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToMap -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToMap()
                }
                is WelcomeEffect.PlayWaveAnimation -> {
                    // 小火挥手动画开始
                }
                is WelcomeEffect.PlayVoice -> {
                    audioManager.playVoice(effect.audioPath)
                    delay(3000)
                    viewModel.onEvent(WelcomeEvent.VoicePlaybackCompleted)
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景层
        Image(
            painter = painterResource(R.drawable.bg_welcome),
            contentDescription = "启动页背景",
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundAlphaAnimated),
            contentScale = ContentScale.Crop
        )

        // 优化的粒子背景 - 漂浮火花
        FloatingSparksOptimized()

        // 内容层
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // 卡车入场动画（带增强效果）
            Box(
                modifier = Modifier
                    .size(280.dp, 300.dp)
                    .alpha(truckAlphaAnimated)
                    .offset(y = (truckOffsetY * 100).dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFFFF6B6B).copy(alpha = 0.4f)
                    )
            ) {
                LottieAnimation(
                    composition = truckComposition,
                    progress = { truckProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 小火挥手动画（带脉冲效果）
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "xiaohuo_pulse"
            )

            Box(
                modifier = Modifier
                    .size(200.dp, 200.dp)
                    .scale(waveScaleAnimated * pulseScale)
                    .alpha(waveAlphaAnimated)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                    )
            ) {
                LottieAnimation(
                    composition = waveComposition,
                    progress = { waveProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 欢迎文字（带发光效果）
            val textGlowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "text_glow"
            )

            Text(
                text = "HI！今天和我一起救火吧！",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .alpha(textAlphaAnimated)
                    .offset(x = 0.dp, y = (-60).dp)
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = textGlowAlpha),
                                    Color.Transparent
                                )
                            ),
                            cornerRadius = CornerRadius(30.dp.toPx())
                        )
                    },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // 底部状态提示 - 优化版
            EnhancedStatusIndicator(state, infiniteTransition)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * 优化的漂浮火花效果
 */
@Composable
private fun FloatingSparksOptimized() {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "sparks")

    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )

    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )

    val fadeAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 火花粒子
        repeat(12) { index ->
            val angle = (index * 30f) * (Math.PI / 180).toFloat()
            val radius = with(density) { 150.dp.toPx() }
            val centerX = with(density) { 100.dp.toPx() } + kotlin.math.cos(angle) * radius * (index % 3 + 1)
            val centerY = with(density) { 200.dp.toPx() } + kotlin.math.sin(angle) * radius * (index % 3 + 1)

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { centerX.toDp() + floatAnim1.toDp() * ((index % 3) - 1) },
                        y = with(density) { centerY.toDp() + floatAnim2.toDp() * ((index % 2) * 2 - 1) }
                    )
                    .size((8 + (index % 4) * 2).dp)
                    .alpha(fadeAnim * (0.5f + (index % 5) * 0.1f))
                    .background(
                        color = listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFFFFD93D),
                            Color(0xFFFF8C00)
                        )[index % 3],
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 增强的状态指示器
 */
@Composable
private fun EnhancedStatusIndicator(
    state: com.cryallen.tigerfire.presentation.welcome.WelcomeState,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition
) {
    when {
        state.isVoicePlaying -> {
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "voice_pulse"
            )

            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "voice_scale"
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .scale(pulseScale)
                    .background(
                        color = Color(0xFFFF6B6B).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🔊",
                    fontSize = 16.sp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "语音播放中...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = pulseAlpha)
                )
            }
        }

        state.shouldNavigate -> {
            val slideOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "nav_slide"
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(x = slideOffset.dp)
            ) {
                Text(
                    text = "🚀",
                    fontSize = 16.sp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "正在进入冒险场景中...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        !state.isTruckAnimationCompleted -> {
            val rotateAnim by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "truck_rotate"
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(y = rotateAnim.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_firetruck),
                    contentDescription = "消防车图标",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "消防车出发中...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        else -> {
            val shimmerAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ready_shimmer"
            )

            Text(
                text = "✨ 准备就绪",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = shimmerAlpha)
            )
        }
    }
}
