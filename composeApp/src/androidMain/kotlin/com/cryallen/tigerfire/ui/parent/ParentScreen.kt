package com.cryallen.tigerfire.ui.parent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlin.math.min

/**
 * 家长模式 Screen - 优化版
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

    // 数学验证输入
    var answerInput by remember { mutableStateOf("") }

    // 页面进入动画
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        contentVisible = true
    }

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
                    answerInput = ""
                }
                is ParentEffect.PlayClickSound -> {
                    // TODO: 音效播放暂时跳过
                }
                is ParentEffect.NavigateToMap -> onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6C757D)) // 灰色背景
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮 + 标题）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                var buttonScale by remember { mutableStateOf(1f) }
                IconButton(
                    onClick = {
                        buttonScale = 0.9f
                        viewModel.onEvent(ParentEvent.BackToMapClicked)
                    },
                    modifier = Modifier
                        .scale(buttonScale)
                        .size(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
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
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                radius = size.minDimension / 2 - 3.dp.toPx(),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                ) {
                    Text(
                        text = "←",
                        fontSize = 28.sp,
                        color = Color(0xFF6C757D)
                    )
                }

                LaunchedEffect(buttonScale) {
                    if (buttonScale != 1f) {
                        delay(100)
                        buttonScale = 1f
                    }
                }

                // 右侧装饰图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.4f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFF8DC),
                                    Color(0xFFFFD700)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔧",
                        fontSize = 28.sp
                    )
                }
            }

            // 主内容区域 - 使用 Column 嵌套结构
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题区域
                Text(
                    text = "🔧 家长模式 🔧",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 使用可滚动内容
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            dailyTimeLimit = state.settings.sessionDurationMinutes,
                            reminderEnabled = state.settings.reminderMinutesBefore > 0,
                            onSessionTimeLimitChange = { minutes ->
                                viewModel.onEvent(ParentEvent.UpdateSessionTimeLimit(minutes))
                            },
                            onDailyTimeLimitChange = { minutes ->
                                viewModel.onEvent(ParentEvent.UpdateDailyTimeLimit(minutes))
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
                answer = answerInput,
                onAnswerChange = { answerInput = it },
                onSubmit = {
                    answerInput.toIntOrNull()?.let { answer ->
                        viewModel.onEvent(ParentEvent.SubmitReverificationAnswer(answer))
                    }
                },
                onDismiss = {
                    answerInput = ""
                    viewModel.onEvent(ParentEvent.CancelReverification)
                }
            )
        }
    }
}

/**
 * 使用统计卡片 - 包含柱状图（优化版）
 */
@Composable
private fun UsageStatsCard(
    todayPlayTime: Long,
    totalPlayTime: Long,
    totalBadgeCount: Int
) {
    // 卡片缩放动画
    val infiniteTransition = rememberInfiniteTransition(label = "usage_card_pulse")
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.005f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.4f)
            )
            .drawBehind {
                // 渐变边框
                val strokeWidth = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFF457B9D),
                            Color(0xFFFFD700)
                        )
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(20.dp.value, 20.dp.value)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📊 使用统计",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatRow(
                label = "今日使用时长",
                value = formatDuration(todayPlayTime),
                valueColor = Color(0xFFE63946)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatRow(
                label = "总使用时长",
                value = formatDuration(totalPlayTime),
                valueColor = Color(0xFF457B9D)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatRow(
                label = "已收集徽章",
                value = "$totalBadgeCount 枚",
                valueColor = Color(0xFF2A9D8F)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 本周使用时长柱状图
            WeeklyUsageChart()
        }
    }
}

/**
 * 本周使用时长柱状图（优化版）
 */
@Composable
private fun WeeklyUsageChart() {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    // 示例数据（模拟）
    val dailyMinutes = listOf(45, 60, 30, 75, 50, 90, 40)
    val maxMinutes = 120

    // 计算总时长
    val totalMinutes = dailyMinutes.sum()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 标题和总计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📈 本周使用时长",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "总计: ${if (hours > 0) "${hours}小时" else ""}${minutes}分钟",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF4A261)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 柱状图
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEachIndexed { index, day ->
                val mins = dailyMinutes[index]
                val barHeight = (mins.toFloat() / maxMinutes * 60).coerceAtLeast(8f)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 柱子 - 使用渐变色和阴影
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(4.dp),
                                spotColor = Color(0xFF457B9D).copy(alpha = 0.4f)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF457B9D),
                                        Color(0xFFA8DADC)
                                    )
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 星期标签
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * 时间设置卡片 - 固定选项（带提前提醒设置）
 */
@Composable
private fun TimeSettingsCard(
    sessionTimeLimit: Int,
    dailyTimeLimit: Int,
    reminderEnabled: Boolean = true,
    onSessionTimeLimitChange: (Int) -> Unit,
    onDailyTimeLimitChange: (Int) -> Unit,
    onReminderToggle: (Boolean) -> Unit = {}
) {
    // 时间选项（固定）
    val timeOptions = listOf(5, 10, 15, 30)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.4f)
            )
            .drawBehind {
                // 渐变边框
                val strokeWidth = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFF457B9D),
                            Color(0xFFFFD700)
                        )
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(20.dp.value, 20.dp.value)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "⏱️ 时间设置",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 每次使用时长标题
            Text(
                text = "每次使用时长",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 时间选项按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeOptions.forEach { minutes ->
                    val isSelected = sessionTimeLimit == minutes
                    TimeOptionButton(
                        minutes = minutes,
                        isSelected = isSelected,
                        onClick = { onSessionTimeLimitChange(minutes) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 提前提醒设置
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "提前提醒",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "时间到前2分钟提醒",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // 开关按钮
                var switchScale by remember { mutableStateOf(1f) }
                Box(
                    modifier = Modifier
                        .scale(switchScale)
                        .width(56.dp)
                        .height(32.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = if (reminderEnabled) {
                                Color(0xFF2A9D8F).copy(alpha = 0.4f)
                            } else {
                                Color.Gray.copy(alpha = 0.2f)
                            }
                        )
                        .clickable {
                            switchScale = 0.95f
                            onReminderToggle(!reminderEnabled)
                        }
                        .background(
                            color = if (reminderEnabled) Color(0xFF2A9D8F) else Color(0xFF757575),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .drawBehind {
                            // 开关圆形指示器
                            val circleSize = 24.dp.toPx()
                            val offset = if (reminderEnabled) {
                                size.width - circleSize - 4.dp.toPx()
                            } else {
                                4.dp.toPx()
                            }
                            drawCircle(
                                color = Color.White,
                                radius = circleSize / 2,
                                center = Offset(offset + circleSize / 2, size.height / 2)
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
        }
    }
}

/**
 * 时间选项按钮
 */
@Composable
private fun TimeOptionButton(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF2A9D8F),
                Color(0xFF95D5B2)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                Color.White.copy(alpha = 0.1f)
            )
        )
    }

    val textColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(10.dp),
                spotColor = if (isSelected) {
                    Color(0xFF2A9D8F).copy(alpha = 0.4f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .background(
                brush = backgroundColor,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${minutes}分钟",
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * 进度管理卡片
 */
@Composable
private fun ProgressManagementCard(
    onResetProgress: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🔧 进度管理",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "重置游戏进度将清除所有徽章和场景完成状态，此操作不可恢复。",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 重置按钮
            Text(
                text = "重置游戏进度",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE63946))
                    .padding(vertical = 12.dp)
                    .clickable(onClick = onResetProgress)
            )
        }
    }
}

/**
 * 卡片容器
 */
@Composable
private fun Card(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

/**
 * 统计行
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFFF4A261)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

/**
 * 重置确认对话框
 */
@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "确认重置进度？",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "此操作将清除所有徽章和场景完成状态，\n且无法恢复！",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 取消按钮
                    Text(
                        text = "取消",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .clickable(onClick = onDismiss)
                    )

                    // 确认按钮
                    Text(
                        text = "确认重置",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE63946))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .clickable(onClick = onConfirm)
                    )
                }
            }
        }
    }
}

/**
 * 数学验证对话框
 */
@Composable
private fun MathVerificationDialog(
    question: String,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔢",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "请回答问题",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = question,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF457B9D)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 答案输入框
                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    placeholder = {
                        Text(
                            text = "输入答案",
                            color = Color.Gray
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 取消按钮
                    Text(
                        text = "取消",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .clickable(onClick = onDismiss)
                    )

                    // 提交按钮
                    Text(
                        text = "提交",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A9D8F))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .clickable(
                                enabled = answer.isNotEmpty()
                            ) {
                                if (answer.isNotEmpty()) onSubmit()
                            }
                    )
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
