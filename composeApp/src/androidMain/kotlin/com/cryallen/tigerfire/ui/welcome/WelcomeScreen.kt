package com.cryallen.tigerfire.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryallen.tigerfire.presentation.welcome.WelcomeEffect
import com.cryallen.tigerfire.presentation.welcome.WelcomeEvent
import com.cryallen.tigerfire.presentation.welcome.WelcomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 欢迎页/启动页 Screen
 *
 * 显示卡车入场动画和小火挥手动画
 * 点击屏幕任意位置进入主地图
 *
 * @param viewModel WelcomeViewModel
 * @param onNavigateToMap 导航到主地图回调
 */
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onNavigateToMap: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 订阅副作用（Effect）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WelcomeEffect.NavigateToMap -> {
                    onNavigateToMap()
                }
                is WelcomeEffect.PlayWaveAnimation -> {
                    // TODO: 在 Task 4.9 实现 LottieAnimationPlayer 后集成
                    // 播放小火挥手动画
                }
            }
        }
    }

    // 全屏可点击
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                viewModel.onEvent(WelcomeEvent.ScreenClicked)
            }
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TODO: Task 4.9 替换为 Lottie 动画
            // 卡车入场动画占位符
            Text(
                text = "🚒",
                fontSize = 120.sp,
                modifier = Modifier.size(200.dp, 200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 小火挥手动画占位符
            if (state.showWaveAnimation) {
                Text(
                    text = "🐯 小火挥手",
                    fontSize = 80.sp,
                    modifier = Modifier.size(160.dp, 160.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "点击屏幕开始冒险！",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            } else {
                // 卡车入场中
                Text(
                    text = "消防车出发中...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 提示文本
            Text(
                text = "TigerFire - 老虎消防车",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
