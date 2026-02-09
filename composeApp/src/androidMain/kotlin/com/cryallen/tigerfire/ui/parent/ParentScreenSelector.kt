package com.cryallen.tigerfire.ui.parent

import androidx.compose.runtime.Composable
import com.cryallen.tigerfire.BuildConfig
import com.cryallen.tigerfire.presentation.parent.ParentViewModel

@Composable
fun ParentScreenSelector(
    viewModel: ParentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    if (BuildConfig.IS_USE_OPTIMIZED_UI) {
        ParentScreenOptimized(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    } else {
        ParentScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}
