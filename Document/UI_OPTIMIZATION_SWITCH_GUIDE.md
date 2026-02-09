# UI 优化版本编译条件切换指南

## 概述

本项目支持通过编译条件在**原始 UI 版本**和**优化 UI 版本**之间切换。

## 配置方法

### 1. 修改 BuildConfig 字段

编辑 `composeApp/build.gradle.kts` 文件：

```kotlin
defaultConfig {
    // ... 其他配置 ...

    // BuildConfig 字段：是否使用优化后的 UI
    // 设置为 "true" 使用优化版本，设置为 "false" 使用原始版本
    buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "true")
}
```

### 2. 同步 Gradle

修改后执行：

```bash
./gradlew :composeApp:sync
```

或点击 Android Studio 的 "Sync Now"

## 使用 Screen Selector

项目中已创建了三个 Screen Selector 组件：

### 1. CollectionScreenSelector

位置：`composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/collection/CollectionScreenSelector.kt`

使用方式：
```kotlin
// 替换原来的调用
CollectionScreen(
    viewModel = viewModel,
    onNavigateBack = onNavigateBack
)

// 改为
CollectionScreenSelector(
    viewModel = viewModel,
    onNavigateBack = onNavigateBack
)
```

### 2. WelcomeScreenSelector

位置：`composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/welcome/WelcomeScreenSelector.kt`

使用方式：
```kotlin
// 替换原来的调用
WelcomeScreen(
    viewModel = viewModel,
    onNavigateToMap = onNavigateToMap
)

// 改为
WelcomeScreenSelector(
    viewModel = viewModel,
    onNavigateToMap = onNavigateToMap
)
```

### 3. MapScreenSelector

位置：`composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/map/MapScreenSelector.kt`

使用方式：
```kotlin
// 替换原来的调用
MapScreen(
    viewModel = viewModel,
    onNavigateToWelcome = onNavigateToWelcome,
    // ... 其他参数
)

// 改为
MapScreenSelector(
    viewModel = viewModel,
    onNavigateToWelcome = onNavigateToWelcome,
    // ... 其他参数
)
```

## 切换逻辑

Selector 组件内部的判断逻辑：

```kotlin
if (BuildConfig.IS_USE_OPTIMIZED_UI) {
    // 使用优化版本
    CollectionScreenOptimized(...)
} else {
    // 使用原始版本
    CollectionScreen(...)
}
```

## 优化版本文件

已创建的优化版本文件（可能需要进一步完善）：

1. `CollectionScreenOptimized.kt` - 收藏页面优化版本（不完整，需要补全组件）
2. `WelcomeScreenOptimized.kt` - 欢迎页面优化版本（不完整，需要补全组件）
3. `MapScreenOptimized.kt` - 地图页面优化版本（不完整，需要补全组件）

## 建议的工作流程

### 方案 A：保留原始版本为主版本（推荐当前使用）

1. 设置 `IS_USE_OPTIMIZED_UI = false`
2. 继续使用 `CollectionScreen`、`WelcomeScreen`、`MapScreen` 等原始函数
3. 逐步完善优化版本文件
4. 完成后切换 `IS_USE_OPTIMIZED_UI = true`

### 方案 B：立即使用 Selector

1. 将所有 `CollectionScreen` 调用替换为 `CollectionScreenSelector`
2. 根据 `IS_USE_OPTIMIZED_UI` 的值自动选择版本
3. 当优化版本不完整时，原始版本作为 fallback

## 注意事项

1. **优化版本不完整**：当前的 `CollectionScreenOptimized.kt` 等文件只包含部分组件，直接使用会导致编译错误
2. **Selector 已经创建**：三个 Selector 文件已经创建，可以随时使用
3. **BuildConfig 已配置**：`IS_USE_OPTIMIZED_UI` 字段已添加到 build.gradle.kts

## 快速验证步骤

1. 确保 BuildConfig 字段已配置：
   ```bash
   ./gradlew :composeApp:generateDebugBuildConfig
   ```

2. 验证 BuildConfig 生成：
   ```bash
   cat composeApp/build/generated/source/buildConfig/debug/com/cryallen/tigerfire/BuildConfig.java
   ```

3. 编译项目验证：
   ```bash
   ./gradlew :composeApp:compileDebugKotlin
   ```

## 总结

本项目已配置好编译条件切换机制：
- ✅ BuildConfig 字段 `IS_USE_OPTIMIZED_UI` 已添加
- ✅ 三个 Screen Selector 已创建
- ⚠️ 优化版本文件需要进一步完善

建议当前使用 **方案 A**（保留原始版本为主版本），待优化版本完善后再切换。
