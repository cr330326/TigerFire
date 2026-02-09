package com.cryallen.tigerfire.ui.map

import androidx.compose.runtime.Composable
import com.cryallen.tigerfire.BuildConfig
import com.cryallen.tigerfire.presentation.common.AppSessionManager
import com.cryallen.tigerfire.presentation.map.MapViewModel

/**
 * MapScreen 选择器
 *
 * 根据 BuildConfig.IS_USE_OPTIMIZED_UI 自动选择使用原始版本或优化版本
 *
 * 使用方法：
 * 1. 在 build.gradle.kts 中设置 buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "true")
 * 2. 将原来的 MapScreen() 调用替换为 MapScreenSelector()
 *
 * @param viewModel MapViewModel 实例
 * @param onNavigateToWelcome 导航到欢迎页回调
 * @param onNavigateToFireStation 导航到消防站回调
 * @param onNavigateToSchool 导航到学校回调
 * @param onNavigateToForest 导航到森林回调
 * @param onNavigateToCollection 导航到收藏页回调
 * @param onNavigateToParent 导航到家长模式回调
 * @param appSessionManager 应用会话管理器（可选）
 */
@Composable
fun MapScreenSelector(
    viewModel: MapViewModel,
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToFireStation: () -> Unit = {},
    onNavigateToSchool: () -> Unit = {},
    onNavigateToForest: () -> Unit = {},
    onNavigateToCollection: () -> Unit = {},
    onNavigateToParent: () -> Unit = {},
    appSessionManager: AppSessionManager? = null
) {
    if (BuildConfig.IS_USE_OPTIMIZED_UI) {
        // 使用优化版本
        MapScreenOptimized(
            viewModel = viewModel,
            onNavigateToWelcome = onNavigateToWelcome,
            onNavigateToFireStation = onNavigateToFireStation,
            onNavigateToSchool = onNavigateToSchool,
            onNavigateToForest = onNavigateToForest,
            onNavigateToCollection = onNavigateToCollection,
            onNavigateToParent = onNavigateToParent,
            appSessionManager = appSessionManager
        )
    } else {
        // 使用原始版本
        MapScreen(
            viewModel = viewModel,
            onNavigateToWelcome = onNavigateToWelcome,
            onNavigateToFireStation = onNavigateToFireStation,
            onNavigateToSchool = onNavigateToSchool,
            onNavigateToForest = onNavigateToForest,
            onNavigateToCollection = onNavigateToCollection,
            onNavigateToParent = onNavigateToParent,
            appSessionManager = appSessionManager
        )
    }
}
