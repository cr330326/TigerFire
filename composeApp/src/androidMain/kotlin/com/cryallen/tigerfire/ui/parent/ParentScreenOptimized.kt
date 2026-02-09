package com.cryallen.tigerfire.ui.parent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.parent.ParentEffect
import com.cryallen.tigerfire.presentation.parent.ParentEvent
import com.cryallen.tigerfire.presentation.parent.ParentViewModel
import kotlinx.coroutines.delay

/**
 * 家长模式 Screen 优化版本
 *
 * 优化内容：
 * 1. 触觉反馈 - 所有交互都带震动反馈 (HapticFeedbackType.LongPress)
 * 2. 增强动画 - 时间选择卡片动画、图表加载动画、按钮交互反馈
 * 3. 粒子背景 - 漂浮齿轮/设置元素效果
 * 4. 微交互 - 按钮缩放反馈、悬停效果
 * 5. 性能优化 - 动画资源预加载
 *
 * @param viewModel ParentViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun ParentScreenOptimized(
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val haptic = LocalHapticFeedback.current

    // 性能优化：预加载音效
    LaunchedEffect(Unit) {
        audioManager.preloadSounds()
    }

    // 页面进入动画 - 优化版：分阶段淡入
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }

    // 背景动画渐变
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_offset"
    )

    // 背景呼吸效果
    val backgroundPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_pulse"
    )

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ParentEffect.ShowSettingsSavedHint -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is ParentEffect.ShowResetSuccessHint -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is ParentEffect.ShowVerificationFailedHint -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is ParentEffect.PlayClickSound -> {
                    audioManager.playClickSound()
                }
                is ParentEffect.NavigateToMap -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateBack()
                }
            }
        }
    }

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A5F7A),
            Color(0xFF159895),
            Color(0xFF57C5B6),
            Color(0xFF159895),
            Color(0xFF1A5F7A)
        ),
        start = androidx.compose.ui.geometry.Offset(
            x = backgroundOffset * 2,
            y = backgroundOffset
        ),
        end = androidx.compose.ui.geometry.Offset(
            x = backgroundOffset * 2 + 1000f,
            y = backgroundOffset + 1000f
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // 优化的粒子背景 - 漂浮齿轮和设置元素
        FloatingGearsBackgroundOptimized()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(backgroundPulse)
        ) {
            // 顶部工具栏 - 优化版
            ParentTopBarOptimized(
                onBackClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.BackToMapClicked)
                }
            )

            // 主内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题区域 - 带增强动画
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)),
                    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
                ) {
                    ParentTitleSectionOptimized()
                }

                // 可滚动内容
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 使用统计卡片 - 优化版
                    item {
                        AnimatedVisibility(
                            visible = contentVisible,
                            enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(500, delayMillis = 100)) + fadeIn(animationSpec = tween(500, delayMillis = 100)),
                            exit = fadeOut()
                        ) {
                            UsageStatsCardOptimized(
                                todayPlayTime = state.todayPlayTime,
                                totalPlayTime = state.totalPlayTime,
                                totalBadgeCount = state.totalBadgeCount,
                                dailyUsageStats = state.settings.dailyUsageStats
                            )
                        }
                    }

                    // 时间设置卡片 - 优化版
                    item {
                        AnimatedVisibility(
                            visible = contentVisible,
                            enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(600, delayMillis = 200)) + fadeIn(animationSpec = tween(600, delayMillis = 200)),
                            exit = fadeOut()
                        ) {
                            TimeSettingsCardOptimized(
                                sessionTimeLimit = state.settings.sessionDurationMinutes,
                                reminderEnabled = state.settings.reminderMinutesBefore > 0,
                                onSessionTimeLimitChange = { minutes ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onEvent(ParentEvent.UpdateSessionTimeLimit(minutes))
                                },
                                onReminderToggle = { enabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onEvent(ParentEvent.UpdateReminderTime(if (enabled) 2 else 0))
                                },
                                onShowDialog = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onEvent(ParentEvent.ShowTimeSettingsDialog)
                                }
                            )
                        }
                    }

                    // 进度管理卡片 - 优化版
                    item {
                        AnimatedVisibility(
                            visible = contentVisible,
                            enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(700, delayMillis = 300)) + fadeIn(animationSpec = tween(700, delayMillis = 300)),
                            exit = fadeOut()
                        ) {
                            ProgressManagementCardOptimized(
                                onResetProgress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onEvent(ParentEvent.ResetProgressClicked)
                                }
                            )
                        }
                    }

                    // 底部留白
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // 重置确认对话框 - 优化版
        if (state.showResetConfirmation) {
            ResetConfirmationDialogOptimized(
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.ConfirmResetProgress)
                },
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.CancelResetProgress)
                }
            )
        }

        // 数学验证对话框 - 优化版
        if (state.showReverification) {
            MathVerificationDialogOptimized(
                question = state.reverificationQuestion?.first ?: "",
                expectedAnswer = state.reverificationQuestion?.second ?: 0,
                onSubmit = { answer ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.SubmitReverificationAnswer(answer))
                },
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.CancelReverification)
                }
            )
        }

        // 时间设置对话框 - 优化版
        if (state.showTimeSettingsDialog) {
            TimeSettingsDialogOptimized(
                sessionEnabled = state.sessionTimeLimitEnabled,
                dailyEnabled = state.dailyTimeLimitEnabled,
                onSessionToggle = { enabled ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.ToggleSessionTimeLimit(enabled))
                },
                onDailyToggle = { enabled ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.ToggleDailyTimeLimit(enabled))
                },
                onSave = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.SaveTimeSettings)
                },
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(ParentEvent.DismissTimeSettingsDialog)
                }
            )
        }

        // 设置保存成功提示 - 优化版
        if (state.showSettingsSavedHint) {
            SettingsSavedHintOverlayOptimized(
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.dismissSettingsSavedHint()
                }
            )
        }

        // 重置成功提示 - 优化版
        if (state.showResetSuccessHint) {
            ResetSuccessHintOverlayOptimized(
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.dismissResetSuccessHint()
                }
            )
        }

        // 验证失败提示 - 优化版
        if (state.showVerificationFailedHint) {
            VerificationFailedHintOverlayOptimized(
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.dismissVerificationFailedHint()
                }
            )
        }
    }
}

/**
 * 优化的漂浮齿轮背景 - 漂浮设置元素效果
 */
@Composable
private fun FloatingGearsBackgroundOptimized() {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "gears_animation")

    // 多层齿轮以不同速度移动和旋转
    val layer1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_layer1"
    )

    val layer2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_layer2"
    )

    val layer3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_layer3"
    )

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val fadeAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 第一层齿轮 - 小而快
        repeat(6) { index ->
            val x = ((layer1Offset * 0.5 + index * 60) % 400).dp
            val y = ((index * 80) % 600).dp + floatAnim.dp
            val rotation = rotationAnim + index * 60f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(20.dp)
                    .rotate(rotation)
                    .alpha(fadeAnim * 0.8f)
            ) {
                Text("⚙️", fontSize = 20.dp.value.sp)
            }
        }

        // 第二层齿轮 - 中等
        repeat(4) { index ->
            val x = ((layer2Offset * 0.3 + index * 100) % 400).dp
            val y = ((index * 120 + 50) % 600).dp - floatAnim.dp * 0.5f
            val rotation = -(rotationAnim * 0.7f) + index * 90f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(28.dp)
                    .rotate(rotation)
                    .alpha(fadeAnim)
            ) {
                Text("🔧", fontSize = 28.dp.value.sp)
            }
        }

        // 第三层齿轮 - 大而慢
        repeat(3) { index ->
            val x = ((layer3Offset * 0.2 + index * 140) % 400).dp
            val y = ((index * 160 + 100) % 600).dp + floatAnim.dp * 0.3f
            val rotation = rotationAnim * 0.5f + index * 120f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(36.dp)
                    .rotate(rotation)
                    .alpha(fadeAnim * 1.2f)
            ) {
                Text("⚙️", fontSize = 36.dp.value.sp)
            }
        }

        // 设置图标 - 漂浮装饰
        repeat(4) { index ->
            val x = ((layer1Offset * 0.4 + index * 90) % 380).dp
            val y = ((index * 140 + 30) % 580).dp + floatAnim.dp * 0.7f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(16.dp)
                    .alpha(fadeAnim * 0.6f)
            ) {
                Text("⚡", fontSize = 16.dp.value.sp)
            }
        }
    }
}

/**
 * 优化的顶部工具栏 - 带触觉反馈
 */
@Composable
private fun ParentTopBarOptimized(
    onBackClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "topbar_animation")

    // 右侧图标呼吸动画
    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_pulse"
    )

    val iconGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮 - 带动画和触觉反馈
        var buttonScale by remember { mutableStateOf(1f) }
        Box(
            modifier = Modifier
                .scale(buttonScale)
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    buttonScale = 0.9f
                    onBackClick()
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFF8DC)
                        )
                    ),
                    shape = CircleShape
                )
                .drawBehind {
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.4f),
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A5F7A)
            )
        }

        LaunchedEffect(buttonScale) {
            if (buttonScale != 1f) {
                delay(100)
                buttonScale = 1f
            }
        }

        // 右侧装饰图标 - 带增强动画
        Box(
            modifier = Modifier
                .scale(iconPulse)
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = iconGlow)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFF4A261)
                        )
                    ),
                    shape = CircleShape
                )
                .drawBehind {
                    // 外发光效果
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = iconGlow * 0.5f),
                        radius = size.minDimension / 2 + 4.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔧",
                fontSize = 32.sp
            )
        }
    }
}

/**
 * 优化的标题区域 - 带增强动画
 */
@Composable
private fun ParentTitleSectionOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = "title_animation")

    // 标题淡入和位移动画
    var titleAlpha by remember { mutableStateOf(0f) }
    var titleOffsetY by remember { mutableStateOf(30f) }

    LaunchedEffect(Unit) {
        titleAlpha = 1f
        titleOffsetY = 0f
    }

    // 小火图标呼吸动画
    val tigerPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiger_pulse"
    )

    val tigerGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiger_glow"
    )

    Column(
        modifier = Modifier
            .alpha(titleAlpha)
            .offset(y = titleOffsetY.dp)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 小火图标 - 带增强动画
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(tigerPulse)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = tigerGlow)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFF8DC)
                        )
                    ),
                    shape = CircleShape
                )
                .drawBehind {
                    // 多层发光效果
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = tigerGlow * 0.5f),
                        radius = size.minDimension / 2 - 5.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 5.dp.toPx()
                        )
                    )
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = tigerGlow * 0.3f),
                        radius = size.minDimension / 2 + 3.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐯",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 标题文字 - 带发光效果
        val titleGlowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "title_glow"
        )

        Text(
            text = "🔧 家长模式 🔧",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = titleGlowAlpha),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "管理孩子使用时间和进度",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 优化的使用统计卡片 - 带增强动画
 */
@Composable
private fun UsageStatsCardOptimized(
    todayPlayTime: Long,
    totalPlayTime: Long,
    totalBadgeCount: Int,
    dailyUsageStats: Map<String, Long>
) {
    val haptic = LocalHapticFeedback.current

    // 卡片入场动画
    var cardVisible by remember { mutableStateOf(false) }
    var cardOffsetY by remember { mutableStateOf(50f) }

    LaunchedEffect(Unit) {
        delay(100)
        cardVisible = true
        cardOffsetY = 0f
    }

    // 卡片呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "usage_card_pulse")
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.005f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )

    // Shimmer效果
    val shimmerOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        shimmerOffset.animateTo(
            targetValue = 1000f,
            animationSpec = tween(2000, easing = LinearEasing)
        )
    }

    var cardClickScale by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = cardOffsetY.dp)
            .scale(cardScale * cardClickScale)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                cardClickScale = 0.98f
            }
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                // 渐变边框
                val strokeWidth = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFF57C5B6),
                            Color(0xFFFFD700)
                        )
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(24.dp.value, 24.dp.value)
                )

                // Shimmer效果
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFD700).copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startX = shimmerOffset.value - 500f,
                        endX = shimmerOffset.value + 500f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .padding(20.dp)
    ) {
        LaunchedEffect(cardClickScale) {
            if (cardClickScale != 1f) {
                delay(100)
                cardClickScale = 1f
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "使用统计",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A5F7A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 今日使用时长
            StatItemOptimized(
                icon = "⏰",
                label = "今日使用时长",
                value = formatDuration(todayPlayTime),
                valueColor = Color(0xFFE63946)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 总使用时长
            StatItemOptimized(
                icon = "⌛",
                label = "总使用时长",
                value = formatDuration(totalPlayTime),
                valueColor = Color(0xFF159895)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 已收集徽章
            StatItemOptimized(
                icon = "🏅",
                label = "已收集徽章",
                value = "$totalBadgeCount 枚",
                valueColor = Color(0xFFF4A261)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 本周使用时长图表 - 优化版
            WeeklyUsageChartOptimized(dailyUsageStats = dailyUsageStats)
        }
    }
}

/**
 * 优化的统计项组件 - 带触觉反馈
 */
@Composable
private fun StatItemOptimized(
    icon: String,
    label: String,
    value: String,
    valueColor: Color
) {
    val haptic = LocalHapticFeedback.current
    var scale by remember { mutableStateOf(1f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(
                color = valueColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scale = 0.98f
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A5F7A)
            )
        }
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }

    LaunchedEffect(scale) {
        if (scale != 1f) {
            delay(100)
            scale = 1f
        }
    }
}

/**
 * 优化的本周使用时长图表 - 带加载动画
 */
@Composable
private fun WeeklyUsageChartOptimized(dailyUsageStats: Map<String, Long>) {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")

    // 获取本周7天的数据（毫秒转分钟）
    val dailyMinutes = getLast7DaysMinutes(dailyUsageStats)
    val maxMinutes = dailyMinutes.maxOrNull()?.coerceAtLeast(60) ?: 60

    val totalMinutes = dailyMinutes.sum()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    // 图表加载动画
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "chart_progress"
    )

    LaunchedEffect(Unit) {
        delay(300)
        animationProgress = 1f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A5F7A).copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // 标题和总计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📈",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "本周使用",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5F7A)
                )
            }
            Text(
                text = if (totalMinutes > 0) "总计 ${hours}h ${minutes}m" else "暂无数据",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF159895)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 柱状图 - 带动画
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEachIndexed { index, day ->
                val mins = dailyMinutes[index]
                val barHeight = if (mins > 0) {
                    ((mins.toFloat() / maxMinutes * 70).coerceAtLeast(10f) * animatedProgress).coerceAtMost(70f)
                } else {
                    4f * animatedProgress
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 柱子 - 渐变色，带动画
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight.dp)
                            .shadow(
                                elevation = if (mins > 0 && animatedProgress > 0.5f) 4.dp else 0.dp,
                                shape = RoundedCornerShape(6.dp),
                                spotColor = Color(0xFF57C5B6).copy(alpha = 0.5f)
                            )
                            .background(
                                brush = if (mins > 0) {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF159895),
                                            Color(0xFF57C5B6)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFE0E0E0),
                                            Color(0xFFEEEEEE)
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 星期标签
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A5F7A)
                    )
                }
            }
        }
    }
}

/**
 * 优化的时间设置卡片 - 带增强动画
 */
@Composable
private fun TimeSettingsCardOptimized(
    sessionTimeLimit: Int,
    reminderEnabled: Boolean = true,
    onSessionTimeLimitChange: (Int) -> Unit,
    onReminderToggle: (Boolean) -> Unit = {},
    onShowDialog: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    // 卡片入场动画
    var cardScale by remember { mutableStateOf(1f) }
    LaunchedEffect(Unit) {
        delay(200)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "time_card_pulse")
    val cardPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.003f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse"
    )

    val timeOptions = listOf(5, 10, 15, 30)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardPulse * cardScale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                cardScale = 0.97f
                onShowDialog()
            }
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF57C5B6),
                            Color(0xFF159895),
                            Color(0xFF57C5B6)
                        )
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(24.dp.value, 24.dp.value)
                )
            }
            .padding(20.dp)
    ) {
        LaunchedEffect(cardScale) {
            if (cardScale != 1f) {
                delay(100)
                cardScale = 1f
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏱️",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "时间设置",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A5F7A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 每次使用时长
            Text(
                text = "每次使用时长",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A5F7A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 时间选项按钮 - 优化版
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                timeOptions.forEach { minutes ->
                    TimeOptionButtonOptimized(
                        minutes = minutes,
                        isSelected = sessionTimeLimit == minutes,
                        onClick = { onSessionTimeLimitChange(minutes) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 提醒设置 - 优化版
            ReminderRowOptimized(
                reminderEnabled = reminderEnabled,
                onReminderToggle = onReminderToggle
            )
        }
    }
}

/**
 * 优化的时间选项按钮 - 带增强动画
 */
@Composable
private fun TimeOptionButtonOptimized(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var buttonScale by remember { mutableStateOf(1f) }

    // 选中时的脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "time_option_pulse")
    val selectedPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selected_pulse"
    )

    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF159895),
                Color(0xFF57C5B6)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFD0D0D0)
            )
        )
    }

    val textColor = if (isSelected) Color.White else Color(0xFF666666)

    Box(
        modifier = modifier
            .scale(buttonScale * selectedPulse)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = if (isSelected) {
                    Color(0xFF159895).copy(alpha = 0.5f)
                } else {
                    Color.Transparent
                }
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                buttonScale = 0.92f
                onClick()
            }
            .background(
                brush = backgroundColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${minutes}分钟",
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }

    LaunchedEffect(buttonScale) {
        if (buttonScale != 1f) {
            delay(100)
            buttonScale = 1f
        }
    }
}

/**
 * 优化的提醒行组件
 */
@Composable
private fun ReminderRowOptimized(
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF4A261).copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🔔",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "提前提醒",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5F7A)
                )
                Text(
                    text = "时间到前2分钟提醒",
                    fontSize = 13.sp,
                    color = Color(0xFF1A5F7A).copy(alpha = 0.7f)
                )
            }
        }

        // 开关按钮 - 优化版
        ToggleSwitchOptimized(
            isEnabled = reminderEnabled,
            onToggle = { onReminderToggle(!reminderEnabled) }
        )
    }
}

/**
 * 优化的开关组件 - 带增强动画
 */
@Composable
private fun ToggleSwitchOptimized(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var switchScale by remember { mutableStateOf(1f) }

    // 动画过渡状态
    val togglePosition by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "toggle_position"
    )

    val glowIntensity by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.3f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "glow_intensity"
    )

    Box(
        modifier = Modifier
            .scale(switchScale)
            .width(60.dp)
            .height(34.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(17.dp),
                spotColor = if (isEnabled) {
                    Color(0xFF159895).copy(alpha = glowIntensity * 0.5f)
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                }
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                switchScale = 0.92f
                onToggle()
            }
            .background(
                color = if (isEnabled) Color(0xFF159895) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(17.dp)
            )
            .drawBehind {
                // 圆形指示器 - 带动画
                val circleSize = 26.dp.toPx()
                val baseOffset = 4.dp.toPx()
                val maxOffset = size.width - circleSize - baseOffset
                val offset = baseOffset + (maxOffset - baseOffset) * togglePosition

                drawCircle(
                    color = Color.White,
                    radius = circleSize / 2,
                    center = androidx.compose.ui.geometry.Offset(
                        offset + circleSize / 2,
                        size.height / 2
                    )
                )

                // 发光效果
                if (isEnabled) {
                    drawCircle(
                        color = Color.White.copy(alpha = glowIntensity * 0.3f),
                        radius = circleSize / 2 + 4.dp.toPx() * glowIntensity,
                        center = androidx.compose.ui.geometry.Offset(
                            offset + circleSize / 2,
                            size.height / 2
                        )
                    )
                }
            }
    )

    LaunchedEffect(switchScale) {
        if (switchScale != 1f) {
            delay(100)
            switchScale = 1f
        }
    }
}

/**
 * 优化的进度管理卡片 - 带增强动画
 */
@Composable
private fun ProgressManagementCardOptimized(
    onResetProgress: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // 卡片入场动画
    LaunchedEffect(Unit) {
        delay(300)
    }

    // 警告脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "warning_pulse")
    val warningPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning_pulse"
    )

    var buttonScale by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFE63946).copy(alpha = 0.4f)
            )
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE63946),
                            Color(0xFFFF6B6B),
                            Color(0xFFE63946)
                        )
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(24.dp.value, 24.dp.value)
                )
            }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔧",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "进度管理",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A5F7A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 警告提示 - 带脉冲动画
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(warningPulse)
                    .background(
                        color = Color(0xFFFFF3CD),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "注意！",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF856404)
                    )
                    Text(
                        text = "重置将清除所有徽章和进度，不可恢复",
                        fontSize = 13.sp,
                        color = Color(0xFF856404).copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 重置按钮 - 优化版
            Box(
                modifier = Modifier
                    .scale(buttonScale)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0xFFE63946).copy(alpha = 0.5f)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        buttonScale = 0.95f
                        onResetProgress()
                    }
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE63946),
                                Color(0xFFFF6B6B)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗑️",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "重置游戏进度",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    LaunchedEffect(buttonScale) {
        if (buttonScale != 1f) {
            delay(100)
            buttonScale = 1f
        }
    }
}

/**
 * 优化的重置确认对话框 - 带触觉反馈
 */
@Composable
private fun ResetConfirmationDialogOptimized(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // 对话框动画
    var dialogScale by remember { mutableStateOf(0.7f) }
    var dialogAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogAlpha = 1f
    }

    // 背景遮罩
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        // 对话框内容
        Box(
            modifier = Modifier
                .scale(dialogScale)
                .padding(32.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0xFFE63946).copy(alpha = 0.5f)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp)
                )
                .drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE63946),
                                Color(0xFFFF6B6B),
                                Color(0xFFE63946)
                            )
                        ),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(28.dp.value, 28.dp.value)
                    )
                }
                .padding(28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 警告图标 - 带动画
                val infiniteTransition = rememberInfiniteTransition(label = "warning_animation")
                val warningScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "warning_scale"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(warningScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFE63946).copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFF3CD),
                                    Color(0xFFFFE5A0)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp
                    )
                }

                // 标题
                Text(
                    text = "确认重置进度？",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5F7A)
                )

                // 警告文字
                Text(
                    text = "此操作将清除所有徽章和场景完成状态\n且无法恢复！",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 按钮行
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 取消按钮
                    var cancelScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .scale(cancelScale)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = Color.Gray.copy(alpha = 0.4f)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                cancelScale = 0.92f
                                onDismiss()
                            }
                            .background(
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                    }

                    LaunchedEffect(cancelScale) {
                        if (cancelScale != 1f) {
                            delay(100)
                            cancelScale = 1f
                        }
                    }

                    // 确认按钮
                    var confirmScale by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .scale(confirmScale)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = Color(0xFFE63946).copy(alpha = 0.5f)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                confirmScale = 0.92f
                                onConfirm()
                            }
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE63946),
                                        Color(0xFFFF6B6B)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "确认重置",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    LaunchedEffect(confirmScale) {
                        if (confirmScale != 1f) {
                            delay(100)
                            confirmScale = 1f
                        }
                    }
                }
            }
        }
    }
}

/**
 * 优化的数学验证对话框 - 带触觉反馈和增强动画
 */
@Composable
private fun MathVerificationDialogOptimized(
    question: String,
    expectedAnswer: Int,
    onSubmit: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // 对话框入场动画
    var dialogScale by remember { mutableStateOf(0.3f) }
    var dialogAlpha by remember { mutableStateOf(0f) }
    var dialogOffsetY by remember { mutableStateOf(100f) }

    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogAlpha = 1f
        dialogOffsetY = 0f
    }

    // 背景遮罩 - 使用径向渐变模糊效果
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF159895).copy(alpha = 0.25f),
                        Color(0xFF1A5F7A).copy(alpha = 0.5f),
                        Color.Black.copy(alpha = 0.75f)
                    )
                )
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        // 对话框内容 - 增强版
        Box(
            modifier = Modifier
                .offset(y = dialogOffsetY.dp)
                .scale(dialogScale)
                .padding(20.dp)
                .widthIn(max = 400.dp)
                .shadow(
                    elevation = 40.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color(0xFF57C5B6).copy(alpha = 0.5f),
                    ambientColor = Color(0xFF159895).copy(alpha = 0.4f)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.97f),
                            Color.White.copy(alpha = 0.90f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0.5f, 1f)
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
                .drawBehind {
                    // 多层次玻璃拟态边框
                    val strokeWidth = 3.5.dp.toPx()

                    // 外层渐变边框
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF57C5B6).copy(alpha = 0.7f),
                                Color(0xFFFFD700).copy(alpha = 0.9f),
                                Color(0xFF57C5B6).copy(alpha = 0.7f)
                            )
                        ),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(36.dp.value, 36.dp.value)
                    )

                    // 内部高光效果
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                        cornerRadius = CornerRadius(32.dp.value, 32.dp.value)
                    )
                }
                .padding(26.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 小火图标 - 带增强动画
                val infiniteTransition = rememberInfiniteTransition(label = "verification_animation")
                var iconScale by remember { mutableStateOf(1f) }
                var iconRotation by remember { mutableStateOf(0f) }
                var iconOffsetY by remember { mutableStateOf(0f) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1500)
                        iconScale = 1.2f
                        iconRotation = 8f
                        iconOffsetY = -5f
                        delay(300)
                        iconScale = 1f
                        iconRotation = 0f
                        iconOffsetY = 0f
                        delay(1500)
                        iconScale = 0.95f
                        iconOffsetY = 3f
                        delay(300)
                        iconScale = 1f
                        iconOffsetY = 0f
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(y = iconOffsetY.dp)
                        .scale(iconScale)
                        .rotate(iconRotation)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.8f),
                            ambientColor = Color(0xFFF4A261).copy(alpha = 0.4f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFF8DC),
                                    Color(0xFFFFE5A0),
                                    Color(0xFFFFD966)
                                )
                            ),
                            shape = CircleShape
                        )
                        .drawBehind {
                            // 多层发光效果
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.5f),
                                        Color(0xFFFFD700).copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 + 12.dp.toPx()
                            )
                            // 内圈高光
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.White.copy(alpha = 0.6f),
                                        0.5f to Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 - 8.dp.toPx()
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐯",
                        fontSize = 48.sp
                    )
                }

                // 标题
                Text(
                    text = "家长验证",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5F7A),
                    modifier = Modifier
                        .drawBehind {
                            // 渐变背景
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF159895).copy(alpha = 0.15f),
                                        Color(0xFFFFD700).copy(alpha = 0.2f),
                                        Color(0xFF159895).copy(alpha = 0.15f)
                                    )
                                ),
                                cornerRadius = CornerRadius(12.dp.value, 12.dp.value)
                            )
                        }
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                )

                // 提示文字
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔐",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "请回答数学问题以继续",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF555555)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 数学问题卡片 - 带增强效果
                var questionScale by remember { mutableStateOf(1f) }
                var questionGlow by remember { mutableStateOf(0f) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(2500)
                        questionScale = 1.06f
                        questionGlow = 1f
                        delay(250)
                        questionScale = 1f
                        delay(250)
                        questionGlow = 0f
                    }
                }

                Box(
                    modifier = Modifier
                        .scale(questionScale)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0xFFE63946).copy(alpha = 0.6f + questionGlow * 0.3f),
                            ambientColor = Color(0xFFE63946).copy(alpha = 0.4f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFFFF5F5),
                                    Color(0xFFFFE8E8)
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1f, 1f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .drawBehind {
                            // 渐变边框 - 动态发光效果
                            val strokeWidth = 3.5.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE63946),
                                        Color(0xFFFF6B6B).copy(alpha = 0.8f + questionGlow * 0.2f),
                                        Color(0xFFE63946)
                                    )
                                ),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                                cornerRadius = CornerRadius(24.dp.value, 24.dp.value)
                            )
                            // 内部高光
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                cornerRadius = CornerRadius(20.dp.value, 20.dp.value)
                            )
                        }
                        .padding(horizontal = 36.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = question,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE63946)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 数字选项网格 - 优化版
                val numberRows = listOf(
                    listOf(2, 3, 4, 5, 6),
                    listOf(7, 8, 9, 10, 11),
                    listOf(12, 13, 14, 15, 16),
                    listOf(17, 18)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    numberRows.forEach { row ->
                        CircularNumberButtonRowOptimized(
                            numbers = row,
                            onSubmit = onSubmit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 取消按钮 - 优化版
                var cancelScale by remember { mutableStateOf(1f) }
                Box(
                    modifier = Modifier
                        .scale(cancelScale)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0xFF6C757D).copy(alpha = 0.4f)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            cancelScale = 0.92f
                            onDismiss()
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6C757D).copy(alpha = 0.15f),
                                    Color(0xFF6C757D).copy(alpha = 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .drawBehind {
                            // 边框
                            val strokeWidth = 1.5.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6C757D).copy(alpha = 0.3f),
                                        Color(0xFF6C757D).copy(alpha = 0.15f),
                                        Color(0xFF6C757D).copy(alpha = 0.3f)
                                    )
                                ),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                                cornerRadius = CornerRadius(24.dp.value, 24.dp.value)
                            )
                        }
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )
                        Text(
                            text = "取消",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C757D)
                        )
                    }
                }

                LaunchedEffect(cancelScale) {
                    if (cancelScale != 1f) {
                        delay(100)
                        cancelScale = 1f
                    }
                }
            }
        }
    }
}

/**
 * 优化的圆形数字按钮行 - 带触觉反馈
 */
@Composable
private fun CircularNumberButtonRowOptimized(
    numbers: List<Int>,
    onSubmit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        numbers.forEach { num ->
            // 每个按钮独立的动画状态
            var buttonScale by remember { mutableStateOf(1f) }
            var buttonRotation by remember { mutableStateOf(0f) }
            var isPressed by remember { mutableStateOf(false) }
            var showRipple by remember { mutableStateOf(false) }

            // 使用不同的渐变色系列
            val colorScheme = when (num) {
                in 2..4 -> listOf(Color(0xFF159895), Color(0xFF57C5B6))
                in 5..8 -> listOf(Color(0xFF2A9D8F), Color(0xFF57C5B6))
                in 9..12 -> listOf(Color(0xFF1A5F7A), Color(0xFF159895))
                in 13..16 -> listOf(Color(0xFF264653), Color(0xFF2A9D8F))
                else -> listOf(Color(0xFFE76F51), Color(0xFFFF6B6B))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(buttonScale)
                    .rotate(buttonRotation)
                    .size(56.dp)
                    .shadow(
                        elevation = if (isPressed) 6.dp else 14.dp,
                        shape = CircleShape,
                        spotColor = colorScheme[0].copy(alpha = 0.6f),
                        ambientColor = colorScheme[1].copy(alpha = 0.4f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        buttonScale = 0.82f
                        buttonRotation = -8f
                        isPressed = true
                        showRipple = true
                        onSubmit(num)
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = colorScheme,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(1f, 1f)
                        ),
                        shape = CircleShape
                    )
                    .drawBehind {
                        // 内部高光效果
                        drawCircle(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.White.copy(alpha = 0.5f),
                                    0.25f to Color.White.copy(alpha = 0.25f),
                                    0.6f to Color.Transparent,
                                    1.0f to Color(0xFF000000).copy(alpha = 0.15f)
                                )
                            ),
                            radius = size.minDimension / 2
                        )
                        // 外部发光效果
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colorScheme[0].copy(alpha = 0.4f),
                                    colorScheme[0].copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2 + 6.dp.toPx(),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                        // 点击时的波纹效果
                        if (showRipple) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2 * 0.8f
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$num",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .drawBehind {
                            // 文字阴影效果
                            drawCircle(
                                color = Color(0xFF000000).copy(alpha = 0.25f),
                                radius = 32.dp.toPx()
                            )
                        }
                )
            }

            // 动画恢复逻辑
            LaunchedEffect(buttonScale, buttonRotation) {
                if (buttonScale != 1f || buttonRotation != 0f) {
                    delay(180)
                    buttonScale = 1f
                    buttonRotation = 0f
                    delay(50)
                    isPressed = false
                    delay(150)
                    showRipple = false
                }
            }
        }
    }
}

/**
 * 优化的时间设置对话框 - 带触觉反馈
 */
@Composable
private fun TimeSettingsDialogOptimized(
    sessionEnabled: Boolean = false,
    dailyEnabled: Boolean = false,
    onSessionToggle: (Boolean) -> Unit = {},
    onDailyToggle: (Boolean) -> Unit = {},
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    // 对话框动画
    var dialogScale by remember { mutableStateOf(0.7f) }
    var dialogAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogAlpha = 1f
    }

    // 背景遮罩
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        // 对话框内容
        Box(
            modifier = Modifier
                .scale(dialogScale)
                .width(320.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFFE63946).copy(alpha = 0.4f)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Text(
                    text = "使用时长设置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5F7A)
                )

                Divider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // 每次使用时长开关 - 优化版
                ToggleRowOptimized(
                    label = "每次使用时长限制",
                    subtitle = "单次使用最长时间",
                    isEnabled = sessionEnabled,
                    onToggle = onSessionToggle
                )

                // 每日总时长开关 - 优化版
                ToggleRowOptimized(
                    label = "每日总时长限制",
                    subtitle = "每天总使用时间",
                    isEnabled = dailyEnabled,
                    onToggle = onDailyToggle
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 保存按钮 - 优化版
                var saveScale by remember { mutableStateOf(1f) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(saveScale)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = Color(0xFFE63946).copy(alpha = 0.5f)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            saveScale = 0.95f
                            onSave()
                        }
                        .background(
                            color = Color(0xFFE63946),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "保存",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                LaunchedEffect(saveScale) {
                    if (saveScale != 1f) {
                        delay(100)
                        saveScale = 1f
                    }
                }
            }
        }
    }
}

/**
 * 优化的开关行组件
 */
@Composable
private fun ToggleRowOptimized(
    label: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF999999)
            )
        }

        // 开关组件 - 优化版
        ToggleSwitchSimpleOptimized(
            isEnabled = isEnabled,
            onToggle = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle(it)
            }
        )
    }
}

/**
 * 优化的简化开关组件
 */
@Composable
private fun ToggleSwitchSimpleOptimized(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var switchScale by remember { mutableStateOf(1f) }

    // 动画过渡状态
    val togglePosition by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "toggle_position"
    )

    Box(
        modifier = Modifier
            .scale(switchScale)
            .width(52.dp)
            .height(28.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = if (isEnabled) {
                    Color(0xFF4CAF50).copy(alpha = 0.4f)
                } else {
                    Color.Gray.copy(alpha = 0.2f)
                }
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                switchScale = 0.92f
                onToggle(!isEnabled)
            }
            .background(
                color = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(14.dp)
            )
            .drawBehind {
                // 圆形指示器 - 带动画
                val circleSize = 22.dp.toPx()
                val baseOffset = 3.dp.toPx()
                val maxOffset = size.width - circleSize - baseOffset
                val offset = baseOffset + (maxOffset - baseOffset) * togglePosition

                drawCircle(
                    color = Color.White,
                    radius = circleSize / 2,
                    center = androidx.compose.ui.geometry.Offset(
                        offset + circleSize / 2,
                        size.height / 2
                    )
                )
            }
    )

    LaunchedEffect(switchScale) {
        if (switchScale != 1f) {
            delay(100)
            switchScale = 1f
        }
    }
}

/**
 * 优化的设置保存成功提示覆盖层
 */
@Composable
private fun SettingsSavedHintOverlayOptimized(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    // 动画效果
    var overlayScale by remember { mutableStateOf(0.8f) }
    var overlayAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        overlayScale = 1f
        overlayAlpha = 1f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "success_animation")
    val checkPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "check_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(overlayScale)
                .alpha(overlayAlpha)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓",
                    fontSize = 28.sp,
                    modifier = Modifier.scale(checkPulse),
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "设置已保存",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * 优化的重置成功提示覆盖层
 */
@Composable
private fun ResetSuccessHintOverlayOptimized(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    // 动画效果
    var overlayScale by remember { mutableStateOf(0.8f) }
    var overlayAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        overlayScale = 1f
        overlayAlpha = 1f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "reset_success_animation")
    val checkPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "check_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(overlayScale)
                .alpha(overlayAlpha)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓",
                    fontSize = 28.sp,
                    modifier = Modifier.scale(checkPulse),
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "游戏进度已重置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * 优化的验证失败提示覆盖层
 */
@Composable
private fun VerificationFailedHintOverlayOptimized(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    // 动画效果
    var overlayScale by remember { mutableStateOf(0.8f) }
    var overlayAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        overlayScale = 1f
        overlayAlpha = 1f
    }

    // 错误震动动画
    val infiniteTransition = rememberInfiniteTransition(label = "error_animation")
    val errorShake by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_shake"
    )

    val errorPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(overlayScale)
                .offset(x = errorShake.dp)
                .alpha(overlayAlpha)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕",
                    fontSize = 28.sp,
                    modifier = Modifier.scale(errorPulse),
                    color = Color(0xFFE63946)
                )
                Text(
                    text = "答案不正确，请重试",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * 格式化时长（毫秒转为可读格式）
 */
private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}小时${minutes % 60}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "${seconds}秒"
    }
}

/**
 * 获取最近7天的使用时长（分钟）
 */
private fun getLast7DaysMinutes(dailyUsageStats: Map<String, Long>): List<Int> {
    val result = mutableListOf<Long>()
    val calendar = java.util.Calendar.getInstance()

    // 从今天开始往前推7天（包括今天）
    for (i in 0..6) {
        calendar.time = java.util.Date()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(calendar.time)
        val milliseconds = dailyUsageStats[dateStr] ?: 0L
        result.add(milliseconds / 1000 / 60)
    }

    return result.map { it.toInt() }.reversed()
}
