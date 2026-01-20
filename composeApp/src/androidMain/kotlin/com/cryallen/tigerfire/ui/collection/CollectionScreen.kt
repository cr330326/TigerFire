package com.cryallen.tigerfire.ui.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cryallen.tigerfire.domain.model.SceneType
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
 * 我的收藏场景 Screen
 *
 * 展示所有收集的徽章，按场景分组显示
 * 支持点击徽章查看详情
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

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
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
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E4BC)) // 羊皮纸色背景
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
                        viewModel.onEvent(CollectionEvent.BackToMapClicked)
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
                    text = "我的收藏",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B4513)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "查看收集的徽章",
                    fontSize = 18.sp,
                    color = Color(0xFF8B4513).copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 统计信息
                StatsRow(
                    totalBadgeCount = state.totalBadgeCount,
                    uniqueBadgeCount = state.uniqueBadgeCount,
                    hasCollectedAllBadges = state.hasCollectedAllBadges
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 徽章列表（按场景分组）
                BadgeList(
                    viewModel = viewModel,
                    onBadgeClick = { badge ->
                        selectedBadge = badge
                        viewModel.onEvent(CollectionEvent.BadgeClicked(badge))
                    }
                )
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
    }
}

/**
 * 统计信息行
 */
@Composable
private fun StatsRow(
    totalBadgeCount: Int,
    uniqueBadgeCount: Int,
    hasCollectedAllBadges: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem(
            label = "徽章总数",
            value = totalBadgeCount.toString(),
            color = Color(0xFFE63946)
        )

        StatItem(
            label = "不同种类",
            value = uniqueBadgeCount.toString(),
            color = Color(0xFF457B9D)
        )

        StatItem(
            label = "收集进度",
            value = if (hasCollectedAllBadges) "完成" else "$uniqueBadgeCount/7",
            color = Color(0xFF2A9D8F)
        )
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
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
    val scenes = listOf(
        SceneType.FIRE_STATION to "消防站",
        SceneType.SCHOOL to "学校",
        SceneType.FOREST to "森林"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        scenes.forEach { (sceneType, sceneName) ->
            if (viewModel.hasBadgesForScene(sceneType)) {
                item {
                    SceneBadgeSection(
                        sceneName = sceneName,
                        sceneType = sceneType,
                        viewModel = viewModel,
                        onBadgeClick = onBadgeClick
                    )
                }
            }
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
    viewModel: CollectionViewModel,
    onBadgeClick: (com.cryallen.tigerfire.domain.model.Badge) -> Unit
) {
    val badges = viewModel.getBadgesForScene(sceneType)
    val sceneColor = when (sceneType) {
        SceneType.FIRE_STATION -> Color(0xFFE63946)
        SceneType.SCHOOL -> Color(0xFF457B9D)
        SceneType.FOREST -> Color(0xFF2A9D8F)
    }

    Column {
        // 场景标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sceneName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = sceneColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${badges.size} 枚",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // 徽章卡片横向滚动
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(badges) { badge ->
                BadgeCard(
                    badge = badge,
                    sceneColor = sceneColor,
                    onClick = { onBadgeClick(badge) }
                )
            }
        }
    }
}

/**
 * 徽章卡片
 */
@Composable
private fun BadgeCard(
    badge: com.cryallen.tigerfire.domain.model.Badge,
    sceneColor: Color,
    onClick: () -> Unit
) {
    val emoji = getBadgeEmoji(badge.scene, badge.variant)

    Column(
        modifier = Modifier
            .width(100.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 徽章图标
        Text(
            text = emoji,
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 场景名称
        Text(
            text = badge.scene.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = sceneColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 获得时间
        Text(
            text = formatEarnedTime(badge.earnedAt),
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

/**
 * 徽章详情对话框
 */
@Composable
private fun BadgeDetailDialog(
    badge: com.cryallen.tigerfire.domain.model.Badge,
    onDismiss: () -> Unit
) {
    val emoji = getBadgeEmoji(badge.scene, badge.variant)
    val sceneColor = when (badge.scene) {
        SceneType.FIRE_STATION -> Color(0xFFE63946)
        SceneType.SCHOOL -> Color(0xFF457B9D)
        SceneType.FOREST -> Color(0xFF2A9D8F)
    }

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
                // 徽章图标
                Text(
                    text = emoji,
                    fontSize = 80.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 徽章名称
                Text(
                    text = "${badge.scene.displayName}徽章",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = sceneColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 徽章 ID
                Text(
                    text = "编号: ${badge.id}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 获得时间
                Text(
                    text = "获得时间: ${formatEarnedTime(badge.earnedAt)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 关闭按钮
                Text(
                    text = "关闭",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(sceneColor)
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }
    }
}

/**
 * 获取徽章 Emoji
 */
private fun getBadgeEmoji(scene: SceneType, variant: Int): String {
    return when (scene) {
        SceneType.FIRE_STATION -> "🚒"
        SceneType.SCHOOL -> "🏫"
        SceneType.FOREST -> "🌲"
    }
}

/**
 * 格式化获得时间
 */
private fun formatEarnedTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}
