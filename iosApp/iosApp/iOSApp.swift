import SwiftUI
import ComposeApp

// 全局 ViewModelFactory 实例
let viewModelFactory = ViewModelFactory()

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            AppRootView()
        }
    }
}