package com.cryallen.tigerfire.ui.map

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryallen.tigerfire.domain.model.SceneStatus
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.map.MapEffect
import com.cryallen.tigerfire.presentation.map.MapEvent
import com.cryallen.tigerfire.presentation.map.MapViewModel

/**
 * 主地图 Screen
 *
 * 显示三个场景入口图标、我的收藏按钮和家长模式入口
 *
 * @param viewModel MapViewModel
 * @param onNavigateToWelcome 导航到欢迎页
 * @param onNavigateToFireStation 导航到消防站
 * @param onNavigateToSchool 导航到学校
 * @param onNavigateToForest 导航到森林
 * @param onNavigateToCollection 导航到我的收藏
 * @param onNavigateToParent 导航到家长模式
 */
@Composable
fun MapScreen(
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

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MapEffect.NavigateToScene -> {
                    when (effect.scene) {
                        SceneType.FIRE_STATION -> onNavigateToFireStation()
                        SceneType.SCHOOL -> onNavigateToSchool()
                        SceneType.FOREST -> onNavigateToForest()
                    }
                }
                is MapEffect.NavigateToCollection -> onNavigateToCollection()
                is MapEffect.NavigateToParent -> onNavigateToParent()
                is MapEffect.PlayLockedHint, is MapEffect.PlaySceneSound,
                is MapEffect.PlaySuccessSound -> {
                    // TODO: 在 Task 4.9/4.10 中集成音效播放
                }
            }
        }
    }

    // 卡通风格渐变背景 - 天空到草地的过渡
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF87CEEB),  // 天空蓝
            Color(0xFFB0E0E6),  // 浅蓝
            Color(0xFF98FB98),  // 嫩绿
            Color(0xFF90EE90)   // 淡绿
        ),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    // 背景装饰动画
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val starFloatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                // ========== 装饰性太阳 ==========
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.95f),
                    radius = size.minDimension * 0.07f,
                    center = Offset(size.width * 0.88f, size.height * 0.14f)
                )
                // 太阳光芒
                repeat(12) { i ->
                    val angle = (i * 30f) * (Math.PI / 180).toFloat()
                    val sunRadius = size.minDimension * 0.07f
                    val sunCenter = Offset(size.width * 0.88f, size.height * 0.14f)
                    val rayLength = 25.dp.toPx()
                    val startOffset = Offset(
                        sunCenter.x + kotlin.math.cos(angle) * sunRadius,
                        sunCenter.y + kotlin.math.sin(angle) * sunRadius
                    )
                    val endOffset = Offset(
                        sunCenter.x + kotlin.math.cos(angle) * (sunRadius + rayLength),
                        sunCenter.y + kotlin.math.sin(angle) * (sunRadius + rayLength)
                    )
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = 0.7f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = 5f
                    )
                }

                // ========== 卡通云朵装饰 ==========
                val cloudColor = Color(0xFFFFFFFF)
                val cloudAlpha = 0.85f

                // 左上角云朵1 - 大朵
                drawRoundRect(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    topLeft = Offset(size.width * 0.03f, size.height * 0.06f),
                    size = Size(size.width * 0.18f, size.height * 0.06f),
                    cornerRadius = CornerRadius(60f, 60f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    radius = size.minDimension * 0.055f,
                    center = Offset(size.width * 0.10f, size.height * 0.07f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    radius = size.minDimension * 0.045f,
                    center = Offset(size.width * 0.16f, size.height * 0.08f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    radius = size.minDimension * 0.04f,
                    center = Offset(size.width * 0.20f, size.height * 0.075f)
                )

                // 右侧云朵2 - 中朵
                drawRoundRect(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    topLeft = Offset(size.width * 0.72f, size.height * 0.18f),
                    size = Size(size.width * 0.14f, size.height * 0.05f),
                    cornerRadius = CornerRadius(50f, 50f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    radius = size.minDimension * 0.04f,
                    center = Offset(size.width * 0.77f, size.height * 0.19f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.82f, size.height * 0.20f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha),
                    radius = size.minDimension * 0.03f,
                    center = Offset(size.width * 0.86f, size.height * 0.19f)
                )

                // 左侧小云朵3
                drawRoundRect(
                    color = cloudColor.copy(alpha = cloudAlpha * 0.9f),
                    topLeft = Offset(size.width * 0.02f, size.height * 0.20f),
                    size = Size(size.width * 0.10f, size.height * 0.035f),
                    cornerRadius = CornerRadius(40f, 40f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha * 0.9f),
                    radius = size.minDimension * 0.03f,
                    center = Offset(size.width * 0.05f, size.height * 0.21f)
                )
                drawCircle(
                    color = cloudColor.copy(alpha = cloudAlpha * 0.9f),
                    radius = size.minDimension * 0.025f,
                    center = Offset(size.width * 0.09f, size.height * 0.22f)
                )

                // ========== 彩虹装饰 ==========
                val rainbowColors = listOf(
                    Color(0xFFFF0000), // 红
                    Color(0xFFFF7F00), // 橙
                    Color(0xFFFFFF00), // 黄
                    Color(0xFF00FF00), // 绿
                    Color(0xFF0000FF), // 蓝
                    Color(0xFF4B0082), // 靛
                    Color(0xFF9400D3)  // 紫
                )
                val rainbowCenter = Offset(size.width * 0.15f, size.height * 0.75f)
                val rainbowRadius = size.minDimension * 0.25f
                rainbowColors.forEachIndexed { index, color ->
                    val currentRadius = rainbowRadius - (index * 8.dp.toPx())
                    if (currentRadius > 0) {
                        drawArc(
                            color = color.copy(alpha = 0.4f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(
                                rainbowCenter.x - currentRadius,
                                rainbowCenter.y - currentRadius
                            ),
                            size = Size(currentRadius * 2, currentRadius * 2),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                }

                // ========== 闪烁星星装饰 ==========
                val starPositions = listOf(
                    Offset(size.width * 0.25f, size.height * 0.15f + starFloatAnimation),
                    Offset(size.width * 0.40f, size.height * 0.12f - starFloatAnimation * 0.5f),
                    Offset(size.width * 0.55f, size.height * 0.18f + starFloatAnimation * 0.7f),
                    Offset(size.width * 0.35f, size.height * 0.25f - starFloatAnimation * 0.3f),
                    Offset(size.width * 0.50f, size.height * 0.10f),
                    Offset(size.width * 0.65f, size.height * 0.20f + starFloatAnimation * 0.5f),
                )
                starPositions.forEach { pos ->
                    // 绘制五角星形状
                    val outerRadius = 8.dp.toPx()
                    val innerRadius = 4.dp.toPx()
                    val points = mutableListOf<Offset>()
                    for (i in 0 until 10) {
                        val angle = (i * 36f - 90f) * (Math.PI / 180).toFloat()
                        val radius = if (i % 2 == 0) outerRadius else innerRadius
                        points.add(
                            Offset(
                                pos.x + kotlin.math.cos(angle) * radius,
                                pos.y + kotlin.math.sin(angle) * radius
                            )
                        )
                    }
                    // 简化：绘制小圆点代替星星
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.8f),
                        radius = 6.dp.toPx(),
                        center = pos
                    )
                    // 星星光芒
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = 0.5f),
                        start = Offset(pos.x - 10.dp.toPx(), pos.y),
                        end = Offset(pos.x + 10.dp.toPx(), pos.y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = 0.5f),
                        start = Offset(pos.x, pos.y - 10.dp.toPx()),
                        end = Offset(pos.x, pos.y + 10.dp.toPx()),
                        strokeWidth = 2f
                    )
                }

                // ========== 漂浮气球装饰 ==========
                val balloonConfigs = listOf(
                    Triple(0.92f, 0.35f, Color(0xFFFF6B6B)), // 红色气球
                    Triple(0.96f, 0.45f, Color(0xFF4ECDC4)), // 青色气球
                    Triple(0.89f, 0.55f, Color(0xFFFFE66D)), // 黄色气球
                )
                balloonConfigs.forEach { (xRatio, yRatio, color) ->
                    val balloonCenter = Offset(size.width * xRatio, size.height * yRatio)
                    val balloonRadius = size.minDimension * 0.035f

                    // 气球绳子
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.4f),
                        start = Offset(balloonCenter.x, balloonCenter.y + balloonRadius),
                        end = Offset(balloonCenter.x, balloonCenter.y + balloonRadius + 40.dp.toPx()),
                        strokeWidth = 2f
                    )

                    // 气球本体
                    drawCircle(
                        color = color.copy(alpha = 0.7f),
                        radius = balloonRadius,
                        center = balloonCenter
                    )

                    // 气球高光
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = balloonRadius * 0.2f,
                        center = Offset(
                            balloonCenter.x - balloonRadius * 0.3f,
                            balloonCenter.y - balloonRadius * 0.3f
                        )
                    )
                }

                // ========== 底部草地纹理装饰 ==========
                val grassColor = Color(0xFF228B22)
                repeat(25) { i ->
                    val x = size.width * (0.02f + i * 0.04f)
                    val baseY = size.height * 0.94f
                    val heightVariation = (i % 3) * 5.dp.toPx()
                    drawLine(
                        color = grassColor.copy(alpha = 0.35f),
                        start = Offset(x, baseY),
                        end = Offset(x + 8.dp.toPx(), baseY - 25.dp.toPx() - heightVariation),
                        strokeWidth = 3.5f
                    )
                    drawLine(
                        color = grassColor.copy(alpha = 0.35f),
                        start = Offset(x + 4.dp.toPx(), baseY),
                        end = Offset(x + 14.dp.toPx(), baseY - 18.dp.toPx() - heightVariation),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = grassColor.copy(alpha = 0.3f),
                        start = Offset(x + 2.dp.toPx(), baseY),
                        end = Offset(x + 6.dp.toPx(), baseY - 30.dp.toPx() - heightVariation),
                        strokeWidth = 2.5f
                    )
                }

                // ========== 小花装饰 ==========
                val flowerPositions = listOf(
                    Offset(size.width * 0.15f, size.height * 0.90f),
                    Offset(size.width * 0.35f, size.height * 0.88f),
                    Offset(size.width * 0.65f, size.height * 0.91f),
                    Offset(size.width * 0.85f, size.height * 0.89f),
                )
                val flowerColors = listOf(
                    Color(0xFFFF69B4), // 粉色
                    Color(0xFFFFB6C1), // 浅粉
                    Color(0xFFFFA07A), // 橙粉
                    Color(0xFFFFD700), // 金黄
                )
                flowerPositions.forEachIndexed { index, pos ->
                    val flowerColor = flowerColors[index % flowerColors.size]
                    val petalRadius = 6.dp.toPx()
                    // 花瓣 - 5个圆形
                    repeat(5) { i ->
                        val angle = (i * 72f) * (Math.PI / 180).toFloat()
                        val petalOffset = Offset(
                            kotlin.math.cos(angle) * petalRadius,
                            kotlin.math.sin(angle) * petalRadius
                        )
                        drawCircle(
                            color = flowerColor.copy(alpha = 0.7f),
                            radius = petalRadius,
                            center = Offset(pos.x + petalOffset.x, pos.y + petalOffset.y)
                        )
                    }
                    // 花心
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.8f),
                        radius = petalRadius * 0.6f,
                        center = pos
                    )
                }
            }
    ) {
        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 我的收藏按钮（左上角）- 增强视觉效果
            IconButton(
                onClick = { viewModel.onEvent(MapEvent.CollectionClicked) },
                modifier = Modifier
                    .size(72.dp)
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
                        // 装饰性光环
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.3f),
                            radius = size.minDimension / 2 - 4.dp.toPx(),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
            ) {
                Text(
                    text = "🐯",
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
                )
            }

            // 家长模式入口（右上角）- 保持低调
            IconButton(
                onClick = { viewModel.onEvent(MapEvent.ParentModeClicked) },
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        spotColor = Color.Gray.copy(alpha = 0.3f)
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
                    .drawBehind {
                        // 虚线边框表示设置
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.3f),
                            radius = size.minDimension / 2 - 3.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 28.sp
                )
            }
        }

        // 中央场景图标区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题 - 增加阴影和边框效果
            Box(
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0xFFE63946).copy(alpha = 0.3f)
                    )
            ) {
                Text(
                    text = "🌟 选择冒险场景 🌟",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946),
                    textAlign = TextAlign.Center
                )
            }

            // 场景图标行 - 增加间距
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 消防站图标 - 红色主题，组合图标
                EnhancedSceneIcon(
                    scene = SceneType.FIRE_STATION,
                    status = state.sceneStatuses[SceneType.FIRE_STATION] ?: SceneStatus.LOCKED,
                    mainIcon = "🚒",
                    subIcon = "🔥",
                    label = "消防站",
                    primaryColor = Color(0xFFE63946),
                    secondaryColor = Color(0xFFFF6B6B),
                    accentColor = Color(0xFFFFD700),
                    onClick = { viewModel.onEvent(MapEvent.SceneClicked(SceneType.FIRE_STATION)) }
                )

                // 学校图标 - 蓝色主题，组合图标
                EnhancedSceneIcon(
                    scene = SceneType.SCHOOL,
                    status = state.sceneStatuses[SceneType.SCHOOL] ?: SceneStatus.LOCKED,
                    mainIcon = "🏫",
                    subIcon = "📚",
                    label = "学校",
                    primaryColor = Color(0xFF457B9D),
                    secondaryColor = Color(0xFFA8DADC),
                    accentColor = Color(0xFFFFE66D),
                    onClick = { viewModel.onEvent(MapEvent.SceneClicked(SceneType.SCHOOL)) }
                )

                // 森林图标 - 绿色主题，组合图标
                EnhancedSceneIcon(
                    scene = SceneType.FOREST,
                    status = state.sceneStatuses[SceneType.FOREST] ?: SceneStatus.LOCKED,
                    mainIcon = "🌲",
                    subIcon = "🐑",
                    label = "森林",
                    primaryColor = Color(0xFF2A9D8F),
                    secondaryColor = Color(0xFF95D5B2),
                    accentColor = Color(0xFFFFB6C1),
                    onClick = { viewModel.onEvent(MapEvent.SceneClicked(SceneType.FOREST)) }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 场景说明 - 更醒目
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFFFD700).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 28.dp, vertical = 14.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                    )
            ) {
                Text(
                    text = "✨ 点击图标开始冒险 ✨",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 家长模式验证对话框
        if (state.showParentVerification) {
            ParentVerificationDialog(
                question = state.mathQuestion?.first ?: "",
                onSubmitAnswer = { answer ->
                    viewModel.onEvent(MapEvent.SubmitParentAnswer(answer))
                },
                onDismiss = {
                    viewModel.onEvent(MapEvent.CancelParentVerification)
                }
            )
        }

        // 时间提醒对话框
        appSessionManager?.let { sessionManager ->
            val timeRemaining by sessionManager.timeRemaining.collectAsState()
            var showTimeReminder by remember { mutableStateOf(false) }

            // 检查是否应该显示时间提醒
            if (sessionManager.shouldShowTimeReminder() && !showTimeReminder) {
                showTimeReminder = true
            }

            if (showTimeReminder) {
                val remainingMinutes = sessionManager.getRemainingMinutes()
                TimeReminderDialog(
                    remainingMinutes = remainingMinutes,
                    onDismiss = {
                        showTimeReminder = false
                        // 标记提醒已显示，避免重复显示
                    }
                )
            }
        }
    }
}

/**
 * 增强版场景图标组件 - 专为3-6岁儿童设计
 *
 * @param scene 场景类型
 * @param status 场景状态
 * @param mainIcon 主图标 emoji
 * @param subIcon 副图标 emoji
 * @param label 标签
 * @param primaryColor 主色调
 * @param secondaryColor 次要色调
 * @param accentColor 强调色
 * @param onClick 点击回调
 */
@Composable
private fun EnhancedSceneIcon(
    scene: SceneType,
    status: SceneStatus,
    mainIcon: String,
    subIcon: String,
    label: String,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    // 无限循环动画 - 用于呼吸闪烁效果
    val infiniteTransition = rememberInfiniteTransition(label = "enhanced_scene_icon_animation")

    // 缩放动画 - 呼吸效果
    val scaleAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // 透明度动画 - 闪烁效果
    val alphaAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    // 旋转动画 - 仅用于已完成的场景
    val rotateAnimation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate_animation"
    )

    // 脉冲光圈动画
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_animation"
    )

    val isClickable = status != SceneStatus.LOCKED
    val isCompleted = status == SceneStatus.COMPLETED

    // 根据状态设置背景色和效果
    val backgroundColor = when (status) {
        SceneStatus.LOCKED -> Color(0xFFBDBDBD)
        SceneStatus.UNLOCKED -> Color.White
        SceneStatus.COMPLETED -> Color(0xFFFFF8DC)
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(scaleAnimation)
            .rotate(if (isCompleted) rotateAnimation else 0f)
            .then(
                if (isCompleted) {
                    // 金色光晕效果
                    Modifier.shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = accentColor.copy(alpha = 0.6f),
                        spotColor = primaryColor.copy(alpha = 0.4f)
                    )
                } else if (isClickable) {
                    // 普通阴影
                    Modifier.shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = secondaryColor.copy(alpha = 0.6f),
                        spotColor = primaryColor.copy(alpha = 0.4f)
                    )
                } else {
                    Modifier.shadow(10.dp, RoundedCornerShape(28.dp))
                }
            )
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .drawBehind {
                // 脉冲光圈效果（仅可点击状态）
                if (isClickable && pulseAnimation > 0) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f * pulseAnimation),
                                secondaryColor.copy(alpha = 0.15f * pulseAnimation)
                            ),
                            radius = size.minDimension / 2
                        ),
                        style = Stroke(width = 8.dp.toPx() * pulseAnimation),
                        cornerRadius = CornerRadius(28.dp.value, 28.dp.value)
                    )
                }

                // 渐变边框
                if (isClickable) {
                    val strokeWidth = 8.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.9f),
                                secondaryColor.copy(alpha = 0.7f),
                                accentColor.copy(alpha = 0.9f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        style = Stroke(width = strokeWidth),
                        cornerRadius = CornerRadius(28.dp.value, 28.dp.value)
                    )
                }

                // 装饰性圆点（四个角）
                if (isClickable) {
                    val dotRadius = 6.dp.toPx()
                    val padding = 12.dp.toPx()
                    listOf(
                        Offset(padding, padding),
                        Offset(size.width - padding, padding),
                        Offset(padding, size.height - padding),
                        Offset(size.width - padding, size.height - padding)
                    ).forEach { pos ->
                        drawCircle(
                            color = accentColor.copy(alpha = 0.7f),
                            radius = dotRadius,
                            center = pos
                        )
                    }
                }
            }
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .alpha(if (isClickable) alphaAnimation else 0.5f)
    ) {
        // 副图标（小，在右上角） - 使用Box包裹
        if (isClickable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = subIcon,
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 锁定状态显示锁图标
            if (!isClickable) {
                Text(
                    text = "🔒",
                    fontSize = 36.sp,
                    modifier = Modifier.offset(y = (-35).dp)
                )
            }

            // 主图标
            Text(
                text = mainIcon,
                fontSize = 72.sp,
                modifier = Modifier
                    .offset(y = if (!isClickable) 15.dp else 0.dp)
                    .then(
                        if (isCompleted) {
                            Modifier.drawBehind {
                                // 星星装饰 - 多个
                                val starOffsets = listOf(
                                    Offset(size.width * 0.1f, size.height * 0.15f),
                                    Offset(size.width * 0.88f, size.height * 0.12f),
                                    Offset(size.width * 0.15f, size.height * 0.85f),
                                    Offset(size.width * 0.85f, size.height * 0.82f),
                                )
                                starOffsets.forEach { offset ->
                                    drawCircle(
                                        color = accentColor.copy(alpha = 0.7f),
                                        radius = 8.dp.toPx(),
                                        center = offset
                                    )
                                }
                            }
                        } else if (isClickable) {
                            Modifier.drawBehind {
                                // 简单的光晕效果
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.1f),
                                    radius = size.minDimension / 2.2f
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 标签
            Box(
                modifier = Modifier
                    .background(
                        color = if (status == SceneStatus.LOCKED) {
                            Color(0xFF757575)
                        } else if (isCompleted) {
                            accentColor.copy(alpha = 0.8f)
                        } else {
                            primaryColor.copy(alpha = 0.85f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // 已完成标识
            if (isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⭐ 已完成",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }
    }
}

/**
 * 家长模式验证对话框
 *
 * @param question 数学问题
 * @param onSubmitAnswer 提交答案回调
 * @param onDismiss 取消回调
 */
@Composable
private fun ParentVerificationDialog(
    question: String,
    onSubmitAnswer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: 使用 Material3 Dialog 实现验证对话框
    // 这里先用简单的 Box 占位
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color.White, shape = MaterialTheme.shapes.large)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "家长验证",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = question,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 简单的答案输入（占位符）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TODO: 添加数字输入按钮
                    (1..5).forEach { num ->
                        Text(
                            text = "$num",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { onSubmitAnswer(num) }
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "取消",
                    fontSize = 16.sp,
                    color = Color.Blue,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
        }
    }
}

/**
 * 时间提醒对话框
 *
 * 当会话时间即将到时（默认 2 分钟前）显示，提醒儿童剩余时间
 *
 * @param remainingMinutes 剩余分钟数
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun TimeReminderDialog(
    remainingMinutes: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    Color(0xFFFFE4B5), // 温暖的米色背景
                    shape = MaterialTheme.shapes.large
                )
                .padding(32.dp)
                .shadow(16.dp, MaterialTheme.shapes.large)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 小火图标
                Text(
                    text = "🐯",
                    fontSize = 64.sp
                )

                // 标题
                Text(
                    text = "时间快到啦！",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946) // 红色警告色
                )

                // 提示内容
                Text(
                    text = "还剩下 $remainingMinutes 分钟哦",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "想再玩一会儿可以请爸爸妈妈帮忙设置~",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 确定按钮
                Text(
                    text = "我知道了",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .background(
                            Color(0xFFE63946),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                )
            }
        }
    }
}
