package com.cryallen.tigerfire.ui.parent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 使用统计卡片
                    item {
                        UsageStatsCard(
                            todayPlayTime = state.todayPlayTime,
                            totalPlayTime = state.totalPlayTime,
                            totalBadgeCount = state.totalBadgeCount,
                            dailyUsageStats = state.settings.dailyUsageStats
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
                            },
                            onShowDialog = {
                                viewModel.onEvent(ParentEvent.ShowTimeSettingsDialog)
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

        // 时间设置对话框
        if (state.showTimeSettingsDialog) {
            TimeSettingsDialog(
                sessionEnabled = state.sessionTimeLimitEnabled,
                dailyEnabled = state.dailyTimeLimitEnabled,
                onSessionToggle = { enabled ->
                    viewModel.onEvent(ParentEvent.ToggleSessionTimeLimit(enabled))
                },
                onDailyToggle = { enabled ->
                    viewModel.onEvent(ParentEvent.ToggleDailyTimeLimit(enabled))
                },
                onSave = {
                    viewModel.onEvent(ParentEvent.SaveTimeSettings)
                },
                onDismiss = {
                    viewModel.onEvent(ParentEvent.DismissTimeSettingsDialog)
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
    totalBadgeCount: Int,
    dailyUsageStats: Map<String, Long>
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
            WeeklyUsageChart(dailyUsageStats = dailyUsageStats)
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
 * 本周使用时长图表 - 使用真实数据
 */
@Composable
private fun WeeklyUsageChart(dailyUsageStats: Map<String, Long>) {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")

    // 获取本周7天的数据（毫秒转分钟）
    val dailyMinutes = getLast7DaysMinutes(dailyUsageStats)
    val maxMinutes = dailyMinutes.maxOrNull()?.coerceAtLeast(60) ?: 60

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
                text = if (totalMinutes > 0) "总计 ${hours}h ${minutes}m" else "暂无数据",
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
                val barHeight = if (mins > 0) {
                    (mins.toFloat() / maxMinutes * 70).coerceAtLeast(10f)
                } else {
                    4f  // 最小高度显示空数据
                }

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
                                elevation = if (mins > 0) 4.dp else 0.dp,
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
 * 获取最近7天的使用时长（分钟）
 * 从后往前：昨天、前天...7天前
 */
private fun getLast7DaysMinutes(dailyUsageStats: Map<String, Long>): List<Int> {
    val result = mutableListOf<Long>()
    val calendar = java.util.Calendar.getInstance()

    // 从昨天开始往前推7天
    for (i in 1..7) {
        calendar.time = java.util.Date()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(calendar.time)
        val milliseconds = dailyUsageStats[dateStr] ?: 0L
        result.add(milliseconds / 1000 / 60)  // 转换为分钟
    }

    return result.map { it.toInt() }.reversed()  // 反转使周一在前
}

/**
 * 时间设置卡片 - 现代化设计
 *
 * 点击卡片可弹出详细设置对话框
 */
@Composable
private fun TimeSettingsCard(
    sessionTimeLimit: Int,
    reminderEnabled: Boolean = true,
    onSessionTimeLimitChange: (Int) -> Unit,
    onReminderToggle: (Boolean) -> Unit = {},
    onShowDialog: () -> Unit = {}
) {
    // 卡片入场动画
    var cardVisible by remember { mutableStateOf(false) }
    var cardScale by remember { mutableStateOf(1f) }
    LaunchedEffect(Unit) {
        delay(200)
        cardVisible = true
    }

    val timeOptions = listOf(5, 10, 15, 30)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .scale(cardScale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
            )
            .clickable {
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
 * 数学验证对话框 - 现代化玻璃拟态设计（增强版）
 * 参考：儿童友好的大触摸目标、明亮的颜色、即时反馈
 */
@Composable
private fun MathVerificationDialog(
    question: String,
    expectedAnswer: Int,
    onSubmit: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 对话框入场动画 - 更流畅的弹簧效果
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
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // 对话框内容 - 增强版玻璃拟态
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

                    // 内部高光效果 - 增加立体感
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
                // 小火图标 - 更生动的动画效果
                var iconScale by remember { mutableStateOf(1f) }
                var iconRotation by remember { mutableStateOf(0f) }
                var iconOffsetY by remember { mutableStateOf(0f) }

                LaunchedEffect(Unit) {
                    while (true) {
                        // 向上浮动并放大
                        delay(1500)
                        iconScale = 1.2f
                        iconRotation = 8f
                        iconOffsetY = -5f
                        delay(300)
                        // 向下浮动
                        iconScale = 1f
                        iconRotation = 0f
                        iconOffsetY = 0f
                        delay(1500)
                        // 向下缩小
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

                // 标题 - 渐变背景效果
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

                // 提示文字 - 添加图标
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

                // 数学问题卡片 - 增强视觉效果
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

                // 数字选项网格 - 5列布局，圆形按钮更适合儿童
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
                        CircularNumberButtonRow(
                            numbers = row,
                            onSubmit = onSubmit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 取消按钮 - 现代化设计
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
 * 圆形数字按钮行 - 每行5个圆形按钮（增强版儿童友好设计）
 * 圆形按钮更适合儿童操作，触摸目标更大且更直观
 */
@Composable
private fun CircularNumberButtonRow(
    numbers: List<Int>,
    onSubmit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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

            // 使用不同的渐变色系列，让按钮更有趣且色彩丰富
            val colorScheme = when (num) {
                in 2..4 -> listOf(Color(0xFF159895), Color(0xFF57C5B6))       // 青绿色系
                in 5..8 -> listOf(Color(0xFF2A9D8F), Color(0xFF57C5B6))      // 绿松石系
                in 9..12 -> listOf(Color(0xFF1A5F7A), Color(0xFF159895))    // 蓝绿色系
                in 13..16 -> listOf(Color(0xFF264653), Color(0xFF2A9D8F))   // 深青绿色系
                else -> listOf(Color(0xFFE76F51), Color(0xFFFF6B6B))        // 珊瑚红系（17-18）
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(buttonScale)
                    .rotate(buttonRotation)
                    .size(56.dp)  // 更大的触摸目标（符合儿童友好的≥100pt标准）
                    .shadow(
                        elevation = if (isPressed) 6.dp else 14.dp,
                        shape = CircleShape,
                        spotColor = colorScheme[0].copy(alpha = 0.6f),
                        ambientColor = colorScheme[1].copy(alpha = 0.4f)
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null  // 禁用默认波纹，使用自定义效果
                    ) {
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
                        // 内部高光效果 - 增加立体感
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
                        // 外部发光效果 - 多层次光晕
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

            // 动画恢复逻辑 - 更平滑的过渡
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
 * 时间设置对话框 - 参考截图设计
 *
 * 两个开关 + 保存按钮的弹出对话框
 */
@Composable
private fun TimeSettingsDialog(
    sessionEnabled: Boolean = false,
    dailyEnabled: Boolean = false,
    onSessionToggle: (Boolean) -> Unit = {},
    onDailyToggle: (Boolean) -> Unit = {},
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {}
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
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // 对话框内容 - 参考截图样式
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

                // 每次使用时长开关
                ToggleRow(
                    label = "每次使用时长限制",
                    subtitle = "单次使用最长时间",
                    isEnabled = sessionEnabled,
                    onToggle = onSessionToggle
                )

                // 每日总时长开关
                ToggleRow(
                    label = "每日总时长限制",
                    subtitle = "每天总使用时间",
                    isEnabled = dailyEnabled,
                    onToggle = onDailyToggle
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 保存按钮 - 红色圆角
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
 * 开关行组件 - 用于对话框中
 */
@Composable
private fun ToggleRow(
    label: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
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

        // 开关组件 - 简化版
        ToggleSwitchSimple(
            isEnabled = isEnabled,
            onToggle = onToggle
        )
    }
}

/**
 * 简化的开关组件 - 参考截图样式
 *
 * 尺寸：52x28dp
 * 开启：绿色 (#4CAF50)
 * 关闭：灰色 (#BDBDBD)
 */
@Composable
private fun ToggleSwitchSimple(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var switchScale by remember { mutableStateOf(1f) }

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
                switchScale = 0.92f
                onToggle(!isEnabled)
            }
            .background(
                color = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(14.dp)
            )
            .drawBehind {
                // 圆形指示器
                val circleSize = 22.dp.toPx()
                val offset = if (isEnabled) {
                    size.width - circleSize - 3.dp.toPx()
                } else {
                    3.dp.toPx()
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
