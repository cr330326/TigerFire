import SwiftUI
import Lottie

/**
 * Lottie 动画视图组件
 *
 * 使用 UIViewRepresentable 包装 Lottie 的 AnimationView，
 * 提供 SwiftUI 兼容的动画播放接口
 */
struct LottieView: UIViewRepresentable {
    /// Lottie 动画文件名（不含扩展名）
    let animationName: String

    /// 动画循环模式
    var loopMode: LottieLoopMode = .playOnce

    /// 动画完成回调
    var onAnimationEnd: (() -> Void)?

    /// 动画进度（外部可控，0.0 ~ 1.0）
    @Binding var progress: Double

    /**
     * 创建 UIViewRepresentable 的上下文
     */
    func makeUIView(context: Context) -> LottieAnimationView {
        let animationView = LottieAnimationView()

        // 从 Bundle 加载动画
        animationView.animation = LottieAnimation.named(animationName)
        animationView.loopMode = loopMode
        animationView.contentMode = .scaleAspectFit

        // 使用 play(completion:) 方法处理完成回调
        animationView.play { finished in
            if finished {
                onAnimationEnd?()
            }
        }

        return animationView
    }

    /**
     * 更新 UIView
     */
    func updateUIView(_ uiView: LottieAnimationView, context: Context) {
        // 如果需要外部控制进度
        if uiView.currentProgress != progress {
            uiView.currentProgress = progress
        }
    }
}

/**
 * 简化版 LottieView（自动播放）
 *
 * 适用于"播放即忘"的场景，不需要外部控制进度
 */
struct SimpleLottieView: View {
    /// Lottie 动画文件名（不含扩展名）
    let animationName: String

    /// 动画完成回调
    let onAnimationEnd: () -> Void

    /// 动画加载超时时间（秒）
    var timeout: Double = 3.0

    /// 内部进度状态
    @State private var progress: Double = 0.0

    /// 是否已触发完成回调
    @State private var hasCompleted = false

    /// 超时定时器
    @State private var timeoutTask: Task<Void, Never>?

    var body: some View {
        LottieView(
            animationName: animationName,
            loopMode: .playOnce,
            onAnimationEnd: {
                completeAnimation()
            },
            progress: $progress
        )
        .onAppear {
            startTimeout()
        }
        .onDisappear {
            timeoutTask?.cancel()
        }
    }

    /**
     * 完成动画
     */
    private func completeAnimation() {
        guard !hasCompleted else { return }
        hasCompleted = true
        timeoutTask?.cancel()
        onAnimationEnd()
    }

    /**
     * 启动超时检测
     *
     * 如果动画在指定时间内未加载完成，视为失败并触发回调
     */
    private func startTimeout() {
        timeoutTask = Task {
            try? await Task.sleep(nanoseconds: UInt64(timeout * 1_000_000_000))
            if !Task.isCancelled && !hasCompleted {
                await MainActor.run {
                    completeAnimation()
                }
            }
        }
    }
}

/**
 * 循环播放 Lottie 动画视图
 *
 * 用于背景动画等需要持续循环播放的场景
 */
struct LoopingLottieView: UIViewRepresentable {
    /// Lottie 动画文件名（不含扩展名）
    let animationName: String

    func makeUIView(context: Context) -> LottieAnimationView {
        let animationView = LottieAnimationView()
        animationView.animation = LottieAnimation.named(animationName)
        animationView.loopMode = .loop
        animationView.contentMode = .scaleAspectFit
        animationView.play()
        return animationView
    }

    func updateUIView(_ uiView: LottieAnimationView, context: Context) {
        // 循环动画无需更新
    }
}
