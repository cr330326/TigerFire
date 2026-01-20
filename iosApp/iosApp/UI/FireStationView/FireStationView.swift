import SwiftUI
import ComposeApp

/**
 * 消防站场景视图
 *
 * 功能：
 * - 显示 4 个可点击设备（灭火器、消防栓、云梯、水枪）
 * - 点击设备播放教学视频
 * - 视频播放完成后显示徽章奖励
 * - 完成所有设备后解锁学校场景
 */
struct FireStationView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: FireStationViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /// 是否正在播放视频
    @State private var isPlayingVideo = false

    /// 当前播放的视频设备 ID
    @State private var currentPlayingDevice: String?

    /**
     * 初始化
     */
    init() {
        let viewModel = FireStationViewModelImpl()
        _viewModelWrapper = StateObject(wrappedValue: FireStationViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 主内容
            if isPlayingVideo, let deviceId = currentPlayingDevice {
                videoPlayerView(for: deviceId)
            } else {
                devicesGrid
            }

            // 返回按钮
            VStack {
                HStack {
                    Button(action: goBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 30))
                            .foregroundColor(.white)
                            .padding(15)
                            .background(Circle().fill(Color.black.opacity(0.3)))
                    }
                    Spacer()
                }
                .padding()
                Spacer()
            }

            // 徽章奖励动画
            if viewModelWrapper.state.showBadgeAnimation {
                badgeRewardOverlay
            }
        }
        .ignoresSafeArea()
    }

    /**
     * 背景
     */
    private var background: some View {
        // TODO: 使用消防站背景图片
        Color(.systemRed)
            .opacity(0.2)
            .ignoresSafeArea()
    }

    /**
     * 设备网格布局
     */
    private var devicesGrid: some View {
        VStack(spacing: 40) {
            Spacer()

            // 第一行
            HStack(spacing: 40) {
                DeviceButton(
                    device: .extinguisher,
                    isCompleted: viewModelWrapper.state.completedDevices.contains("extinguisher"),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick("extinguisher")
                }

                DeviceButton(
                    device: .hydrant,
                    isCompleted: viewModelWrapper.state.completedDevices.contains("hydrant"),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick("hydrant")
                }
            }

            // 第二行
            HStack(spacing: 40) {
                DeviceButton(
                    device: .ladder,
                    isCompleted: viewModelWrapper.state.completedDevices.contains("ladder"),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick("ladder")
                }

                DeviceButton(
                    device: .hose,
                    isCompleted: viewModelWrapper.state.completedDevices.contains("hose"),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick("hose")
                }
            }

            Spacer().frame(height: 150)
        }
    }

    /**
     * 视频播放器视图
     */
    private func videoPlayerView(for deviceId: String) -> some View {
        SimpleVideoPlayerView(
            videoName: "firestation_\(deviceId)",
            onPlaybackCompleted: {
                handleVideoCompleted(deviceId: deviceId)
            }
        )
        .ignoresSafeArea()
    }

    /**
     * 徽章奖励覆盖层
     */
    private var badgeRewardOverlay: some View {
        VStack(spacing: 20) {
            Spacer()

            // TODO: 显示徽章动画
            Image(systemName: "star.fill")
                .font(.system(size: 80))
                .foregroundColor(.yellow)
                .scaleEffect(viewModelWrapper.state.showBadgeAnimation ? 1.0 : 0.0)
                .animation(.spring(), value: viewModelWrapper.state.showBadgeAnimation)

            Text("获得徽章！")
                .font(.title)
                .foregroundColor(.white)

            Button("继续") {
                handleBadgeAnimationCompleted()
            }
            .buttonStyle(.borderedProminent)
            .tint(.yellow)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black.opacity(0.7))
    }

    /**
     * 处理设备点击
     */
    private func handleDeviceClick(_ deviceId: String) {
        guard !viewModelWrapper.state.completedDevices.contains(deviceId) else { return }

        viewModelWrapper.onDeviceClicked(deviceId: deviceId)

        // 播放视频
        currentPlayingDevice = deviceId
        withAnimation {
            isPlayingVideo = true
        }
    }

    /**
     * 处理视频播放完成
     */
    private func handleVideoCompleted(deviceId: String) {
        withAnimation {
            isPlayingVideo = false
            currentPlayingDevice = nil
        }

        viewModelWrapper.onVideoPlaybackCompleted()
    }

    /**
     * 处理徽章动画完成
     */
    private func handleBadgeAnimationCompleted() {
        viewModelWrapper.onBadgeAnimationCompleted()
    }

    /**
     * 返回地图
     */
    private func goBack() {
        coordinator.goBack()
    }
}

// MARK: - 设备按钮组件

/**
 * 消防设备枚举
 */
enum FireStationDevice {
    case extinguisher  // 灭火器
    case hydrant       // 消防栓
    case ladder        // 云梯
    case hose          // 水枪

    var systemIcon: String {
        switch self {
        case .extinguisher: return "fire.extinguisher.fill"
        case .hydrant: return "drop.fill"
        case .ladder: return "ladder"
        case .hose: return "waterwings"
        }
    }

    var displayName: String {
        switch self {
        case .extinguisher: return "灭火器"
        case .hydrant: return "消防栓"
        case .ladder: return "云梯"
        case .hose: return "水枪"
        }
    }
}

/**
 * 设备按钮组件
 */
struct DeviceButton: View {
    /// 设备类型
    let device: FireStationDevice

    /// 是否已完成
    let isCompleted: Bool

    /// 是否正在播放视频
    let isVideoPlaying: Bool

    /// 点击回调
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                // 背景
                RoundedRectangle(cornerRadius: 20)
                    .fill(backgroundColor)
                    .frame(width: 120, height: 120)

                // 图标
                Image(systemName: device.systemIcon)
                    .font(.system(size: 50))
                    .foregroundColor(iconColor)

                // 完成标记
                if isCompleted {
                    VStack {
                        HStack {
                            Spacer()
                            Image(systemName: "star.fill")
                                .font(.system(size: 25))
                                .foregroundColor(.yellow)
                                .padding(10)
                        }
                        Spacer()
                    }
                }
            }
        }
        .disabled(isVideoPlaying)
        .opacity(isVideoPlaying && !isCompleted ? 0.5 : 1.0)
    }

    private var backgroundColor: Color {
        if isCompleted {
            return .yellow.opacity(0.3)
        } else {
            return .white.opacity(0.9)
        }
    }

    private var iconColor: Color {
        if isCompleted {
            return .yellow
        } else {
            return .red
        }
    }
}

// MARK: - FireStationViewModelWrapper

/**
 * FireStationViewModel 的 SwiftUI 包装器
 */
@MainActor
class FireStationViewModelWrapper: ViewModelWrapper<FireStationViewModel, FireStationState> {
    init(viewModel: FireStationViewModel) {
        let initialState = viewModel.frameState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.frameState)
    }

    func onDeviceClicked(deviceId: String) {
        sendEvent {
            baseViewModel.onDeviceClicked(deviceId: deviceId)
        }
    }

    func onVideoPlaybackCompleted() {
        sendEvent {
            baseViewModel.onVideoPlaybackCompleted()
        }
    }

    func onBadgeAnimationCompleted() {
        sendEvent {
            baseViewModel.onBadgeAnimationCompleted()
        }
    }

    func onBackPressed() {
        sendEvent {
            baseViewModel.onBackPressed()
        }
    }
}

// MARK: - 预览

#Preview {
    FireStationView()
        .environmentObject(AppCoordinator())
}
