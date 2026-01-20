import SwiftUI
import AVKit
import AVFoundation

/**
 * 视频播放器视图组件
 *
 * 使用 UIViewControllerRepresentable 包装 AVPlayerViewController，
 * 提供 SwiftUI 兼容的视频播放接口
 */
struct VideoPlayerView: UIViewControllerRepresentable {
    /// 视频文件名（不含扩展名）
    let videoName: String

    /// 视频播放完成回调
    let onPlaybackCompleted: () -> Void

    /// 是否应该从头播放（切后台恢复时）
    var shouldRestartFromBeginning: Bool = true

    /**
     * 创建 UIViewControllerRepresentable 的上下文
     */
    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()

        // 从 Bundle 加载视频
        if let url = Bundle.main.url(forResource: videoName, withExtension: "mp4") {
            let player = AVPlayer(url: url)
            controller.player = player

            // 监听播放完成
            NotificationCenter.default.addObserver(
                forName: .AVPlayerItemDidPlayToEndTime,
                object: player.currentItem,
                queue: .main
            ) { [weak player] _ in
                onPlaybackCompleted()
                player?.seek(to: .zero)
            }

            // 开始播放
            player.play()
        }

        // 隐藏播放控制（如果是自动播放场景）
        controller.showsPlaybackControls = false
        controller.videoGravity = .resizeAspect

        return controller
    }

    /**
     * 更新 UIViewController
     */
    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {
        // 切后台恢复时从头播放
        if shouldRestartFromBeginning {
            let player = uiViewController.player
            if let currentItem = player?.currentItem,
                  currentItem.currentTime().seconds > 1.0 { // 稍微播放过才重置
                player?.seek(to: .zero)
                player?.play()
            }
        }
    }
}

/**
 * 简化版视频播放器（带降级处理）
 *
 * 如果视频加载失败，显示降级 UI
 */
struct SimpleVideoPlayerView: View {
    /// 视频文件名（不含扩展名）
    let videoName: String

    /// 视频播放完成回调
    let onPlaybackCompleted: () -> Void

    /// 是否显示降级 UI
    @State private var shouldShowFallback = false

    /// 加载超时时间（秒）
    var timeout: Double = 5.0

    var body: some View {
        Group {
            if shouldShowFallback {
                fallbackView
            } else {
                VideoPlayerView(
                    videoName: videoName,
                    onPlaybackCompleted: onPlaybackCompleted
                )
            }
        }
        .onAppear {
            startTimeout()
        }
    }

    /**
     * 降级视图
     *
     * 当视频加载失败时显示
     */
    private var fallbackView: some View {
        VStack(spacing: 20) {
            Image(systemName: "video.slash")
                .font(.system(size: 60))
                .foregroundColor(.gray)

            Text("视频暂时无法播放")
                .font(.title3)
                .foregroundColor(.secondary)

            Button("继续") {
                onPlaybackCompleted()
            }
            .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }

    /**
     * 启动超时检测
     *
     * 如果视频在指定时间内未开始播放，显示降级 UI
     */
    private func startTimeout() {
        Task {
            try? await Task.sleep(nanoseconds: UInt64(timeout * 1_000_000_000))
            if !Task.isCancelled {
                await MainActor.run {
                    shouldShowFallback = true
                }
            }
        }
    }
}

/**
 * 视频播放状态枚举
 */
enum VideoPlayerState {
    /// 准备中
    case loading
    /// 播放中
    case playing
    /// 暂停
    case paused
    /// 已完成
    case completed
    /// 错误
    case error(Error)
}

/**
 * 带状态管理的视频播放器
 *
 * 提供更细粒度的播放状态控制
 */
struct StatefulVideoPlayerView: UIViewControllerRepresentable {
    /// 视频文件名（不含扩展名）
    let videoName: String

    /// 播放状态绑定
    @Binding var playbackState: VideoPlayerState

    /// 视频播放完成回调
    let onPlaybackCompleted: () -> Void

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()

        if let url = Bundle.main.url(forResource: videoName, withExtension: "mp4") {
            let player = AVPlayer(url: url)
            controller.player = player

            // 监听播放完成
            NotificationCenter.default.addObserver(
                forName: .AVPlayerItemDidPlayToEndTime,
                object: player.currentItem,
                queue: .main
            ) { [weak player] _ in
                playbackState = .completed
                onPlaybackCompleted()
                player?.seek(to: .zero)
            }

            playbackState = .playing
            player.play()
        } else {
            playbackState = .error(NSError(domain: "VideoPlayer", code: -1, userInfo: [
                NSLocalizedDescriptionKey: "视频文件未找到: \(videoName).mp4"
            ]))
        }

        controller.showsPlaybackControls = false
        controller.videoGravity = .resizeAspect

        return controller
    }

    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {
        guard let player = uiViewController.player else { return }

        switch playbackState {
        case .playing:
            if player.rate == 0 {
                player.play()
            }
        case .paused:
            player.pause()
        case .completed:
            player.seek(to: .zero)
        default:
            break
        }
    }
}
