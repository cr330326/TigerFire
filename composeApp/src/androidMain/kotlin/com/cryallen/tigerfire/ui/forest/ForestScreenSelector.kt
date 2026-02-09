package com.cryallen.tigerfire.ui.forest

import androidx.compose.runtime.Composable
import com.cryallen.tigerfire.BuildConfig
import com.cryallen.tigerfire.presentation.forest.ForestViewModel

@Composable
fun ForestScreenSelector(
    viewModel: ForestViewModel,
    onNavigateBack: () -> Unit = {}
) {
    if (BuildConfig.IS_USE_OPTIMIZED_UI) {
        ForestScreenOptimized(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    } else {
        ForestScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}
