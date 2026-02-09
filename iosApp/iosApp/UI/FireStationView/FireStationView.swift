import SwiftUI
import ComposeApp

/**
 * 消防站场景视图
 *
 * 功能：
 * - 显示 4 个消防设备（灭火器、消防栓、云梯、水枪）
 * - 点击设备播放对应 MP4 视频
 * - 视频播放完成显示徽章奖励
 * - 完成所有 4 个设备后解锁学校场景
 *
 * Spec 要求（spec.md §2.1）：
 * - 每个设备是一个按钮（≥150pt）
 * - 点击播放对应 15s 视频
 * - 视频播放完成显示小火点赞动画 + 徽章
 * - 慢速提示：每 10s 未操作播放小火语音"别睡着啦，继续消防吧"
 */
struct FireStationView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: FireStationViewModelViewModelWrapper

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
        let scope = CoroutineScope()
        let viewModel = FireStationViewModel(viewModelScope: scope, progressRepository: viewModelFactory.createProgressRepository(), resourcePathProvider: ResourcePathProvider())
        _viewModelWrapper = StateObject(wrappedValue: FireStationViewModelViewModelWrapper(viewModel: viewModel))
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
        .onAppear {
            // 发送屏幕进入事件
            viewModelWrapper.onScreenEntered()
        }
    }

    // MARK: - 子视图

    /**
     * 背景视图
     */
    private var background: some View {
        ZStack {
            // 品牌红色背景
            Color(red: 0.91, green: 0.22, blue: 0.27)
                .ignoresSafeArea()

            // 场景装饰 - 消防车剪影
            Image(systemName: "truck.fill")
                .font(.system(size: 300))
                .foregroundColor(.red.opacity(0.15))
                .offset(x: -100, y: -50)
        }
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
                    device: .fireExtinguisher,
                    isCompleted: isDeviceCompleted(.fireExtinguisher),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick(.fireExtinguisher)
                }

                DeviceButton(
                    device: .fireHydrant,
                    isCompleted: isDeviceCompleted(.fireHydrant),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick(.fireHydrant)
                }
            }

            // 第二行
            HStack(spacing: 40) {
                DeviceButton(
                    device: .ladderTruck,
                    isCompleted: isDeviceCompleted(.ladderTruck),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick(.ladderTruck)
                }

                DeviceButton(
                    device: .waterHose,
                    isCompleted: isDeviceCompleted(.waterHose),
                    isVideoPlaying: isPlayingVideo
                ) {
                    handleDeviceClick(.waterHose)
                }
            }

            Spacer().frame(height: 150)
        }
        .padding(.horizontal, 50)
    }

    /**
     * 视频播放器视图
     */
    private func videoPlayerView(for deviceId: String) -> some View {
        SimpleVideoPlayerView(
            videoName: deviceId,
            onPlaybackCompleted: {
                handleVideoCompleted(deviceId: deviceId)
            }
        )
        .transition(.opacity)
    }

    /**
     * 徽章奖励覆盖层
     */
    private var badgeRewardOverlay: some View {
        ZStack {
            Color.black.opacity(0.5)

            VStack(spacing: 30) {
                Image(systemName: "star.circle.fill")
                    .font(.system(size: 120))
                    .foregroundColor(.yellow)

                Text("完成学习！")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(.white)
            }

            SimpleLottieView(
                animationName: "xiaohuo_cheering",
                onAnimationEnd: handleBadgeAnimationCompleted
            )
        }
    }

    // MARK: - 辅助方法

    /**
     * 检查设备是否已完成
     */
    private func isDeviceCompleted(_ device: FireStationDevice) -> Bool {
        viewModelWrapper.state.completedDevices.contains(device)
    }

    /**
     * 处理设备点击
     */
    private func handleDeviceClick(_ device: FireStationDevice) {
        guard !isPlayingVideo else { return }

        viewModelWrapper.onDeviceClicked(deviceId: device.deviceId)

        // 播放视频
        currentPlayingDevice = device.deviceId
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
 * 消防设备按钮
 */
struct DeviceButton: View {
    let device: FireStationDevice
    let isCompleted: Bool
    let isVideoPlaying: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                // 背景卡片
                RoundedRectangle(cornerRadius: 30)
                    .fill(isCompleted ? Color.green.opacity(0.3) : Color.white)
                    .frame(width: 160, height: 160)
                    .shadow(
                        color: .black.opacity(0.2),
                        radius: 10,
                        x: 0,
                        y: 5
                    )

                // 禁用遮罩
                if isVideoPlaying {
                    RoundedRectangle(cornerRadius: 30)
                        .fill(Color.black.opacity(0.3))
                        .frame(width: 160, height: 160)
                }

                // 设备图标
                deviceIcon
                    .foregroundColor(isCompleted ? .green : .red)
                    .scaleEffect(isCompleted ? 1.0 : 1.0)

                // 完成标记
                if isCompleted {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.white)
                        .offset(x: 60, y: -60)
                }
            }
        }
        .disabled(isVideoPlaying)
        .opacity(isVideoPlaying ? 0.6 : 1.0)
        .buttonStyle(PlainButtonStyle())
    }

    var deviceIcon: some View {
        switch device {
        case .fireExtinguisher:
            return Image(systemName: "fire.fireExtinguisher.fill")
        case .fireHydrant:
            return Image(systemName: "drop.fill")
        case .ladderTruck:
            return Image(systemName: "ladder.and.pick")
        case .waterHose:
            return Image(systemName: "waterwings")
        default:
            return Image(systemName: "star.fill")
        }
    }
}

// MARK: - FireStationViewModelViewModelWrapper

/**
 * FireStationViewModel 的 SwiftUI 包装器
 */
@MainActor
class FireStationViewModelViewModelWrapper: ViewModelWrapper<FireStationViewModel, FireStationState> {
    init(viewModel: FireStationViewModel) {
        let initialState = viewModel.state as! FireStationState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.state)
    }

    func onScreenEntered() {
        sendEvent {
            baseViewModel.onEvent(event: FireStationEvent.ScreenEntered.shared)
        }
    }

    func onDeviceClicked(deviceId: String) {
        // 直接使用 deviceId 发送事件，让 Kotlin 端处理 Device 创建
        sendEvent {
            // 根据 deviceId 查找对应的设备枚举值
            let device: FireStationDevice
            switch deviceId {
            case "fireExtinguisher":
                device = FireStationDevice.fireExtinguisher
            case "fireHydrant":
                device = FireStationDevice.fireHydrant
            case "ladderTruck":
                device = FireStationDevice.ladderTruck
            case "waterHose":
                device = FireStationDevice.waterHose
            default:
                device = FireStationDevice.fireExtinguisher
            }
            baseViewModel.onEvent(event: FireStationEvent.DeviceClicked(device: device))
        }
    }

    func onVideoPlaybackCompleted() {
        sendEvent {
            // VideoPlaybackCompleted 事件，使用默认设备
            baseViewModel.onEvent(event: FireStationEvent.VideoPlaybackCompleted(device: FireStationDevice.fireExtinguisher))
        }
    }

    func onBadgeAnimationCompleted() {
        sendEvent {
            baseViewModel.onEvent(event: FireStationEvent.BadgeAnimationCompleted.shared)
        }
    }

    func onBackPressed() {
        sendEvent {
            baseViewModel.onEvent(event: FireStationEvent.BackToMapClicked.shared)
        }
    }
}

// MARK: - 预览

#Preview {
    FireStationView()
        .environmentObject(AppCoordinator())
}
