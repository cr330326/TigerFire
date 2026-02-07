# TigerFire (老虎消防车)

> 学前儿童消防安全教育应用

专为 **3-6 岁儿童**设计的互动式消防安全教育应用，通过"**小火**"（Little Fire）老虎消防员 IP 角色，引导儿童学习消防知识和应急技能。

---

## 目录

- [项目简介](#项目简介)
- [快速开始](#快速开始)
- [场景说明](#场景说明)
- [核心功能](#核心功能)
- [开发指南](#开发指南)
- [性能指标](#性能指标)
- [项目截图](#项目截图)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 项目简介

TigerFire 是一个使用 **Kotlin Multiplatform Mobile (KMM)** 技术开发的跨平台教育应用，实现了 Android 和 iOS 平台之间的业务逻辑共享，同时保持各自原生的 UI 体验。

### IP 角色设定：小火

| 属性 | 设定 |
|------|------|
| **形象** | 一只开消防车的老虎卡通角色，全动画呈现 |
| **名字** | 小火 |
| **性格** | 小孩子调皮风格，像小朋友的朋友 |
| **说话风格** | 正常语速，带停顿；鼓励型回复（如"再靠近一点点哦！""你记得真牢！"） |
| **出现方式** | 关键时刻弹出（如通关、提示、庆祝） |
| **呈现形式** | 全动画角色，2D 扁平风格 |

### 核心特性

- **三个学习场景**：消防站、学校、森林（渐进式解锁）
- **互动式学习**：点击设备观看教育视频、点击救援小游戏
- **徽章收集系统**：7 种基础徽章，最多 4 种颜色变体，集齐解锁彩蛋动画
- **家长控制**：使用时长管理（5/10/15/30 分钟）、数学验证、使用统计图表
- **儿童友好设计**：大尺寸触控目标（≥100pt）、语音引导、防误操作、无疯狂点击限制

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
TigerFire/                              # KMM 项目根目录
├── composeApp/                         # 共享 Kotlin 模块
│   ├── src/
│   │   ├── commonMain/                # 平台无关代码
│   │   │   ├── kotlin/com/cryallen/tigerfire/
│   │   │   │   ├── domain/            # 领域层（业务规则）
│   │   │   │   │   ├── model/         # 数据模型（SceneType, Badge, GameProgress）
│   │   │   │   │   ├── usecase/       # 用例层
│   │   │   │   │   └── repository/    # 仓储接口
│   │   │   │   ├── data/              # 数据层
│   │   │   │   │   ├── local/         # SQLDelight 数据库
│   │   │   │   │   ├── repository/    # 仓储实现
│   │   │   │   │   └── resource/      # 资源路径管理
│   │   │   │   ├── presentation/      # 表现层（ViewModels）
│   │   │   │   │   ├── welcome/       # 欢迎页
│   │   │   │   │   ├── map/           # 地图导航
│   │   │   │   │   ├── firestation/   # 消防站场景
│   │   │   │   │   ├── school/        # 学校场景
│   │   │   │   │   ├── forest/        # 森林场景
│   │   │   │   │   ├── collection/    # 徽章收藏
│   │   │   │   │   ├── parent/        # 家长模式
│   │   │   │   │   └── common/        # 共享 UI 组件
│   │   │   │   ├── factory/           # ViewModel 工厂
│   │   │   │   └── ui/theme/          # Compose 主题配置
│   │   │   └── composeResources/      # Compose 资源
│   │   │       └── files/
│   │   │           └── lottie/        # Lottie 动画
│   │   ├── androidMain/               # Android 平台代码
│   │   │   ├── kotlin/.../ui/         # Compose UI 屏幕
│   │   │   ├── kotlin/.../navigation/ # 导航设置
│   │   │   ├── kotlin/.../component/  # 平台组件
│   │   │   └── assets/
│   │   │       ├── videos/            # MP4 视频文件
│   │   │       │   ├── firestation/   # 4 个设备教学视频
│   │   │       │   ├── school/        # 1 个剧情动画
│   │   │       │   ├── forest/        # 2 个救援片段
│   │   │       │   └── celebration.mp4
│   │   │       └── audio/             # 音频文件
│   │   └── androidTest/               # Android UI 测试
│   └── build.gradle.kts
├── iosApp/                            # iOS 应用入口
│   └── iosApp/                        # SwiftUI UI + 导航
├── specs/                             # 项目规格文档
│   ├── spec.md                        # 完整功能规格
│   ├── plan.md                        # 技术实现方案
│   └── tasks.md                       # 详细任务分解
├── document/                          # 项目文档
│   ├── E2E_TEST_GUIDE.md              # 端到端测试指南
│   ├── UI_AUTOMATION_TEST_GUIDE.md    # UI 自动化测试指南
│   ├── TESTING_CHECKLIST.md           # 测试清单
│   └── constitution.md                # 架构规范（不可变）
├── scripts/                           # 构建与测试脚本
│   ├── e2e_test.sh                    # 端到端测试
│   ├── run_ui_tests.sh                # UI 自动化测试
│   ├── test_badge_fix.sh              # 徽章系统测试
│   ├── test_parent_screen.sh          # 家长模式测试
│   ├── verify_database.sh             # 数据库验证
│   └── monitor_badge_realtime.sh      # 实时徽章监控
├── constitution.md                    # 项目宪法（不可变规则）
├── CLAUDE.md                          # AI 开发指南
└── README.md                          # 本文件
```

## 快速开始

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17 或更高 | 推荐使用 JDK 17 LTS |
| **Kotlin** | 1.9.x | 与 Gradle 版本兼容 |
| **Android Studio** | Hedgehog (2023.1.3) 或更高 | 推荐 Iguana (2024.1.1) |
| **Xcode** | 14.0 或更高 | iOS 开发必需 |
| **Gradle** | 8.0+ | 项目已配置 Gradle Wrapper |
| **Android SDK** | API 24+ (Android 7.0) | 最低支持版本 |

#### 验证环境

```bash
# 检查 JDK 版本
java -version

# 检查 Kotlin 版本
kotlinc -version

# 检查 Android SDK
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list

# 检查 Xcode 版本（macOS）
xcodebuild -version
```

### 构建 Android 应用

```bash
# 清理并构建 Debug APK
./gradlew clean :composeApp:assembleDebug

# 安装到已连接设备
./gradlew :composeApp:installDebug

# 查看可用构建变体
./gradlew :composeApp:tasks --all | grep assemble

# 构建 Release APK（需要签名配置）
./gradlew :composeApp:assembleRelease
```

### 构建 iOS 应用

```bash
# 方式一：使用 Xcode 打开（推荐）
open iosApp/iosApp.xcodeproj

# 方式二：使用命令行构建
# 先确保已配置好签名和 Team
cd iosApp
xcodebuild -project iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  build

# 安装到真机（需要连接设备并配置签名）
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -destination 'platform=iOS,name=你的设备名称' \
  install
```

### 运行测试

```bash
# 运行 Android 单元测试
./gradlew :composeApp:testDebugUnitTest

# 运行特定测试类
./gradlew :composeApp:testDebugUnitTest --tests "com.cryallen.tigerfire.domain.model.GameProgressTest"

# 运行 Android 连接测试（需要连接设备）
./gradlew :composeApp:connectedDebugAndroidTest

# 运行 iOS 测试
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  test
```

## 场景说明

### 场景解锁流程

```
┌─────────────────────────────────────────────────────────────────┐
│  消防站 (默认解锁)                                               │
│      ↓                                                          │
│  完成 4 个设备学习（灭火器、消防栓、云梯、水枪）                  │
│      ↓                                                          │
│  学校 (自动解锁)                                                 │
│      ↓                                                          │
│  完成动画观看 (School_Fire_Safety_Knowledge.mp4)                 │
│      ↓                                                          │
│  森林 (自动解锁)                                                 │
│      ↓                                                          │
│  救援 2 只小羊（rescue_sheep_1.mp4、rescue_sheep_2.mp4）         │
└─────────────────────────────────────────────────────────────────┘
```

### 1. 消防站 (Fire Station)

**玩法**：儿童可以点击 4 个消防设备（灭火器、消防栓、云梯、水枪），观看教育视频，学习每种设备的用途。

**交互流程**：
1. 点击未学习设备 → 图标缩放动画 + 点击音效（"咔"）
2. 播放对应 MP4 教学视频（15 秒）
3. 视频播放完毕 → 设备图标变为"星星点亮"状态（金色闪烁）
4. 弹出徽章获得动画 + 播放成功音效
5. 重复直至 4 个设备全部完成

**徽章奖励**：每完成一个设备获得 1 枚徽章，重复通关可获得颜色变体（红/黄/蓝/绿）

**教学视频**：
| 设备 | 视频文件 | 时长 | 内容 |
|------|---------|------|------|
| 灭火器 | firestation_extinguisher.mp4 | 15秒 | 展示灭火器外观 → 拔保险销 → 喷射灭火 |
| 消防栓 | firestation_hydrant.mp4 | 15秒 | 展示消防栓 → 连接水带 → 打开水阀门 |
| 云梯 | firestation_ladder.mp4 | 15秒 | 展示云梯伸展 → 消防员登高 → 救援演示 |
| 水枪 | firestation_hose.mp4 | 15秒 | 展示水枪 → 握持姿势 → 喷水控制 |

### 2. 学校 (School)

**玩法**：观看剧情动画，了解火灾发生时学校的正确应对流程。

**交互流程**：
1. 进入场景 → 自动触发警报音效 + 屏幕边缘红光闪烁
2. 小火语音提示："学校着火啦！快叫消防车！"
3. 显示超大播放按钮图标（屏幕中央，≥150pt）
4. 点击播放按钮 → 警报音效停止 → 红光停止闪烁
5. 播放视频 `School_Fire_Safety_Knowledge.mp4`（45 秒）
6. 视频播放完毕 → 弹出小火点赞动画 + 语音："你真棒！记住，着火要找大人帮忙！"
7. 弹出 1 枚徽章 + 播放成功音效 → 自动返回主地图

**徽章奖励**：完成观看获得学校徽章，重复观看可获得边框颜色变体

**剧情动画**：小朋友发现火 → 打 119 → 消防车到达 → 喷水灭火 → 排队撤离

### 3. 森林 (Forest)

**玩法**：点击小羊触发直升机救援，放下云梯救出受困的小羊。

**交互流程**：
1. 进入场景 → 小火语音提示："小羊被困啦！快开直升机救它们！"
2. 屏幕左侧显示直升机图标（≥150pt），持续播放螺旋桨飞行动画
3. 屏幕右侧显示两只小羊图标，小羊周边被火苗包围
4. 点击小羊图标1 → 直升机缓慢飞行到小羊图标1上方 → 显示放下梯子按钮（≥100pt）
5. 点击放下梯子按钮 → 播放 `rescue_sheep_1.mp4`（10 秒）
6. 视频结束 → 小羊消失 + 弹出徽章 + 成功音效
7. 重复流程救援小羊2 → 播放 `rescue_sheep_2.mp4`
8. 两个小羊都救出 → 播放庆祝动画 + 语音总结："直升机能从天上救人，真厉害！"

**徽章奖励**：每救援一只小羊获得 1 枚徽章（共 2 枚），重复通关可获得小羊表情变体

**救援视频**：
| 视频 | 文件名 | 时长 | 内容 |
|------|--------|------|------|
| 小羊1 | rescue_sheep_1.mp4 | 10秒 | 小羊爬上梯子 → 直升机飞离 |
| 小羊2 | rescue_sheep_2.mp4 | 10秒 | 小羊爬上梯子 → 直升机飞离 |

## 核心功能

### 徽章系统

**基础徽章清单**：

| 场景 | 徽章类型 | 数量 | 变体规则 | 最大数量 |
|------|---------|------|---------|---------|
| 消防站 | 4 个设备 | 4 枚 | 红色、黄色、蓝色、绿色 | 16 枚 (4×4) |
| 学校 | 剧情动画 | 1 枚 | 不同边框颜色 | 4 枚 (1×4) |
| 森林 | 2 只小羊 | 2 枚 | 不同小羊表情 | 8 枚 (2×4) |
| **总计** | **7 种基础** | **7 枚** | - | **28 枚** |

**变体分配算法**：
```kotlin
fun calculateNextVariant(badges: List<Badge>, baseType: String): Int {
    val existingCount = badges.count { it.baseType == baseType }
    return existingCount % MAX_VARIANTS_PER_TYPE  // MAX_VARIANTS_PER_TYPE = 4
}
```

**集齐奖励**：集齐 7 种基础徽章后，解锁隐藏彩蛋动画（小火跳舞 + 放烟花庆祝，20 秒）

**徽章展示页面**（我的收藏）：
- 点击主地图左上角小火头像进入
- 按场景分组显示已获得徽章
- 未获得徽章显示为灰色轮廓
- 已获得徽章显示完整图标 + 变体颜色标识

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

**入口与验证**：
- **入口位置**：主地图右上角齿轮图标 ⚙️（半透明，不抢眼）
- **验证方式**：数学题验证（如 "5 + 3 = ?"，6 岁以下儿童大概率不会）

**时间控制设置**：

| 设置项 | 选项 |
|-------|------|
| 每次使用时长 | 5 分钟 / 10 分钟 / **15 分钟（默认）** / 30 分钟 |
| 提前提醒 | 提前 2 分钟语音提示 |

**时间到处理流程**：
```
1. 小火弹出语音："时间到啦！我们明天再玩吧！"
2. 屏幕显示家长验证界面："输入数学题继续"
   ├─ 答对 → 显示"再玩 5 分钟"按钮
   └─ 取消 → App 退出到桌面
```

**使用统计**：
- 图表展示：本周每日使用时长柱状图
- 示例：
```
    本周使用时长
    ┌─────────────────────────────────┐
    │ ▁▃▄▂▁█▅                         │
    │ 一二三四五六日                   │
    │ 总计：1小时23分钟                │
    └─────────────────────────────────┘
```

**进度管理**：
- **重置进度按钮**：清空所有场景进度和徽章收藏，重新开始
- 确认弹窗："确定要重置所有进度吗？此操作不可撤销。"

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

### 测试脚本

项目提供了多个测试脚本，位于 `scripts/` 目录：

| 脚本 | 用途 |
|------|------|
| `e2e_test.sh` | 端到端测试 |
| `run_ui_tests.sh` | UI 自动化测试 |
| `test_badge_fix.sh` | 徽章系统测试 |
| `test_parent_screen.sh` | 家长模式测试 |
| `test_weekly_chart.sh` | 周统计图表测试 |
| `test_forest_fixes.sh` | 森林场景测试 |
| `test_back_button.sh` | 返回按钮测试 |
| `verify_database.sh` | 数据库验证 |
| `monitor_badge_realtime.sh` | 实时徽章监控 |
| `debug_crash.sh` | 崩溃调试 |
| `verify_fix.sh` | 修复验证 |

**运行测试**：
```bash
# 运行所有单元测试
./gradlew :composeApp:testDebugUnitTest

# 运行特定测试类
./gradlew :composeApp:testDebugUnitTest --tests "com.cryallen.tigerfire.domain.model.GameProgressTest"

# 运行端到端测试
./scripts/e2e_test.sh

# 运行 UI 自动化测试
./scripts/run_ui_tests.sh
```

### 资源管理

- **Lottie 动画**：`composeApp/src/commonMain/composeResources/files/lottie/`
- **视频文件**：`composeApp/src/androidMain/assets/videos/`
- **音频文件**：`composeApp/src/androidMain/assets/audio/`

## 性能指标

### 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 冷启动时间 | ≤1.2s | 从点击图标到首屏显示 |
| 单场景内存 | ≤120 MB | 单个场景占用内存 |
| 安装包体积 | ≤300 MB | 包含所有资源 |
| 动画帧率 | ≥30 FPS | Lottie 动画流畅度 |

### 稳定性指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| **崩溃率** | ≤0.1% | 端到端测试中崩溃次数不超过千分之一 |
| **白屏率** | ≤0.05% | 任何场景下出现白屏的概率不超过万分之五 |
| **ANR 率（Android）** | ≤0.1% | 主线程阻塞导致的 ANR 不超过千分之一 |
| **内存泄漏** | 零容忍 | 单场景内存占用应稳定，长时间运行无持续增长 |
| **恢复能力** | 自动恢复 | 崩溃后重启应能恢复到最后正常状态 |

### 兼容性要求

| 平台 | 最低版本 | 推荐版本 |
|------|----------|----------|
| Android | API 24 (Android 7.0) | API 30+ (Android 11+) |
| iOS | iOS 14.0 | iOS 16+ |

### 崩溃日志系统

应用内置崩溃日志记录功能：
- **存储位置**：
  - Android: `filesDir/crash_logs/`
  - iOS: `Application Support/crash_logs/`
- **日志格式**：JSON 文件，便于分析
- **文件命名**：`crash_[timestamp]_[device_id].log`
- **保留数量**：最多保留 20 个日志文件，超过则自动删除最旧的
- **单文件大小**：单个日志文件不超过 100 KB
- **日志内容**：应用版本、设备型号、OS 版本、崩溃类型、堆栈跟踪、场景信息、用户操作、内存使用情况

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

## 常见问题 (FAQ)

### Q: 为什么选择 KMM 而不是 Flutter/React Native？

A: KMM 允许我们共享核心业务逻辑，同时使用原生 UI 框架（Jetpack Compose 和 SwiftUI），提供最佳的用户体验和性能。此外，Kotlin 与 Android 生态系统无缝集成，对于 Android 原生开发者更加友好。

### Q: 如何添加新的 Lottie 动画？

A: 将 JSON 文件放入 `composeApp/src/commonMain/composeResources/files/lottie/`，然后在代码中使用 `LottieAnimationPlayer` 组件播放。如果动画加载失败，会自动降级为静态图 + 语音描述。

### Q: 数据存储在哪里？

A: 使用 SQLDelight 进行本地存储：
- Android: `/data/data/com.cryallen.tigerfire/databases/`
- iOS: 应用沙盒的 Documents 目录

### Q: 如何配置不同的使用时长？

A: 进入家长模式 → 时长设置 → 选择 5/10/15/30 分钟。需要回答数学题验证身份（默认设置为 15 分钟）。

### Q: 应用是否需要网络连接？

A: 不需要。TigerFire 完全离线运行，不访问任何网络资源，不收集任何用户数据。

### Q: 如何重置游戏进度？

A: 进入家长模式 → 点击"重置进度"按钮 → 确认操作。此操作不可撤销，将清空所有场景进度和徽章收藏。

### Q: 为什么视频切后台后从头播放？

A: 3-6 岁儿童注意力分散，中断后可能忘记前文。从头播放确保知识完整接收。视频时长 ≤45 秒，重复观看负担小。

## 项目文档

### 核心文档

| 文档 | 说明 |
|------|------|
| `constitution.md` | 项目宪法（不可变规则），最高优先级 |
| `CLAUDE.md` | AI 开发指南 |
| `specs/spec.md` | 完整功能规格说明 |
| `specs/plan.md` | 技术实现方案 |
| `specs/tasks.md` | 详细任务分解 |

### 测试文档

| 文档 | 说明 |
|------|------|
| `document/E2E_TEST_GUIDE.md` | 端到端测试指南 |
| `document/UI_AUTOMATION_TEST_GUIDE.md` | UI 自动化测试指南 |
| `document/TESTING_CHECKLIST.md` | 测试清单 |
| `document/iOS_TEST_GUIDE.md` | iOS 测试指南 |

### 开发指南

- 严格遵循 **Clean Architecture**，不得违反分层边界
- 所有业务逻辑必须在 Shared 模块中实现
- 平台特定代码（Android/iOS）仅负责 UI 渲染和平台适配
- 优先级顺序：`constitution.md` > `CLAUDE.md` > `specs/` > 用户指令

## 项目截图

> 注：以下截图来自设计稿，实际界面可能略有不同

### 主地图与场景导航
```
┌─────────────────────────────────────┐
│  ┌─────┐                    ⚙️     │
│  │ 😺  │   我的收藏                │
│  └─────┘      ┌───┐               │
│              🚒   🏫   🌲          │
│              消防站  学校   森林    │
│                                  │
└─────────────────────────────────────┘
```

### 消防站场景
```
┌─────────────────────────────────────┐
│  🧯          🚒          🪜        │
│  灭火器    消防栓       云梯       │
│                                  │
│          💦                       │
│         水枪                     │
│                                  │
│  ┌─────────────────────────────┐│
│  │   点击设备观看教学视频       ││
│  └─────────────────────────────┘│
└─────────────────────────────────────┘
```

### 徽章收藏页面
```
┌─────────────────────────────────────┐
│  我的收藏                    🏠   │
│                                  │
│  消防站 🚒  [🎖️🎖️🎖️🎖️...]       │
│  学校   🏫  [🎖️...]             │
│  森林   🌲  [🎖️🎖️...]          │
│                                  │
│  ──────────────── 未获得 ────────  │
│  [░░] [░░] [░░] [░░]            │
│                                  │
└─────────────────────────────────────┘
```

## 贡献指南

我们欢迎社区贡献！请遵循以下流程：

### 提交 Issue

- 使用清晰的标题描述问题
- 提供复现步骤和环境信息
- 如果是功能请求，请说明使用场景

### 提交 Pull Request

1. Fork 仓库并创建功能分支
2. 遵循现有的代码风格和架构
3. 确保所有测试通过
4. 更新相关文档
5. 提交 PR 并描述改动内容

### 代码规范

- **Kotlin**: 遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Swift**: 遵循 [Swift Style Guide](https://google.github.io/swift/)
- **提交信息**: 使用 [Conventional Commits](https://www.conventionalcommits.org/)

## 相关资源

### 官方链接

| 资源 | 链接 | 说明 |
|------|------|------|
| KMM 官方文档 | https://kotlinlang.org/docs/multiplatform.html | Kotlin Multiplatform 官方指南 |
| Jetpack Compose | https://developer.android.com/jetpack/compose | Android UI 开发文档 |
| SwiftUI | https://developer.apple.com/documentation/swiftui | iOS UI 开发文档 |
| SQLDelight | https://sqldelight.github.io/sqldelight/ | 类型安全的 SQL 数据库 |

### 推荐工具

| 工具 | 用途 |
|------|------|
| Android Studio Iguana+ | Android 开发 IDE |
| Xcode 14+ | iOS/macOS 开发 IDE |
| Figma | UI 设计和原型 |
| LottieFiles | 动画预览和下载 |

## 许可证

```
TigerFire (老虎消防车) - 学前儿童消防安全教育应用

Copyright (c) 2024 TigerFire Contributors

本项目为教育用途开发，遵循以下原则：
1. 允许个人学习、研究和非商业用途使用
2. 禁止将本应用用于任何商业目的
3. 禁止基于本应用开发类似功能的商业产品
4. 修改和分发时必须保留版权声明

本软件按"原样"提供，不附带任何明示或暗示的保证。
```

## 贡献

我们欢迎各种形式的贡献！

### 如何贡献

1. **报告问题**：发现 Bug 或有功能建议？请提交 [Issue](../../issues)
2. **提交代码**：Fork 仓库，创建功能分支，提交 Pull Request
3. **完善文档**：帮助改进文档、翻译或添加示例
4. **分享项目**：推荐给朋友，给项目点 Star ⭐

### 贡献者

感谢所有为 TigerFire 做出贡献的开发者！

---

<div align="center">

**TigerFire** - 让消防安全教育变得有趣！🔥🐯

[⬆ 回到顶部](#目录)

</div>
