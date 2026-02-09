import Foundation
import SwiftUI
import ComposeApp

@MainActor
class ViewModelWrapper<TViewModel, TState>: ObservableObject where TViewModel: AnyObject {
    @Published var state: TState

    private let viewModel: TViewModel
    private var updateTimer: Timer?

    init(viewModel: TViewModel, initialState: TState) {
        self.viewModel = viewModel
        self.state = initialState
    }

    func subscribeState(_ stateFlow: AnyObject) {
        // 尝试作为 StateFlow 处理（有 value 属性）
        if let stateFlow = stateFlow as? Kotlinx_coroutines_coreStateFlow {
            updateStateFrom(stateFlow)

            // 设置定时轮询来更新状态（每 100ms）
            updateTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
                self?.updateStateFrom(stateFlow)
            }
        } else if let sharedFlow = stateFlow as? Kotlinx_coroutines_coreSharedFlow {
            // 对于纯 SharedFlow，没有 value 属性，只能通过 replayCache 获取
            if let replayCache = sharedFlow.replayCache as? [TState], let lastValue = replayCache.last {
                state = lastValue
            }
        }
    }

    private func updateStateFrom(_ stateFlow: Kotlinx_coroutines_coreStateFlow) {
        if let value = stateFlow.value as? TState {
            self.state = value
        }
    }

    func sendEvent(_ event: () -> Void) {
        event()
    }

    var baseViewModel: TViewModel {
        return viewModel
    }

    deinit {
        updateTimer?.invalidate()
    }
}
