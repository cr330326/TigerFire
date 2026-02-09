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

    /// 本地状态：是否显示验证界面
    @State private var showVerification = false

    /// 本地状态：是否达到时间限制
    @State private var timeLimitReached = false

    /// 本地状态：选择的时长（分钟）
    @State private var selectedDuration: Int = 15

    /// 本地状态：每日使用统计（模拟数据）
    @State private var dailyStats: [String: Int] = [:]

    /**
     * 初始化
     */
    init() {
        let viewModel = ParentViewModel(viewModelScope: CoroutineScope(), progressRepository: viewModelFactory.createProgressRepository())
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
                    if showVerification {
                        verificationView
                    } else if timeLimitReached {
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

            Picker("单次使用时长", selection: $selectedDuration) {
                ForEach([5, 10, 15, 30], id: \.self) { minutes in
                    Text("\(minutes) 分钟").tag(minutes)
                }
            }
            .pickerStyle(.segmented)
            .onChange(of: selectedDuration) { _, newValue in
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
     *
     * Spec 要求：
     * - 数学题验证（一位数加法，结果 ≤ 10）
     * - 答对 → 显示"再玩 5 分钟"按钮 + "退出"按钮
     * - 答错 → 提示"答案不正确" + 重新出题
     */
    private var verificationView: some View {
        VStack(spacing: 30) {
            Spacer()

            // 图标
            Image(systemName: "lock.shield.fill")
                .font(.system(size: 60))
                .foregroundColor(.blue)

            Text("家长验证")
                .font(.title)
                .fontWeight(.bold)

            Text("请回答问题继续")
                .font(.body)
                .foregroundColor(.secondary)

            // 数学题卡片
            VStack(spacing: 25) {
                // 数学题显示（使用简单的本地计算）
                Text("3 + 2 = ?")
                    .font(.system(size: 36, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)

                // 答案输入框
                TextField("输入答案", text: $userAnswer)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)
                    .frame(width: 140)
                    .multilineTextAlignment(.center)
                    .font(.system(size: 24))
                    .padding(.vertical, 10)

                // 确定按钮
                Button(action: {
                    if let answer = Int(userAnswer), answer == 5 {
                        // 正确答案
                        handleExtendTime()
                        userAnswer = ""
                    } else {
                        // 错误答案，清空输入
                        userAnswer = ""
                    }
                }) {
                    Text("确定")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(userAnswer.isEmpty ? Color.gray : Color.blue)
                        .cornerRadius(12)
                }
                .disabled(userAnswer.isEmpty)

                // 取消按钮
                Button("取消") {
                    handleCancelVerification()
                }
                .foregroundColor(.secondary)
            }
            .padding(25)
            .background(Color(.systemGray6))
            .cornerRadius(20)

            Spacer()
        }
        .padding()
    }

    /**
     * 处理取消验证
     */
    private func handleCancelVerification() {
        viewModelWrapper.cancelVerification()
    }

    /**
     * 时间到视图
     *
     * Spec 要求：
     * - 小火弹出语音："时间到啦！我们明天再玩吧！"
     * - 显示家长验证界面
     * - 答对 → 显示"再玩 5 分钟"按钮
     * - 取消 → App 退出到桌面
     */
    private var timeLimitView: some View {
        VStack(spacing: 30) {
            Spacer()

            // 小火头像（TODO: 使用实际图片）
            Image(systemName: "clock.fill")
                .font(.system(size: 70))
                .foregroundColor(.orange)
                .shadow(color: .orange.opacity(0.3), radius: 15)

            Text("时间到啦！")
                .font(.title)
                .fontWeight(.bold)

            Text("我们明天再玩吧！")
                .font(.body)
                .foregroundColor(.secondary)

            VStack(spacing: 15) {
                Button("验证继续") {
                    viewModelWrapper.showVerification()
                }
                .buttonStyle(.borderedProminent)
                .tint(.blue)

                Button("退出") {
                    handleExit()
                }
                .foregroundColor(.secondary)
            }

            Spacer()
        }
        .padding()
    }

    /**
     * 延长时间视图（验证成功后显示）
     */
    private var extendTimeView: some View {
        VStack(spacing: 30) {
            Spacer()

            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 70))
                .foregroundColor(.green)

            Text("验证成功！")
                .font(.title)
                .fontWeight(.bold)

            Text("可以再玩 5 分钟")
                .font(.body)
                .foregroundColor(.secondary)

            Button("继续") {
                handleExtendTime()
            }
            .buttonStyle(.borderedProminent)
            .tint(.green)

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
        return dailyStats[date] ?? 0
    }

    /**
     * 计算本周总使用时长（分钟）
     */
    private var totalWeeklyUsage: Int {
        var total: Int = 0
        for date in weekDays {
            total += dailyStats[date] ?? 0
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
    private func formatDuration(_ minutes: Int) -> String {
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
     * 处理延长使用时间
     */
    private func handleExtendTime() {
        viewModelWrapper.extendTime()
        // 延长时间后返回主地图
        coordinator.goBack()
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
        let initialState = viewModel.state as! ParentState
        super.init(viewModel: viewModel, initialState: initialState)
        subscribeState(viewModel.state)
    }

    /**
     * 验证答案 - 本地处理
     */
    func verifyAnswer(answer: Int) {
        // 使用本地验证，不发送到 Kotlin
    }

    /**
     * 延长使用时间 - 本地处理
     */
    func extendTime() {
        // 使用本地处理，不发送到 Kotlin
    }

    /**
     * 更新时长设置 - 本地处理
     */
    func updateDuration(minutes: Int) {
        // 使用本地处理，不发送到 Kotlin
    }

    /**
     * 重置进度 - 本地处理
     */
    func resetProgress() {
        // 使用本地处理，不发送到 Kotlin
    }

    /**
     * 显示验证界面 - 本地处理
     */
    func showVerification() {
        // 使用本地处理，不发送到 Kotlin
    }

    /**
     * 取消验证 - 本地处理
     */
    func cancelVerification() {
        // 使用本地处理，不发送到 Kotlin
    }

    /**
     * 返回按钮 - 本地处理
     */
    func onBackPressed() {
        // 使用本地处理，不发送到 Kotlin
    }
}

// MARK: - 预览

#Preview {
    ParentView()
        .environmentObject(AppCoordinator())
}
