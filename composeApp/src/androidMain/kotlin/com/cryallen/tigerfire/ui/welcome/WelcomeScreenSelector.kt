package com.cryallen.tigerfire.ui.welcome

import androidx.compose.runtime.Composable
import com.cryallen.tigerfire.BuildConfig
import com.cryallen.tigerfire.presentation.welcome.WelcomeViewModel

/**
 * WelcomeScreen 选择器
 *
 * 根据 BuildConfig.IS_USE_OPTIMIZED_UI 自动选择使用原始版本或优化版本
 *
 * 使用方法：
 * 1. 在 build.gradle.kts 中设置 buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "true")
 * 2. 将原来的 WelcomeScreen() 调用替换为 WelcomeScreenSelector()
 *
 * @param viewModel WelcomeViewModel 实例
 * @param onNavigateToMap 导航到地图回调
 */
@Composable
fun WelcomeScreenSelector(
    viewModel: WelcomeViewModel,
    onNavigateToMap: () -> Unit
) {
    if (BuildConfig.IS_USE_OPTIMIZED_UI) {
        // 使用优化版本
        WelcomeScreenOptimized(
            viewModel = viewModel,
            onNavigateToMap = onNavigateToMap
        )
    } else {
        // 使用原始版本
        WelcomeScreen(
            viewModel = viewModel,
            onNavigateToMap = onNavigateToMap
        )
    }
}
