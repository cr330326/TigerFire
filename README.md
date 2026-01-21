# TigerFire (老虎消防车)

> 学前儿童消防安全教育应用

专为 3-6 岁儿童设计的互动式消防安全教育应用，通过"小火"（Little Fire）老虎消防员角色，引导儿童学习消防知识和应急技能。

## 项目简介

TigerFire 是一个使用 **Kotlin Multiplatform Mobile (KMM)** 技术开发的跨平台教育应用，实现了 Android 和 iOS 平台之间的业务逻辑共享，同时保持各自原生的 UI 体验。

### 核心特性

- **三个学习场景**：消防站、学校、森林
- **互动式学习**：点击设备观看教育视频、拖拽救援小游戏
- **徽章收集系统**：7 种基础徽章，支持颜色变体
- **家长控制**：使用时长管理、数学验证、使用统计
- **儿童友好设计**：大尺寸触控目标、语音引导、防误操作

### 技术栈

| 层级 | 技术选型 |
|------|---------|
| **跨平台框架** | Kotlin Multiplatform Mobile (KMM) |
| **Android UI** | Jetpack Compose + Material 3 |
| **iOS UI** | SwiftUI + Lottie-iOS |
| **本地存储** | SQLDelight |
| **异步处理** | Kotlin Coroutines + Flow |
| **动画** | Lottie (JSON 动画) |
| **视频播放** | ExoPlayer (Android) / AVPlayer (iOS) |

## 项目结构

```
TigerFire/
├── composeApp/              # KMM 共享模块
│   ├── src/
│   │   ├── commonMain/     # 平台无关代码
│   │   │   └── kotlin/com/cryallen/tigerfire/
│   │   │       ├── domain/           # 领域层
│   │   │       │   ├── model/        # 数据模型
│   │   │       │   └── repository/  # 仓储接口
│   │   │       ├── data/            # 数据层
│   │   │       │   ├── local/       # 本地存储
│   │   │       │   ├── repository/  # 仓储实现
│   │   │       │   └── resource/    # 资源管理
│   │   │       └── presentation/    # 表现层
│   │   │           ├── welcome/     # 欢迎页
│   │   │           ├── map/        # 地图导航
│   │   │           ├── firestation/# 消防站场景
│   │   │           ├── school/     # 学校场景
│   │   │           ├── forest/     # 森林场景
│   │   │           ├── collection/ # 徽章收藏
│   │   │           ├── parent/     # 家长模式
│   │   │           └── common/     # 通用组件
│   │   ├── androidMain/      # Android 平台代码
│   │   │   ├── ui/             # Compose UI
│   │   │   ├── navigation/     # 导航组件
│   │   │   ├── component/      # 平台组件
│   │   │   └── factory/        # ViewModel 工厂
│   │   └── iosMain/          # iOS 平台代码
│   │       └── ...            # iOS 特定实现
│   └── build.gradle.kts
├── iosApp/                   # iOS 应用入口
│   └── iosApp/              # SwiftUI UI + 导航协调器
├── Design/                   # 设计资源
│   ├── design-guide.md      # 设计规范
│   └── assets/              # 图片、视频、动画
└── Document/                # 项目文档
    ├── spec.md             # 功能规格
    ├── constitution.md     # 架构规范
    └── CLAUDE.md           # AI 开发指南
```

## 快速开始

### 环境要求

- **Kotlin**: 1.9.x
- **Android Studio**: Hedgehog (2023.1.3) 或更高版本
- **Xcode**: 14.0 或更高版本（iOS 开发）
- **Gradle**: 8.0+

### 构建 Android 应用

```bash
# 构建 Debug APK
./gradlew :composeApp:assembleDebug

# 安装到设备
./gradlew :composeApp:installDebug
```

### 构建 iOS 应用

```bash
# 使用 Xcode 打开 iOS 项目
open iosApp/iosApp

# 或使用命令行构建
cd iosApp/iosApp
xcodebuild -scheme TigerFire -configuration Debug build
```

## 场景说明

### 1. 消防站 (Fire Station)

儿童可以点击 4 个消防设备（灭火器、消防栓、云梯、水带），观看教育视频，学习每种设备的用途。完成所有 4 个设备后，解锁学校场景。

**徽章奖励**：每完成一个设备获得一枚徽章（4 种变体）

### 2. 学校 (School)

自动播放 45 秒应急响应动画，展示火灾发生时学校的正确应对流程。观看完成后获得徽章，解锁森林场景。

**徽章奖励**：完成观看获得学校徽章

### 3. 森林 (Forest)

拖拽直升机到着火点，放下云梯救援受困的小羊。成功救援 2 只小羊后获得徽章。

**徽章奖励**：每救援一只小羊获得一枚徽章（共 2 种）

## 核心功能

### 徽章系统

- **7 种基础徽章**：消防站 4 种 + 学校 1 种 + 森林 2 种
- **颜色变体**：每种徽章最多 4 种颜色变体
- **收集进度**：在"我的收藏"中查看所有获得的徽章
- **彩蛋动画**：集齐所有 7 种基础徽章后播放庆祝动画

### 场景解锁逻辑

```
消防站 (默认解锁)
    ↓
完成 4 个设备学习
    ↓
学校 (自动解锁)
    ↓
完成动画观看
    ↓
森林 (自动解锁)
```

### 家长控制

- **时长限制**：5/10/15/30 分钟可选
- **提前提醒**：时间到前 2 分钟语音提示
- **数学验证**：修改设置需回答简单数学题
- **使用统计**：查看本周每日使用时长
- **进度重置**：重置所有游戏进度

## 开发指南

### 架构原则

项目严格遵循 **Clean Architecture**：

1. **Domain 层** (`commonMain/domain`)
   - 业务实体和用例
   - 仓储接口定义
   - 平台无关

2. **Data 层** (`commonMain/data`)
   - 仓储实现
   - 本地数据存储 (SQLDelight)
   - 资源路径管理

3. **Presentation 层** (`commonMain/presentation`)
   - ViewModel (MVVM 模式)
   - 状态管理 (StateFlow)
   - 副作用处理 (Channel)

### 添加新功能

1. **Domain 层**：定义数据模型和业务规则
2. **Data 层**：实现数据持久化
3. **Presentation 层**：创建 ViewModel
4. **Android 层**：实现 Compose UI
5. **iOS 层**：实现 SwiftUI UI

### 测试

```bash
# 运行所有单元测试
./gradlew :composeApp:testDebugUnitTest

# 运行特定测试类
./gradlew :composeApp:testDebugUnitTest --tests "com.cryallen.tigerfire.domain.model.GameProgressTest"
```

### 资源管理

- **Lottie 动画**：`composeApp/src/commonMain/composeResources/files/lottie/`
- **视频文件**：`composeApp/src/androidMain/assets/videos/`
- **音频文件**：`composeApp/src/androidMain/assets/audio/`

## 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 冷启动时间 | ≤1.2s | 从点击图标到首屏显示 |
| 单场景内存 | ≤120 MB | 单个场景占用内存 |
| 安装包体积 | ≤300 MB | 包含所有资源 |
| 动画帧率 | ≥30 FPS | Lottie 动画流畅度 |

## 设计规范

### 儿童友好设计

- **触控目标**：≥100pt (主要图标 ≥120pt)
- **元素间距**：≥40pt
- **文字大小**：≥24pt
- **单点触控**：忽略多点触控事件

### 颜色方案

| 用途 | 颜色 | 十六进制 |
|------|------|----------|
| 消防站主色 | 红 | `#E63946` |
| 学校主色 | 蓝 | `#457B9D` |
| 森林主色 | 绿 | `#2A9D8F` |
| 强调色 | 黄 | `#F4A261` |

## 常见问题

### Q: 为什么选择 KMM 而不是 Flutter/React Native？

A: KMM 允许我们共享核心业务逻辑，同时使用原生 UI 框架（Jetpack Compose 和 SwiftUI），提供最佳的用户体验和性能。

### Q: 如何添加新的 Lottie 动画？

A: 将 JSON 文件放入 `composeApp/src/commonMain/composeResources/files/lottie/`，然后在代码中使用 `LottieAnimationPlayer` 组件播放。

### Q: 数据存储在哪里？

A: 使用 SQLDelight 进行本地存储：
- Android: `/data/data/com.cryallen.tigerfire/databases/`
- iOS: 应用沙盒的 Documents 目录

### Q: 如何配置不同的使用时长？

A: 进入家长模式 → 时长设置 → 选择 5/10/15/30 分钟。需要回答数学题验证身份。

## 许可证

本项目为教育用途开发。

## 贡献

欢迎提交 Issue 和 Pull Request。

---

**TigerFire** - 让消防安全教育变得有趣！
