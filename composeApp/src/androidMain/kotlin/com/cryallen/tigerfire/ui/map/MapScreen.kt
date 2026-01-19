package com.cryallen.tigerfire.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    onNavigateToParent: () -> Unit = {}
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB)) // 天空蓝背景（后续替换为地图图片）
    ) {
        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 我的收藏按钮（左上角）
            IconButton(
                onClick = { viewModel.onEvent(MapEvent.CollectionClicked) },
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Text(
                    text = "🐯",
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }

            // 家长模式入口（右上角）
            IconButton(
                onClick = { viewModel.onEvent(MapEvent.ParentModeClicked) },
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 24.sp
                )
            }
        }

        // 中央场景图标区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题
            Text(
                text = "选择冒险场景",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // 场景图标行
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 消防站图标
                SceneIcon(
                    scene = SceneType.FIRE_STATION,
                    status = state.sceneStatuses[SceneType.FIRE_STATION] ?: SceneStatus.LOCKED,
                    icon = { Text("🚒", fontSize = 48.sp) },
                    label = "消防站",
                    onClick = { viewModel.onEvent(MapEvent.SceneClicked(SceneType.FIRE_STATION)) }
                )

                // 学校图标
                SceneIcon(
                    scene = SceneType.SCHOOL,
                    status = state.sceneStatuses[SceneType.SCHOOL] ?: SceneStatus.LOCKED,
                    icon = { Text("🏫", fontSize = 48.sp) },
                    label = "学校",
                    onClick = { viewModel.onEvent(MapEvent.SceneClicked(SceneType.SCHOOL)) }
                )

                // 森林图标
                SceneIcon(
                    scene = SceneType.FOREST,
                    status = state.sceneStatuses[SceneType.FOREST] ?: SceneStatus.LOCKED,
                    icon = { Text("🌲", fontSize = 48.sp) },
                    label = "森林",
                    onClick = { viewModel.onEvent(MapEvent.SceneClicked(SceneType.FOREST)) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 场景说明
            Text(
                text = "点击图标开始冒险",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
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
    }
}

/**
 * 场景图标组件
 *
 * @param scene 场景类型
 * @param status 场景状态
 * @param icon 图标内容
 * @param label 标签
 * @param onClick 点击回调
 */
@Composable
private fun SceneIcon(
    scene: SceneType,
    status: SceneStatus,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    val backgroundColor = when (status) {
        SceneStatus.LOCKED -> Color.Gray
        SceneStatus.UNLOCKED -> Color.White
        SceneStatus.COMPLETED -> Color(0xFFFFD700) // 金色光效
    }
    val isClickable = status != SceneStatus.LOCKED
    val showGlow = status == SceneStatus.COMPLETED

    Box(
        modifier = Modifier
            .size(120.dp)
            .then(
                if (showGlow) {
                    Modifier.shadow(16.dp, CircleShape, ambientColor = Color.Yellow, spotColor = Color.Yellow)
                } else {
                    Modifier.shadow(8.dp, CircleShape)
                }
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (status == SceneStatus.LOCKED) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
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
