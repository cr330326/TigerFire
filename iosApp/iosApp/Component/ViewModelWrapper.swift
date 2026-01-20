import Foundation
import SwiftUI
import Combine
import ComposeApp

/**
 * ViewModel 包装器基类
 *
 * 用于桥接 Shared Kotlin ViewModel 和 SwiftUI，实现状态订阅和事件发送
 *
 * 泛型参数:
 * - TViewModel: Shared Kotlin ViewModel 类型
 * - TState: ViewModel 的状态类型
 */
@MainActor
class ViewModelWrapper<TViewModel, TState>: ObservableObject where TViewModel: AnyObject {
    /// 当前状态（已桥接到 SwiftUI）
    @Published var state: TState

    /// 内部持有的 Shared ViewModel 实例
    private let viewModel: TViewModel

    /// 订阅 cancellables
    private var cancellables = Set<AnyCancellable>()

    /**
     * 初始化 ViewModel 包装器
     *
     * - Parameter viewModel: Shared Kotlin ViewModel 实例
     * - Parameter initialState: 初始状态（在订阅建立前使用）
     */
    init(viewModel: TViewModel, initialState: TState) {
        self.viewModel = viewModel
        self.state = initialState
    }

    /**
     * 订阅 ViewModel 的状态流
     *
     * 此方法应在子类的 init 中调用，用于建立状态订阅
     *
     * - Parameter stateFlow: ViewModel 的 StateFlow
     */
    func subscribeState(_ stateFlow: KotlinFlow<TState>) {
        // 将 Kotlin Flow 转换为 SwiftUI 的 Combine Publisher
        FlowPublisher<TState>(flow: stateFlow)
            .receive(on: DispatchQueue.main)
            .sink { [weak self] newState in
                self?.state = newState
            }
            .store(in: &cancellables)
    }

    /**
     * 发送事件到 ViewModel
     *
     * 子类应提供类型安全的事件发送方法
     *
     * - Parameter event: 闭包，执行事件发送逻辑
     */
    func sendEvent(_ event: () -> Void) {
        event()
    }

    /**
     * 获取底层 ViewModel 实例
     *
     * 用于子类访问 ViewModel 的特定方法
     */
    var baseViewModel: TViewModel {
        return viewModel
    }
}

// MARK: - Kotlin Flow to Combine Publisher Bridge

/**
 * 将 Kotlin Flow 转换为 Combine Publisher 的辅助类
 */
class FlowPublisher<T>: Publisher {
    typealias Output = T
    typealias Failure = Never

    private let flow: KotlinFlow<T>

    init(flow: KotlinFlow<T>) {
        self.flow = flow
    }

    func receive<S>(subscriber: S) where S: Subscriber, Never == S.Failure, T == S.Input {
        let subscription = FlowSubscription(flow: flow, subscriber: subscriber)
        subscriber.receive(subscription: subscription)
    }
}

/**
 * Flow Subscription 实现
 */
class FlowSubscription<S: Subscriber>: Subscription where S.Input == Any, S.Failure == Never {
    private var cancellable: Cancellable?

    init(flow: KotlinFlow<Any>, subscriber: S) {
        // 订阅 Kotlin Flow
        cancellable = flow.subscribe(
            onEach: { value in
                _ = subscriber.receive(value)
            },
            onComplete: {
                subscriber.receive(completion: .finished)
            },
            onThrow: { error in
                // Kotlin Flow 不应该抛出错误，但如果发生则忽略
            }
        )
    }

    func request(_ demand: Subscribers.Demand) {
        // Kotlin Flow 是背压无关的，忽略需求
    }

    func cancel() {
        cancellable?.cancel()
        cancellable = nil
    }
}
