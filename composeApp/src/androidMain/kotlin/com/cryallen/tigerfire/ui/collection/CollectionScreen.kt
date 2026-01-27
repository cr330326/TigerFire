package com.cryallen.tigerfire.ui.collection

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cryallen.tigerfire.domain.model.SceneType
import kotlinx.coroutines.CancellationException
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.collection.CollectionEffect
import com.cryallen.tigerfire.presentation.collection.CollectionEvent
import com.cryallen.tigerfire.presentation.collection.CollectionViewModel

/**
 * 获取场景类型的显示名称
 */
private val SceneType.displayName: String
    get() = when (this) {
        SceneType.FIRE_STATION -> "消防站"
        SceneType.SCHOOL -> "学校"
        SceneType.FOREST -> "森林"
    }

/**
 * 获取场景类型的图标 emoji
 */
private val SceneType.iconEmoji: String
    get() = when (this) {
        SceneType.FIRE_STATION -> "🚒"
        SceneType.SCHOOL -> "🏫"
        SceneType.FOREST -> "🌲"
    }

/**
 * 我的收藏场景 Screen - 全新设计
 *
 * 展示所有收集的徽章，按场景分组显示
 * 支持点击徽章查看详情
 * 集齐所有徽章触发彩蛋动画
 *
 * @param viewModel CollectionViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    var selectedBadge by remember { mutableStateOf<com.cryallen.tigerfire.domain.model.Badge?>(null) }

    // 页面进入动画 - 使用 produceState 确保正确清理
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        contentVisible = true
    }

    // 监听收集完成状态，播放音效
    LaunchedEffect(state.hasCollectedAllBadges) {
        if (state.hasCollectedAllBadges) {
            audioManager.playAllCompletedSound()
        }
    }

    // 订阅副作用（Effect）- 使用 CollectAsState 或者在离开时正确清理
    // 这里使用 LaunchedEffect + Flow.collect，会自动在组件离开时取消
    LaunchedEffect(Unit) {
        try {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is CollectionEffect.ShowBadgeDetail -> {
                        selectedBadge = effect.badge
                    }
                    is CollectionEffect.PlayClickSound -> {
                        audioManager.playClickSound(null)
                    }
                    is CollectionEffect.PlayBadgeSound -> {
                        audioManager.playBadgeSound()
                    }
                    is CollectionEffect.PlayCompletionAnimation -> {
                        audioManager.playSuccessSound()
                    }
                    is CollectionEffect.NavigateToMap -> onNavigateBack()
                }
            }
        } catch (e: CancellationException) {
            // 组件离开时取消，这是正常行为
            throw e
        }
    }

    // 主背景渐变 - 紫色到金色（高级收藏感）
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6A5ACD),  // 板岩紫
            Color(0xFF9370DB),  // 中紫色
            Color(0xFFDDA0DD),  // 梅红紫
            Color(0xFFFFD700),  // 金色底部
            Color(0xFFFFF8DC)   // 亮象牙色
        ),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    // 闪烁星星动画
    val infiniteTransition = rememberInfiniteTransition(label = "star_animation")
    val starFloatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                // 装饰性星星
                val starPositions = listOf(
                    Offset(size.width * 0.1f, size.height * 0.08f + starFloatAnimation),
                    Offset(size.width * 0.25f, size.height * 0.15f - starFloatAnimation * 0.5f),
                    Offset(size.width * 0.4f, size.height * 0.1f + starFloatAnimation * 0.3f),
                    Offset(size.width * 0.6f, size.height * 0.12f - starFloatAnimation * 0.7f),
                    Offset(size.width * 0.75f, size.height * 0.06f + starFloatAnimation * 0.5f),
                    Offset(size.width * 0.9f, size.height * 0.14f - starFloatAnimation * 0.3f),
                )
                starPositions.forEach { pos ->
                    // 绘制星星
                    val starSize = 8.dp.toPx()
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.6f),
                        radius = starSize,
                        center = pos
                    )
                    // 星星光芒
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = 0.4f),
                        start = Offset(pos.x - 12.dp.toPx(), pos.y),
                        end = Offset(pos.x + 12.dp.toPx(), pos.y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = 0.4f),
                        start = Offset(pos.x, pos.y - 12.dp.toPx()),
                        end = Offset(pos.x, pos.y + 12.dp.toPx()),
                        strokeWidth = 2f
                    )
                }

                // 装饰性光晕
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.08f),
                    radius = size.minDimension * 0.3f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏（返回按钮 + 标题）
            CollectionTopBar(
                onBackClick = {
                    viewModel.onEvent(CollectionEvent.BackToMapClicked)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 主内容区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题区域 - 带动画
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
                ) {
                    CollectionTitle()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 统计卡片 - 带动画
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                    exit = fadeOut()
                ) {
                    CollectionStatsCard(
                        totalBadges = state.totalBadgeCount,
                        uniqueBadges = state.uniqueBadgeCount,
                        hasCollectedAll = state.hasCollectedAllBadges
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 徽章列表
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                    exit = fadeOut()
                ) {
                    BadgeList(
                        viewModel = viewModel,
                        onBadgeClick = { badge ->
                            viewModel.onEvent(CollectionEvent.BadgeClicked(badge))
                        }
                    )
                }
            }
        }

        // 徽章详情弹窗
        selectedBadge?.let { badge ->
            BadgeDetailDialog(
                badge = badge,
                onDismiss = {
                    selectedBadge = null
                    viewModel.onEvent(CollectionEvent.CloseBadgeDetail)
                }
            )
        }

        // 集齐所有徽章彩蛋动画
        if (state.hasCollectedAllBadges) {
            CompletionCelebrationOverlay()
        }
    }
}

/**
 * 顶部工具栏
 */
@Composable
private fun CollectionTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        var buttonScale by remember { mutableStateOf(1f) }

        IconButton(
            onClick = {
                buttonScale = 0.9f
                onBackClick()
            },
            modifier = Modifier
                .scale(buttonScale)
                .size(56.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                    ambientColor = Color(0xFFFFD700).copy(alpha = 0.3f)
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
                    // 金色光环
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
                color = Color(0xFF6A5ACD)
            )
        }

        LaunchedEffect(buttonScale) {
            if (buttonScale != 1f) {
                kotlinx.coroutines.delay(100)
                buttonScale = 1f
            }
        }

        // 右侧小火头像装饰
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
                text = "🐯",
                fontSize = 28.sp
            )
        }
    }
}

/**
 * 标题区域
 */
@Composable
private fun CollectionTitle() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // 主标题
        Text(
            text = "🏆 我的收藏 🏆",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                )
                .drawBehind {
                    // 文字阴影效果
                    drawRoundRect(
                        color = Color(0xFF6A5ACD).copy(alpha = 0.3f),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 副标题
        Text(
            text = "查看收集的徽章",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

/**
 * 统计卡片
 */
@Composable
private fun CollectionStatsCard(
    totalBadges: Int,
    uniqueBadges: Int,
    hasCollectedAll: Boolean
) {
    // 卡片缩放动画
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse")
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )

    // 完成时的闪光动画 - 使用带 key 的 LaunchedEffect 确保正确重置
    val shimmerOffset = remember { Animatable(0f) }
    LaunchedEffect(hasCollectedAll) {
        // 重置动画状态
        shimmerOffset.snapTo(0f)
        if (hasCollectedAll) {
            shimmerOffset.animateTo(
                targetValue = 1000f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (hasCollectedAll) cardScale else 1f)
            .shadow(
                elevation = if (hasCollectedAll) 16.dp else 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.6f),
                ambientColor = Color(0xFF6A5ACD).copy(alpha = 0.4f)
            )
            .background(
                color = Color.White.copy(alpha = if (hasCollectedAll) 0.95f else 0.9f),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                // 渐变边框
                val strokeWidth = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFF6A5ACD),
                            Color(0xFFFFD700)
                        )
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(20.dp.value, 20.dp.value)
                )

                // 完成时的闪光效果
                if (hasCollectedAll) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFD700).copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            startX = shimmerOffset.value - 500f,
                            endX = shimmerOffset.value + 500f
                        ),
                        cornerRadius = CornerRadius(20.dp.value, 20.dp.value)
                    )
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 总徽章数
        StatItem(
            icon = "🏅",
            label = "徽章总数",
            value = totalBadges.toString(),
            color = Color(0xFFE63946)
        )

        // 不同种类
        StatItem(
            icon = "⭐",
            label = "不同种类",
            value = "$uniqueBadges/7",
            color = Color(0xFF457B9D)
        )

        // 收集进度
        StatItem(
            icon = if (hasCollectedAll) "🎉" else "📈",
            label = "收集进度",
            value = "${(uniqueBadges * 100 / 7)}%",
            color = if (hasCollectedAll) Color(0xFFFFD700) else Color(0xFF2A9D8F)
        )
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * 徽章列表（按场景分组）
 */
@Composable
private fun BadgeList(
    viewModel: CollectionViewModel,
    onBadgeClick: (com.cryallen.tigerfire.domain.model.Badge) -> Unit
) {
    val state = viewModel.state.value


    // 调试日志 - 打印徽章状态
    LaunchedEffect(state.badges) {
        Log.e("CollectionBadgeList", "uniqueBadgeCount=${state.uniqueBadgeCount}, totalBadgeCount=${state.totalBadgeCount}, badges=${state.badges.map { "${it.baseType}(v${it.variant})" }}")
    }

    // 检查是否有任何徽章 - 使用 uniqueBadgeCount 更可靠
    // 因为森林场景有变体系统，totalBadgeCount 可能包含多个变体
    val hasAnyBadges = state.uniqueBadgeCount > 0

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 如果没有徽章，显示空状态提示
        if (!hasAnyBadges) {
            item {
                EmptyStateContent()
            }
        } else {
            // 显示所有场景的徽章分组（包括已获得和未获得的）
            item {
                SceneBadgeSection(
                    sceneName = "消防站",
                    sceneType = SceneType.FIRE_STATION,
                    totalSlots = 4,
                    viewModel = viewModel,
                    onBadgeClick = onBadgeClick
                )
            }

            item {
                SceneBadgeSection(
                    sceneName = "学校",
                    sceneType = SceneType.SCHOOL,
                    totalSlots = 1,
                    viewModel = viewModel,
                    onBadgeClick = onBadgeClick
                )
            }

            item {
                SceneBadgeSection(
                    sceneName = "森林",
                    sceneType = SceneType.FOREST,
                    totalSlots = 2,
                    viewModel = viewModel,
                    onBadgeClick = onBadgeClick
                )
            }
        }
    }
}

/**
 * 空状态内容
 */
@Composable
private fun EmptyStateContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 脉冲动画
        val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        // 大图标
        Text(
            text = "🏆",
            fontSize = 80.sp,
            modifier = Modifier.scale(pulseScale)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 主提示文字
        Text(
            text = "还没有徽章哦",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 副提示文字
        Text(
            text = "去各个场景冒险，收集你的第一枚徽章吧！",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 场景提示卡片
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SceneHintCard("🚒", "消防站", "4 枚徽章", Color(0xFFE63946))
            SceneHintCard("🏫", "学校", "1 枚徽章", Color(0xFF457B9D))
            SceneHintCard("🌲", "森林", "2 枚徽章", Color(0xFF2A9D8F))
        }
    }
}

/**
 * 场景提示卡片
 */
@Composable
private fun SceneHintCard(
    icon: String,
    name: String,
    badgeInfo: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.3f)
            )
            .background(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = icon,
            fontSize = 40.sp
        )
        Column {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = badgeInfo,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 场景徽章分组
 */
@Composable
private fun SceneBadgeSection(
    sceneName: String,
    sceneType: SceneType,
    totalSlots: Int,
    viewModel: CollectionViewModel,
    onBadgeClick: (com.cryallen.tigerfire.domain.model.Badge) -> Unit
) {
    val badges = viewModel.getBadgesForScene(sceneType)
    val sceneColor = when (sceneType) {
        SceneType.FIRE_STATION -> Color(0xFFE63946)
        SceneType.SCHOOL -> Color(0xFF457B9D)
        SceneType.FOREST -> Color(0xFF2A9D8F)
    }

    // 场景卡片缩放动画
    val infiniteTransition = rememberInfiniteTransition(label = "scene_pulse")
    val scenePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scene_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scenePulse)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = sceneColor.copy(alpha = 0.4f)
            )
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                // 渐变边框
                val strokeWidth = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            sceneColor,
                            sceneColor.copy(alpha = 0.6f),
                            sceneColor
                        )
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(20.dp.value, 20.dp.value)
                )
            }
            .padding(16.dp)
    ) {
        // 场景标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = sceneType.iconEmoji,
                    fontSize = 32.sp
                )
                Column {
                    Text(
                        text = sceneName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = sceneColor
                    )
                    Text(
                        text = "${badges.size}/$totalSlots",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // 完成标记
            if (badges.size == totalSlots) {
                Text(
                    text = "✨ 已完成",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // 徽章卡片横向滚动
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 显示已获得的徽章
            items(badges.size) { index ->
                val badge = badges[index]
                BadgeCard(
                    badge = badge,
                    sceneColor = sceneColor,
                    onClick = { onBadgeClick(badge) }
                )
            }

            // 显示未获得的徽章槽位（灰色轮廓）
            val remainingSlots = totalSlots - badges.size
            items(remainingSlots) { index ->
                EmptyBadgeSlot(
                    sceneColor = sceneColor,
                    sceneType = sceneType
                )
            }
        }
    }
}

/**
 * 徽章卡片 - 已获得
 */
@Composable
private fun BadgeCard(
    badge: com.cryallen.tigerfire.domain.model.Badge,
    sceneColor: Color,
    onClick: () -> Unit
) {
    // 使用 rememberInfiniteTransition 替代 Animatable + LaunchedEffect
    // 这样可以自动管理动画生命周期，避免内存泄漏
    val infiniteTransition = rememberInfiniteTransition(label = "badge_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // 点击缩放动画
    var cardScale by remember { mutableStateOf(1f) }

    // 徽章图标
    val badgeIcon = when (badge.scene) {
        SceneType.FIRE_STATION -> getFireStationIcon(badge.baseType)
        SceneType.SCHOOL -> "🏆"
        SceneType.FOREST -> "🐑"
    }

    // 变体颜色
    val variantColor = when (badge.variant) {
        1 -> Color(0xFFFF6B6B) // 红色变体
        2 -> Color(0xFFFFD93D) // 黄色变体
        3 -> Color(0xFF6BCB77) // 绿色变体
        4 -> Color(0xFF4D96FF) // 蓝色变体
        else -> Color(0xFFFFD700) // 默认金色
    }

    Column(
        modifier = Modifier
            .width(110.dp)
            .scale(cardScale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = sceneColor.copy(alpha = 0.5f)
            )
            .clickable {
                cardScale = 0.95f
                onClick()
            }
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .drawBehind {
                // Shimmer 闪光效果
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFD700).copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        startX = shimmerOffset - 500f,
                        endX = shimmerOffset + 500f
                    ),
                    cornerRadius = CornerRadius(16.dp.value, 16.dp.value)
                )

                // 渐变边框
                val strokeWidth = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            variantColor,
                            sceneColor,
                            variantColor
                        )
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(16.dp.value, 16.dp.value)
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 徽章图标
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = variantColor.copy(alpha = 0.6f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            variantColor.copy(alpha = 0.3f),
                            variantColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeIcon,
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 徽章名称
        Text(
            text = badge.baseType,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = sceneColor,
            maxLines = 1
        )

        // 变体标识
        if (badge.variant > 0) {
            Text(
                text = "v${badge.variant}",
                fontSize = 10.sp,
                color = variantColor,
                modifier = Modifier
                    .background(
                        color = variantColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }

    LaunchedEffect(cardScale) {
        if (cardScale != 1f) {
            kotlinx.coroutines.delay(100)
            cardScale = 1f
        }
    }
}

/**
 * 获取消防站徽章图标
 */
private fun getFireStationIcon(baseType: String): String {
    return when (baseType) {
        "extinguisher" -> "🧯"
        "hydrant" -> "💧"
        "ladder" -> "🪜"
        "hose" -> "🐍"
        else -> "🚒"
    }
}

/**
 * 空徽章槽位 - 未获得
 */
@Composable
private fun EmptyBadgeSlot(
    sceneColor: Color,
    sceneType: SceneType
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Gray.copy(alpha = 0.2f)
            )
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp)
            )
            .drawBehind {
                // 虚线边框
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = sceneColor.copy(alpha = 0.3f),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(16.dp.value, 16.dp.value)
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 占位图标
        Box(
            modifier = Modifier
                .size(60.dp)
                .alpha(0.3f)
                .background(
                    color = sceneColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontSize = 36.sp,
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 场景名称
        Text(
            text = sceneType.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 未获得提示
        Text(
            text = "???",
            fontSize = 10.sp,
            color = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

/**
 * 徽章详情弹窗
 */
@Composable
private fun BadgeDetailDialog(
    badge: com.cryallen.tigerfire.domain.model.Badge,
    onDismiss: () -> Unit
) {
    val sceneColor = when (badge.scene) {
        SceneType.FIRE_STATION -> Color(0xFFE63946)
        SceneType.SCHOOL -> Color(0xFF457B9D)
        SceneType.FOREST -> Color(0xFF2A9D8F)
    }

    // 弹窗缩放动画
    var dialogScale by remember { mutableStateOf(0.8f) }
    LaunchedEffect(Unit) {
        dialogScale = 1f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 半透明背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss)
            )

            // 弹窗内容
            Box(
                modifier = Modifier
                    .scale(dialogScale)
                    .padding(32.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = sceneColor.copy(alpha = 0.5f)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 徽章图标
                    val badgeIcon = when (badge.scene) {
                        SceneType.FIRE_STATION -> getFireStationIcon(badge.baseType)
                        SceneType.SCHOOL -> "🏆"
                        SceneType.FOREST -> "🐑"
                    }

                    Text(
                        text = badgeIcon,
                        fontSize = 80.sp
                    )

                    // 徽章名称
                    Text(
                        text = badge.baseType,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = sceneColor
                    )

                    // 徽章详情
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DetailRow("场景", badge.scene.displayName)
                        DetailRow("变体", "v${badge.variant}")
                        DetailRow("获得时间", android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", badge.earnedAt).toString())
                    }

                    // 关闭按钮
                    Text(
                        text = "关闭",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .background(
                                color = sceneColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 详情行
 */
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

/**
 * 集齐所有徽章庆祝动画 - 烟花效果
 */
@Composable
private fun CompletionCelebrationOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "fireworks")

    // 烟花粒子
    val fireworkColors = listOf(
        Color(0xFFFF0000), // 红
        Color(0xFFFF7F00), // 橙
        Color(0xFFFFFF00), // 黄
        Color(0xFF00FF00), // 绿
        Color(0xFF0000FF), // 蓝
        Color(0xFF9400D3)  // 紫
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        // 烟花粒子效果
        fireworkColors.forEachIndexed { index, color ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -300f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + index * 200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "firework_$index"
            )

            val offsetX = (index * 100f + 50f) % 400f + 100f
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + index * 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "firework_alpha_$index"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width * (offsetX / 500f)
                val centerY = size.height + offsetY

                // 绘制烟花粒子
                repeat(12) { i ->
                    val angle = (i * 30f) * (Math.PI / 180).toFloat()
                    val radius = 30.dp.toPx()
                    val x = centerX + kotlin.math.cos(angle) * radius
                    val y = centerY + kotlin.math.sin(angle) * radius

                    drawCircle(
                        color = color.copy(alpha = alpha * 0.8f),
                        radius = 8.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // 庆祝文字
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "celebration_scale"
            )

            Text(
                text = "🎉🎊",
                fontSize = 80.sp,
                modifier = Modifier.scale(scale)
            )
            Text(
                text = "恭喜你！",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "你收集了所有徽章！",
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
