# UI 优化版本切换快速指南

## 当前状态

✅ **已完成配置**:
- `BuildConfig.IS_USE_OPTIMIZED_UI` 已添加到 `build.gradle.kts`
- 三个 Selector 文件已创建：
  - `CollectionScreenSelector.kt`
  - `WelcomeScreenSelector.kt`
  - `MapScreenSelector.kt`

⚠️ **需要注意**:
- 优化版本文件 (`*Optimized.kt`) 目前不完整，只包含部分组件

## 快速操作

### 1. 检查当前配置

```bash
./scripts/verify_ui_switch.sh check
```

### 2. 切换版本

```bash
# 切换到原始版本
./scripts/verify_ui_switch.sh switch-original

# 切换到优化版本
./scripts/verify_ui_switch.sh switch-optimized
```

### 3. 重新构建

```bash
./gradlew :composeApp:assembleDebug
```

## 在代码中使用 Selector

### 修改前的代码：

```kotlin
import com.cryallen.tigerfire.ui.collection.CollectionScreen

// ...
CollectionScreen(
    viewModel = viewModel,
    onNavigateBack = onNavigateBack
)
```

### 修改后的代码：

```kotlin
import com.cryallen.tigerfire.ui.collection.CollectionScreenSelector

// ...
CollectionScreenSelector(
    viewModel = viewModel,
    onNavigateBack = onNavigateBack
)
```

对其他页面也是同样的修改方式：
- `WelcomeScreen` → `WelcomeScreenSelector`
- `MapScreen` → `MapScreenSelector`

## 手动修改 BuildConfig

如果需要手动修改配置，编辑 `composeApp/build.gradle.kts`：

```kotlin
defaultConfig {
    // ... 其他配置 ...

    // 设置为 "true" 使用优化版本，"false" 使用原始版本
    buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "false")
}
```

## 常见问题

### Q: 切换后需要清理构建吗？
A: 建议执行清理后重新构建：
```bash
./gradlew :composeApp:clean :composeApp:assembleDebug
```

### Q: 优化版本文件不完整会怎样？
A: 如果 `IS_USE_OPTIMIZED_UI = true` 但优化文件不完整，会导致编译错误。建议当前使用原始版本（设置为 `false`）。

### Q: 如何验证当前使用的是哪个版本？
A: 运行检查脚本：
```bash
./scripts/verify_ui_switch.sh check
```

## 下一步建议

1. **当前阶段**：使用原始版本（`IS_USE_OPTIMIZED_UI = false`）
2. **完善优化版本**：补充 `*Optimized.kt` 文件中的缺失组件
3. **测试验证**：切换为优化版本进行全面测试
4. **生产发布**：确认稳定后使用优化版本
