import SwiftUI
import ComposeApp

/**
 * 主地图视图
 *
 * 功能：
 * - 显示 3 个场景图标（消防站、学校、森林）
 * - 显示"我的收藏"按钮（左上角小火头像）
 * - 显示家长模式入口（右上角齿轮）
 * - 根据解锁状态显示不同视觉效果
 */
struct MapView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: MapViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /**
     * 初始化
     */
    init() {
        let scope = CoroutineScope()
        let viewModel = MapViewModel(viewModelScope: scope, progressRepository: viewModelFactory.createProgressRepository())
        _viewModelWrapper = StateObject(wrappedValue: MapViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 地图背景
            mapBackground

            // 场景图标
            VStack(spacing: 40) {
                Spacer()
                sceneIcons
                Spacer().frame(height: 100)
            }

            // 左上角：我的收藏按钮
            VStack {
                HStack {
                    collectionButton
                    Spacer()
                    parentModeButton
                }
                Spacer()
            }
            .padding(.horizontal, 30)
            .padding(.top, 20)
        }
        .ignoresSafeArea()
    }

    /**
     * 地图背景
     */
    private var mapBackground: some View {
        // TODO: 使用实际地图背景图片
        Color(.systemGroupedBackground)
            .ignoresSafeArea()
    }

    /**
     * 场景图标容器
     */
    private var sceneIcons: some View {
        HStack(spacing: 50) {
            // 消防站
            SceneIconButton(
                scene: .fireStation,
                status: viewModelWrapper.state.sceneStatuses[.fireStation] ?? .locked,
                onTap: {
                    handleSceneTap(.fireStation)
                }
            )

            // 学校
            SceneIconButton(
                scene: .school,
                status: viewModelWrapper.state.sceneStatuses[.school] ?? .locked,
                onTap: {
                    handleSceneTap(.school)
                }
            )

            // 森林
            SceneIconButton(
                scene: .forest,
                status: viewModelWrapper.state.sceneStatuses[.forest] ?? .locked,
                onTap: {
                    handleSceneTap(.forest)
                }
            )
        }
    }

    /**
     * 我的收藏按钮
     */
    private var collectionButton: some View {
        Button(action: {
            coordinator.navigate(to: .collection)
        }) {
            // TODO: 使用小火头像图标
            Image(systemName: "star.circle.fill")
                .font(.system(size: 50))
                .foregroundColor(.yellow)
        }
        .accessibilityLabel("我的收藏")
    }

    /**
     * 家长模式按钮
     */
    private var parentModeButton: some View {
        Button(action: {
            coordinator.navigate(to: .parent)
        }) {
            Image(systemName: "gearshape.fill")
                .font(.system(size: 40))
                .foregroundColor(.gray.opacity(0.5))
        }
        .accessibilityLabel("家长模式")
    }

    /**
     * 处理场景点击
     */
    private func handleSceneTap(_ scene: SceneType) {
        viewModelWrapper.onSceneClicked(scene: scene)

        // 根据场景类型导航
        switch scene {
        case .fireStation:
            coordinator.navigate(to: .firestation)
        case .school:
            coordinator.navigate(to: .school)
        case .forest:
            coordinator.navigate(to: .forest)
        default:
            break
        }
    }
}

// MARK: - 场景图标按钮

/**
 * 场景图标按钮组件
 */
struct SceneIconButton: View {
    /// 场景类型
    let scene: SceneType

    /// 场景状态
    let status: SceneStatus

    /// 点击回调
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                // 背景圆
                Circle()
                    .fill(backgroundColor)
                    .frame(width: 120, height: 120)

                // 图标
                Image(systemName: systemIcon)
                    .font(.system(size: 50))
                    .foregroundColor(iconColor)

                // 锁图标（如果锁定）
                if status == .locked {
                    Image(systemName: "lock.fill")
                        .font(.system(size: 30))
                        .foregroundColor(.white)
                        .offset(x: 30, y: -30)
                }
            }
        }
        .disabled(status == .locked)
        .opacity(status == .locked ? 0.5 : 1.0)
        .accessibilityLabel(sceneDisplayName)
    }

    /**
     * 背景颜色
     */
    private var backgroundColor: Color {
        switch status {
        case .locked:
            return .gray.opacity(0.3)
        case .unlocked:
            return sceneColor
        case .completed:
            return sceneColor
        default:
            return .gray
        }
    }

    /**
     * 图标颜色
     */
    private var iconColor: Color {
        switch status {
        case .locked:
            return .gray
        case .unlocked, .completed:
            return .white
        default:
            return .gray
        }
    }

    /**
     * 场景颜色
     */
    private var sceneColor: Color {
        switch scene {
        case .fireStation:
            return .red
        case .school:
            return .blue
        case .forest:
            return .green
        default:
            return .gray
        }
    }

    /**
     * 系统图标名称
     */
    private var systemIcon: String {
        switch scene {
        case .fireStation:
            return "flame.fill"
        case .school:
            return "building.columns.fill"
        case .forest:
            return "tree.fill"
        default:
            return "star.fill"
        }
    }

    /**
     * 场景显示名称
     */
    private var sceneDisplayName: String {
        switch scene {
        case .fireStation:
            return "消防站"
        case .school:
            return "学校"
        case .forest:
            return "森林"
        default:
            return "场景"
        }
    }
}

// MARK: - MapViewModelWrapper

/**
 * MapViewModel 的 SwiftUI 包装器
 */
@MainActor
class MapViewModelWrapper: ViewModelWrapper<MapViewModel, MapState> {
    init(viewModel: MapViewModel) {
        let initialState = viewModel.state as! MapState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.state)
    }

    func onSceneClicked(scene: SceneType) {
        sendEvent {
            let kotlinScene: ComposeApp.SceneType
            switch scene {
            case .fireStation:
                kotlinScene = ComposeApp.SceneType.fireStation
            case .school:
                kotlinScene = ComposeApp.SceneType.school
            case .forest:
                kotlinScene = ComposeApp.SceneType.forest
            default:
                kotlinScene = ComposeApp.SceneType.fireStation
            }
            baseViewModel.onEvent(event: MapEvent.SceneClicked(scene: kotlinScene))
        }
    }

    func onCollectionClicked() {
        sendEvent {
            baseViewModel.onEvent(event: MapEvent.CollectionClicked.shared)
        }
    }

    func onParentModeClicked() {
        sendEvent {
            baseViewModel.onEvent(event: MapEvent.ParentModeClicked.shared)
        }
    }
}

// MARK: - 预览

#Preview("解锁状态") {
    MapView()
        .environmentObject(AppCoordinator())
}
