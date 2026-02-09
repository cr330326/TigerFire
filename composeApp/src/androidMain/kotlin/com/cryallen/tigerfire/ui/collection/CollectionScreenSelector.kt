package com.cryallen.tigerfire.ui.collection

import androidx.compose.runtime.Composable
import com.cryallen.tigerfire.BuildConfig
import com.cryallen.tigerfire.presentation.collection.CollectionViewModel

/**
 * CollectionScreen 选择器
 *
 * 根据 BuildConfig.IS_USE_OPTIMIZED_UI 自动选择使用原始版本或优化版本
 *
 * 使用方法：
 * 1. 在 build.gradle.kts 中设置 buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "true")
 * 2. 将原来的 CollectionScreen() 调用替换为 CollectionScreenSelector()
 *
 * @param viewModel CollectionViewModel 实例
 * @param onNavigateBack 返回回调
 */
@Composable
fun CollectionScreenSelector(
    viewModel: CollectionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    if (BuildConfig.IS_USE_OPTIMIZED_UI) {
        // 使用优化版本
        CollectionScreenOptimized(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    } else {
        // 使用原始版本
        CollectionScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}
