import SwiftUI
import ComposeApp

/**
 * 学校场景视图
 *
 * 功能：
 * - 自动播放剧情动画（45 秒）
 * - 播放警报音效与屏幕红光闪烁
 * - 视频播放完毕后显示徽章奖励
 * - 自动解锁森林场景
 */
struct SchoolView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: SchoolViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /// 屏幕边缘红光闪烁动画
    @State private var isAlertPulse = false

    /**
     * 初始化
     */
    init() {
        let viewModel = SchoolViewModelImpl()
        _viewModelWrapper = StateObject(wrappedValue: SchoolViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 警报红光效果
            if viewModelWrapper.state.isVideoPlaying {
                alertPulseOverlay
            }

            // 视频播放器或徽章奖励
            if viewModelWrapper.state.showBadgeAnimation {
                badgeRewardOverlay
            } else {
                videoPlayerView
            }

            // 返回按钮
            if !viewModelWrapper.state.isVideoPlaying {
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
            }
        }
        .ignoresSafeArea()
        .onAppear {
            // 进入场景自动播放
            if !viewModelWrapper.state.isCompleted {
                startVideoPlayback()
            }
        }
    }

    /**
     * 背景
     */
    private var background: some View {
        // TODO: 使用学校背景图片
        Color(.systemBlue)
            .opacity(0.2)
            .ignoresSafeArea()
    }

    /**
     * 警报红光闪烁效果
     */
    private var alertPulseOverlay: some View {
        Color.red
            .opacity(isAlertPulse ? 0.2 : 0.0)
            .ignoresSafeArea()
            .animation(
                Animation.easeInOut(duration: 0.8).repeatForever(autoreverses: true),
                value: isAlertPulse
            )
            .onAppear {
                isAlertPulse = true
            }
    }

    /**
     * 视频播放器
     */
    private var videoPlayerView: some View {
        SimpleVideoPlayerView(
            videoName: "school_emergency",
            onPlaybackCompleted: {
                handleVideoCompleted()
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

            // 小火点赞动画（TODO: 使用 Lottie）
            Image(systemName: "hand.thumbsup.fill")
                .font(.system(size: 80))
                .foregroundColor(.yellow)
                .scaleEffect(viewModelWrapper.state.showBadgeAnimation ? 1.0 : 0.0)
                .animation(.spring(), value: viewModelWrapper.state.showBadgeAnimation)

            Text("你真棒！")
                .font(.title)
                .foregroundColor(.white)

            Text("记住，着火要找大人帮忙！")
                .font(.body)
                .foregroundColor(.white.opacity(0.9))
                .multilineTextAlignment(.center)
                .padding()

            Button("继续") {
                handleBadgeAnimationCompleted()
            }
            .buttonStyle(.borderedProminent)
            .tint(.blue)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black.opacity(0.7))
    }

    /**
     * 启动视频播放
     */
    private func startVideoPlayback() {
        isAlertPulse = true
    }

    /**
     * 处理视频播放完成
     */
    private func handleVideoCompleted() {
        isAlertPulse = false
        viewModelWrapper.onVideoPlaybackCompleted()
    }

    /**
     * 处理徽章动画完成
     */
    private func handleBadgeAnimationCompleted() {
        viewModelWrapper.onBadgeAnimationCompleted()

        // 森林场景自动解锁
        if viewModelWrapper.state.isCompleted {
            // 可以在这里添加解锁提示
        }
    }

    /**
     * 返回地图
     */
    private func goBack() {
        coordinator.goBack()
    }
}

// MARK: - SchoolViewModelWrapper

/**
 * SchoolViewModel 的 SwiftUI 包装器
 */
@MainActor
class SchoolViewModelWrapper: ViewModelWrapper<SchoolViewModel, SchoolState> {
    init(viewModel: SchoolViewModel) {
        let initialState = viewModel.frameState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.frameState)
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
    SchoolView()
        .environmentObject(AppCoordinator())
}
