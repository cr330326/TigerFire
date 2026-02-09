package com.cryallen.tigerfire.ui.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.map.MapEffect
import com.cryallen.tigerfire.presentation.map.MapEvent
import com.cryallen.tigerfire.presentation.map.MapViewModel
import com.cryallen.tigerfire.ui.theme.ThemeGradients
import com.cryallen.tigerfire.ui.theme.createVerticalGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryallen.tigerfire.R

/**
 * MapScreen Phase 1 优化版本 - 简化版（修复编译错误）
 *
 * 优化内容：
 * 1. 增强转场动画 - 卡车行驶过渡效果
 * 2. 微交互升级 - 触感反馈、粒子效果、弹性动画
 * 3. 视差背景效果 - 云朵、山脉分层移动
 * 4. 小火引导动画 - 空闲时自动引导
 */
@Composable
fun MapScreenOptimized(
    viewModel: MapViewModel,
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToFireStation: () -> Unit = {},
    onNavigateToSchool: () -> Unit = {},
    onNavigateToForest: () -> Unit = {},
    onNavigateToCollection: () -> Unit = {},
    onNavigateToParent: () -> Unit = {},
    appSessionManager: com.cryallen.tigerfire.presentation.common.AppSessionManager? = null
) {
    val state by viewModel.state.collectAsState()
    val selectedScene by viewModel.selectedScene.collectAsState()
    val animationTrigger by viewModel.animationTrigger.collectAsState()
    val scenePositions by viewModel.scenePositions.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }
    val scope = rememberCoroutineScope()

    // 卡车转场动画状态
    var showTruckTransition by remember { mutableStateOf(false) }
    var truckTargetScene by remember { mutableStateOf<SceneType?>(null) }

    // 视差背景状态
    val infiniteTransition = rememberInfiniteTransition(label = "parallax")

    // 多层云朵以不同速度移动
    val cloudLayer1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_layer_1"
    )

    val cloudLayer2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_layer_2"
    )

    // 漂浮动画（星星）
    val floatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // 小火引导动画
    var showXiaoHuoGuide by remember { mutableStateOf(false) }
    var idleTime by remember { mutableStateOf(0) }

    // 空闲检测
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            idleTime++
            if (idleTime >= 30 && !showTruckTransition) {
                showXiaoHuoGuide = true
            }
        }
    }

    // 重置空闲时间当用户交互
    val resetIdleTime = {
        idleTime = 0
        showXiaoHuoGuide = false
    }

    // 原有状态
    var isJumping by remember { mutableStateOf(false) }
    var pendingNavigationScene by remember { mutableStateOf<SceneType?>(null) }

    // 预加载音效
    LaunchedEffect(Unit) {
        audioManager.preloadSounds()
    }

    // 监听跳跃动画完成
    LaunchedEffect(isJumping, pendingNavigationScene) {
        if (!isJumping && pendingNavigationScene != null) {
            truckTargetScene = pendingNavigationScene
            showTruckTransition = true
            delay(2000)
            when (pendingNavigationScene) {
                SceneType.FIRE_STATION -> onNavigateToFireStation()
                SceneType.SCHOOL -> onNavigateToSchool()
                SceneType.FOREST -> onNavigateToForest()
                null -> {}
            }
            pendingNavigationScene = null
            showTruckTransition = false
        }
    }

    // 订阅副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MapEffect.NavigateToScene -> {}
                is MapEffect.NavigateToCollection -> onNavigateToCollection()
                is MapEffect.NavigateToParent -> onNavigateToParent()
                is MapEffect.PlayLockedHint -> {
                    audioManager.playHintSound()
                }
                is MapEffect.PlaySceneSound -> {
                    audioManager.playClickSound(effect.scene)
                }
                is MapEffect.PlaySuccessSound -> {
                    audioManager.playSuccessSound()
                }
            }
        }
    }

    val backgroundBrush = createVerticalGradient(ThemeGradients.Map)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // 视差背景效果
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // 远处山脉
                    val mountainColor1 = Color(0xFF87CEEB).copy(alpha = 0.3f)
                    val mountainPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height * 0.4f)
                        lineTo(size.width * 0.2f, size.height * 0.25f)
                        lineTo(size.width * 0.4f, size.height * 0.35f)
                        lineTo(size.width * 0.6f, size.height * 0.2f)
                        lineTo(size.width * 0.8f, size.height * 0.3f)
                        lineTo(size.width, size.height * 0.35f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(mountainPath, color = mountainColor1)

                    // 太阳
                    val sunCenter = Offset(size.width * 0.88f, size.height * 0.14f)
                    val sunRadius = size.minDimension * 0.07f
                    val glowAlpha = 0.3f + (floatAnimation / 20f) * 0.1f
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                        radius = sunRadius * 1.5f,
                        center = sunCenter
                    )
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.95f),
                        radius = sunRadius,
                        center = sunCenter
                    )
                }
        )

        // 云朵层1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val cloudOffset = (cloudLayer1 % 100) / 100f * size.width
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.minDimension * 0.15f,
                        center = Offset(size.width * 0.2f + cloudOffset, size.height * 0.15f)
                    )
                }
        )

        // 云朵层2
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val cloudOffset = (cloudLayer2 % 100) / 100f * size.width
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = size.minDimension * 0.12f,
                        center = Offset(size.width * 0.7f + cloudOffset, size.height * 0.25f)
                    )
                }
        )

        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 我的收藏按钮
            EnhancedCollectionButton(
                onClick = {
                    resetIdleTime()
                    viewModel.onEvent(MapEvent.CollectionClicked)
                }
            )

            // 家长模式入口
            EnhancedParentButton(
                onClick = {
                    resetIdleTime()
                    viewModel.onEvent(MapEvent.ParentModeClicked)
                }
            )
        }

        // 中央场景图标区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题
            EnhancedTitle()

            Spacer(modifier = Modifier.height(40.dp))

            // 场景图标行
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 消防站
                OptimizedSceneIcon(
                    scene = SceneType.FIRE_STATION,
                    status = state.sceneStatuses[SceneType.FIRE_STATION] ?: SceneStatus.LOCKED,
                    mainIcon = "🚒",
                    subIcon = "🔥",
                    label = "消防站",
                    primaryColor = Color(0xFFE63946),
                    secondaryColor = Color(0xFFFF6B6B),
                    accentColor = Color(0xFFFFD700),
                    onClick = {
                        resetIdleTime()
                        if (isJumping) return@OptimizedSceneIcon
                        audioManager.playClickSound(SceneType.FIRE_STATION)
                        viewModel.onEvent(MapEvent.UpdateSelectedScene(SceneType.FIRE_STATION))
                        isJumping = true
                        pendingNavigationScene = SceneType.FIRE_STATION
                    },
                    onPositioned = { offset ->
                        viewModel.onEvent(MapEvent.UpdateScenePosition(SceneType.FIRE_STATION, offset))
                    }
                )

                // 学校
                OptimizedSceneIcon(
                    scene = SceneType.SCHOOL,
                    status = state.sceneStatuses[SceneType.SCHOOL] ?: SceneStatus.LOCKED,
                    mainIcon = "🏫",
                    subIcon = "📚",
                    label = "学校",
                    primaryColor = Color(0xFF457B9D),
                    secondaryColor = Color(0xFFA8DADC),
                    accentColor = Color(0xFFFFE66D),
                    onClick = {
                        resetIdleTime()
                        if (isJumping) return@OptimizedSceneIcon
                        audioManager.playClickSound(SceneType.SCHOOL)
                        viewModel.onEvent(MapEvent.UpdateSelectedScene(SceneType.SCHOOL))
                        isJumping = true
                        pendingNavigationScene = SceneType.SCHOOL
                    },
                    onPositioned = { offset ->
                        viewModel.onEvent(MapEvent.UpdateScenePosition(SceneType.SCHOOL, offset))
                    }
                )

                // 森林
                OptimizedSceneIcon(
                    scene = SceneType.FOREST,
                    status = state.sceneStatuses[SceneType.FOREST] ?: SceneStatus.LOCKED,
                    mainIcon = "🌲",
                    subIcon = "🐑",
                    label = "森林",
                    primaryColor = Color(0xFF2A9D8F),
                    secondaryColor = Color(0xFF95D5B2),
                    accentColor = Color(0xFFFFB6C1),
                    onClick = {
                        resetIdleTime()
                        if (isJumping) return@OptimizedSceneIcon
                        audioManager.playClickSound(SceneType.FOREST)
                        viewModel.onEvent(MapEvent.UpdateSelectedScene(SceneType.FOREST))
                        isJumping = true
                        pendingNavigationScene = SceneType.FOREST
                    },
                    onPositioned = { offset ->
                        viewModel.onEvent(MapEvent.UpdateScenePosition(SceneType.FOREST, offset))
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Avatar 角色组件（简化版）
        AvatarCharacter(
            selectedScene = selectedScene,
            scenePositions = scenePositions,
            isJumping = isJumping,
            animationTrigger = animationTrigger,
            onJumpComplete = { isJumping = false }
        )

        // 卡车转场动画
        if (showTruckTransition && truckTargetScene != null) {
            TruckTransitionAnimation(
                targetScene = truckTargetScene!!,
                onAnimationComplete = {
                    showTruckTransition = false
                }
            )
        }

        // 小火引导动画
        if (showXiaoHuoGuide && !isJumping) {
            XiaoHuoGuideAnimation(
                onDismiss = { showXiaoHuoGuide = false }
            )
        }
    }
}

// Enhanced Collection Button
@Composable
private fun EnhancedCollectionButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(if (isPressed) 0.9f else 1f)
            .shadow(12.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(colors = listOf(Color.White, Color(0xFFFFF8DC))),
                shape = CircleShape
            )
            .border(3.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape)
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🐯", fontSize = 36.sp)
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Enhanced Parent Button
@Composable
private fun EnhancedParentButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(if (isPressed) 0.9f else 1f)
            .shadow(6.dp, CircleShape)
            .background(Color.White.copy(alpha = 0.7f), CircleShape)
            .border(2.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "⚙️", fontSize = 28.sp)
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Enhanced Title
@Composable
private fun EnhancedTitle() {
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

    Box(
        modifier = Modifier
            .padding(bottom = 40.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(24.dp))
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFFFFD700))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        // 背景发光效果
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        cornerRadius = CornerRadius(24.dp.value)
                    )
                }
        )

        Text(
            text = "🌟 选择冒险场景 🌟",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE63946),
            textAlign = TextAlign.Center
        )
    }
}

// Optimized Scene Icon
@Composable
private fun OptimizedSceneIcon(
    scene: SceneType,
    status: SceneStatus,
    mainIcon: String,
    subIcon: String,
    label: String,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    onPositioned: (Offset) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scene_icon")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val isClickable = status != SceneStatus.LOCKED
    val isCompleted = status == SceneStatus.COMPLETED
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(160.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                onPositioned(
                    Offset(
                        x = position.x + size.width / 2,
                        y = position.y + size.height
                    )
                )
            }
            .then(
                if (isClickable) {
                    Modifier.clickable {
                        isPressed = true
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .scale(
                when {
                    isPressed -> 0.9f
                    else -> if (isClickable) breatheScale else 1f
                }
            )
            .alpha(if (isClickable) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        // 背景容器
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isPressed) 8.dp else 18.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (isCompleted) accentColor else primaryColor
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    when {
                        isCompleted -> accentColor.copy(alpha = 0.2f)
                        isClickable -> Color.White
                        else -> Color(0xFFBDBDBD)
                    }
                )
                .then(
                    if (isClickable) {
                        Modifier.border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(primaryColor, secondaryColor, accentColor)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                    } else Modifier
                )
        ) {
            // 脉冲光环
            if (isClickable) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = pulseAlpha),
                                        Color.Transparent
                                    )
                                ),
                                cornerRadius = CornerRadius(28.dp.value)
                            )
                        }
                )
            }

            // 内容列
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 副图标
                if (isClickable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp, end = 12.dp)
                            .size(32.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = subIcon, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 锁定图标
                if (!isClickable) {
                    Text(
                        text = "🔒",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // 主图标
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mainIcon,
                        fontSize = 56.sp,
                        modifier = Modifier.scale(if (isPressed) 0.9f else 1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 标签
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                status == SceneStatus.LOCKED -> Color(0xFF757575)
                                isCompleted -> accentColor.copy(alpha = 0.8f)
                                else -> primaryColor.copy(alpha = 0.85f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}


// 简化版的卡车转场动画
@Composable
private fun TruckTransitionAnimation(
    targetScene: SceneType,
    onAnimationComplete: () -> Unit
) {
    var truckX by remember { mutableStateOf((-200).dp) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = (-200).dp.value,
            targetValue = 1200.dp.value,
            animationSpec = tween(2000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            truckX = value.dp
        }
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        // 道路
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.Center)
                .background(Color(0xFF5D4037))
        )

        // 卡车
        Box(
            modifier = Modifier
                .offset(x = truckX, y = 260.dp)
                .size(140.dp, 100.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFFE53935), RoundedCornerShape(16.dp))
                .border(3.dp, Color(0xFFB71C1C), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🚒", fontSize = 40.sp)
                Text(
                    text = when (targetScene) {
                        SceneType.FIRE_STATION -> "去消防站!"
                        SceneType.SCHOOL -> "去学校!"
                        SceneType.FOREST -> "去森林!"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 进度条
        LinearProgressIndicator(
            progress = { (truckX.value + 200) / 1200f },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 50.dp, end = 50.dp)
                .fillMaxWidth()
                .height(12.dp),
            color = Color(0xFFFFD700),
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

// 简化版的小火引导动画
@Composable
private fun XiaoHuoGuideAnimation(
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "guide")

    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val handWave by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
    ) {
        // 提示气泡
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-150).dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "👋 小朋友，点击图标开始冒险吧！",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF457B9D)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "(点击任意处关闭提示)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // 小火角色
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = bounceY.dp)
                .size(80.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF8DC), Color(0xFFFFD700))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🐯", fontSize = 48.sp)
        }

        // 挥手手势
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 45.dp, y = (-20).dp + handWave.dp)
                .size(32.dp)
        ) {
            Text(text = "👋", fontSize = 24.sp)
        }
    }
}

/**
 * 家长验证对话框（优化版 - 带触觉反馈）
 *
 * @param question 数学问题
 * @param onSubmitAnswer 提交答案回调
 * @param onDismiss 取消回调
 */
@Composable
private fun ParentVerificationDialogOptimized(
    question: String,
    onSubmitAnswer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var dialogScale by remember { mutableStateOf(0.7f) }
    var dialogAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogAlpha = 1f
    }

    val numberOptions = (2..10).toList()

    val infiniteTransition = rememberInfiniteTransition(label = "verification_dialog_animation")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_breath"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
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
                .scale(dialogScale)
                .padding(32.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0xFF457B9D).copy(alpha = 0.4f),
                    ambientColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                )
                .background(
                    color = Color(0xFFFFF8DC),
                    shape = RoundedCornerShape(28.dp)
                )
                .drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF457B9D),
                                Color(0xFFFFD700),
                                Color(0xFF457B9D)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        style = Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(28.dp.value, 28.dp.value)
                    )
                }
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(iconScale)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color(0xFFFFF8DC))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐯", fontSize = 40.sp)
                }

                Text(
                    text = "家长验证",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF457B9D)
                )

                Text(
                    text = "请回答数学问题",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0xFF457B9D).copy(alpha = 0.3f)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = question,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE63946)
                    )
                }

                // 数字按钮行（每行3个）
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    numberOptions.chunked(3).forEach { row ->
                        NumberButtonRowOptimized(row, onSubmitAnswer)
                    }
                }
            }
        }
    }
}

/**
 * 数字按钮行（优化版 - 带触觉反馈）
 */
@Composable
private fun NumberButtonRowOptimized(
    numbers: List<Int>,
    onSubmitAnswer: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        numbers.forEach { num ->
            var buttonScale by remember { mutableStateOf(1f) }

            val buttonGradient = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF457B9D),
                    Color(0xFFA8DADC)
                )
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(buttonScale)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0xFF457B9D).copy(alpha = 0.4f)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        buttonScale = 0.9f
                        onSubmitAnswer(num)
                    }
                    .background(
                        brush = buttonGradient,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .drawBehind {
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
                    fontSize = 22.sp,
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
 * 时间提醒对话框（优化版 - 带触觉反馈）
 *
 * @param remainingMinutes 剩余分钟数
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun TimeReminderDialogOptimized(
    remainingMinutes: Int,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var dialogScale by remember { mutableStateOf(0.7f) }
    var dialogAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        dialogScale = 1f
        dialogAlpha = 1f
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    var buttonScale by remember { mutableStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "time_reminder_animation")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_breath"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dialogAlpha)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
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
                .scale(dialogScale)
                .padding(32.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0xFFFF6B6B).copy(alpha = 0.4f),
                    ambientColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                )
                .background(
                    color = Color(0xFFFFF8DC),
                    shape = RoundedCornerShape(28.dp)
                )
                .drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFFD700),
                                Color(0xFFFF6B6B)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        style = Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(28.dp.value, 28.dp.value)
                    )
                }
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(iconScale)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color(0xFFFFF8DC))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⏰", fontSize = 40.sp)
                }

                Text(
                    text = "时间提醒",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946)
                )

                Text(
                    text = "还剩 $remainingMinutes 分钟哦",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF457B9D)
                )

                Text(
                    text = "完成后记得休息一下眼睛~",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .scale(buttonScale)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0xFFE63946).copy(alpha = 0.4f)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            buttonScale = 0.95f
                            onDismiss()
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
                        .padding(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "我知道了",
                        fontSize = 16.sp,
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
}

/**
 * Avatar 角色组件（优化版）
 *
 * @param selectedScene 当前选中的场景
 * @param scenePositions 各场景图框的位置信息
 * @param isJumping 是否正在跳跃动画中
 * @param animationTrigger 动画触发器，用于强制触发动画
 * @param onJumpComplete 跳跃动画完成回调
 */
@Composable
private fun AvatarCharacter(
    selectedScene: SceneType,
    scenePositions: Map<SceneType, Offset>,
    isJumping: Boolean,
    animationTrigger: Int,
    onJumpComplete: () -> Unit
) {
    val density = LocalDensity.current

    // 计算目标位置
    val targetPosition = scenePositions[selectedScene]
    val targetX = if (targetPosition != null) {
        targetPosition.x - with(density) { 48.dp.toPx() }
    } else {
        val screenWidth = with(density) { 360.dp.toPx() }
        when (selectedScene) {
            SceneType.FIRE_STATION -> screenWidth * 0.3f - with(density) { 48.dp.toPx() }
            SceneType.SCHOOL -> screenWidth * 0.5f - with(density) { 48.dp.toPx() }
            SceneType.FOREST -> screenWidth * 0.7f - with(density) { 48.dp.toPx() }
        }
    }

    val targetY = if (targetPosition != null) {
        targetPosition.y + with(density) { 10.dp.toPx() }
    } else {
        with(density) { 380.dp.toPx() }
    }

    // 位置动画
    val animatedX = remember { Animatable(targetX) }
    val animatedY = remember { Animatable(targetY) }

    // 跳跃动画
    val jumpScale = remember { Animatable(1f) }
    val jumpRotation = remember { Animatable(0f) }
    val jumpOffset = remember { Animatable(0f) }

    LaunchedEffect(selectedScene, animationTrigger, isJumping) {
        if (isJumping) {
            val deltaX = animatedX.value - targetX
            val deltaY = animatedY.value - targetY
            val distanceSquared = deltaX * deltaX + deltaY * deltaY

            if (distanceSquared > 1f) {
                val movementSpec = spring<Float>(
                    dampingRatio = 0.4f,
                    stiffness = 450f
                )

                val bounceSpec = spring<Float>(
                    dampingRatio = 0.35f,
                    stiffness = 500f
                )

                // 位置移动动画
                animatedX.animateTo(targetX, animationSpec = movementSpec)
                animatedY.animateTo(targetY, animationSpec = movementSpec)

                // 跳跃弧线效果
                jumpOffset.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.3f, stiffness = 600f)
                )

                // 缩放动画
                jumpScale.animateTo(
                    targetValue = 1.15f,
                    animationSpec = bounceSpec
                )

                // 旋转动画
                jumpRotation.animateTo(
                    targetValue = 5f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
                )

                // 落地恢复
                jumpScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
                )
                jumpRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
                )
                jumpOffset.snapTo(0f)
            } else {
                animatedX.snapTo(targetX)
                animatedY.snapTo(targetY)
                jumpScale.animateTo(
                    targetValue = 1.12f,
                    animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f)
                )
                jumpScale.animateTo(1f, animationSpec = spring(0.4f, 400f))
            }

            onJumpComplete()
        }
    }

    val jumpHeight = with(density) { (-35).dp.toPx() * jumpOffset.value }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = with(density) { animatedX.value.toDp() },
                y = with(density) { (animatedY.value + jumpHeight).toDp() }
            )
    ) {
        // 动态阴影
        Box(
            modifier = Modifier
                .size(96.dp)
                .offset(y = with(density) { (-jumpHeight * 0.3f).toDp() })
                .drawBehind {
                    val shadowAlpha = 0.25f * (1f - jumpOffset.value * 0.5f)
                    val shadowScale = 1f - jumpOffset.value * 0.3f
                    drawCircle(
                        color = Color(0xFF8B4513).copy(alpha = shadowAlpha),
                        radius = (size.minDimension / 2 - 4.dp.toPx()) * shadowScale,
                        style = Stroke(width = 6.dp.toPx() * shadowScale)
                    )
                }
        )

        // Avatar 圆形图片
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(jumpScale.value)
                .rotate(jumpRotation.value)
                .clip(CircleShape)
                .shadow(
                    elevation = with(density) { (12.dp.toPx() + jumpOffset.value * 8.dp.toPx()).toDp() },
                    shape = CircleShape,
                    spotColor = Color(0xFFFFD700).copy(alpha = 0.4f + jumpOffset.value * 0.2f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF8DC), Color(0xFFFFD700))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_avatar),
                contentDescription = "好好",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
            )

            // 光晕效果
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.3f + jumpOffset.value * 0.3f),
                            radius = size.minDimension / 2 - 2.dp.toPx(),
                            style = Stroke(width = (3.dp.toPx() + jumpOffset.value * 2.dp.toPx()))
                        )
                    }
            )
        }

        // 跳跃粒子效果
        if (jumpOffset.value > 0.3f) {
            val particleAlpha = (jumpOffset.value - 0.3f) * 0.5f
            listOf(
                -20.dp to -25.dp,
                20.dp to -30.dp,
                0.dp to -35.dp,
                -15.dp to -15.dp,
                15.dp to -20.dp
            ).forEach { (xDp, yDp) ->
                Box(
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .size(8.dp)
                        .alpha(particleAlpha)
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = particleAlpha),
                                radius = size.minDimension / 2
                            )
                        }
                )
            }
        }
    }
}
