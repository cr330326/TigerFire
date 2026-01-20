import SwiftUI
import ComposeApp

/**
 * 我的收藏（徽章展示）视图
 *
 * 功能：
 * - 按场景分组展示已获得徽章
 * - 消防站 4 个槽位、学校 1 个、森林 2 个
 * - 未获得徽章显示灰色轮廓
 * - 已获得徽章显示完整图标 + 变体颜色
 * - 集齐 7 枚徽章后播放彩蛋动画
 */
struct CollectionView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: CollectionViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /**
     * 初始化
     */
    init() {
        let viewModel = CollectionViewModelImpl()
        _viewModelWrapper = StateObject(wrappedValue: CollectionViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 主内容
            ScrollView {
                VStack(spacing: 30) {
                    // 标题
                    titleView

                    // 徽章分组
                    badgeSections

                    Spacer().frame(height: 50)
                }
                .padding()
            }

            // 彩蛋动画覆盖层
            if viewModelWrapper.state.showEggAnimation {
                easterEggOverlay
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
        // TODO: 使用收藏页面背景图片
        Color(.systemGroupedBackground)
            .ignoresSafeArea()
    }

    /**
     * 标题视图
     */
    private var titleView: some View {
        VStack(spacing: 10) {
            Text("我的收藏")
                .font(.largeTitle)
                .fontWeight(.bold)

            Text("已收集 \(viewModelWrapper.state.badges.count) / 7 枚徽章")
                .font(.body)
                .foregroundColor(.secondary)
        }
        .padding(.top, 20)
    }

    /**
     * 徽章分组区域
     */
    private var badgeSections: some View {
        VStack(spacing: 30) {
            // 消防站徽章
            badgeSection(
                title: "消防站",
                scene: .firestation,
                slots: 4,
                badges: viewModelWrapper.state.badges.filter { $0.scene == .firestation }
            )

            // 学校徽章
            badgeSection(
                title: "学校",
                scene: .school,
                slots: 1,
                badges: viewModelWrapper.state.badges.filter { $0.scene == .school }
            )

            // 森林徽章
            badgeSection(
                title: "森林",
                scene: .forest,
                slots: 2,
                badges: viewModelWrapper.state.badges.filter { $0.scene == .forest }
            )
        }
    }

    /**
     * 徽章分组组件
     */
    private func badgeSection(title: String, scene: SceneType, slots: Int, badges: [Badge]) -> some View {
        VStack(alignment: .leading, spacing: 15) {
            // 分组标题
            Text(title)
                .font(.title2)
                .fontWeight(.semibold)

            // 徽章网格
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 15), count: min(slots, 4)),
                spacing: 15
            ) {
                ForEach(0..<slots, id: \.self) { index in
                    if index < badges.count {
                        // 已获得的徽章
                        BadgeBadgeView(badge: badges[index])
                    } else {
                        // 空槽位
                        EmptyBadgeSlot()
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(15)
        .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
    }

    /**
     * 彩蛋动画覆盖层
     */
    private var easterEggOverlay: some View {
        VStack(spacing: 20) {
            Spacer()

            // 小火跳舞动画（TODO: 使用 Lottie）
            LoopingLottieView(animationName: "anim_xiaohuo_dance")
                .frame(width: 200, height: 200)

            Text("恭喜集齐所有徽章！")
                .font(.title)
                .foregroundColor(.white)

            Text("你真是个消防安全小专家！")
                .font(.body)
                .foregroundColor(.white.opacity(0.9))
                .multilineTextAlignment(.center)

            Button("太棒了！") {
                handleEggAnimationCompleted()
            }
            .buttonStyle(.borderedProminent)

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(
            // 烟花效果背景（TODO: 使用 Lottie）
            Color.black.opacity(0.8)
        )
    }

    /**
     * 处理彩蛋动画完成
     */
    private func handleEggAnimationCompleted() {
        viewModelWrapper.onEggAnimationCompleted()
    }

    /**
     * 返回地图
     */
    private func goBack() {
        coordinator.goBack()
    }
}

// MARK: - 徽章视图组件

/**
 * 徽章显示组件
 */
struct BadgeBadgeView: View {
    /// 徽章数据
    let badge: Badge

    /// 变体颜色映射
    private var variantColor: Color {
        switch badge.variant {
        case 0: return .yellow
        case 1: return .red
        case 2: return .blue
        case 3: return .green
        default: return .yellow
        }
    }

    var body: some View {
        VStack(spacing: 8) {
            ZStack {
                Circle()
                    .fill(variantColor.opacity(0.2))
                    .frame(width: 70, height: 70)

                Image(systemName: badgeSystemIcon)
                    .font(.system(size: 35))
                    .foregroundColor(variantColor)
            }

            Text(badgeDisplayName)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }

    /**
     * 系统图标名称
     */
    private var badgeSystemIcon: String {
        switch badge.baseType {
        case "extinguisher": return "fire.extinguisher.fill"
        case "hydrant": return "drop.fill"
        case "ladder": return "ladder"
        case "hose": return "waterwings"
        case "school": return "building.columns.fill"
        case "sheep1", "sheep2": return "hare.fill"
        default: return "star.fill"
        }
    }

    /**
     * 徽章显示名称
     */
    private var badgeDisplayName: String {
        switch badge.baseType {
        case "extinguisher": return "灭火器"
        case "hydrant": return "消防栓"
        case "ladder": return "云梯"
        case "hose": return "水枪"
        case "school": return "学校"
        case "sheep1", "sheep2": return "小羊"
        default: return "徽章"
        }
    }
}

/**
 * 空槽位组件
 */
struct EmptyBadgeSlot: View {
    var body: some View {
        VStack(spacing: 8) {
            ZStack {
                Circle()
                    .stroke(Color.gray.opacity(0.3), lineWidth: 2)
                    .frame(width: 70, height: 70)

                Image(systemName: "questionmark")
                    .font(.system(size: 30))
                    .foregroundColor(.gray.opacity(0.3))
            }

            Text("??")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - CollectionViewModelWrapper

/**
 * CollectionViewModel 的 SwiftUI 包装器
 */
@MainActor
class CollectionViewModelWrapper: ViewModelWrapper<CollectionViewModel, CollectionState> {
    init(viewModel: CollectionViewModel) {
        let initialState = viewModel.frameState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.frameState)
    }

    func onEggAnimationCompleted() {
        sendEvent {
            baseViewModel.onEggAnimationCompleted()
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
    CollectionView()
        .environmentObject(AppCoordinator())
}
