import SwiftUI
import Charts
import ComposeApp

/**
 * 家长模式视图
 *
 * 功能：
 * - 显示时间设置选项（5/10/15/30 分钟）
 * - 显示本周使用时长柱状图
 * - 显示重置进度按钮
 * - 数学题验证界面
 * - "再玩 5 分钟"按钮
 */
struct ParentView: View {
    /// ViewModel 包装器
    @StateObject private var viewModelWrapper: ParentViewModelWrapper

    /// 导航协调器
    @EnvironmentObject private var coordinator: AppCoordinator

    /// 用户输入的答案
    @State private var userAnswer: String = ""

    /**
     * 初始化
     */
    init() {
        let viewModel = ParentViewModelImpl()
        _viewModelWrapper = StateObject(wrappedValue: ParentViewModelWrapper(viewModel: viewModel))
    }

    var body: some View {
        ZStack {
            // 背景
            background

            // 主内容
            ScrollView {
                VStack(spacing: 25) {
                    // 标题
                    titleView

                    // 验证界面或设置界面
                    if viewModelWrapper.state.showVerification {
                        verificationView
                    } else if viewModelWrapper.state.timeLimitReached {
                        timeLimitView
                    } else {
                        settingsView
                    }

                    Spacer().frame(height: 50)
                }
                .padding()
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
        // TODO: 使用家长模式背景图片
        Color(.systemGray6)
            .ignoresSafeArea()
    }

    /**
     * 标题视图
     */
    private var titleView: some View {
        VStack(spacing: 5) {
            Text("家长模式")
                .font(.largeTitle)
                .fontWeight(.bold)

            Text("设置与统计")
                .font(.body)
                .foregroundColor(.secondary)
        }
        .padding(.top, 20)
    }

    /**
     * 设置视图
     */
    private var settingsView: some View {
        VStack(spacing: 25) {
            // 时间设置
            timeSettingSection

            // 使用统计图表
            usageStatsSection

            // 重置进度
            resetProgressButton
        }
    }

    /**
     * 时间设置区域
     */
    private var timeSettingSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("使用时长设置")
                .font(.title2)
                .fontWeight(.semibold)

            Picker("单次使用时长", selection: .constant(viewModelWrapper.state.settings.sessionDurationMinutes)) {
                ForEach([5, 10, 15, 30], id: \.self) { minutes in
                    Text("\(minutes) 分钟").tag(minutes)
                }
            }
            .pickerStyle(.segmented)
            .onChange(of: viewModelWrapper.state.settings.sessionDurationMinutes) { _, newValue in
                viewModelWrapper.updateDuration(minutes: newValue)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(15)
        .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
    }

    /**
     * 使用统计图表区域
     */
    private var usageStatsSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            Text("本周使用时长")
                .font(.title2)
                .fontWeight(.semibold)

            // 柱状图
            usageBarChart

            // 总计
            Text("总计：\(formatDuration(totalWeeklyUsage))")
                .font(.body)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(15)
        .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
    }

    /**
     * 使用时长柱状图
     */
    private var usageBarChart: some View {
        HStack(alignment: .bottom, spacing: 10) {
            ForEach(weekDays, id: \.self) { date in
                VStack(spacing: 5) {
                    Rectangle()
                        .fill(barColor(for: date))
                        .frame(height: barHeight(for: date))
                        .animation(.easeInOut, value: viewModelWrapper.state.dailyStats[date])

                    Text(dateSuffix(from: date))
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
        }
        .frame(height: 100)
    }

    /**
     * 重置进度按钮
     */
    private var resetProgressButton: some View {
        Button(action: handleResetProgress) {
            HStack {
                Image(systemName: "arrow.counterclockwise")
                Text("重置进度")
            }
            .font(.headline)
            .foregroundColor(.red)
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.red.opacity(0.1))
            .cornerRadius(15)
        }
    }

    /**
     * 验证视图
     */
    private var verificationView: some View {
        VStack(spacing: 30) {
            Spacer()

            Text("请回答问题继续")
                .font(.title)
                .fontWeight(.bold)

            // 数学题
            VStack(spacing: 20) {
                Text(viewModelWrapper.state.mathQuestion.question)
                    .font(.title)
                    .fontWeight(.semibold)

                // 答案输入
                TextField("输入答案", text: $userAnswer)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)
                    .frame(width: 120)
                    .multilineTextAlignment(.center)

                // 答案按钮
                Button("确定") {
                    if let answer = Int(userAnswer) {
                        viewModelWrapper.verifyAnswer(answer: answer)
                        userAnswer = ""
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(userAnswer.isEmpty)
            }

            Spacer()
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(15)
    }

    /**
     * 时间到视图
     */
    private var timeLimitView: some View {
        VStack(spacing: 30) {
            Spacer()

            Image(systemName: "clock.fill")
                .font(.system(size: 60))
                .foregroundColor(.orange)

            Text("时间到啦！")
                .font(.title)
                .fontWeight(.bold)

            Text("我们明天再玩吧！")
                .font(.body)
                .foregroundColor(.secondary)

            Button("验证继续") {
                viewModelWrapper.showVerification()
            }
            .buttonStyle(.borderedProminent)

            Button("退出") {
                handleExit()
            }
            .foregroundColor(.secondary)

            Spacer()
        }
        .padding()
    }

    // MARK: - 辅助方法

    /**
     * 本周日期列表
     */
    private var weekDays: [String] {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        var dates: [String] = []

        for i in 6...0 {
            if let date = calendar.date(byAdding: .day, value: -i, to: today) {
                let formatter = DateFormatter()
                formatter.dateFormat = "yyyy-MM-dd"
                dates.append(formatter.string(from: date))
            }
        }

        return dates
    }

    /**
     * 从日期字符串提取日期后缀（如"01" -> "一"）
     */
    private func dateSuffix(from dateString: String) -> String {
        let weekdays = ["日", "一", "二", "三", "四", "五", "六"]

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        if let date = formatter.date(from: dateString) {
            let calendar = Calendar.current
            let weekday = calendar.component(.weekday, from: date)
            return weekdays[weekday - 1]
        }

        return ""
    }

    /**
     * 获取指定日期的使用时长（分钟）
     */
    private func usageForDate(_ date: String) -> Int {
        let millis = viewModelWrapper.state.dailyStats[date] ?? 0
        return Int(millis / 1000 / 60)
    }

    /**
     * 计算本周总使用时长（毫秒）
     */
    private var totalWeeklyUsage: Long {
        var total: Long = 0
        for date in weekDays {
            total += viewModelWrapper.state.dailyStats[date] ?? 0
        }
        return total
    }

    /**
     * 获取柱子高度
     */
    private func barHeight(for date: String) -> CGFloat {
        let minutes = usageForDate(date)
        let maxMinutes = max(10, usageForDate(weekDays.max(by: { usageForDate($0) > usageForDate($1) }) ?? ""))
        return max(10, CGFloat(minutes) / CGFloat(maxMinutes) * 80)
    }

    /**
     * 获取柱子颜色
     */
    private func barColor(for date: String) -> Color {
        let isToday = Calendar.current.isDateInToday(parseDate(date))
        return isToday ? .blue : .gray.opacity(0.5)
    }

    /**
     * 解析日期字符串
     */
    private func parseDate(_ dateString: String) -> Date {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.date(from: dateString) ?? Date()
    }

    /**
     * 格式化时长显示
     */
    private func formatDuration(_ millis: Long) -> String {
        let minutes = Int(millis / 1000 / 60)
        let hours = minutes / 60
        let mins = minutes % 60

        if hours > 0 {
            return "\(hours)小时\(mins)分钟"
        } else {
            return "\(mins)分钟"
        }
    }

    /**
     * 处理重置进度
     */
    private func handleResetProgress() {
        viewModelWrapper.resetProgress()
    }

    /**
     * 处理退出
     */
    private func handleExit() {
        // TODO: 实现 App 退出
        coordinator.popToRoot()
    }

    /**
     * 返回地图
     */
    private func goBack() {
        coordinator.goBack()
    }
}

// MARK: - ParentViewModelWrapper

/**
 * ParentViewModel 的 SwiftUI 包装器
 */
@MainActor
class ParentViewModelWrapper: ViewModelWrapper<ParentViewModel, ParentState> {
    init(viewModel: ParentViewModel) {
        let initialState = viewModel.frameState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.frameState)
    }

    func verifyAnswer(answer: Int) {
        sendEvent {
            let event = ParentVerifyAnswer(answer: answer)
            baseViewModel.onEvent(event: event)
        }
    }

    func extendTime() {
        sendEvent {
            baseViewModel.onEvent(event: ParentExtendTime())
        }
    }

    func updateDuration(minutes: Int) {
        sendEvent {
            baseViewModel.onEvent(event: ParentUpdateDuration(minutes: minutes))
        }
    }

    func resetProgress() {
        sendEvent {
            baseViewModel.onEvent(event: ParentResetProgress())
        }
    }

    func showVerification() {
        // 触发显示验证界面
        // TODO: 需要在 ViewModel 中添加对应方法
    }

    func onBackPressed() {
        sendEvent {
            baseViewModel.onEvent(event: ParentBackPressed())
        }
    }
}

// MARK: - 事件辅助类（临时）

// 临时创建事件类，实际应该在 Shared 模块中
struct ParentVerifyAnswer {
    let answer: Int
}

struct ParentExtendTime {}

struct ParentUpdateDuration {
    let minutes: Int
}

struct ParentResetProgress {}

struct ParentBackPressed {}

// MARK: - 预览

#Preview {
    ParentView()
        .environmentObject(AppCoordinator())
}
