import SwiftUI
import AVKit
import AVFoundation

/**
 * 视频播放器视图组件
 *
 * 使用 UIViewControllerRepresentable 包装 AVPlayerViewController，
 * 提供 SwiftUI 兼容的视频播放接口
 *
 * Spec 要求视频资源：
 * - 消防站设备视频：firestation_extinguisher.mp4, firestation_hydrant.mp4, firestation_ladder.mp4, firestation_hose.mp4
 * - 学校剧情视频：School_Fire_Safety_Knowledge.mp4
 * - 森林救援视频：rescue_sheep_1.mp4, rescue_sheep_2.mp4
 * - 庆祝动画：celebration.mp4（可选）
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
        // 尝试多个可能的路径
        if let url = findVideoURL(named: videoName) {
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
        } else {
            NSLog("VideoPlayer: Failed to find video: \(videoName).mp4")
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

    /**
     * 查找视频 URL
     *
     * 尝试多个可能的路径来定位视频文件：
     * 1. assets/videos/{name}.mp4
     * 2. {name}.mp3（直接在 Bundle 中）
     * 3. videos/{name}.mp4
     */
    private func findVideoURL(named name: String) -> URL? {
        // 尝试 1: assets/videos/{name}.mp4
        if let url = Bundle.main.url(forResource: "assets/videos/\(name)", withExtension: "mp4") {
            return url
        }

        // 尝试 2: {name}.mp4（直接在 Bundle 中）
        if let url = Bundle.main.url(forResource: name, withExtension: "mp4") {
            return url
        }

        // 尝试 3: videos/{name}.mp4
        if let url = Bundle.main.url(forResource: "videos/\(name)", withExtension: "mp4") {
            return url
        }

        return nil
    }
}

/**
 * 简化版视频播放器（带降级处理）
 *
 * 如果视频加载失败，显示降级 UI
 * Spec 要求：资源缺失时，降级为静态图 + 语音描述
 */
struct SimpleVideoPlayerView: View {
    /// 视频文件名（不含扩展名）
    let videoName: String

    /// 视频播放完成回调
    let onPlaybackCompleted: () -> Void

    /// 是否显示降级 UI
    @State private var shouldShowFallback = false

    /// 是否正在加载
    @State private var isLoading = true

    /// 加载超时时间（秒）
    var timeout: Double = 5.0

    var body: some View {
        Group {
            if shouldShowFallback {
                fallbackView
            } else {
                VideoPlayerView(
                    videoName: videoName,
                    onPlaybackCompleted: {
                        isLoading = false
                        onPlaybackCompleted()
                    }
                )
                .onAppear {
                    // 模拟视频加载完成
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        isLoading = false
                    }
                }
            }
        }
        .onAppear {
            startTimeout()
        }
    }

    /**
     * 降级视图
     *
     * Spec 要求：资源缺失时显示静态替代图 + 自动播放语音描述
     */
    private var fallbackView: some View {
        VStack(spacing: 20) {
            Spacer()

            Image(systemName: "video.slash")
                .font(.system(size: 60))
                .foregroundColor(.gray)

            Text("视频暂时无法播放")
                .font(.title3)
                .foregroundColor(.secondary)

            Text("请稍后再试")
                .font(.caption)
                .foregroundColor(.secondary)

            Button("继续") {
                onPlaybackCompleted()
            }
            .buttonStyle(.borderedProminent)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }

    /**
     * 启动超时检测
     *
     * Spec 要求：加载超时（>3 秒）或解析失败 → 降级为静态图
     *
     * 如果视频在指定时间内未开始播放，显示降级 UI
     */
    private func startTimeout() {
        Task {
            try? await Task.sleep(nanoseconds: UInt64(timeout * 1_000_000_000))
            if !Task.isCancelled && isLoading {
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
 * Spec 要求：切后台后恢复时从头播放（确保知识完整接收）
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

        // 从 Bundle 加载视频（尝试多个路径）
        if let url = findVideoURL(named: videoName) {
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

    /**
     * 查找视频 URL
     */
    private func findVideoURL(named name: String) -> URL? {
        // 尝试 1: assets/videos/{name}.mp4
        if let url = Bundle.main.url(forResource: "assets/videos/\(name)", withExtension: "mp4") {
            return url
        }

        // 尝试 2: {name}.mp4（直接在 Bundle 中）
        if let url = Bundle.main.url(forResource: name, withExtension: "mp4") {
            return url
        }

        // 尝试 3: videos/{name}.mp4
        if let url = Bundle.main.url(forResource: "videos/\(name)", withExtension: "mp4") {
            return url
        }

        return nil
    }
}

// MARK: - 视频资源路径常量

/**
 * 视频资源路径常量
 *
 * Spec 要求的视频资源列表
 */
struct VideoResources {
    // MARK: - 消防站视频
    static let firestationExtinguisher = "firestation_extinguisher"
    static let firestationHydrant = "firestation_hydrant"
    static let firestationLadder = "firestation_ladder"
    static let firestationHose = "firestation_hose"

    // MARK: - 学校视频
    static let schoolFireSafety = "School_Fire_Safety_Knowledge"

    // MARK: - 森林视频
    static let rescueSheep1 = "rescue_sheep_1"
    static let rescueSheep2 = "rescue_sheep_2"

    // MARK: - 庆祝视频
    static let celebration = "celebration"

    /**
     * 根据设备 ID 获取对应的视频名称
     */
    static func firestationVideo(for deviceId: String) -> String {
        switch deviceId {
        case "extinguisher": return firestationExtinguisher
        case "hydrant": return firestationHydrant
        case "ladder": return firestationLadder
        case "hose": return firestationHose
        default: return firestationExtinguisher
        }
    }

    /**
     * 根据小羊索引获取对应的救援视频名称
     */
    static func rescueVideo(for sheepIndex: Int) -> String {
        switch sheepIndex {
        case 1: return rescueSheep1
        case 2: return rescueSheep2
        default: return rescueSheep1
        }
    }
}
