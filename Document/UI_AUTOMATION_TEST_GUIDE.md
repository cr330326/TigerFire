# TigerFire UI自动化测试文档

## 📚 概述

本文档介绍TigerFire应用的UI自动化测试框架、测试用例和执行方法。

## 🎯 测试目标

通过自动化UI测试验证以下功能:
- ✅ 应用导航流程正常
- ✅ 徽章收集功能正确
- ✅ 家长模式功能完整
- ✅ 应用性能达标
- ✅ 无内存泄漏和崩溃

## 🏗️ 测试架构

### 技术栈
- **测试框架**: Jetpack Compose UI Test
- **断言库**: JUnit 4
- **UI自动化**: Espresso + UiAutomator
- **构建工具**: Gradle
- **语言**: Kotlin

### 测试类型
1. **功能测试**: 验证业务逻辑正确性
2. **导航测试**: 验证页面跳转和导航流程
3. **性能测试**: 测试启动时间、场景切换性能
4. **压力测试**: 测试快速点击、连续操作的稳定性

## 📁 项目结构

```
composeApp/
├── src/
│   ├── androidMain/          # Android主代码
│   ├── androidTest/          # UI自动化测试
│   │   └── kotlin/
│   │       └── com/cryallen/tigerfire/ui/
│   │           ├── AppNavigationTest.kt      # 导航流程测试
│   │           ├── BadgeCollectionTest.kt    # 徽章收集测试
│   │           ├── ParentModeTest.kt         # 家长模式测试
│   │           └── PerformanceTest.kt        # 性能压力测试
│   └── commonMain/           # 跨平台共享代码
└── build.gradle.kts          # 测试依赖配置
```

## 🧪 测试用例详情

### 1. AppNavigationTest - 导航流程测试

| 测试方法 | 测试内容 | 验证点 |
|---------|---------|--------|
| `test_app_launches_successfully` | App启动和欢迎页显示 | 欢迎页元素存在 |
| `test_navigate_from_welcome_to_map` | 从欢迎页到主地图 | 主地图元素显示 |
| `test_navigate_to_fire_station` | 导航到消防站场景 | 消防站设备显示 |
| `test_navigate_to_school` | 导航到学校场景 | 学校设备显示 |
| `test_navigate_to_forest` | 导航到森林场景 | 森林设备显示 |
| `test_back_navigation` | 返回按钮导航 | 正确返回上一页 |

**关键代码示例:**
```kotlin
@Test
fun test_navigate_to_fire_station() {
    navigateToMap()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithContentDescription("消防站", substring = true)
            .fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithContentDescription("消防站", substring = true).performClick()

    // 验证进入消防站场景
    composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithContentDescription("消防栓", substring = true)
            .fetchSemanticsNodes().isNotEmpty()
    }
}
```

### 2. BadgeCollectionTest - 徽章收集测试

| 测试方法 | 测试内容 | 验证点 |
|---------|---------|--------|
| `test_play_device_video_and_collect_badge` | 播放视频获取徽章 | 视频播放、徽章显示 |
| `test_view_collection_page` | 查看收藏页面 | 收藏页面元素正确 |
| `test_collect_different_badge_variants` | 收集不同变体徽章 | 多次观看获得不同变体 |

**测试流程:**
1. 进入场景
2. 点击设备图标
3. 等待视频播放完成（8秒）
4. 验证徽章弹窗
5. 检查收藏页面

### 3. ParentModeTest - 家长模式测试

| 测试方法 | 测试内容 | 验证点 |
|---------|---------|--------|
| `test_enter_parent_mode` | 进入家长模式 | 家长模式页面显示 |
| `test_view_usage_statistics` | 查看使用时长统计 | 统计数据显示 |
| `test_set_time_limit` | 设置时间限制 | 设置功能可用 |
| `test_exit_parent_mode` | 退出家长模式 | 正确返回主页 |

### 4. PerformanceTest - 性能压力测试

| 测试方法 | 测试内容 | 性能指标 |
|---------|---------|---------|
| `test_app_launch_time` | 应用启动时间 | < 5秒 |
| `test_rapid_click_protection` | 快速点击防抖 | 防止重复触发 |
| `test_scene_switching_performance` | 场景切换性能 | < 3秒/次 |
| `test_continuous_navigation_stress` | 连续导航压力测试 | 10次迭代无崩溃 |
| `test_memory_stability` | 内存稳定性测试 | 5次循环无泄漏 |

**性能测试示例:**
```kotlin
@Test
fun test_app_launch_time() {
    val launchTime = measureTimeMillis {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("开始游戏", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    println("📊 App启动时间: ${launchTime}ms")
    assert(launchTime < 5000) { "App启动时间过长: ${launchTime}ms" }
}
```

## 🚀 执行测试

### 方式1: 使用测试脚本（推荐）

```bash
# 进入项目根目录
cd /Users/vsh9p8q/Personal/Project/TigerTruck/TigerFire

# 执行所有UI测试
bash scripts/run_ui_tests.sh
```

**脚本功能:**
- ✅ 自动检查设备连接
- ✅ 清理旧测试数据
- ✅ 编译和安装测试APK
- ✅ 执行所有测试用例
- ✅ 生成HTML和Markdown测试报告
- ✅ 自动打开测试报告（Mac）

### 方式2: 使用Gradle命令

```bash
# 执行所有连接设备的测试
./gradlew connectedDebugAndroidTest

# 执行特定测试类
./gradlew connectedDebugAndroidTest --tests "*.AppNavigationTest"

# 执行特定测试方法
./gradlew connectedDebugAndroidTest --tests "*.AppNavigationTest.test_app_launches_successfully"
```

### 方式3: 在Android Studio中执行

1. 打开Android Studio
2. 导航到测试文件（如`AppNavigationTest.kt`）
3. 右键点击测试类或方法
4. 选择"Run 'test_xxx'"

## 📊 测试报告

### 报告位置
测试完成后会生成以下报告:

```
test-reports/ui-tests/
├── test-report-YYYYMMDD_HHMMSS.md    # Markdown格式报告
└── test-output-YYYYMMDD_HHMMSS.log   # 详细测试日志

composeApp/build/reports/androidTests/connected/
└── index.html                         # HTML格式详细报告
```

### HTML报告内容
- 测试用例执行结果（通过/失败）
- 每个测试的执行时间
- 失败测试的堆栈跟踪
- 设备信息和测试统计

### Markdown报告内容
- 测试概览（日期、设备、状态）
- 测试结果汇总
- 测试覆盖范围
- 性能指标
- 失败原因分析（如有）

## ⚙️ 测试配置

### build.gradle.kts配置

```kotlin
android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // UI测试框架
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

### libs.versions.toml配置

```toml
[versions]
androidx-espresso = "3.7.0"
androidx-testExt = "1.3.0"
androidx-uiautomator = "2.3.0"
compose-uitest = "1.8.0"

[libraries]
androidx-testExt-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-testExt" }
androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "androidx-espresso" }
androidx-uiautomator = { module = "androidx.test.uiautomator:uiautomator", version.ref = "androidx-uiautomator" }
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4", version.ref = "compose-uitest" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest", version.ref = "compose-uitest" }
```

## 🔧 测试环境要求

### 硬件要求
- Android设备或模拟器（Android 7.0+）
- 至少2GB RAM
- USB调试已启用

### 软件要求
- Android SDK Platform Tools
- JDK 11+
- Gradle 8.0+
- ADB (Android Debug Bridge)

### 设备设置
```bash
# 启用USB调试
设置 → 开发者选项 → USB调试

# 验证设备连接
adb devices

# 确认设备已连接
List of devices attached
DEVICE_ID    device
```

## 🐛 常见问题

### 1. 测试超时
**问题**: 测试用例执行超时
**解决**: 增加`waitUntil`的`timeoutMillis`参数

```kotlin
composeTestRule.waitUntil(timeoutMillis = 10000) {
    // 条件检查
}
```

### 2. 找不到UI元素
**问题**: `onNodeWithText`或`onNodeWithContentDescription`找不到元素
**解决**:
- 检查UI元素的`contentDescription`是否正确设置
- 使用`substring = true`进行模糊匹配
- 使用`printToLog()`打印语义树

```kotlin
composeTestRule.onRoot().printToLog("TAG")
```

### 3. 设备未连接
**问题**: `adb devices`显示no devices
**解决**:
- 检查USB连接
- 重启ADB: `adb kill-server && adb start-server`
- 检查设备驱动程序

### 4. 编译失败
**问题**: Gradle编译测试APK失败
**解决**:
- 清理构建: `./gradlew clean`
- 重新同步依赖: `./gradlew --refresh-dependencies`
- 检查网络连接（下载依赖）

## 📈 测试最佳实践

### 1. 使用语义选择器
优先使用`contentDescription`而非`text`进行元素查找:

```kotlin
// 推荐
composeTestRule.onNodeWithContentDescription("消防站").performClick()

// 避免（除非必要）
composeTestRule.onNodeWithText("消防站").performClick()
```

### 2. 添加等待机制
避免直接操作，使用`waitUntil`等待元素出现:

```kotlin
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onAllNodesWithContentDescription("目标元素")
        .fetchSemanticsNodes().isNotEmpty()
}
```

### 3. 提取公共辅助函数
将重复代码提取为辅助函数:

```kotlin
private fun navigateToMap() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithText("开始游戏", substring = true, ignoreCase = true)
            .fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithText("开始游戏", substring = true, ignoreCase = true).performClick()
    Thread.sleep(2000)
}
```

### 4. 添加详细日志
在关键步骤添加日志输出:

```kotlin
println("📊 App启动时间: ${launchTime}ms")
println("✅ 测试步骤完成")
```

### 5. 测试隔离
每个测试应该独立，不依赖其他测试的状态。可以在测试开始时清理应用数据:

```bash
adb shell pm clear com.cryallen.tigerfire
```

## 🔗 相关资源

- [Jetpack Compose测试文档](https://developer.android.com/jetpack/compose/testing)
- [Espresso测试框架](https://developer.android.com/training/testing/espresso)
- [UiAutomator文档](https://developer.android.com/training/testing/other-components/ui-automator)
- [Android测试最佳实践](https://developer.android.com/training/testing/fundamentals)

## 📝 更新日志

### 2026-01-30
- ✅ 初始版本发布
- ✅ 添加4个测试类，共18个测试用例
- ✅ 配置测试框架和依赖
- ✅ 创建自动化测试执行脚本
- ✅ 支持HTML和Markdown测试报告

---

**文档维护**: TigerFire开发团队
**最后更新**: 2026-01-30
