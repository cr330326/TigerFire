package com.cryallen.tigerfire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryallen.tigerfire.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 儿童友好组件库
 *
 * 设计原则：
 * - 超大触控目标（≥100pt）
 * - 明显的点击反馈（缩放+音效）
 * - 圆润的外观（大圆角）
 * - 明亮的色彩（emoji + 渐变）
 */

/**
 * 统一的返回按钮组件
 *
 * @param onClick 点击回调
 * @param size 按钮大小（默认64dp）
 */
@Composable
fun KidsBackButton(
    onClick: () -> Unit,
    size: Dp = 64.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 使用动画状态而不是手动管理scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "back_button_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = Color(0xFFFFD700).copy(alpha = 0.5f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
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
                    radius = size.toPx() / 2 - 4.dp.toPx(),
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
}

/**
 * 卡通播放按钮（学校场景、森林场景专用）
 *
 * @param onClick 点击回调
 * @param size 按钮大小
 * @param text 显示文字
 */
@Composable
fun CartoonPlayButton(
    onClick: () -> Unit,
    size: Dp = 180.dp,
    text: String = "点我播放"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "play_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else pulseScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "press_scale"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = KidsShadows.ExtraLarge,
                shape = CircleShape,
                spotColor = Color(0xFFF4A261).copy(alpha = 0.5f)
            )
            .background(
                brush = createRadialGradient(
                    colors = listOf(
                        Color(0xFFFFE066),  // 明亮黄
                        Color(0xFFFFAA66)   // 橙黄
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KidsSpacing.Small)
        ) {
            // 大三角播放图标emoji
            Text(
                text = "▶️",
                fontSize = 64.sp
            )
            // 提示文字
            Text(
                text = text,
                fontSize = KidsTextSize.Medium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Q版火焰效果（替代真实火焰）
 *
 * @param modifier 修饰符
 * @param size 火焰大小
 */
@Composable
fun CartoonFlame(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_animation")

    // 缩放动画（跳动效果）
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    // 旋转动画（摇摆效果）
    val flameRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🔥",
            fontSize = (size.value * 0.67f).sp,  // 火焰emoji大小约为容器的2/3
            modifier = Modifier
                .scale(flameScale)
                .graphicsLayer(rotationZ = flameRotation)
        )
    }
}

/**
 * 卡通化小羊组件（森林场景专用）
 *
 * @param isRescued 是否已被救
 * @param onClick 点击回调
 */
@Composable
fun CartoonSheep(
    isRescued: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // 求救动画（未救援时跳动）
    val infiniteTransition = rememberInfiniteTransition(label = "sheep_animation")
    val jumpOffset by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = if (isRescued) 0.dp else 20.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jump_offset"
    )

    // 脉冲光圈（视觉引导）
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRescued) 1f else 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size(KidsTouchTarget.Large)
            .offset(y = -jumpOffset),
        contentAlignment = Alignment.Center
    ) {
        // 脉冲光圈（未救援时显示）
        if (!isRescued) {
            Box(
                modifier = Modifier
                    .size(KidsTouchTarget.Large)
                    .scale(pulseScale)
                    .background(
                        color = Color(0xFFFFE066).copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }

        // 小羊图标
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = if (isRescued) {
                        createRadialGradient(
                            colors = listOf(
                                Color(0xFF98FB98),  // 嫩绿（已救）
                                Color(0xFF7FD98E)
                            )
                        )
                    } else {
                        createRadialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF0F0F0)
                            )
                        )
                    },
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = if (isRescued) ({}) else onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRescued) "🐑✅" else "🐑",
                    fontSize = 48.sp
                )
                if (!isRescued) {
                    Text(
                        text = "救我",
                        fontSize = KidsTextSize.Small,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }

        // 火焰包围（未救援时显示）
        if (!isRescued) {
            // 左侧火焰
            CartoonFlame(
                modifier = Modifier.offset(x = (-60).dp, y = 10.dp),
                size = 36.dp
            )
            // 右侧火焰
            CartoonFlame(
                modifier = Modifier.offset(x = 60.dp, y = 10.dp),
                size = 36.dp
            )
        }
    }
}

/**
 * 3D悬浮徽章组件（收藏页专用）
 *
 * @param emoji 徽章emoji
 * @param color 徽章颜色
 * @param onClick 点击回调
 */
@Composable
fun FloatingBadge(
    emoji: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_float")

    // 悬浮动画
    val floatY by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = 10.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // 旋转动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "press_scale"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .offset(y = floatY)
            .scale(scale)
            .graphicsLayer(rotationZ = rotation)
            .shadow(
                elevation = KidsShadows.Large,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                brush = createRadialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.9f),
                        color.copy(alpha = 0.7f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 48.sp
        )
    }
}

/**
 * 儿童友好进度卡片
 *
 * @param current 当前进度
 * @param total 总数
 * @param title 标题
 * @param emoji 图标emoji
 */
@Composable
fun KidsProgressCard(
    current: Int,
    total: Int,
    title: String = "已完成",
    emoji: String = "⭐"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .scale(pulseScale)
            .shadow(
                elevation = KidsShadows.Medium,
                shape = KidsShapes.Large,
                spotColor = Color.Black.copy(alpha = 0.18f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            )
            .background(
                brush = createLinearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = KidsShapes.Large
            )
            .padding(horizontal = KidsSpacing.ExtraLarge, vertical = KidsSpacing.Large)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KidsSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = KidsTextSize.Medium
            )
            Text(
                text = "$title: $current/$total",
                fontSize = KidsTextSize.Medium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
