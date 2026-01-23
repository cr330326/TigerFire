package com.cryallen.tigerfire.component

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 视频播放器组件
 *
 * 使用 ExoPlayer (Media3) 播放 MP4 视频文件
 * 使用 PlayerView (Media3 官方组件) 进行渲染，确保视频画面正常显示
 *
 * @param videoPath 视频资源路径（如 "videos/firestation_extinguisher.mp4"）
 * @param modifier 修饰符
 * @param onPlaybackCompleted 播放完成回调
 * @param onPlaybackError 播放错误回调
 * @param autoPlay 是否自动播放，默认为 true
 * @param loopMode 是否循环播放，默认为 false
 */
@Composable
fun VideoPlayer(
    videoPath: String,
    modifier: Modifier = Modifier,
    onPlaybackCompleted: () -> Unit = {},
    onPlaybackError: (Exception) -> Unit = {},
    autoPlay: Boolean = true,
    showControls: Boolean = false,
    loopMode: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying by remember { mutableStateOf(autoPlay) }
    var videoSize by remember { mutableStateOf(VideoSize.UNKNOWN) }

    suspend fun copyAssetToCache(assetPath: String): File = withContext(Dispatchers.IO) {
        val cacheFile = File(context.cacheDir, assetPath.substringAfterLast("/"))
        try {
            context.assets.open(assetPath).use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            if (assetPath.startsWith("/")) {
                File(assetPath)
            } else {
                cacheFile
            }
        }
        cacheFile
    }

    // 创建 ExoPlayer - 使用默认配置
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        if (!loopMode) {
                            onPlaybackCompleted()
                        }
                    }
                }

                override fun onPlayerErrorChanged(error: androidx.media3.common.PlaybackException?) {
                    onPlaybackError(Exception(error?.message ?: "Unknown error"))
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onVideoSizeChanged(size: VideoSize) {
                    videoSize = size
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    isPlaying = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (autoPlay) {
                        exoPlayer.seekTo(0)
                        exoPlayer.play()
                        isPlaying = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // PlayerView - 使用 Media3 官方组件确保视频正常渲染
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                // 隐藏播放控制器（如果需要）
                useController = showControls
                // 将 ExoPlayer 绑定到 PlayerView
                player = exoPlayer
            }
        },
        update = { playerView ->
            // 确保 PlayerView 与 ExoPlayer 保持绑定
            if (playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
        },
        modifier = modifier
    )

    // 加载并播放视频
    LaunchedEffect(videoPath, exoPlayer) {
        delay(500)

        val videoUri = if (videoPath.startsWith("file:///")) {
            Uri.parse(videoPath)
        } else if (videoPath.startsWith("/")) {
            Uri.fromFile(java.io.File(videoPath))
        } else {
            val cacheFile = copyAssetToCache(videoPath)
            Uri.fromFile(cacheFile)
        }

        val mediaItem = MediaItem.fromUri(videoUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = autoPlay
        isPlaying = autoPlay
    }
}

/**
 * 简化版视频播放器
 *
 * 用于快速播放单次视频，自动播放且无控制按钮
 *
 * @param videoPath 视频资源路径
 * @param modifier 修饰符
 * @param onCompleted 播放完成回调
 */
@Composable
fun VideoPlayerSimple(
    videoPath: String,
    modifier: Modifier = Modifier,
    onCompleted: () -> Unit = {}
) {
    VideoPlayer(
        videoPath = videoPath,
        modifier = modifier,
        onPlaybackCompleted = onCompleted,
        autoPlay = true,
        showControls = false
    )
}

/**
 * 视频播放器状态
 *
 * 用于跟踪视频播放的各种状态
 */
data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 带状态管理的视频播放器
 *
 * 提供完整的播放状态控制，适用于需要自定义 UI 的场景
 *
 * @param videoPath 视频资源路径
 * @param modifier 修饰符
 * @param state 播放状态
 * @param onStateChanged 状态变化回调
 * @param onPlaybackCompleted 播放完成回调
 */
@Composable
fun VideoPlayerWithState(
    videoPath: String,
    modifier: Modifier = Modifier,
    state: VideoPlayerState = remember { VideoPlayerState() },
    onStateChanged: (VideoPlayerState) -> Unit = {},
    onPlaybackCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentState by remember { mutableStateOf(state) }

    suspend fun copyAssetToCache(assetPath: String): File = withContext(Dispatchers.IO) {
        val cacheFile = File(context.cacheDir, assetPath.substringAfterLast("/"))
        try {
            context.assets.open(assetPath).use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            if (assetPath.startsWith("/")) {
                File(assetPath)
            } else {
                cacheFile
            }
        }
        cacheFile
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val newState = when (playbackState) {
                        Player.STATE_IDLE -> currentState.copy(
                            isPlaying = false,
                            isBuffering = false
                        )
                        Player.STATE_BUFFERING -> currentState.copy(
                            isBuffering = true
                        )
                        Player.STATE_READY -> currentState.copy(
                            isBuffering = false,
                            duration = duration
                        )
                        Player.STATE_ENDED -> currentState.copy(
                            isPlaying = false,
                            currentPosition = duration
                        )
                        else -> currentState
                    }
                    currentState = newState
                    onStateChanged(newState)

                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackCompleted()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    currentState = currentState.copy(isPlaying = isPlaying)
                    onStateChanged(currentState)
                }

                override fun onPlayerErrorChanged(error: androidx.media3.common.PlaybackException?) {
                    val errorState = currentState.copy(
                        hasError = true,
                        errorMessage = error?.message ?: "Unknown error"
                    )
                    currentState = errorState
                    onStateChanged(errorState)
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.seekTo(0)
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
        update = { playerView ->
            if (playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
        },
        modifier = modifier
    )

    LaunchedEffect(videoPath, exoPlayer) {
        delay(500)

        val videoUri = if (videoPath.startsWith("file:///")) {
            Uri.parse(videoPath)
        } else if (videoPath.startsWith("/")) {
            Uri.fromFile(java.io.File(videoPath))
        } else {
            val cacheFile = copyAssetToCache(videoPath)
            Uri.fromFile(cacheFile)
        }

        val mediaItem = MediaItem.fromUri(videoUri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }
}

/**
 * 视频播放器加载失败占位符
 *
 * 当视频加载失败时显示
 *
 * @param modifier 修饰符
 * @param errorMessage 错误消息
 */
@Composable
fun VideoPlayerErrorPlaceholder(
    modifier: Modifier = Modifier,
    errorMessage: String = "视频加载失败"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = errorMessage,
            color = Color.White
        )
    }
}
