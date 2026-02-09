# UI 优化文件快速修复策略

## 当前问题

三个优化文件都极不完整：
- CollectionScreenOptimized.kt: 399行 (需要 ~1340行) - 缺失率 70%
- WelcomeScreenOptimized.kt: 461行 (需要 ~800行) - 缺失率 42%
- MapScreenOptimized.kt: 976行 (需要 ~1500行) - 缺失率 35%

## 快速修复方案

### 方案1: 直接使用原始文件 (推荐立即使用)

修改 build.gradle.kts:
```kotlin
buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "false")
```

然后直接使用原始文件，放弃优化版本。

### 方案2: 简单包装器方案

创建简单的包装文件，直接委托给原始实现：

```kotlin
// CollectionScreenOptimized.kt
@Composable
fun CollectionScreenOptimized(
    viewModel: CollectionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    // 直接调用原始版本
    CollectionScreen(viewModel, onNavigateBack)
}
```

### 方案3: 渐进式增强

只添加最关键的优化功能：

1. 只添加触觉反馈
2. 只添加简单的淡入动画
3. 保持原始代码结构不变

### 方案4: 完整重写 (需要大量时间)

完全重写三个文件，添加所有优化功能：
- 预计需要添加 2000+ 行代码
- 预计耗时 2-4 小时
- 需要仔细测试

## 当前建议

**立即执行：**
1. 使用方案1（直接使用原始文件）
2. 设置 IS_USE_OPTIMIZED_UI = false
3. 确保应用正常工作

**后续考虑：**
- 如果时间充足，选择方案4进行完整重写
- 如果时间有限，选择方案3进行渐进式增强

## 操作命令

```bash
# 检查当前配置
./scripts/verify_ui_switch.sh check

# 切换到原始版本
./scripts/verify_ui_switch.sh switch-original

# 重新构建
./gradlew :composeApp:assembleDebug
```
