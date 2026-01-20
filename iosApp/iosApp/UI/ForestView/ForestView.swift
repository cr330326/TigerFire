import SwiftUI
import ComposeApp

/**
 * 森林场景视图
 *
 * 功能：
 * - 显示火灾森林背景
 * - 显示 2 只小羊位置
 * - 拖拽直升机救援小羊
 * - 靠近小羊时显示"放下梯子"按钮
 * - 播放救援片段并弹出徽章
 */
struct ForestView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: ForestViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /// 直升机位置
    @State private var helicopterOffset: CGSize = .zero

    /// 小羊位置（相对）
    private let sheepPositions: [CGPoint] = [
        CGPoint(x: 100, y: 150),
        CGPoint(x: 300, y: 250)
    ]

    /**
     * 初始化
     */
    init() {
        let viewModel = ForestViewModelImpl()
        _viewModelWrapper = StateObject(wrappedValue: ForestViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 小羊
            ForEach(Array(sheepPositions.enumerated()), id: \.offset) { index, position in
                if index >= viewModelWrapper.state.rescuedSheepCount {
                    SheepView(
                        position: position,
                        isRescued: false
                    )
                }
            }

            // 直升机（可拖拽）
            helicopterView
                .offset(
                    width: helicopterOffset.width + CGFloat(viewModelWrapper.state.helicopterPosition.x),
                    height: helicopterOffset.height + CGFloat(viewModelWrapper.state.helicopterPosition.y)
                )
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            handleHelicopterDrag(value.translation)
                        }
                        .onEnded { _ in
                            // 释放时重置偏移
                            helicopterOffset = .zero
                        }
                )

            // "放下梯子"按钮
            if viewModelWrapper.state.showLadderButton {
                ladderButton
            }

            // 视频播放器
            if let videoId = viewModelWrapper.state.currentRescueVideo {
                videoPlayerView(for: videoId)
            }

            // 徽章奖励
            if viewModelWrapper.state.showBadgeAnimation {
                badgeRewardOverlay
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
        }
        .ignoresSafeArea()
    }

    /**
     * 背景
     */
    private var background: some View {
        // TODO: 使用森林火灾背景图片
        Color(.systemGreen)
            .opacity(0.3)
            .ignoresSafeArea()
    }

    /**
     * 直升机视图
     */
    private var helicopterView: some View {
        Image(systemName: "helicopter")
            .font(.system(size: 80))
            .foregroundColor(.white)
            .shadow(radius: 5)
    }

    /**
     * "放下梯子"按钮
     */
    private var ladderButton: some View {
        VStack {
            Spacer()
            Button(action: handleLadderButtonTap) {
                VStack(spacing: 8) {
                    Image(systemName: "arrow.down.to.line")
                        .font(.system(size: 30))
                    Text("放下梯子")
                        .font(.headline)
                }
                .foregroundColor(.white)
                .padding(20)
                .background(Circle().fill(Color.blue))
                .frame(width: 120, height: 120)
            }
            .padding(.bottom, 50)
        }
    }

    /**
     * 视频播放器
     */
    private func videoPlayerView(for videoId: String) -> some View {
        SimpleVideoPlayerView(
            videoName: "forest_rescue_\(videoId)",
            onPlaybackCompleted: {
                handleRescueVideoCompleted()
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

            Image(systemName: "star.fill")
                .font(.system(size: 80))
                .foregroundColor(.yellow)
                .scaleEffect(viewModelWrapper.state.showBadgeAnimation ? 1.0 : 0.0)
                .animation(.spring(), value: viewModelWrapper.state.showBadgeAnimation)

            Text("小羊得救了！")
                .font(.title)
                .foregroundColor(.white)

            Button("继续") {
                handleBadgeAnimationCompleted()
            }
            .buttonStyle(.borderedProminent)
            .tint(.green)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black.opacity(0.7))
    }

    /**
     * 处理直升机拖拽
     */
    private func handleHelicopterDrag(_ translation: CGSize) {
        // 应用速度衰减系数 0.6
        let adjustedTranslation = CGSize(
            width: translation.width * 0.6,
            height: translation.height * 0.6
        )

        helicopterOffset = adjustedTranslation

        // 检测与小羊的距离
        let helicopterPos = CGPoint(
            x: UIScreen.main.bounds.width / 2 + adjustedTranslation.width,
            y: UIScreen.main.bounds.height / 2 + adjustedTranslation.height
        )

        for (index, sheepPos) in sheepPositions.enumerated() {
            if index >= viewModelWrapper.state.rescuedSheepCount {
                let distance = sqrt(
                    pow(helicopterPos.x - sheepPos.x, 2) +
                    pow(helicopterPos.y - sheepPos.y, 2)
                )

                // 80pt 触发吸附
                if distance <= 80 {
                    viewModelWrapper.onHelicopterDragged(
                        x: Float(sheepPos.x),
                        y: Float(sheepPos.y)
                    )
                    return
                }
            }
        }
    }

    /**
     * 处理"放下梯子"按钮点击
     */
    private func handleLadderButtonTap() {
        viewModelWrapper.onLadderButtonClicked()
    }

    /**
     * 处理救援视频完成
     */
    private func handleRescueVideoCompleted() {
        viewModelWrapper.onRescueVideoCompleted()
    }

    /**
     * 处理徽章动画完成
     */
    private func handleBadgeAnimationCompleted() {
        viewModelWrapper.onBadgeAnimationCompleted()

        // 全部完成时显示庆祝
        if viewModelWrapper.state.isAllCompleted {
            // TODO: 显示庆祝动画
        }
    }

    /**
     * 返回地图
     */
    private func goBack() {
        coordinator.goBack()
    }
}

// MARK: - 小羊视图

/**
 * 小羊视图组件
 */
struct SheepView: View {
    /// 位置
    let position: CGPoint

    /// 是否已获救
    let isRescued: Bool

    var body: some View {
        Group {
            if isRescued {
                EmptyView()
            } else {
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.3))
                        .frame(width: 80, height: 80)

                    Image(systemName: "hare.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.white)
                }
                .frame(width: 80, height: 80)
                .position(x: position.x, y: position.y)
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
        let initialState = viewModel.frameState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.frameState)
    }

    func onHelicopterDragged(x: Float, y: Float) {
        sendEvent {
            baseViewModel.onHelicopterDragged(x: x, y: y)
        }
    }

    func onLadderButtonClicked() {
        sendEvent {
            baseViewModel.onLadderButtonClicked()
        }
    }

    func onRescueVideoCompleted() {
        sendEvent {
            baseViewModel.onRescueVideoCompleted()
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
    ForestView()
        .environmentObject(AppCoordinator())
}
