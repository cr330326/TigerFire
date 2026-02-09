import SwiftUI
import ComposeApp

/**
 * 学校场景视图
 *
 * 功能：
 * - 进入场景时显示警报效果（红光闪烁 + 警报音效）
 * - 显示超大播放按钮（≥150pt），用户点击后播放视频
 * - 播放剧情视频 School_Fire_Safety_Knowledge.mp4（45秒）
 * - 视频播放完毕后显示徽章奖励
 * - 自动解锁森林场景
 *
 * Spec 要求（spec.md §2.1）：
 * - 进入后自动触发警报音效 + 屏幕边缘红光闪烁
 * - 小火语音提示："学校着火啦！快叫消防车！"
 * - 显示超大播放按钮图标（屏幕中央，≥150pt）
 * - 用户点击播放按钮 → 警报音效停止 → 红光停止闪烁 → 播放视频
 * - 视频播放完成 → 弹出小火点赞动画 + 语音 + 徽章 → 解锁森林
 */
struct SchoolView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: SchoolViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /// 屏幕边缘红光闪烁动画
    @State private var isAlertPulse = false

    /// 是否正在播放视频
    @State private var isVideoPlaying = false

    /**
     * 初始化
     */
    init() {
        let scope = CoroutineScope()
        let viewModel = SchoolViewModel(viewModelScope: scope, progressRepository: viewModelFactory.createProgressRepository(), resourcePathProvider: ResourcePathProvider())
        _viewModelWrapper = StateObject(wrappedValue: SchoolViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 警报红光效果（仅在显示播放按钮时显示）
            if viewModelWrapper.state.showAlarmEffect && !isVideoPlaying {
                alertPulseOverlay
            }

            // 主内容
            if viewModelWrapper.state.showBadgeAnimation {
                // 徽章奖励覆盖层
                badgeRewardOverlay
            } else if isVideoPlaying {
                // 视频播放器
                videoPlayerView
            } else {
                // 播放按钮
                playButtonView
            }

            // 返回按钮（仅在未播放视频时显示）
            if !isVideoPlaying && !viewModelWrapper.state.showBadgeAnimation {
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
            // 进入场景时启动警报效果
            startAlarmEffects()
        }
        .onDisappear {
            // 离开场景时停止警报
            stopAlarmEffects()
        }
    }

    // MARK: - 背景视图

    /**
     * 背景
     */
    private var background: some View {
        // TODO: 使用学校背景图片
        ZStack {
            Color(.systemBlue)
                .opacity(0.2)
                .ignoresSafeArea()

            // 学校建筑剪影（可选装饰）
            Image(systemName: "building.columns.fill")
                .font(.system(size: 200))
                .foregroundColor(.white.opacity(0.1))
                .offset(y: 100)
        }
    }

    // MARK: - 警报效果

    /**
     * 警报红光闪烁效果
     *
     * 柔和脉冲动画，非刺眼
     */
    private var alertPulseOverlay: some View {
        Color.red
            .opacity(isAlertPulse ? 0.15 : 0.0)
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
     * 启动警报效果
     */
    private func startAlarmEffects() {
        isAlertPulse = true

        // TODO: 播放警报音效（循环播放）
        // sfx_school_alarm.mp3

        // TODO: 播放语音提示："学校着火啦！快叫消防车！"
        // voice_school_fire_alert.mp3
    }

    /**
     * 停止警报效果
     */
    private func stopAlarmEffects() {
        isAlertPulse = false

        // TODO: 停止警报音效
    }

    // MARK: - 播放按钮视图

    /**
     * 超大播放按钮视图
     *
     * Spec 要求：
     * - 屏幕中央显示
     * - 尺寸 ≥150pt
     * - 红色渐变圆形 + 播放三角形图标
     */
    private var playButtonView: some View {
        VStack(spacing: 30) {
            Spacer()

            // 提示文本
            Text("学校着火啦！")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.white)

            // 超大播放按钮
            Button(action: handlePlayButtonClicked) {
                ZStack {
                    // 红色渐变圆形背景
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color.red, Color(red: 0.8, green: 0.2, blue: 0.2)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 160, height: 160)
                        .shadow(color: .red.opacity(0.5), radius: 20, x: 0, y: 10)

                    // 播放三角形图标
                    Image(systemName: "play.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.white)
                        .offset(x: 8) // 视觉修正，让三角形居中
                }
            }
            .scaleEffect(isAlertPulse ? 1.05 : 1.0) // 配合警报脉冲呼吸
            .animation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true), value: isAlertPulse)

            // 副标题
            Text("点击播放视频")
                .font(.body)
                .foregroundColor(.white.opacity(0.9))

            Spacer()
            Spacer().frame(height: 100)
        }
    }

    /**
     * 处理播放按钮点击
     */
    private func handlePlayButtonClicked() {
        // 停止警报效果
        stopAlarmEffects()

        // 发送事件到 ViewModel
        viewModelWrapper.onPlayButtonClicked()

        // 开始播放视频
        withAnimation {
            isVideoPlaying = true
        }
    }

    // MARK: - 视频播放器视图

    /**
     * 视频播放器
     */
    private var videoPlayerView: some View {
        SimpleVideoPlayerView(
            videoName: "School_Fire_Safety_Knowledge",
            onPlaybackCompleted: {
                handleVideoCompleted()
            }
        )
        .ignoresSafeArea()
    }

    /**
     * 处理视频播放完成
     */
    private func handleVideoCompleted() {
        withAnimation {
            isVideoPlaying = false
        }

        viewModelWrapper.onVideoPlaybackCompleted()
    }

    // MARK: - 徽章奖励视图

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
                .animation(.spring(response: 0.6, dampingFraction: 0.7), value: viewModelWrapper.state.showBadgeAnimation)

            Text("你真棒！")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.white)

            Text("记住，着火要找大人帮忙！")
                .font(.body)
                .foregroundColor(.white.opacity(0.9))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 20)

            Button("继续") {
                handleBadgeAnimationCompleted()
            }
            .buttonStyle(.borderedProminent)
            .tint(.blue)
            .font(.title3)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black.opacity(0.7))
    }

    /**
     * 处理徽章动画完成
     */
    private func handleBadgeAnimationCompleted() {
        viewModelWrapper.onBadgeAnimationCompleted()

        // 森林场景自动解锁
        if viewModelWrapper.state.isCompleted {
            // TODO: 可以添加解锁提示
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
        let initialState = viewModel.state as! SchoolState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.state)
    }

    /**
     * 播放按钮点击事件
     */
    func onPlayButtonClicked() {
        sendEvent {
            baseViewModel.onEvent(event: SchoolEvent.PlayButtonClicked.shared)
        }
    }

    func onVideoPlaybackCompleted() {
        sendEvent {
            baseViewModel.onEvent(event: SchoolEvent.VideoPlaybackCompleted.shared)
        }
    }

    func onBadgeAnimationCompleted() {
        sendEvent {
            baseViewModel.onEvent(event: SchoolEvent.BadgeAnimationCompleted.shared)
        }
    }

    func onBackPressed() {
        sendEvent {
            baseViewModel.onEvent(event: SchoolEvent.BackToMapClicked.shared)
        }
    }
}

// MARK: - 预览

#Preview {
    SchoolView()
        .environmentObject(AppCoordinator())
}
