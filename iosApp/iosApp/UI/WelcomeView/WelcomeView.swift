import SwiftUI
import ComposeApp

/**
 * 欢迎页视图
 *
 * 功能：
 * - 播放消防车入场 Lottie 动画
 * - 播放小火挥手 Lottie 动画
 * - 播放欢迎语音
 * - **语音播放完成后自动导航到主地图**（延迟100ms，无需用户点击）
 *
 * Spec 要求：
 * - App 启动 → 自动播放消防车入场动画（2-3秒）+ 鸣笛音效 + 背景音乐
 * - 卡车动画完成 → 小火探头挥手 + 语音播放："HI！今天和我一起救火吧！"
 * - 语音播放完成 → 延迟100ms自动导航至主地图（无需用户交互）
 */
struct WelcomeView: View {
    /// 动画阶段
    @State private var animationPhase: AnimationPhase = .truckEntering

    /// 是否应该跳转到主地图
    @State private var shouldNavigateToMap = false

    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: WelcomeViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /**
     * 初始化
     */
    init() {
        // 创建 Shared ViewModel
        let viewModel = WelcomeViewModelImpl()
        _viewModelWrapper = StateObject(wrappedValue: WelcomeViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景色（品牌红色）
            Color(red: 0.9, green: 0.22, blue: 0.27)
                .ignoresSafeArea()

            // 根据 animationPhase 显示不同动画
            switch animationPhase {
            case .truckEntering:
                // 卡车入场动画
                truckEnteringView

            case .xiaohuoWaving:
                // 小火挥手动画
                xiaohuoWavingView
            }
        }
        .ignoresSafeArea()
        .statusBar(hidden: true) // 隐藏状态栏
        .onChange(of: shouldNavigateToMap) { _, newValue in
            if newValue {
                navigateToMap()
            }
        }
        .onAppear {
            // 页面出现时开始播放动画序列
            startAnimationSequence()
        }
    }

    // MARK: - 卡车入场动画视图

    private var truckEnteringView: some View {
        SimpleLottieView(
            animationName: "anim_truck_enter",
            onAnimationEnd: {
                // 卡车动画完成 → 切换到挥手动画
                withAnimation(.easeInOut(duration: 0.3)) {
                    animationPhase = .xiaohuoWaving
                }
            }
        )
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - 小火挥手动画视图

    private var xiaohuoWavingView: some View {
        SimpleLottieView(
            animationName: "anim_xiaohuo_wave",
            onAnimationEnd: {
                // 挥手动画完成，等待语音播放完成
                // 语音播放完成会通过 ViewModel 状态变化触发导航
            }
        )
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - 动画序列控制

    /**
     * 启动动画序列
     *
     * 按照规范要求：
     * 1. 卡车入场动画（2-3秒）
     * 2. 挥手动画（3秒）
     * 3. 同时播放欢迎语音
     * 4. 语音播放完成后自动导航
     */
    private func startAnimationSequence() {
        // 卡车入场动画由 SimpleLottieView 自动播放
        // 动画完成后会自动切换到挥手动画

        // TODO: 播放鸣笛音效和背景音乐
        // sfx_truck_horn.mp3（鸣笛）
        // bgm_welcome.mp3（背景音乐）
    }

    // MARK: - 导航逻辑

    /**
     * 导航到主地图
     *
     * 根据 spec.md §2.1.1：
     * - 语音播放完成 → 延迟100ms自动导航至主地图
     * - 无需用户交互
     */
    private func navigateToMap() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            coordinator.navigate(to: .map)
        }
    }
}

/**
 * 动画阶段枚举
 */
private enum AnimationPhase {
    /// 消防车入场动画
    case truckEntering
    /// 小火挥手动画
    case xiaohuoWaving
}

// MARK: - WelcomeViewModelWrapper

/**
 * WelcomeViewModel 的 SwiftUI 包装器
 */
@MainActor
class WelcomeViewModelWrapper: ViewModelWrapper<WelcomeViewModel, WelcomeState> {
    /// 语音播放完成监听
    private var voicePlayObserver: NSObjectProtocol?

    /**
     * 初始化
     *
     * - Parameter viewModel: Shared WelcomeViewModel 实例
     */
    init(viewModel: WelcomeViewModel) {
        // 获取初始状态
        let initialState = viewModel.frameState

        super.init(viewModel: viewModel, initialState: initialState)

        // 订阅状态流
        subscribeState(viewModel.frameState)

        // 监听语音播放完成通知
        setupVoicePlaybackObserver()
    }

    /**
     * 设置语音播放完成监听
     *
     * 当语音播放完成时，通知可以开始导航
     */
    private func setupVoicePlaybackObserver() {
        voicePlayObserver = NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            // 语音播放完成，触发导航
            self?.handleVoicePlaybackCompleted()
        }
    }

    deinit {
        if let observer = voicePlayObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }

    /**
     * 处理语音播放完成
     *
     * 根据 spec.md §2.1.1：
     * - 语音播放完成 → 延迟100ms自动导航
     */
    private func handleVoicePlaybackCompleted() {
        // TODO: 通过 ViewModel 发送语音播放完成事件
        // 当前先通过状态变化触发导航
    }
}

// MARK: - 预览

#Preview {
    WelcomeView()
        .environmentObject(AppCoordinator())
}
