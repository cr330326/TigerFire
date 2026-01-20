import SwiftUI
import ComposeApp

/**
 * 欢迎页视图
 *
 * 功能：
 * - 播放消防车入场 Lottie 动画
 * - 播放小火挥手 Lottie 动画
 * - 全屏点击跳转到主地图
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
            // 背景色
            Color(.systemBackground)
                .ignoresSafeArea()

            // 根据 animationPhase 显示不同动画
            switch animationPhase {
            case .truckEntering:
                SimpleLottieView(
                    animationName: "anim_truck_enter",
                    onAnimationEnd: {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            animationPhase = .xiaohuoWaving
                        }
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .xiaohuoWaving:
                SimpleLottieView(
                    animationName: "anim_xiaohuo_wave",
                    onAnimationEnd: {
                        // 动画完成后保持显示
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            handleScreenTap()
        }
        .onChange(of: shouldNavigateToMap) { newValue in
            if newValue {
                coordinator.navigate(to: .map)
            }
        }
    }

    /**
     * 处理屏幕点击事件
     */
    private func handleScreenTap() {
        // 发送点击事件到 ViewModel
        viewModelWrapper.sendEvent {
            viewModelWrapper.baseViewModel.onScreenClick()
        }

        // 触发导航
        shouldNavigateToMap = true
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
    }

    /**
     * 发送屏幕点击事件
     */
    func onScreenClick() {
        sendEvent {
            baseViewModel.onScreenClick()
        }
    }
}

// MARK: - 预览

#Preview {
    WelcomeView()
        .environmentObject(AppCoordinator())
}
