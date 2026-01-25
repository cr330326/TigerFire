package com.cryallen.tigerfire.ui.parent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
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
 * 家长模式 Screen - 现代化儿童友好风格
 *
 * 提供时间管理、使用统计、进度重置等功能
 * 敏感操作需要数学验证
 *
 * @param viewModel ParentViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun ParentScreen(
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }

    // 页面进入动画
    var contentVisible by remember { mutableStateOf(false) }
    var pageScale by remember { mutableStateOf(0.85f) }
    var pageAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        pageScale = 1f
        pageAlpha = 1f
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

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ParentEffect.ShowSettingsSavedHint -> {
                    // TODO: 显示设置保存成功提示
                }
                is ParentEffect.ShowResetSuccessHint -> {
                    // TODO: 显示重置成功提示
                }
                is ParentEffect.ShowVerificationFailedHint -> {
                    // TODO: 显示验证失败提示
                }
                is ParentEffect.PlayClickSound -> {
                    audioManager.playClickSound()
                }
                is ParentEffect.NavigateToMap -> onNavigateBack()
            }
        }
    }

    // 渐变背景（蓝绿色系）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A5F7A),  // 深蓝色
                        Color(0xFF159895),  // 青绿色
                        Color(0xFF57C5B6),  // 浅青绿色
                        Color(0xFF159895),  // 青绿色
                        Color(0xFF1A5F7A)   // 深蓝色
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
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(pageScale)
                .alpha(pageAlpha)
        ) {
            // 顶部工具栏
            ParentTopBar(
                onBackClick = {
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
                // 标题区域
                ParentTitleSection()

                // 可滚动内容
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 使用统计卡片
                    item {
                        UsageStatsCard(
                            todayPlayTime = state.todayPlayTime,
                            totalPlayTime = state.totalPlayTime,
                            totalBadgeCount = state.totalBadgeCount
                        )
                    }

                    // 时间设置卡片
                    item {
                        TimeSettingsCard(
                            sessionTimeLimit = state.settings.sessionDurationMinutes,
                            reminderEnabled = state.settings.reminderMinutesBefore > 0,
                            onSessionTimeLimitChange = { minutes ->
                                viewModel.onEvent(ParentEvent.UpdateSessionTimeLimit(minutes))
                            },
                            onReminderToggle = { enabled ->
                                viewModel.onEvent(ParentEvent.UpdateReminderTime(if (enabled) 2 else 0))
                            }
                        )
                    }

                    // 进度管理卡片
                    item {
                        ProgressManagementCard(
                            onResetProgress = {
                                viewModel.onEvent(ParentEvent.ResetProgressClicked)
                            }
                        )
                    }

                    // 底部留白
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // 重置确认对话框
        if (state.showResetConfirmation) {
            ResetConfirmationDialog(
                onConfirm = {
                    viewModel.onEvent(ParentEvent.ConfirmResetProgress)
                },
                onDismiss = {
                    viewModel.onEvent(ParentEvent.CancelResetProgress)
                }
            )
        }

        // 数学验证对话框
        if (state.showReverification) {
            MathVerificationDialog(
                question = state.reverificationQuestion?.first ?: "",
                expectedAnswer = state.reverificationQuestion?.second ?: 0,
                onSubmit = { answer ->
                    viewModel.onEvent(ParentEvent.SubmitReverificationAnswer(answer))
                },
                onDismiss = {
                    viewModel.onEvent(ParentEvent.CancelReverification)
                }
            )
        }
    }
}

/**
 * 顶部工具栏
 */
@Composable
private fun ParentTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮 - 带动画
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

        // 右侧装饰图标
        var iconScale by remember { mutableStateOf(1f) }
        Box(
            modifier = Modifier
                .scale(iconScale)
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFF4A261)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔧",
                fontSize = 32.sp
            )
        }

        // 图标呼吸动画
        LaunchedEffect(Unit) {
            while (true) {
                delay(1500)
                iconScale = 1.1f
                delay(1500)
                iconScale = 1f
            }
        }
    }
}

/**
 * 标题区域 - 带动画效果
 */
@Composable
private fun ParentTitleSection() {
    // 标题淡入动画
    var titleAlpha by remember { mutableStateOf(0f) }
    var titleOffsetY by remember { mutableStateOf(30f) }

    LaunchedEffect(Unit) {
        titleAlpha = 1f
        titleOffsetY = 0f
    }

    Column(
        modifier = Modifier
            .alpha(titleAlpha)
            .offset(y = titleOffsetY.dp)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 小火图标
        var tigerScale by remember { mutableStateOf(1f) }
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(tigerScale)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.6f)
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
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.5f),
                        radius = size.minDimension / 2 - 5.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 5.dp.toPx()
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

        // 标题文字
        Text(
            text = "🔧 家长模式 🔧",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "管理孩子使用时间和进度",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        // 小火图标动画
        LaunchedEffect(Unit) {
            while (true) {
                delay(2000)
                tigerScale = 1.08f
                delay(2000)
                tigerScale = 1f
            }
        }
    }
}

/**
 * 使用统计卡片 - 现代化设计
 */
@Composable
private fun UsageStatsCard(
    todayPlayTime: Long,
    totalPlayTime: Long,
    totalBadgeCount: Int
) {
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
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = cardOffsetY.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
            .scale(cardScale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
            )
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
            }
            .padding(20.dp)
    ) {
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
            StatItem(
                icon = "⏰",
                label = "今日使用时长",
                value = formatDuration(todayPlayTime),
                valueColor = Color(0xFFE63946)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 总使用时长
            StatItem(
                icon = "⌛",
                label = "总使用时长",
                value = formatDuration(totalPlayTime),
                valueColor = Color(0xFF159895)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 已收集徽章
            StatItem(
                icon = "🏅",
                label = "已收集徽章",
                value = "$totalBadgeCount 枚",
                valueColor = Color(0xFFF4A261)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 本周使用时长图表
            WeeklyUsageChart()
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = valueColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
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
}

/**
 * 本周使用时长图表 - 改进版
 */
@Composable
private fun WeeklyUsageChart() {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    val dailyMinutes = listOf(45, 60, 30, 75, 50, 90, 40)
    val maxMinutes = 120

    val totalMinutes = dailyMinutes.sum()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

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
                text = "总计 ${hours}h ${minutes}m",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF159895)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 柱状图
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEachIndexed { index, day ->
                val mins = dailyMinutes[index]
                val barHeight = (mins.toFloat() / maxMinutes * 70).coerceAtLeast(10f)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 柱子 - 渐变色
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(6.dp),
                                spotColor = Color(0xFF57C5B6).copy(alpha = 0.5f)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF159895),
                                        Color(0xFF57C5B6)
                                    )
                                ),
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
 * 时间设置卡片 - 现代化设计
 */
@Composable
private fun TimeSettingsCard(
    sessionTimeLimit: Int,
    reminderEnabled: Boolean = true,
    onSessionTimeLimitChange: (Int) -> Unit,
    onReminderToggle: (Boolean) -> Unit = {}
) {
    // 卡片入场动画
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        cardVisible = true
    }

    val timeOptions = listOf(5, 10, 15, 30)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
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

            // 时间选项按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                timeOptions.forEach { minutes ->
                    TimeOptionButton(
                        minutes = minutes,
                        isSelected = sessionTimeLimit == minutes,
                        onClick = { onSessionTimeLimitChange(minutes) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 提醒设置
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

                // 开关按钮
                ToggleSwitch(
                    isEnabled = reminderEnabled,
                    onToggle = { onReminderToggle(!reminderEnabled) }
                )
            }
        }
    }
}

/**
 * 时间选项按钮 - 现代化设计
 */
@Composable
private fun TimeOptionButton(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var buttonScale by remember { mutableStateOf(1f) }

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
            .scale(buttonScale)
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
 * 开关组件 - 现代化设计
 */
@Composable
private fun ToggleSwitch(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    var switchScale by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .scale(switchScale)
            .width(60.dp)
            .height(34.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(17.dp),
                spotColor = if (isEnabled) {
                    Color(0xFF159895).copy(alpha = 0.5f)
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                }
            )
            .clickable {
                switchScale = 0.92f
                onToggle()
            }
            .background(
                color = if (isEnabled) Color(0xFF159895) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(17.dp)
            )
            .drawBehind {
                // 圆形指示器
                val circleSize = 26.dp.toPx()
                val offset = if (isEnabled) {
                    size.width - circleSize - 4.dp.toPx()
                } else {
                    4.dp.toPx()
                }
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
 * 进度管理卡片 - 现代化设计
 */
@Composable
private fun ProgressManagementCard(
    onResetProgress: () -> Unit
) {
    // 卡片入场动画
    LaunchedEffect(Unit) {
        delay(300)
    }

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

            // 警告提示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

            // 重置按钮
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
 * 重置确认对话框 - 现代化设计
 */
@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
            .clickable(onClick = onDismiss),
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
                // 警告图标
                var warningScale by remember { mutableStateOf(1f) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(800)
                        warningScale = 1.15f
                        delay(800)
                        warningScale = 1f
                    }
                }

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
 * 数学验证对话框 - 现代化数字按钮设计
 */
@Composable
private fun MathVerificationDialog(
    question: String,
    expectedAnswer: Int,
    onSubmit: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 对话框动画
    var dialogScale by remember { mutableStateOf(0.7f) }
    var dialogAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogAlpha = 1f
    }

    // 数字选项（2-18覆盖所有可能的答案）
    val numberOptions = (2..18).toList()

    // 背景遮罩
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
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
                    spotColor = Color(0xFF159895).copy(alpha = 0.5f)
                )
                .background(
                    color = Color(0xFFFFF8DC), // 象牙色
                    shape = RoundedCornerShape(28.dp)
                )
                .drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF159895),
                                Color(0xFFFFD700),
                                Color(0xFF159895)
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
                // 小火图标 - 呼吸动画
                var iconScale by remember { mutableStateOf(1f) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1500)
                        iconScale = 1.1f
                        delay(1500)
                        iconScale = 1f
                    }
                }

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .scale(iconScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFFFF8DC)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐯",
                        fontSize = 40.sp
                    )
                }

                // 标题
                Text(
                    text = "家长验证",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A5F7A)
                )

                // 提示文字
                Text(
                    text = "请回答数学问题",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                // 数学问题
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0xFFE63946).copy(alpha = 0.4f)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = question,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE63946)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 数字选项网格（6列）
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 第一行：2-7
                    NumberButtonRow(
                        numbers = listOf(2, 3, 4, 5, 6, 7),
                        onSubmit = onSubmit
                    )
                    // 第二行：8-13
                    NumberButtonRow(
                        numbers = listOf(8, 9, 10, 11, 12, 13),
                        onSubmit = onSubmit
                    )
                    // 第三行：14-18
                    NumberButtonRow(
                        numbers = listOf(14, 15, 16, 17, 18),
                        onSubmit = onSubmit,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 取消按钮
                var cancelScale by remember { mutableStateOf(1f) }
                Text(
                    text = "取消",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6C757D),
                    modifier = Modifier
                        .scale(cancelScale)
                        .clickable {
                            cancelScale = 0.92f
                            onDismiss()
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )

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
 * 数字按钮行 - 每行6个数字
 */
@Composable
private fun NumberButtonRow(
    numbers: List<Int>,
    onSubmit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        numbers.forEach { num ->
            var buttonScale by remember { mutableStateOf(1f) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(buttonScale)
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0xFF159895).copy(alpha = 0.5f)
                    )
                    .clickable {
                        buttonScale = 0.88f
                        onSubmit(num)
                    }
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF159895),
                                Color(0xFF57C5B6)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .drawBehind {
                        // 高光效果
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            cornerRadius = CornerRadius(12.dp.value, 12.dp.value)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$num",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
