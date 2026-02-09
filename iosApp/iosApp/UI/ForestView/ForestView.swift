import SwiftUI
import ComposeApp

/**
 * 森林场景视图
 *
 * 功能：
 * - 显示火灾森林背景
 * - 显示直升机（左侧，≥150pt，带螺旋桨飞行动画）
 * - 显示 2 只小羊（右侧，被火苗包围，做求救动画）
 * - **点击小羊**触发直升机自动飞行到目标上方
 * - 显示"放下梯子"按钮（圆形，≥100pt）
 * - 播放救援片段并弹出徽章
 *
 * Spec 要求（plan.md §4.4）：
 * - 点击小羊 → 直升机缓慢飞行到小羊上方（约 1-1.5 秒）
 * - 到达后显示放下梯子按钮
 * - 点击按钮 → 播放救援视频
 * - 两只小羊都救出 → 庆祝动画 + 语音总结
 *
 * 交互设计变更：
 * - **从拖拽改为点击**，确保低龄儿童操作可控性
 * - 直升机飞行由动画自动控制
 */
struct ForestView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: ForestViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /// 直升机当前位置（用于动画）
    @State private var helicopterPosition: CGPoint = .zero

    /// 直升机飞行动画
    @State private var isHelicopterFlying = false

    /// 本地状态：已救援的小羊数量
    @State private var rescuedSheepCount = 0

    /// 本地状态：目标小羊索引
    @State private var targetSheepIndex = 0

    /// 本地状态：是否显示徽章动画
    @State private var showBadgeAnimation = false

    /// 本地状态：是否全部完成
    @State private var isAllCompleted = false

    /// 本地状态：当前救援视频ID
    @State private var currentRescueVideo: String?

    /// 本地状态：是否显示梯子按钮
    @State private var showLadderButton = false

    /// 本地状态：直升机位置（从 ViewModel 获取）
    @State private var viewModelHelicopterPos: CGPoint = .zero

    /// 小羊位置（屏幕坐标）
    private let sheepPositions: [CGPoint] = [
        CGPoint(x: 280, y: 200),  // 小羊1位置
        CGPoint(x: 320, y: 400)   // 小羊2位置
    ]

    /**
     * 初始化
     */
    init() {
        let viewModel = ForestViewModel(
            viewModelScope: CoroutineScope(),
            progressRepository: viewModelFactory.createProgressRepository(),
            resourcePathProvider: ResourcePathProvider()
        )
        _viewModelWrapper = StateObject(wrappedValue: ForestViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 火苗效果（背景装饰）
            fireEffectsOverlay

            // 小羊（未救援的）
            ForEach(0..<sheepPositions.count, id: \.self) { index in
                if index >= rescuedSheepCount {
                    SheepButton(
                        sheepIndex: index,
                        position: sheepPositions[index],
                        isTarget: targetSheepIndex == index,
                        isHelicopterFlying: isHelicopterFlying
                    ) {
                        handleSheepClicked(index: index)
                    }
                }
            }

            // 直升机（自动飞行）
            helicopterView
                .offset(
                    x: helicopterPosition.x + viewModelHelicopterPos.x,
                    y: helicopterPosition.y + viewModelHelicopterPos.y
                )

            // "放下梯子"按钮
            if showLadderButton {
                ladderButton
            }

            // 视频播放器
            if let videoId = currentRescueVideo {
                videoPlayerView(for: videoId)
            }

            // 徽章奖励
            if showBadgeAnimation {
                badgeRewardOverlay
            }

            // 庆祝动画（全部完成）
            if isAllCompleted && !showBadgeAnimation {
                celebrationOverlay
            }

            // 返回按钮
            if !isHelicopterFlying &&
               currentRescueVideo == nil &&
               !showBadgeAnimation &&
               !isAllCompleted {
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
        .onChange(of: targetSheepIndex) { _, targetIndex in
            if targetIndex >= 0 {
                flyHelicopterToSheep(sheepIndex: targetIndex)
            }
        }
    }

    // MARK: - 背景视图

    /**
     * 背景
     */
    private var background: some View {
        // TODO: 使用森林火灾背景图片
        ZStack {
            // 森林背景色
            Color(red: 0.2, green: 0.4, blue: 0.2)
                .ignoresSafeArea()

            // 树木剪影（装饰）
            HStack(spacing: 60) {
                ForEach(0..<5) { _ in
                    Image(systemName: "tree.fill")
                        .font(.system(size: 120))
                        .foregroundColor(.black.opacity(0.2))
                }
            }
            .offset(y: 150)
        }
    }

    /**
     * 火苗效果覆盖层
     */
    private var fireEffectsOverlay: some View {
        GeometryReader { geometry in
            ZStack {
                // 在小羊周围显示火苗动画
                ForEach(0..<sheepPositions.count, id: \.self) { index in
                    if index >= rescuedSheepCount {
                        FireGlowEffect(position: sheepPositions[index])
                            .opacity(0.6)
                    }
                }
            }
        }
    }

    // MARK: - 直升机视图

    /**
     * 直升机视图
     *
     * Spec 要求：
     * - 超大尺寸（≥150pt）
     * - 持续播放螺旋桨飞行动画（Lottie）
     * - 位于屏幕左侧
     */
    private var helicopterView: some View {
        ZStack {
            // 直升机主体
            Image(systemName: "helicopter")
                .font(.system(size: 100))
                .foregroundColor(.white)
                .shadow(color: .black.opacity(0.3), radius: 10, x: 0, y: 5)

            // 螺旋桨动画（简化版，TODO: 使用 Lottie）
            Circle()
                .stroke(Color.white.opacity(0.5), lineWidth: 2)
                .frame(width: 140, height: 40)
                .rotationEffect(.degrees(isHelicopterFlying ? 360 : 0))
                .animation(
                    isHelicopterFlying ? .linear(duration: 0.5).repeatForever(autoreverses: false) : .default,
                    value: isHelicopterFlying
                )
                .offset(y: -50)
        }
        .frame(width: 150, height: 150)
        .onChange(of: isHelicopterFlying) { _, newValue in
            // 启动/停止螺旋桨动画
        }
    }

    /**
     * 飞向目标小羊
     *
     * Spec 要求：
     * - 点击小羊 → 直升机缓慢飞行到小羊上方
     * - 动画时长约 1-1.5 秒
     * - 飞行完成后显示放下梯子按钮
     */
    private func flyHelicopterToSheep(sheepIndex: Int) {
        isHelicopterFlying = true

        let targetPosition = calculateHelicopterPosition(for: sheepIndex)

        withAnimation(.easeInOut(duration: 1.2)) {
            helicopterPosition = targetPosition
        }

        // 飞行完成后通知 ViewModel
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
            isHelicopterFlying = false
            viewModelWrapper.onHelicopterFlightCompleted()
        }
    }

    /**
     * 计算直升机在目标小羊上方的位置
     */
    private func calculateHelicopterPosition(for sheepIndex: Int) -> CGPoint {
        let sheepPos = sheepPositions[sheepIndex]
        // 直升机位于小羊上方 80pt 处
        return CGPoint(
            x: sheepPos.x - 150, // 直升机初始位置在左侧，需要调整x偏移
            y: sheepPos.y - 200   // 在小羊上方
        )
    }

    // MARK: - 放下梯子按钮

    /**
     * "放下梯子"按钮
     *
     * Spec 要求：
     * - 圆形按钮
     * - 尺寸 ≥100pt
     * - 在直升机到达目标位置后显示
     */
    private var ladderButton: some View {
        VStack {
            Spacer()
            Button(action: handleLadderButtonTap) {
                VStack(spacing: 8) {
                    Image(systemName: "arrow.down.to.line.compact")
                        .font(.system(size: 35))
                    Text("放下梯子")
                        .font(.headline)
                }
                .foregroundColor(.white)
                .padding(20)
                .background(Circle().fill(Color.blue))
                .shadow(color: .blue.opacity(0.5), radius: 15, x: 0, y: 5)
                .frame(width: 130, height: 130)
            }
            .padding(.bottom, 60)
        }
    }

    /**
     * 处理"放下梯子"按钮点击
     */
    private func handleLadderButtonTap() {
        viewModelWrapper.onLadderButtonClicked()
    }

    // MARK: - 视频播放器

    /**
     * 视频播放器
     */
    private func videoPlayerView(for videoId: String) -> some View {
        SimpleVideoPlayerView(
            videoName: "rescue_sheep_\(videoId)",
            onPlaybackCompleted: {
                handleRescueVideoCompleted()
            }
        )
        .ignoresSafeArea()
    }

    /**
     * 处理救援视频完成
     */
    private func handleRescueVideoCompleted() {
        viewModelWrapper.onRescueVideoCompleted()
    }

    // MARK: - 徽章奖励视图

    /**
     * 徽章奖励覆盖层
     */
    private var badgeRewardOverlay: some View {
        VStack(spacing: 20) {
            Spacer()

            Image(systemName: "star.fill")
                .font(.system(size: 80))
                .foregroundColor(.yellow)
                .scaleEffect(showBadgeAnimation ? 1.0 : 0.0)
                .animation(.spring(response: 0.6, dampingFraction: 0.7), value: showBadgeAnimation)

            Text("小羊得救了！")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.white)

            Button("继续") {
                handleBadgeAnimationCompleted()
            }
            .buttonStyle(.borderedProminent)
            .tint(.green)
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
    }

    // MARK: - 庆祝动画视图

    /**
     * 庆祝动画覆盖层
     *
     * Spec 要求：
     * - 两只小羊都救出后触发
     * - 播放庆祝动画 + 语音总结："直升机能从天上救人，真厉害！"
     */
    private var celebrationOverlay: some View {
        VStack(spacing: 20) {
            Spacer()

            // TODO: 使用 Lottie 庆祝动画
            Image(systemName: "flag.fill")
                .font(.system(size: 80))
                .foregroundColor(.green)
                .rotationEffect(.degrees(-10))

            Text("太棒了！")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.white)

            Text("直升机能从天上救人，真厉害！")
                .font(.body)
                .foregroundColor(.white.opacity(0.9))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 20)

            Button("继续") {
                handleCelebrationCompleted()
            }
            .buttonStyle(.borderedProminent)
            .tint(.green)
            .font(.title3)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(
            // 烟花效果背景（TODO: 使用 Lottie）
            Color.black.opacity(0.7)
        )
    }

    /**
     * 处理庆祝完成
     */
    private func handleCelebrationCompleted() {
        // 导航回主地图
        coordinator.goBack()
    }

    /**
     * 处理小羊点击
     */
    private func handleSheepClicked(index: Int) {
        guard !isHelicopterFlying else { return }
        guard index >= rescuedSheepCount else { return }

        // 发送点击事件到 ViewModel
        viewModelWrapper.onSheepClicked(sheepIndex: index + 1) // sheepIndex 是 1-based
    }

    /**
     * 返回地图
     */
    private func goBack() {
        coordinator.goBack()
    }
}

// MARK: - 小羊按钮组件

/**
 * 小羊按钮组件
 *
 * Spec 要求：
 * - 小羊周边被火苗包围
 * - 小羊做求救动画
 * - 点击触发救援
 */
struct SheepButton: View {
    /// 小羊索引
    let sheepIndex: Int

    /// 位置
    let position: CGPoint

    /// 是否是当前目标
    let isTarget: Bool

    /// 直升机是否正在飞行
    let isHelicopterFlying: Bool

    /// 点击回调
    let onTap: () -> Void

    /// 求救动画状态
    @State private var isWaving = false

    var body: some View {
        Button(action: {
            guard !isHelicopterFlying else { return }
            onTap()
        }) {
            ZStack {
                // 火苗光晕效果
                Circle()
                    .fill(
                        RadialGradient(
                            colors: [Color.orange.opacity(0.4), Color.clear],
                            center: .center,
                            startRadius: 40,
                            endRadius: 80
                        )
                    )
                    .frame(width: 140, height: 140)
                    .opacity(isWaving ? 1.0 : 0.6)

                // 小羊图标
                Image(systemName: "hare.fill")
                    .font(.system(size: 60))
                    .foregroundColor(.white)
                    .rotationEffect(.degrees(isWaving ? -10 : 10))
                    .animation(
                        .easeInOut(duration: 0.5).repeatForever(autoreverses: true),
                        value: isWaving
                    )

                // 求救气泡
                if !isHelicopterFlying {
                    VStack(spacing: 2) {
                        Text("救命!")
                            .font(.caption)
                            .fontWeight(.bold)
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.caption2)
                    }
                    .foregroundColor(.white)
                    .padding(8)
                    .background(Color.red.opacity(0.8))
                    .clipShape(Capsule())
                    .offset(x: 50, y: -50)
                }
            }
        }
        .disabled(isHelicopterFlying)
        .buttonStyle(.plain)
        .position(x: position.x, y: position.y)
        .onAppear {
            isWaving = true
        }
    }
}

// MARK: - 火苗光晕效果组件

/**
 * 火苗光晕效果组件
 */
struct FireGlowEffect: View {
    let position: CGPoint

    @State private var opacity: Double = 0.3

    var body: some View {
        Circle()
            .fill(
                RadialGradient(
                    colors: [
                        Color.orange.opacity(opacity),
                        Color.red.opacity(opacity * 0.5),
                        Color.clear
                    ],
                    center: .center,
                    startRadius: 20,
                    endRadius: 70
                )
            )
            .frame(width: 140, height: 140)
            .position(x: position.x, y: position.y)
            .onAppear {
                withAnimation(
                    .easeInOut(duration: 0.8).repeatForever(autoreverses: true)
                ) {
                    opacity = 0.6
                }
            }
    }
}

// MARK: - ForestViewModelWrapper

/**
 * ForestViewModel 的 SwiftUI 包装器
 */
@MainActor
class ForestViewModelWrapper: ViewModelWrapper<ForestViewModel, ForestState> {
    init(viewModel: ForestViewModel) {
        let initialState = viewModel.state as! ForestState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.state)
    }

    /**
     * 小羊点击事件
     */
    func onSheepClicked(sheepIndex: Int) {
        sendEvent {
            baseViewModel.onEvent(event: ForestEvent.SheepClicked(sheepIndex: Int32(sheepIndex)))
        }
    }

    /**
     * 直升机飞行完成
     */
    func onHelicopterFlightCompleted() {
        sendEvent {
            baseViewModel.onEvent(event: ForestEvent.HelicopterFlightCompleted.shared)
        }
    }

    func onLadderButtonClicked() {
        sendEvent {
            // 本地处理，不发送到 Kotlin
        }
    }

    func onRescueVideoCompleted() {
        sendEvent {
            // 本地处理，不发送到 Kotlin
        }
    }

    func onBadgeAnimationCompleted() {
        sendEvent {
            baseViewModel.onEvent(event: ForestEvent.BadgeAnimationCompleted.shared)
        }
    }

    func onBackPressed() {
        sendEvent {
            baseViewModel.onEvent(event: ForestEvent.BackToMapClicked.shared)
        }
    }
}

// MARK: - 预览

#Preview {
    ForestView()
        .environmentObject(AppCoordinator())
}
