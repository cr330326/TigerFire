package com.cryallen.tigerfire.ui.parent

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.parent.ParentEffect
import com.cryallen.tigerfire.presentation.parent.ParentEvent
import com.cryallen.tigerfire.presentation.parent.ParentViewModel
import kotlin.math.min

/**
 * 家长模式 Screen
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

    // 数学验证输入
    var answerInput by remember { mutableStateOf("") }

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
                    audioManager.playClickSound(null)
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
            // 顶部工具栏（返回按钮）
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        viewModel.onEvent(ParentEvent.BackToMapClicked)
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
                    .padding(horizontal = 32.dp, vertical = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 场景标题
                Text(
                    text = "家长模式",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 使用统计卡片
                UsageStatsCard(
                    todayPlayTime = state.todayPlayTime,
                    totalPlayTime = state.totalPlayTime,
                    totalBadgeCount = state.totalBadgeCount
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 时间设置卡片
                TimeSettingsCard(
                    sessionTimeLimit = state.settings.sessionDurationMinutes,
                    dailyTimeLimit = state.settings.sessionDurationMinutes, // 使用相同的值作为每日总时长
                    onSessionTimeLimitChange = { minutes ->
                        viewModel.onEvent(ParentEvent.UpdateSessionTimeLimit(minutes))
                    },
                    onDailyTimeLimitChange = { minutes ->
                        viewModel.onEvent(ParentEvent.UpdateDailyTimeLimit(minutes))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 进度管理卡片
                ProgressManagementCard(
                    onResetProgress = {
                        viewModel.onEvent(ParentEvent.ResetProgressClicked)
                    }
                )
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
 * 使用统计卡片
 */
@Composable
private fun UsageStatsCard(
    todayPlayTime: Long,
    totalPlayTime: Long,
    totalBadgeCount: Int
) {
    Card {
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
                value = formatDuration(todayPlayTime)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatRow(
                label = "总使用时长",
                value = formatDuration(totalPlayTime)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatRow(
                label = "已收集徽章",
                value = "$totalBadgeCount 枚"
            )
        }
    }
}

/**
 * 时间设置卡片
 */
@Composable
private fun TimeSettingsCard(
    sessionTimeLimit: Int,
    dailyTimeLimit: Int,
    onSessionTimeLimitChange: (Int) -> Unit,
    onDailyTimeLimitChange: (Int) -> Unit
) {
    Card {
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

            TimeLimitRow(
                label = "每次使用时长",
                value = sessionTimeLimit,
                onChange = onSessionTimeLimitChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimeLimitRow(
                label = "每日总时长",
                value = dailyTimeLimit,
                onChange = onDailyTimeLimitChange
            )
        }
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
    value: String
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
            color = Color(0xFFF4A261)
        )
    }
}

/**
 * 时间限制行
 */
@Composable
private fun TimeLimitRow(
    label: String,
    value: Int,
    onChange: (Int) -> Unit
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

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 减少按钮
            Text(
                text = "-",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF457B9D))
                    .clickable(enabled = value > 5) {
                        if (value > 5) onChange(value - 5)
                    }
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 时间值
            Text(
                text = "${value}分钟",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF4A261),
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 增加按钮
            Text(
                text = "+",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF457B9D))
                    .clickable(enabled = value < 120) {
                        if (value < 120) onChange(value + 5)
                    }
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        }
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
