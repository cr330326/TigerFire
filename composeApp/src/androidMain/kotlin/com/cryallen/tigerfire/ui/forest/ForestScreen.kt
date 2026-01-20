package com.cryallen.tigerfire.ui.forest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.cryallen.tigerfire.component.VideoPlayer
import com.cryallen.tigerfire.component.getAudioManager
import com.cryallen.tigerfire.presentation.forest.ForestEffect
import com.cryallen.tigerfire.presentation.forest.ForestEvent
import com.cryallen.tigerfire.presentation.forest.ForestViewModel
import kotlin.math.roundToInt

/**
 * 森林场景 Screen
 *
 * 手势拖拽救援场景：拖拽直升机靠近小羊，放下梯子救援
 * 救援完成后播放动画并获得徽章
 *
 * @param viewModel ForestViewModel
 * @param onNavigateBack 返回主地图回调
 */
@Composable
fun ForestScreen(
    viewModel: ForestViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val audioManager = remember { context.getAudioManager() }

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ForestEffect.PlayRescueVideo -> {
                    // 视频播放由状态驱动，不需要额外处理
                }
                is ForestEffect.ShowBadgeAnimation -> {
                    // 徽章动画在 showBadgeAnimation 状态中处理
                }
                is ForestEffect.ShowCompletionHint -> {
                    // 完成提示由状态驱动
                }
                is ForestEffect.PlayClickSound -> {
                    audioManager.playClickSound(com.cryallen.tigerfire.domain.model.SceneType.FOREST)
                }
                is ForestEffect.PlayDragSound -> {
                    audioManager.playDragSound()
                }
                is ForestEffect.PlaySnapSound -> {
                    audioManager.playSnapSound()
                }
                is ForestEffect.PlayBadgeSound -> {
                    audioManager.playBadgeSound()
                }
                is ForestEffect.PlayAllCompletedSound -> {
                    audioManager.playAllCompletedSound()
                }
                is ForestEffect.NavigateToMap -> onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A9D8F)) // 森林绿背景
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
                        viewModel.onEvent(ForestEvent.BackToMapClicked)
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

            // 中央游戏区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                // 场景标题和进度
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "森林",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "拖拽直升机去救小羊！",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "已救援: ${state.rescuedSheep.size}/2",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // 全部完成提示
                    if (state.isAllCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 全部完成！",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Yellow
                        )
                    }
                }

                // 游戏区域（小羊和直升机）
                ForestGameArea(
                    state = state,
                    onDragStarted = { viewModel.onEvent(ForestEvent.DragStarted) },
                    onDragUpdated = { x, y ->
                        viewModel.onEvent(ForestEvent.DragUpdated(x, y))
                    },
                    onDragEnded = { viewModel.onEvent(ForestEvent.DragEnded) },
                    onLowerLadderClick = { sheepIndex ->
                        viewModel.onEvent(ForestEvent.LowerLadderClicked(sheepIndex))
                    }
                )
            }
        }

        // 救援视频播放覆盖层
        if (state.isPlayingRescueVideo && state.currentPlayingSheepIndex != null) {
            RescueVideoOverlay(
                sheepIndex = state.currentPlayingSheepIndex!!,
                onPlaybackComplete = { sheepIndex ->
                    viewModel.onEvent(ForestEvent.RescueVideoCompleted(sheepIndex))
                }
            )
        }

        // 徽章收集动画覆盖层
        BadgeAnimationOverlay(
            show = state.showBadgeAnimation,
            sheepIndex = state.earnedBadgeSheepIndex,
            onAnimationComplete = {
                viewModel.onEvent(ForestEvent.BadgeAnimationCompleted)
            }
        )
    }
}

/**
 * 森林游戏区域
 *
 * 包含小羊、直升机和放下梯子按钮
 */
@Composable
private fun ForestGameArea(
    state: com.cryallen.tigerfire.presentation.forest.ForestState,
    onDragStarted: () -> Unit,
    onDragUpdated: (Float, Float) -> Unit,
    onDragEnded: () -> Unit,
    onLowerLadderClick: (Int) -> Unit
) {
    val screenWidth = 400.dp
    val screenHeight = 300.dp

    Box(
        modifier = Modifier
            .size(screenWidth, screenHeight)
            .background(Color.Transparent)
    ) {
        // 小羊位置（屏幕比例）
        val sheepPositions = listOf(
            0.6f to 0.3f,  // 小羊 1
            0.7f to 0.7f   // 小羊 2
        )

        // 绘制小羊
        sheepPositions.forEachIndexed { index, (xRatio, yRatio) ->
            val isRescued = index in state.rescuedSheep
            val isNearby = state.nearbySheepIndex == index

            Sheep(
                xRatio = xRatio,
                yRatio = yRatio,
                isRescued = isRescued,
                isNearby = isNearby,
                containerWidth = screenWidth,
                containerHeight = screenHeight
            )
        }

        // 直升机（可拖拽）
        Helicopter(
            xRatio = state.helicopterX,
            yRatio = state.helicopterY,
            isDragging = state.isDraggingHelicopter,
            containerWidth = screenWidth,
            containerHeight = screenHeight,
            onDragStarted = onDragStarted,
            onDragUpdated = onDragUpdated,
            onDragEnded = onDragEnded
        )

        // "放下梯子"按钮（当靠近小羊时显示）
        if (state.showLowerLadderButton && state.nearbySheepIndex != null) {
            LowerLadderButton(
                xRatio = state.helicopterX,
                yRatio = state.helicopterY,
                containerWidth = screenWidth,
                containerHeight = screenHeight,
                onClick = { onLowerLadderClick(state.nearbySheepIndex!!) }
            )
        }
    }
}

/**
 * 小羊组件
 *
 * @param xRatio X 坐标（屏幕比例）
 * @param yRatio Y 坐标（屏幕比例）
 * @param isRescued 是否已被救援
 * @param isNearby 直升机是否靠近
 */
@Composable
private fun Sheep(
    xRatio: Float,
    yRatio: Float,
    isRescued: Boolean,
    isNearby: Boolean,
    containerWidth: androidx.compose.ui.unit.Dp,
    containerHeight: androidx.compose.ui.unit.Dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isNearby) 1.2f else 1f,
        animationSpec = spring(),
        label = "sheep_scale"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (xRatio * containerWidth.value).dp,
                y = (yRatio * containerHeight.value).dp
            )
            .size(60.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 小羊图标
            Text(
                text = if (isRescued) "🐑✅" else "🐑",
                fontSize = (40 * scale).sp,
                modifier = Modifier
                    .size(50.dp * scale)
                    .shadow(
                        elevation = if (isNearby) 12.dp else 4.dp,
                        shape = CircleShape,
                        ambientColor = if (isNearby) Color.Yellow else Color.Transparent
                    )
            )
        }
    }
}

/**
 * 直升机组件（可拖拽）
 *
 * @param xRatio X 坐标（屏幕比例）
 * @param yRatio Y 坐标（屏幕比例）
 * @param isDragging 是否正在拖拽
 */
@Composable
private fun Helicopter(
    xRatio: Float,
    yRatio: Float,
    isDragging: Boolean,
    containerWidth: androidx.compose.ui.unit.Dp,
    containerHeight: androidx.compose.ui.unit.Dp,
    onDragStarted: () -> Unit,
    onDragUpdated: (Float, Float) -> Unit,
    onDragEnded: () -> Unit
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset(
                x = (xRatio * containerWidth.value - 40).dp,
                y = (yRatio * containerHeight.value - 40).dp
            )
            .size(80.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onDragStarted()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val offsetX = change.position.x - change.previousPosition.x
                        val offsetY = change.position.y - change.previousPosition.y
                        currentPosition += Offset(offsetX, offsetY)

                        // 转换为屏幕比例
                        val newXRatio = ((xRatio * containerWidth.value - 40) + offsetX) / containerWidth.value
                        val newYRatio = ((yRatio * containerHeight.value - 40) + offsetY) / containerHeight.value
                        onDragUpdated(newXRatio, newYRatio)
                    },
                    onDragEnd = {
                        currentPosition = Offset.Zero
                        onDragEnded()
                    }
                )
            }
            .shadow(
                elevation = if (isDragging) 16.dp else 8.dp,
                shape = CircleShape,
                ambientColor = Color(0xFFF4A261)
            )
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (isDragging) 0.9f else 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚁",
                fontSize = 40.sp
            )
        }
    }
}

/**
 * "放下梯子"按钮
 *
 * @param xRatio X 坐标（屏幕比例）
 * @param yRatio Y 坐标（屏幕比例）
 */
@Composable
private fun LowerLadderButton(
    xRatio: Float,
    yRatio: Float,
    containerWidth: androidx.compose.ui.unit.Dp,
    containerHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(
                x = (xRatio * containerWidth.value).dp,
                y = (yRatio * containerHeight.value + 50).dp
            )
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF4A261))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "🪜 放下梯子",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 救援视频播放覆盖层
 *
 * @param sheepIndex 小羊索引
 * @param onPlaybackComplete 播放完成回调
 */
@Composable
private fun RescueVideoOverlay(
    sheepIndex: Int,
    onPlaybackComplete: (Int) -> Unit
) {
    val videoPath = "videos/rescue_sheep_${sheepIndex + 1}.mp4"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // 视频播放器
        VideoPlayer(
            videoPath = videoPath,
            modifier = Modifier
                .width(320.dp)
                .height(240.dp),
            onPlaybackCompleted = {
                onPlaybackComplete(sheepIndex)
            },
            autoPlay = true,
            showControls = false
        )
    }
}

/**
 * 徽章收集动画覆盖层
 *
 * @param show 是否显示
 * @param sheepIndex 获得徽章的小羊索引
 * @param onAnimationComplete 动画完成回调
 */
@Composable
private fun BadgeAnimationOverlay(
    show: Boolean,
    sheepIndex: Int?,
    onAnimationComplete: () -> Unit
) {
    AnimatedVisibility(
        visible = show,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 小羊图标
                Text(
                    text = "🐑",
                    fontSize = 100.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 徽章图标
                Text(
                    text = "🏅",
                    fontSize = 80.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "你真棒！",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "成功救出第 ${sheepIndex?.plus(1)} 只小羊！",
                    fontSize = 20.sp,
                    color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 点击继续
                Text(
                    text = "点击继续",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(
                            Color(0xFF2A9D8F),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clickable(onClick = onAnimationComplete)
                )
            }
        }
    }
}
