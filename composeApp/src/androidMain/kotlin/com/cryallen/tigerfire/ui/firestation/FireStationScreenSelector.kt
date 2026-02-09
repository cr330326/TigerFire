package com.cryallen.tigerfire.ui.firestation

import androidx.compose.runtime.Composable
import com.cryallen.tigerfire.BuildConfig
import com.cryallen.tigerfire.presentation.firestation.FireStationViewModel

/**
 * FireStationScreen 选择器
 *
 * 根据 BuildConfig.IS_USE_OPTIMIZED_UI 自动选择使用原始版本或优化版本
 *
 * 使用方法：
 * 1. 在 build.gradle.kts 中设置 buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "true")
 * 2. 将原来的 FireStationScreen() 调用替换为 FireStationScreenSelector()
 *
 * @param viewModel FireStationViewModel 实例
 * @param onNavigateBack 返回回调
 */
@Composable
fun FireStationScreenSelector(
    viewModel: FireStationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    if (BuildConfig.IS_USE_OPTIMIZED_UI) {
        // 使用优化版本
        FireStationScreenOptimized(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    } else {
        // 使用原始版本
        FireStationScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}
