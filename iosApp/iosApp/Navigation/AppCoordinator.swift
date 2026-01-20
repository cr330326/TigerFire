import SwiftUI
import Combine

/**
 * 应用路由枚举
 *
 * 定义所有导航目标页面
 */
enum AppRoute: Hashable {
    /// 启动欢迎页
    case welcome
    /// 主地图页面
    case map
    /// 消防站场景
    case firestation
    /// 学校场景
    case school
    /// 森林场景
    case forest
    /// 我的收藏（徽章展示）
    case collection
    /// 家长模式
    case parent
}

/**
 * 导航协调器
 *
 * 管理应用的导航状态，提供页面跳转方法
 */
@MainActor
class AppCoordinator: ObservableObject {
    /// 当前导航路径（用于 NavigationStack）
    @Published var navigationPath = [AppRoute]()

    /// 当前页面（用于单页面展示）
    @Published var currentRoute: AppRoute = .welcome

    /**
     * 导航到指定路由
     *
     * - Parameter route: 目标路由
     */
    func navigate(to route: AppRoute) {
        navigationPath.append(route)
        currentRoute = route
    }

    /**
     * 返回上一页
     */
    func goBack() {
        if !navigationPath.isEmpty {
            navigationPath.removeLast()
            if !navigationPath.isEmpty {
                currentRoute = navigationPath.last ?? .welcome
            } else {
                currentRoute = .welcome
            }
        }
    }

    /**
     * 返回到根页面（欢迎页）
     */
    func popToRoot() {
        navigationPath.removeAll()
        currentRoute = .welcome
    }

    /**
     * 替换当前路由（不保留返回堆栈）
     *
     * - Parameter route: 新路由
     */
    func replace(with route: AppRoute) {
        popToRoot()
        navigate(to: route)
    }
}

/**
 * 应用根视图
 *
 * 使用 NavigationStack 管理页面导航
 */
struct AppRootView: View {
    @StateObject private var coordinator = AppCoordinator()

    var body: some View {
        NavigationStack(path: $coordinator.navigationPath) {
            WelcomeView()
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case .welcome:
                        WelcomeView()
                    case .map:
                        MapView()
                    case .firestation:
                        FireStationView()
                    case .school:
                        SchoolView()
                    case .forest:
                        ForestView()
                    case .collection:
                        CollectionView()
                    case .parent:
                        ParentView()
                    }
                }
        }
        .environmentObject(coordinator)
    }
}
