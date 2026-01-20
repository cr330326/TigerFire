package com.cryallen.tigerfire.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

/**
 * Lottie 动画播放器组件
 *
 * 支持从 assets 文件加载 Lottie JSON 动画并播放
 * 提供播放完成回调
 *
 * @param animationRes 动画资源路径（相对于 assets 目录）
 * @param modifier 修饰符
 * @param iterations 播放迭代次数，默认为 1
 * @param onAnimationEnd 动画播放完成回调
 * @param isPlaying 是否播放动画，默认为 true
 */
@Composable
fun LottieAnimationPlayer(
    animationRes: String,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    colorFilter: ColorFilter? = null,
    onAnimationEnd: () -> Unit = {},
    onLoadingFailed: () -> Unit = {},
    isPlaying: Boolean = true
) {
    // 加载 Lottie 动画资源（返回 State<LottieComposition?>）
    val composition = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(animationRes)
    ).value

    // 用于跟踪播放进度
    var isPlayingInternal by remember(isPlaying) { mutableStateOf(isPlaying) }

    // 渲染动画
    if (composition != null) {
        LottieAnimation(
            composition = composition,
            modifier = modifier,
            isPlaying = isPlayingInternal,
            iterations = iterations,
        )
    }

    // 监听动画完成
    LaunchedEffect(isPlayingInternal) {
        if (isPlayingInternal && iterations != LottieConstants.IterateForever) {
            delay(3000)
            isPlayingInternal = false
            onAnimationEnd()
        }
    }
}

/**
 * 简化版 Lottie 动画播放器
 */
@Composable
fun LottieAnimationPlayerSimple(
    animationRes: String,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val composition = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(animationRes)
    ).value

    var isPlaying by remember { mutableStateOf(true) }

    if (composition != null) {
        LottieAnimation(
            composition = composition,
            modifier = modifier,
            isPlaying = isPlaying,
            iterations = 1,
        )
    }

    LaunchedEffect(Unit) {
        delay(3000)
        isPlaying = false
        onAnimationEnd()
    }
}

/**
 * 循环播放的 Lottie 动画
 */
@Composable
fun LottieAnimationLoop(
    animationRes: String,
    modifier: Modifier = Modifier
) {
    val composition = rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(animationRes)
    ).value

    if (composition != null) {
        LottieAnimation(
            composition = composition,
            modifier = modifier,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
        )
    }
}

/**
 * Lottie 动画配置数据类
 */
data class LottieAnimationConfig(
    val assetPath: String,
    val iterations: Int = 1,
    val colorFilter: ColorFilter? = null,
    val autoPlay: Boolean = true
)

/**
 * 配置化的 Lottie 动画播放器
 */
@Composable
fun LottieAnimationPlayer(
    config: LottieAnimationConfig,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    LottieAnimationPlayer(
        animationRes = config.assetPath,
        modifier = modifier,
        iterations = config.iterations,
        colorFilter = config.colorFilter,
        onAnimationEnd = onAnimationEnd,
        isPlaying = config.autoPlay
    )
}
