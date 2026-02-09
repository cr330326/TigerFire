package com.cryallen.tigerfire.ui.collection

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.domain.model.Badge
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.collection.CollectionEffect
import com.cryallen.tigerfire.presentation.collection.CollectionEvent
import com.cryallen.tigerfire.presentation.collection.CollectionViewModel
import com.cryallen.tigerfire.ui.components.KidsBackButton
import com.cryallen.tigerfire.ui.theme.ThemeGradients
import com.cryallen.tigerfire.ui.theme.createVerticalGradient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

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
 * CollectionScreen 优化版本
 *
 * 优化内容：
 * 1. 触觉反馈 - 所有交互都带震动反馈
 * 2. 增强动画 - 更多平滑的过渡效果
 * 3. 微交互 - 按压反馈、悬停效果
 * 4. 粒子背景 - 增强的视觉体验
 * 5. 更流畅的加载动画
 */
@Composable
fun CollectionScreenOptimized(
    viewModel: CollectionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val haptic = LocalHapticFeedback.current
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    var isNavigating by remember { mutableStateOf(false) }

    // 页面进入动画 - 优化版：分阶段淡入
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }

    // 监听收集完成状态
    LaunchedEffect(state.hasCollectedAllBadges) {
        if (state.hasCollectedAllBadges) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            audioManager.playAllCompletedSound()
        }
    }

    // 订阅副作用
    LaunchedEffect(Unit) {
        try {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is CollectionEffect.ShowBadgeDetail -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    is CollectionEffect.NavigateToMap -> {
                        if (!isNavigating) {
                            isNavigating = true
                            onNavigateBack()
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        }
    }

    val backgroundBrush = createVerticalGradient(ThemeGradients.Collection)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // 优化的背景星星动画 - 多层视差效果
        FloatingStarsBackgroundOptimized()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            CollectionTopBarOptimized(
                onBackClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onEvent(CollectionEvent.BackToMapClicked)
                }
            )

            // 主内容区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题区域 - 带增强动画
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)),
                    exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
                ) {
                    CollectionTitleOptimized()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 统计卡片 - 带增强动画
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(500, delayMillis = 100)) + fadeIn(animationSpec = tween(500, delayMillis = 100)),
                    exit = fadeOut()
                ) {
                    CollectionStatsCardOptimized(
                        totalBadges = state.totalBadgeCount,
                        uniqueBadges = state.uniqueBadgeCount,
                        hasCollectedAll = state.hasCollectedAllBadges
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 徽章列表
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = expandIn(expandFrom = Alignment.TopCenter, animationSpec = tween(600, delayMillis = 200)) + fadeIn(animationSpec = tween(600, delayMillis = 200)),
                    exit = fadeOut()
                ) {
                    BadgeListOptimized(
                        viewModel = viewModel,
                        onBadgeClick = { badge ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onEvent(CollectionEvent.BadgeClicked(badge))
                        }
                    )
                }
            }
        }

        // 徽章详情弹窗
        selectedBadge?.let { badge ->
            BadgeDetailDialogOptimized(
                badge = badge,
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedBadge = null
                    viewModel.onEvent(CollectionEvent.CloseBadgeDetail)
                }
            )
        }

        // 集齐所有徽章彩蛋动画
        if (state.hasCollectedAllBadges) {
            CompletionCelebrationOverlayOptimized()
        }
    }
}

/**
 * 优化的漂浮星星背景 - 多层视差效果
 */
@Composable
private fun FloatingStarsBackgroundOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars_bg")

    // 多层星星以不同速度移动，创造深度感
    val layer1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "layer1"
    )

    val layer2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "layer2"
    )

    val layer3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "layer3"
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 第一层星星 - 小而快
        repeat(8) { index ->
            val x = ((layer1Offset * 0.5 + index * 50) % 400).dp
            val y = ((index * 70) % 500).dp + floatAnim.dp
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(8.dp)
                    .alpha(0.4f)
            ) {
                Text("⭐", fontSize = 8.dp.value.sp)
            }
        }

        // 第二层星星 - 中等
        repeat(6) { index ->
            val x = ((layer2Offset * 0.3 + index * 80) % 400).dp
            val y = ((index * 100 + 50) % 500).dp - floatAnim.dp * 0.5f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(12.dp)
                    .alpha(0.5f)
            ) {
                Text("✨", fontSize = 12.dp.value.sp)
            }
        }

        // 第三层星星 - 大而慢
        repeat(4) { index ->
            val x = ((layer3Offset * 0.2 + index * 120) % 400).dp
            val y = ((index * 130 + 100) % 500).dp + floatAnim.dp * 0.3f
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(16.dp)
                    .alpha(0.6f)
            ) {
                Text("🌟", fontSize = 16.dp.value.sp)
            }
        }
    }
}

/**
 * 优化的顶部工具栏 - 带触觉反馈
 */
@Composable
private fun CollectionTopBarOptimized(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KidsBackButton(onClick = onBackClick)

        // 右侧小火头像装饰 - 带脉冲动画
        val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(pulseScale)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.4f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF8DC), Color(0xFFFFD700))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🐯", fontSize = 28.sp)
        }
    }
}

/**
 * 优化的标题区域 - 带增强动画
 */
@Composable
private fun CollectionTitleOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = "title")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(titleScale)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0xFFFFD700).copy(alpha = glowAlpha)
                )
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFFFFD700))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = glowAlpha), Color.Transparent)
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx())
                    )
                }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "🏆 我的收藏 🏆",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE63946),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "查看收集的徽章",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

/**
 * 优化的统计卡片 - 带触觉反馈和增强动画
 */
@Composable
private fun CollectionStatsCardOptimized(
    totalBadges: Int,
    uniqueBadges: Int,
    hasCollectedAll: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "stats")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasCollectedAll) 1.03f else 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse_scale"
    )

    val shimmerOffset = remember { Animatable(0f) }
    LaunchedEffect(hasCollectedAll) {
        shimmerOffset.snapTo(0f)
        if (hasCollectedAll) {
            shimmerOffset.animateTo(
                targetValue = 1000f,
                animationSpec = tween(1500, easing = LinearEasing)
            )
        }
    }

    var cardScale by remember { mutableStateOf(1f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale * cardScale)
            .shadow(
                elevation = if (hasCollectedAll) 20.dp else 14.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (hasCollectedAll) Color(0xFFFFD700).copy(alpha = 0.6f)
                else Color(0xFF6A5ACD).copy(alpha = 0.4f)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                cardScale = 0.97f
            }
            .background(
                color = Color.White.copy(alpha = if (hasCollectedAll) 0.97f else 0.93f),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFF6A5ACD), Color(0xFFFFD700))
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )

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
                        cornerRadius = CornerRadius(20.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItemOptimized("🏅", "徽章总数", totalBadges.toString(), Color(0xFFE63946))
        StatItemOptimized("⭐", "不同种类", "$uniqueBadges/7", Color(0xFF457B9D))
        StatItemOptimized(
            if (hasCollectedAll) "🎉" else "📈",
            "收集进度",
            "${(uniqueBadges * 100 / 7)}%",
            if (hasCollectedAll) Color(0xFFFFD700) else Color(0xFF2A9D8F)
        )
    }

    LaunchedEffect(cardScale) {
        if (cardScale != 1f) {
            delay(100)
            cardScale = 1f
        }
    }
}

/**
 * 优化的统计项 - 带触觉反馈
 */
@Composable
private fun StatItemOptimized(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    val haptic = LocalHapticFeedback.current
    var scale by remember { mutableStateOf(1f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scale = 0.9f
            }
    ) {
        Text(text = icon, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }

    LaunchedEffect(scale) {
        if (scale != 1f) {
            delay(100)
            scale = 1f
        }
    }
}

/**
 * 优化的徽章列表
 */
@Composable
private fun BadgeListOptimized(
    viewModel: CollectionViewModel,
    onBadgeClick: (Badge) -> Unit
) {
    val state = viewModel.state.value
    val hasAnyBadges = state.uniqueBadgeCount > 0

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (!hasAnyBadges) {
            item {
                EmptyStateContentOptimized()
            }
        } else {
            item {
                SceneBadgeSectionOptimized(
                    sceneName = "消防站",
                    sceneType = SceneType.FIRE_STATION,
                    totalSlots = 4,
                    viewModel = viewModel,
                    onBadgeClick = onBadgeClick
                )
            }

            item {
                SceneBadgeSectionOptimized(
                    sceneName = "学校",
                    sceneType = SceneType.SCHOOL,
                    totalSlots = 1,
                    viewModel = viewModel,
                    onBadgeClick = onBadgeClick
                )
            }

            item {
                SceneBadgeSectionOptimized(
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
 * 优化的空状态内容
 */
@Composable
private fun EmptyStateContentOptimized() {
    val haptic = LocalHapticFeedback.current
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

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏆",
            fontSize = 80.sp,
            modifier = Modifier.scale(pulseScale).offset(y = (-floatAnim).dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "还没有徽章哦",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "去各个场景冒险，收集你的第一枚徽章吧！",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SceneHintCardOptimized("🚒", "消防站", "4 枚徽章", Color(0xFFE63946))
            SceneHintCardOptimized("🏫", "学校", "1 枚徽章", Color(0xFF457B9D))
            SceneHintCardOptimized("🌲", "森林", "2 枚徽章", Color(0xFF2A9D8F))
        }
    }
}

/**
 * 优化的场景提示卡片
 */
@Composable
private fun SceneHintCardOptimized(
    icon: String,
    name: String,
    badgeInfo: String,
    color: Color
) {
    val haptic = LocalHapticFeedback.current
    var scale by remember { mutableStateOf(1f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.4f)
            )
            .background(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scale = 0.97f
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = icon, fontSize = 40.sp)
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

    LaunchedEffect(scale) {
        if (scale != 1f) {
            delay(100)
            scale = 1f
        }
    }
}

/**
 * 优化的场景徽章分组
 */
@Composable
private fun SceneBadgeSectionOptimized(
    sceneName: String,
    sceneType: SceneType,
    totalSlots: Int,
    viewModel: CollectionViewModel,
    onBadgeClick: (Badge) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val badges = viewModel.getBadgesForScene(sceneType)
    val sceneColor = when (sceneType) {
        SceneType.FIRE_STATION -> Color(0xFFE63946)
        SceneType.SCHOOL -> Color(0xFF457B9D)
        SceneType.FOREST -> Color(0xFF2A9D8F)
    }

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
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = sceneColor.copy(alpha = 0.5f)
            )
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                val strokeWidth = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(sceneColor, sceneColor.copy(alpha = 0.6f), sceneColor)
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
            .padding(16.dp)
    ) {
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
                Text(text = sceneType.iconEmoji, fontSize = 32.sp)
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(badges.size) { index ->
                val badge = badges[index]
                BadgeCardOptimized(
                    badge = badge,
                    sceneColor = sceneColor,
                    onClick = { onBadgeClick(badge) }
                )
            }

            val remainingSlots = maxOf(0, totalSlots - badges.size)
            items(remainingSlots) {
                EmptyBadgeSlotOptimized(
                    sceneColor = sceneColor,
                    sceneType = sceneType
                )
            }
        }
    }
}

/**
 * 优化的徽章卡片 - 带触觉反馈和3D效果
 */
@Composable
private fun BadgeCardOptimized(
    badge: Badge,
    sceneColor: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "badge_float")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_angle"
    )

    var cardScale by remember { mutableStateOf(1f) }

    val badgeIcon = when (badge.scene) {
        SceneType.FIRE_STATION -> getFireStationIconOptimized(badge.baseType)
        SceneType.SCHOOL -> "🏆"
        SceneType.FOREST -> "🐑"
    }

    val variantColor = when (badge.variant) {
        1 -> Color(0xFFFF6B6B)
        2 -> Color(0xFFFFD93D)
        3 -> Color(0xFF6BCB77)
        4 -> Color(0xFF4D96FF)
        else -> Color(0xFFFFD700)
    }

    Column(
        modifier = Modifier
            .width(110.dp)
            .offset(y = -floatOffset.dp)
            .graphicsLayer { rotationZ = rotationAngle }
            .scale(cardScale)
            .shadow(
                elevation = 10.dp + (floatOffset / 2).dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = sceneColor.copy(alpha = 0.6f)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                cardScale = 0.92f
                onClick()
            }
            .background(Color.White, RoundedCornerShape(16.dp))
            .drawBehind {
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
                    cornerRadius = CornerRadius(16.dp.toPx())
                )

                val strokeWidth = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(variantColor, sceneColor, variantColor)
                    ),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = variantColor.copy(alpha = 0.7f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(variantColor.copy(alpha = 0.3f), variantColor.copy(alpha = 0.1f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = badgeIcon, fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = badge.baseType,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = sceneColor,
            maxLines = 1
        )

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
            delay(100)
            cardScale = 1f
        }
    }
}

/**
 * 获取消防站徽章图标 - 优化版
 */
private fun getFireStationIconOptimized(baseType: String): String {
    return when (baseType) {
        "extinguisher" -> "🧯"
        "hydrant" -> "💧"
        "ladder" -> "🪜"
        "hose" -> "🐍"
        else -> "🚒"
    }
}

/**
 * 优化的空徽章槽位
 */
@Composable
private fun EmptyBadgeSlotOptimized(
    sceneColor: Color,
    sceneType: SceneType
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Gray.copy(alpha = 0.3f)
            )
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp)
            )
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = sceneColor.copy(alpha = 0.4f),
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .alpha(0.4f)
                .background(
                    color = sceneColor.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontSize = 36.sp,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = sceneType.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "???",
            fontSize = 10.sp,
            color = Color.Gray.copy(alpha = 0.6f)
        )
    }
}

/**
 * 优化的徽章详情弹窗
 */
@Composable
private fun BadgeDetailDialogOptimized(
    badge: Badge,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sceneColor = when (badge.scene) {
        SceneType.FIRE_STATION -> Color(0xFFE63946)
        SceneType.SCHOOL -> Color(0xFF457B9D)
        SceneType.FOREST -> Color(0xFF2A9D8F)
    }

    var dialogScale by remember { mutableStateOf(0.7f) }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    }
            )

            Box(
                modifier = Modifier
                    .scale(dialogScale)
                    .padding(32.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = sceneColor.copy(alpha = 0.6f)
                    )
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val badgeIcon = when (badge.scene) {
                        SceneType.FIRE_STATION -> getFireStationIconOptimized(badge.baseType)
                        SceneType.SCHOOL -> "🏆"
                        SceneType.FOREST -> "🐑"
                    }

                    Text(text = badgeIcon, fontSize = 80.sp)

                    Text(
                        text = badge.baseType,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = sceneColor
                    )

                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DetailRowOptimized("场景", badge.scene.displayName)
                        DetailRowOptimized("变体", "v${badge.variant}")
                        DetailRowOptimized("获得时间", android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", badge.earnedAt).toString())
                    }

                    Text(
                        text = "关闭",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                            }
                            .background(sceneColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 优化的详情行
 */
@Composable
private fun DetailRowOptimized(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

/**
 * 优化的完成庆祝覆盖层
 */
@Composable
private fun CompletionCelebrationOverlayOptimized() {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "fireworks")

    val fireworkColors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFF7F00),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF0000FF),
        Color(0xFF9400D3)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
    ) {
        fireworkColors.forEachIndexed { index, color ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -350f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500 + index * 200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "firework_$index"
            )

            val offsetX = (index * 100f + 50f) % 400f + 100f
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500 + index * 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "firework_alpha_$index"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width * (offsetX / 500f)
                val centerY = size.height + offsetY

                repeat(16) { i ->
                    val angle = (i * 22.5f) * (Math.PI / 180).toFloat()
                    val radius = 35.dp.toPx()
                    val x = centerX + kotlin.math.cos(angle) * radius
                    val y = centerY + kotlin.math.sin(angle) * radius

                    drawCircle(
                        color = color.copy(alpha = alpha * 0.9f),
                        radius = 10.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
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
