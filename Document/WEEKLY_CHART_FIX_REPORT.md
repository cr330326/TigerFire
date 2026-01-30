# ParentScreen 本周使用柱状图修复报告

## 修复日期
2026-01-30

## 问题描述
本周使用柱状图显示"暂无数据",即使今天有使用记录也不显示。

## 根本原因
`getLast7DaysMinutes()` 函数使用了错误的日期范围:
```kotlin
// ❌ 错误代码（修复前）
for (i in 1..7) {  // 从昨天开始，排除今天
    calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
    // ...
}
```

这导致:
- 循环从 `i=1` 开始,表示昨天
- 循环到 `i=7`,表示7天前
- **结果:显示昨天到7天前的数据,排除了今天**

## 修复方案
修改循环范围,从今天(i=0)开始:

```kotlin
// ✅ 正确代码（修复后）
for (i in 0..6) {  // 从今天开始，包含今天
    calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
    // ...
}
```

现在:
- `i=0`:今天
- `i=1`:昨天
- `i=2`:前天
- ...
- `i=6`:6天前
- **结果:显示最近7天的数据,包括今天**

## 修改的文件
- [ParentScreen.kt](composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/parent/ParentScreen.kt#L763)
  - 第763行:修改循环范围从 `for (i in 1..7)` 为 `for (i in 0..6)`

## 测试结果

### 测试步骤
1. 启动应用并进入任一场景（消防站/学校/森林）
2. 在场景中停留2-3分钟(让 SessionTimer 记录使用时长)
3. 返回主页面(触发 `endSessionAndRecord()`)
4. 进入家长模式
5. 查看「本周使用」柱状图

### 验证要点
- ✅ 柱状图显示7个柱子（从左到右:6天前→今天）
- ✅ 最后一个柱子（今天）有绿色渐变（表示有数据）
- ✅ 柱子高度 > 4dp（不是灰色最小高度）
- ✅ 总计时长 > 0
- ✅ 数据与今日使用时长匹配

### 测试结果
**✅ 所有测试通过**

## 相关技术细节

### 数据记录流程
1. **场景游玩**: SessionTimer 开始计时
2. **退出场景**: 调用 `SessionTimer.endSessionAndRecord()`
3. **记录数据**:
   ```kotlin
   // SessionTimer.kt line 217
   progressRepository.recordUsage(todayDate, finalElapsed)
   ```
4. **保存到数据库**:
   ```kotlin
   // ParentSettings.kt line 73-75
   val currentDuration = dailyUsageStats[date] ?: 0L
   copy(dailyUsageStats = dailyUsageStats + (date to (currentDuration + durationMillis)))
   ```

### 日期格式
- **格式**: `yyyy-MM-dd` (例如: `2026-01-30`)
- **获取方式**:
  ```kotlin
  // ParentDateUtilsActual.kt
  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  return dateFormat.format(Date())
  ```

### 数据显示
```kotlin
// ParentViewModel.kt line 59
val todayPlayTime = settings.dailyUsageStats[today] ?: 0L

// ParentViewModel.kt line 62
val totalPlayTime = settings.dailyUsageStats.values.sum()
```

## 影响范围
- ✅ 本周使用柱状图显示正确
- ✅ 总计时长计算正确（包含今天）
- ✅ 不影响其他功能
- ✅ 向后兼容（已有数据仍正确显示）

## 关联修复
本次修复是 ParentScreen 系列修复的最后一个,完成了所有统计显示问题:

1. **总使用时长显示0** ✅ 已修复
   - 从 `progress.totalPlayTime` 改为 `settings.dailyUsageStats.values.sum()`

2. **重置游戏进度不工作** ✅ 已修复
   - `executeResetProgress()` 现在正确获取并更新 initialSettings

3. **本周使用柱状图无数据** ✅ 已修复（本次）
   - 日期范围从 `i=1..7` 改为 `i=0..6`

## 测试脚本
创建了以下测试脚本帮助验证:
- [test_weekly_chart.sh](scripts/test_weekly_chart.sh) - 本周柱状图验证脚本
- [add_test_usage_data.sh](scripts/add_test_usage_data.sh) - 添加测试使用数据指南

## 结论
本次修复成功解决了本周使用柱状图不显示今天数据的问题。通过修改日期范围从 `for (i in 1..7)` 为 `for (i in 0..6)`,确保柱状图正确显示包括今天在内的最近7天使用数据。

所有测试通过,功能正常,ParentScreen 统计显示问题已全部修复完成。
