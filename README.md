# TigerFire (老虎消防车)

> 学前儿童消防安全教育应用 · Kotlin Multiplatform Mobile

专为 **3-6 岁儿童**设计的互动式消防安全教育应用，通过"**小火**"（Little Fire）老虎消防员 IP 角色，引导儿童学习消防知识和应急技能。

**当前状态**：Android / iOS 双端功能已完成，Android 已产出签名 Release 包（`release/composeApp-release_v1.0.0.apk`，43 MB）。

---

## 目录

- [项目状态](#项目状态)
- [项目简介](#项目简介)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [场景说明](#场景说明)
- [核心功能](#核心功能)
- [UI 双轨制与编译开关](#ui-双轨制与编译开关)
- [数据持久化](#数据持久化)
- [崩溃日志系统](#崩溃日志系统)
- [测试](#测试)
- [性能与稳定性指标](#性能与稳定性指标)
- [设计规范](#设计规范)
- [已知问题与待办](#已知问题与待办)
- [常见问题-faq](#常见问题-faq)
- [项目文档索引](#项目文档索引)
- [项目截图](#项目截图)
- [开发指南](#开发指南)
- [贡献指南](#贡献指南)
- [相关资源](#相关资源)
- [许可证](#许可证)

---

## 项目状态

| 模块 | 状态 | 说明 |
|------|------|------|
| Domain 层（模型 / 用例 / 仓储接口） | ✅ 完成 | 9 个模型 + 2 个扩展文件 + 5 个 UseCase |
| Data 层（SQLDelight 持久化） | ✅ 完成 | 3 张表，单行表设计 |
| Presentation 层（7 个 ViewModel） | ✅ 完成 | StateFlow + Channel(Effect) |
| Android UI（Jetpack Compose） | ✅ 完成 | 7 个场景 × 原始版 + 优化版双实现 |
| iOS UI（SwiftUI） | ✅ 完成 | 7 个 View + AppCoordinator 导航 |
| 崩溃日志系统 | ✅ 完成 | Android / iOS 双端 expect-actual |
| 家长模式 | ✅ 完成 | 数学题验证 + 时长控制 + 周统计 |
| 单元测试 | ✅ 90 个 | `commonTest`，覆盖 Domain + 会话工具 |
| Android UI 自动化测试 | ✅ 18 个 | `androidTest`（导航 / 徽章 / 家长 / 性能） |
| iOS UI 测试 | ✅ 11 个 | `iosAppUITests` |
| Android Release 构建 | ✅ 完成 | 签名 + R8 混淆 + 资源压缩 |
| iOS 发布构建 | ⏳ 未做 | 需配置 TEAM_ID 与证书 |

---

## 项目简介

TigerFire 使用 **Kotlin Multiplatform Mobile (KMM)** 开发，Android 与 iOS 共享全部业务逻辑（Domain / Data / Presentation 三层），各自使用原生 UI 框架渲染（Jetpack Compose / SwiftUI）。

### IP 角色设定：小火

| 属性 | 设定 |
|------|------|
| **形象** | 一只开消防车的老虎卡通角色，2D 扁平风格 |
| **名字** | 小火 |
| **性格** | 小孩子调皮风格，像小朋友的朋友 |
| **说话风格** | 正常语速，带停顿；鼓励型回复（如"再靠近一点点哦！""你记得真牢！"） |
| **出现方式** | 关键时刻弹出（通关、提示、庆祝） |

### 核心特性

- **三个学习场景**：消防站、学校、森林（渐进式解锁）
- **互动式学习**：点击设备观看教育视频、点击救援小游戏
- **徽章收集系统**：7 种基础徽章 + 分类型变体（消防站 4 / 学校 3 / 森林 2），最多 23 枚
- **家长控制**：使用时长管理（5/10/15/30 分钟）、数学题验证、本周使用统计图表、进度重置
- **儿童友好设计**：大尺寸触控目标（≥100dp）、语音引导、疯狂点击保护、空闲提示

### 技术栈

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| **跨平台框架** | Kotlin Multiplatform | Kotlin 2.3.0 |
| **UI 框架（共享 Theme）** | Compose Multiplatform | 1.10.0 |
| **Android UI** | Jetpack Compose + Material 3 | Compose MP 1.10.0 |
| **Android 导航** | AndroidX Navigation Compose | 2.8.5 |
| **iOS UI** | SwiftUI + Lottie-iOS | lottie-ios 4.6.0 (SPM) |
| **本地存储** | SQLDelight | 2.1.0 |
| **异步处理** | Kotlin Coroutines + Flow | 1.10.1 |
| **序列化** | kotlinx.serialization | 1.8.0 |
| **动画（Android）** | Lottie Compose | 6.6.2 |
| **视频播放** | Media3 / ExoPlayer (Android) · AVPlayer (iOS) | media3 1.5.0 |
| **构建** | AGP 8.11.2 / Gradle 8.14.3 | — |
| **内存检测（debug）** | LeakCanary | 2.13 |

---

## 项目结构

```
TigerFire/                              # KMM 项目根目录
├── composeApp/                         # 共享 Kotlin 模块（同时是 Android 应用模块）
│   ├── src/
│   │   ├── commonMain/                 # 平台无关代码
│   │   │   ├── kotlin/com/cryallen/tigerfire/
│   │   │   │   ├── domain/             # 领域层（业务规则，零平台依赖）
│   │   │   │   │   ├── model/          # Badge, GameProgress, ParentSettings,
│   │   │   │   │   │                   #   SceneType, SceneStatus, CrashInfo,
│   │   │   │   │   │                   #   NonFatalError, ErrorType, CrashLogFile
│   │   │   │   │   ├── usecase/        # UnlockScene / AwardBadge / CheckTimeLimit
│   │   │   │   │   │                   #   / RecordUsage / ResetProgress
│   │   │   │   │   ├── repository/     # ProgressRepository, CrashLogger(expect)
│   │   │   │   │   └── utils/          # TimeUtils(expect)
│   │   │   │   ├── data/               # 数据层
│   │   │   │   │   ├── local/          # DatabaseFactory, PlatformSqlDriver(expect),
│   │   │   │   │   │                   #   LogFileManager(expect)
│   │   │   │   │   ├── repository/     # ProgressRepositoryImpl
│   │   │   │   │   └── resource/       # ResourcePathProvider(expect)
│   │   │   │   ├── presentation/       # 表现层（7 组 ViewModel/State/Event/Effect）
│   │   │   │   │   ├── welcome/ map/ firestation/ school/ forest/
│   │   │   │   │   ├── collection/ parent/
│   │   │   │   │   ├── audio/          # AudioManager 接口
│   │   │   │   │   └── common/         # AppSessionManager, SessionTimer,
│   │   │   │   │                       #   IdleTimer, RapidClickGuard, PlatformDateTime
│   │   │   │   ├── factory/            # UseCaseFactory
│   │   │   │   └── ui/theme/           # KidsTheme（渐变/字号/圆角/触控目标）
│   │   │   ├── sqldelight/.../database/# GameProgress.sq / Badge.sq / ParentSettings.sq
│   │   │   └── composeResources/       # Compose 资源（drawable）
│   │   ├── androidMain/
│   │   │   ├── kotlin/.../ui/          # Compose 屏幕（每个场景 3 个文件，见下）
│   │   │   ├── kotlin/.../navigation/  # AppNavigation, Route
│   │   │   ├── kotlin/.../component/   # VideoPlayer, LottieAnimationPlayer,
│   │   │   │                           #   AndroidAudioManager, HapticManager
│   │   │   ├── kotlin/.../factory/     # ViewModelFactory (Android)
│   │   │   ├── AndroidManifest.xml
│   │   │   └── assets/
│   │   │       ├── videos/             # 7 个 MP4（扁平目录，非分场景子目录）
│   │   │       ├── audio/
│   │   │       │   ├── music/          # fire_engine.mp3
│   │   │       │   ├── sound_effects/  # 8 个 wav 音效
│   │   │       │   └── voices/         # 9 个 mp3 语音
│   │   │       └── lottie/             # anim_truck_enter.json, anim_xiaohuo_wave.json
│   │   ├── iosMain/                    # iOS 平台 actual 实现 + IosAudioManager
│   │   ├── commonTest/                 # 90 个单元测试
│   │   └── androidTest/                # 18 个 UI 自动化测试
│   ├── proguard-rules.pro
│   └── build.gradle.kts
├── iosApp/                             # iOS 应用
│   ├── Configuration/Config.xcconfig   # TEAM_ID / Bundle ID / 版本号
│   ├── iosApp/
│   │   ├── iOSApp.swift, ContentView.swift
│   │   ├── Navigation/AppCoordinator.swift
│   │   ├── UI/{Welcome,Map,FireStation,School,Forest,Collection,Parent}View/
│   │   ├── Component/{VideoPlayerView,LottieView,ViewModelWrapper}.swift
│   │   ├── Audio/IosAudioPlayer.swift
│   │   └── Resources/{videos,audio,lottie}/
│   ├── iosAppUITests/                  # 11 个 UI 测试
│   └── iosApp.xcodeproj
├── specs/                              # 规格文档（spec / plan / tasks）
├── document/                           # 31 篇 Markdown 开发过程文档与测试报告
├── scripts/                            # 20 个构建 / 测试 / 诊断脚本
├── release/                            # Release APK + 截图
├── keystore.properties(.example)       # 签名配置（真实文件不入库）
├── constitution.md                     # 项目宪法（不可变规则）
├── CLAUDE.md                           # AI 开发指南
└── README.md                           # 本文件
```

### Android 场景屏幕的三文件结构

每个场景在 `androidMain/.../ui/<scene>/` 下有三个文件：

| 文件 | 作用 |
|------|------|
| `XxxScreen.kt` | 原始实现（当前默认启用） |
| `XxxScreenOptimized.kt` | UI/动效优化实现 |
| `XxxScreenSelector.kt` | 按 `BuildConfig.IS_USE_OPTIMIZED_UI` 分发到上面二者之一 |

`AppNavigation.kt` 中注册的始终是 `XxxScreenSelector`。详见 [UI 双轨制与编译开关](#ui-双轨制与编译开关)。

---

## 快速开始

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| **JDK** | 17 或更高 | AGP 8.11 要求；产物字节码为 Java 11 |
| **Kotlin** | 2.3.0 | 由 Gradle 版本目录锁定 |
| **Gradle** | 8.14.3 | 项目已配置 Wrapper，无需手动安装 |
| **Android Gradle Plugin** | 8.11.2 | — |
| **Android SDK** | compileSdk / targetSdk 36，minSdk 24 | 最低 Android 7.0 |
| **Android Studio** | Ladybug (2024.2) 或更高 | 需支持 AGP 8.11 |
| **Xcode** | 16.2 或更高 | iOS Deployment Target 为 **18.2** |
| **iOS 目标架构** | `iosArm64` + `iosSimulatorArm64` | **未配置 x86_64 模拟器**，Intel Mac 无法跑模拟器 |

> ⚠️ iOS 部署目标为 18.2，低于 iOS 18.2 的设备/模拟器无法安装。若需下调，修改 `iosApp/iosApp.xcodeproj` 中的 `IPHONEOS_DEPLOYMENT_TARGET`。

#### 验证环境

```bash
java -version
./gradlew --version
xcodebuild -version
```

### 构建 Android 应用

```bash
./gradlew clean :composeApp:assembleDebug
```

```bash
./gradlew :composeApp:installDebug
```

### 构建 Android Release（签名包）

Release 构建启用了 R8 混淆（`isMinifyEnabled`）与资源压缩（`isShrinkResources`），并从项目根目录的 `keystore.properties` 读取签名配置。若该文件不存在，Release 构建仍可执行但不会签名。

首次配置：

```bash
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

```bash
cp keystore.properties.example keystore.properties
```

随后填入密码，并使用封装好的脚本构建（含签名校验、体积报告等 6 个步骤）：

```bash
./scripts/build_release.sh
```

或直接：

```bash
./gradlew :composeApp:assembleRelease
```

详见 [`document/RELEASE_BUILD_GUIDE.md`](document/RELEASE_BUILD_GUIDE.md) 与 [`document/APK_OPTIMIZATION_GUIDE.md`](document/APK_OPTIMIZATION_GUIDE.md)。

### 构建 iOS 应用

iOS 端依赖 KMM 产出的 `ComposeApp.framework`（静态框架，`baseName = "ComposeApp"`），以及通过 SPM 引入的 `lottie-ios 4.6.0`。

```bash
open iosApp/iosApp.xcodeproj
```

命令行构建（先在 `iosApp/Configuration/Config.xcconfig` 中填入 `TEAM_ID`）：

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16' build
```

### 运行测试

```bash
./gradlew :composeApp:testDebugUnitTest
```

```bash
./gradlew :composeApp:connectedDebugAndroidTest
```

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16' test
```

---

## 场景说明

### 场景解锁流程

```
消防站 (数据库初始状态：UNLOCKED)
    ↓ 完成 4 个设备学习
学校 (自动解锁)
    ↓ 完成动画观看
森林 (自动解锁)
    ↓ 救援 2 只小羊
全部完成
```

> 数据库种子数据为 `{"FIRE_STATION":"UNLOCKED","SCHOOL":"LOCKED","FOREST":"LOCKED"}`，即全新安装走渐进式解锁。
> 注意 `GameProgress.defaultSceneStatuses()` 目前是"测试模式"（三个场景全解锁）并带有 TODO，见[已知问题](#已知问题与待办)。

### 1. 消防站 (Fire Station)

**玩法**：点击 4 个消防设备，观看教育视频，学习每种设备的用途。

**交互流程**：
1. 点击未学习设备 → 图标缩放动画 + 点击音效
2. 播放对应 MP4 教学视频
3. 视频播放完毕 → 设备图标变为"已点亮"状态
4. 弹出徽章获得动画 + 成功音效
5. 重复直至 4 个设备全部完成

**设备与视频对应关系**（`FireStationDevice` 枚举，见 `presentation/firestation/FireStationState.kt`）：

| 设备 | deviceId | 视频文件 |
|------|----------|---------|
| 灭火器 | `fire_extinguisher` | `videos/firefighter_cartoon.mp4` |
| 消防栓 | `fire_hydrant` | `videos/firehydrant_cartoon.mp4` |
| 云梯 | `ladder_truck` | `videos/fireladder_truck_cartoon.mp4` |
| 水枪 | `water_hose` | `videos/firenozzle_cartoon.mp4` |

**徽章奖励**：每完成一个设备获得 1 枚徽章，重复通关循环获得 4 种变体之一。

### 2. 学校 (School)

**玩法**：观看剧情动画，了解火灾发生时的正确应对流程。

**交互流程**：
1. 进入场景 → 触发警报音效 + 屏幕边缘红光呼吸（最大透明度 0.15，避免刺眼）
2. 小火语音提示（`voices/school_fire.mp3`）
3. 显示超大播放按钮（屏幕中央）
4. 点击播放 → 警报停止 → 播放 `videos/School_Fire_Safety_Knowledge.mp4`
5. 播放完毕 → 小火点赞动画 + 语音（`voices/school_praise.mp3`）
6. 弹出 1 枚徽章 + 成功音效

**徽章奖励**：学校徽章 `school`，最多 3 种变体。

### 3. 森林 (Forest)

**玩法**：点击小羊触发直升机救援，放下云梯救出受困的小羊。

**交互流程**：
1. 进入场景 → 小火语音提示（`voices/forest_start.mp3`）
2. 左侧直升机图标持续播放飞行动画（`sound_effects/helicopter.wav`）
3. 右侧显示两只被火苗包围的小羊
4. 点击小羊 → 直升机飞至小羊上方 → 显示"放下梯子"按钮
5. 点击按钮 → 播放 `videos/rescue_sheep_1.mp4` / `rescue_sheep_2.mp4`
6. 视频结束 → 小羊消失 + 弹出徽章 + 成功音效
7. 两只全部救出 → 庆祝 + 语音总结（`voices/forest_complete.mp3`）

**徽章奖励**：`forest_sheep1` / `forest_sheep2`，各最多 2 种变体。

### 资源清单

**视频**（`composeApp/src/androidMain/assets/videos/`，iOS 同名文件位于 `iosApp/iosApp/Resources/videos/`）：

| 文件 | 用途 | 体积 |
|------|------|------|
| `firefighter_cartoon.mp4` | 灭火器教学 | 2.2 MB |
| `firehydrant_cartoon.mp4` | 消防栓教学 | 2.1 MB |
| `fireladder_truck_cartoon.mp4` | 云梯教学 | 2.1 MB |
| `firenozzle_cartoon.mp4` | 水枪教学 | 1.5 MB |
| `School_Fire_Safety_Knowledge.mp4` | 学校剧情动画 | 13.0 MB |
| `rescue_sheep_1.mp4` | 小羊 1 救援 | 1.5 MB |
| `rescue_sheep_2.mp4` | 小羊 2 救援 | 2.3 MB |

> 视频已经过压缩处理，脚本见 [`scripts/compress_videos.sh`](scripts/compress_videos.sh)。

**音效**（`assets/audio/sound_effects/`）：`click.wav`、`success.wav`、`collect.wav`、`alert.wav`、`hint.wav`、`helicopter.wav`、`water.wav`、`truck_horn.wav`

**语音**（`assets/audio/voices/`）：`welcome_greeting.mp3`、`school_fire.mp3`、`school_praise.mp3`、`forest_start.mp3`、`forest_complete.mp3`、`collection_egg.mp3`、`hint_idle.mp3`、`slow_down.mp3`、`time_up.mp3`

**背景音乐**（`assets/audio/music/`）：`fire_engine.mp3`

**Lottie 动画**（`assets/lottie/`）：`anim_truck_enter.json`、`anim_xiaohuo_wave.json`

---

## 核心功能

### 徽章系统

**基础徽章清单**（`TOTAL_UNIQUE_BADGES = 7`）：

| 场景 | baseType | 基础徽章数 | 变体上限/类型 | 该场景最大徽章数 |
|------|----------|-----------|--------------|----------------|
| 消防站 | `fire_extinguisher` / `fire_hydrant` / `ladder_truck` / `water_hose` | 4 | 4 | 16 |
| 学校 | `school` | 1 | 3 | 3 |
| 森林 | `forest_sheep1` / `forest_sheep2` | 2 | 2 | 4 |
| **总计** | — | **7** | — | **23** |

变体上限由 `getMaxVariantsForBaseType()` 定义（`domain/model/GameProgressExtensions.kt`）：

```kotlin
fun getMaxVariantsForBaseType(baseType: String): Int = when (baseType) {
    "fire_hydrant", "ladder_truck", "fire_extinguisher", "water_hose" -> 4
    "school" -> 3
    "forest_sheep1", "forest_sheep2" -> 2
    else -> 1
}
```

**变体分配算法**（循环递增，取模）：

```kotlin
fun List<Badge>.calculateNextVariant(baseType: String): Int {
    val maxVariants = getMaxVariantsForBaseType(baseType)
    val existingCount = this.count { it.baseType == baseType }
    return existingCount % maxVariants
}
```

**集齐判定**：`GameProgress.hasCollectedAllBadges()` —— 去重后的 `baseType` 数量 ≥ 7 即视为集齐，解锁彩蛋（语音 `voices/collection_egg.mp3`）。

**徽章展示页面（我的收藏）**：
- 从主地图进入
- 按场景分组显示；未获得徽章显示为灰色空槽
- 已获得徽章显示图标 + 变体标识，可点开详情弹窗

### 家长控制

| 项目 | 实现 |
|------|------|
| **入口** | 主地图右上角齿轮图标（半透明，不抢眼） |
| **验证** | 数学题验证（3-6 岁儿童大概率无法通过） |
| **单次使用时长** | 5 / 10 / 15 / 30 分钟，默认 **15**（`ParentSettings.AVAILABLE_DURATIONS`） |
| **提前提醒** | 默认提前 **2** 分钟语音提示（`ParentSettings.DEFAULT_REMINDER_MINUTES`） |
| **时间到** | 语音 `voices/time_up.mp3` + 家长验证界面（答对可续时，取消则退出） |
| **使用统计** | 本周每日使用时长柱状图，数据存于 `ParentSettings.dailyUsageStats`（`"yyyy-MM-dd" -> 毫秒`） |
| **进度重置** | `ResetProgressUseCase`，二次确认后清空全部进度与徽章 |

### 儿童行为保护机制

位于 `presentation/common/`，均为纯 Kotlin、可单测：

| 机制 | 文件 | 参数 |
|------|------|------|
| **疯狂点击保护** | `RapidClickGuard.kt` | 500 ms 内连续 3 次点击触发，播放 `voices/slow_down.mp3` |
| **空闲提示** | `IdleTimer.kt` | 默认 30 秒无操作触发提示（可配置 5 s ~ 300 s），播放 `voices/hint_idle.mp3` |
| **会话计时** | `SessionTimer.kt` | 按家长设置的时长倒计时，含提前提醒 |
| **会话总控** | `AppSessionManager.kt` | 单例，串联上述三者与用量记录 |

---

## UI 双轨制与编译开关

项目在 Android 侧保留了**两套完整的 UI 实现**，通过编译期常量切换，便于对比与灰度回退。

### 开关位置

`composeApp/build.gradle.kts`：

```kotlin
defaultConfig {
    // BuildConfig 字段：是否使用优化后的 UI
    buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "false")
}
```

**当前值为 `false`，即默认运行原始版 UI。**

### 两套实现的规模

| 场景 | 原始版 | 优化版 |
|------|--------|--------|
| Welcome | 349 行 | 510 行 |
| Map | 1729 行 | 1479 行 |
| FireStation | 1494 行 | 1633 行 |
| School | 1094 行 | 1779 行 |
| Forest | 1478 行 | 1717 行 |
| Collection | 1340 行 | 1347 行 |
| Parent | 2350 行 | 2878 行 |

7 个场景的优化版均已实现完毕（早期的 `document/UI_OPTIMIZATION_STATUS.md` 记录的"文件不完整"问题已解决）。

### 切换方式

1. 将 `IS_USE_OPTIMIZED_UI` 改为 `"true"`
2. 同步 Gradle，重新构建
3. 用 [`scripts/verify_ui_switch.sh`](scripts/verify_ui_switch.sh) 校验实际生效的版本

参考：[`document/UI_OPTIMIZATION_SWITCH_GUIDE.md`](document/UI_OPTIMIZATION_SWITCH_GUIDE.md)、[`document/QUICK_START_UI_SWITCH.md`](document/QUICK_START_UI_SWITCH.md)

---

## 数据持久化

SQLDelight 2.1.0，数据库名 `TigerFireDatabase`，生成包 `com.cryallen.tigerfire.database`。Schema 位于 `composeApp/src/commonMain/sqldelight/com/cryallen/tigerfire/database/`。

三张表均采用**单行表设计**（`id` 恒为 1）：

| 表 | 关键字段 | 说明 |
|----|---------|------|
| `GameProgress` | `sceneStatuses`(JSON)、`fireStationCompletedItems`(JSON 数组)、`forestRescuedSheep`、`totalPlayTime` | 场景进度 |
| `Badge` | `id`(PK, `baseType_v{variant}` 形式)、`scene`、`baseType`、`variant`、`earnedAt`；含 `scene` / `baseType` 两个索引 | 已获得的徽章 |
| `ParentSettings` | `sessionDurationMinutes`(默认 15)、`reminderMinutesBefore`(默认 2)、`dailyUsageStats`(JSON) | 家长设置与用量统计 |

驱动通过 `expect/actual` 提供：Android 使用 `AndroidSqliteDriver`，iOS 使用 `NativeSqliteDriver`。

**数据库位置**：
- Android：`/data/data/com.cryallen.tigerfire/databases/`
- iOS：由 `NativeSqliteDriver` 管理的应用沙盒默认数据库目录

**校验脚本**：[`scripts/verify_database.sh`](scripts/verify_database.sh)、[`scripts/verify_badge_database.sh`](scripts/verify_badge_database.sh)、[`scripts/monitor_badge_realtime.sh`](scripts/monitor_badge_realtime.sh)

---

## 崩溃日志系统

`CrashLogger` 为 `expect class`，两端实现程度不同：

- **Android**（完整实现）：通过 `Thread.UncaughtExceptionHandler` 捕获未处理异常，在 `TigerFireApplication.onCreate()` 中初始化
- **iOS**（简化版）：受 Kotlin/Native 设置 C 函数指针的限制，**未安装 `NSSetUncaughtExceptionHandler` 全局处理器**，仅提供手动日志记录能力；且当前 iOS 端没有任何位置调用 `CrashLogger.initialize()`

| 项目 | 配置 |
|------|------|
| 存储位置（Android） | `filesDir/crash_logs/`，即 `/data/data/com.cryallen.tigerfire/files/crash_logs/` |
| 存储位置（iOS） | `NSTemporaryDirectory()/crash_logs/` |
| 日志格式 | JSON |
| 最多保留文件数 | 20（`LogFileManager.MAX_LOG_FILES`） |
| 单文件大小上限 | 100 KB（`LogFileManager.MAX_FILE_SIZE`） |
| 记录内容 | 应用版本、设备型号、OS 版本、崩溃类型、堆栈、当前场景（`setCurrentScene`）、最后操作（`setLastAction`）、内存占用 |

除致命崩溃外，还支持非致命错误上报（`NonFatalError` + `ErrorType`，含 `VIDEO_LOAD_FAILED` 等类型）。

**调试入口**（仅 debug 使用）：
- `ui/debug/CrashTestActivity.kt`（已在 `AndroidManifest.xml` 注册，`exported="true"`）
- `ui/debug/CrashLogDebugScreen.kt`
- [`scripts/debug_crash.sh`](scripts/debug_crash.sh)

> ⚠️ `CrashTestActivity` 目前在所有构建类型中注册且 `exported="true"`，正式发布前建议移入 `debug` source set，见[已知问题](#已知问题与待办)。

---

## 测试

### 测试规模

| 类型 | 位置 | 数量 |
|------|------|------|
| KMM 单元测试 | `composeApp/src/commonTest/` | **90** |
| Android UI 自动化测试 | `composeApp/src/androidTest/` | **18** |
| iOS UI 测试 | `iosApp/iosAppUITests/` | **11** |

**单元测试分布**：

| 测试类 | 用例数 |
|--------|-------|
| `GameProgressTest` | 17 |
| `ParentSettingsTest` | 15 |
| `RapidClickGuardTest` | 14 |
| `BadgeExtensionsTest` | 13 |
| `GameProgressExtensionsTest` | 11 |
| `BadgeTest` | 5 |
| `IdleTimerStateTest` | 5 |
| `SceneStatusTest` / `SessionTimerStateTest` | 3 / 3 |
| `SceneTypeTest` / `ComposeAppCommonTest` | 2 / 2 |

**Android UI 测试**：`AppNavigationTest`(6)、`PerformanceTest`(5)、`ParentModeTest`(4)、`BadgeCollectionTest`(3)
**iOS UI 测试**：`NavigationFlowTests`(8)、`iosAppUITests`(3)

测试依赖：Espresso 3.7.0、UI Automator 2.3.0、Compose UI Test 1.8.0。

### 测试与诊断脚本（`scripts/`）

| 脚本 | 用途 |
|------|------|
| `e2e_test.sh` | 端到端测试 |
| `run_e2e_test_optimized.sh` | 优化版 UI 的端到端测试 |
| `run_ui_tests.sh` | UI 自动化测试套件 |
| `test_android.sh` | Android 构建 + 测试组合 |
| `test_on_device.sh` | 真机测试 |
| `test_badge_fix.sh` | 徽章系统回归 |
| `test_parent_screen.sh` | 家长模式回归 |
| `test_weekly_chart.sh` | 周统计图表回归 |
| `test_forest_fixes.sh` | 森林场景回归 |
| `test_back_button.sh` | 返回键行为 |
| `verify_database.sh` / `verify_badge_database.sh` | 数据库状态校验 |
| `monitor_badge_realtime.sh` | 实时监控徽章写入 |
| `verify_ui_switch.sh` | 校验 UI 双轨开关实际生效版本 |
| `verify_fix.sh` | 通用修复验证 |
| `debug_crash.sh` | 拉取并解析崩溃日志 |
| `build_release.sh` | Release 打包（6 步，含签名校验与体积报告） |
| `compress_videos.sh` | 视频资源压缩 |
| `add_test_usage_data.sh` | 注入家长模式测试用量数据 |
| `generate_images.py` | 图片资源生成 |

### 目标用户验证要点

- 3-4 岁能否在无引导情况下完成任务？
- 5 岁能否独立完成森林救援？
- 低端 Android（1 GB RAM）与 iPhone 8 级设备兼容性

---

## 性能与稳定性指标

### 性能目标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 冷启动时间 | ≤1.2 s | 从点击图标到首屏显示 |
| 单场景内存 | ≤120 MB | 单个场景占用内存 |
| 安装包体积 | ≤300 MB | 实际 Release APK **43 MB** |
| 动画帧率 | ≥30 FPS | Lottie 动画流畅度 |

包体优化手段：R8 混淆 + 资源压缩 + 语言资源裁剪（仅 `zh`/`zh-rCN`）+ 视频压缩。ABI 分包已配置但默认关闭（`splits.abi.isEnable = false`）。

### 稳定性目标

| 指标 | 目标值 |
|------|--------|
| 崩溃率 | ≤0.1% |
| 白屏率 | ≤0.05% |
| ANR 率（Android） | ≤0.1% |
| 内存泄漏 | 零容忍（debug 构建集成 LeakCanary 2.13） |
| 恢复能力 | 崩溃后重启应恢复到最后正常状态 |

历史验证报告见 [`document/E2E_TEST_REPORT.md`](document/E2E_TEST_REPORT.md)、[`document/E2E_TEST_VALIDATION_REPORT.md`](document/E2E_TEST_VALIDATION_REPORT.md)。

### 兼容性

| 平台 | 最低版本 | 说明 |
|------|----------|------|
| Android | API 24 (Android 7.0) | targetSdk 36 |
| iOS | **iOS 18.2** | 由 `IPHONEOS_DEPLOYMENT_TARGET` 决定；仅支持 arm64 |

### 权限与隐私

- Android 仅申请 `android.permission.VIBRATE`（触觉反馈）
- **完全离线**，无网络权限、无任何数据上报、无第三方 SDK 采集

---

## 设计规范

主题系统集中在 `commonMain/.../ui/theme/KidsTheme.kt`，全部为可复用的设计 token。

### 场景渐变色（每个场景 3~4 层垂直渐变）

| 场景 | 渐变色 |
|------|--------|
| 消防站 | `#FF6B6B` → `#FFAA66` → `#FFE066`（柔和红 / 温暖橙 / 明亮黄） |
| 学校 | `#4ECDC4` → `#7FCDFF` → `#B4E7FF`（青绿蓝 / 天空蓝 / 淡蓝） |
| 森林 | `#2ECC71` → `#7FD98E` → `#B8F5A4`（翠绿 / 嫩绿 / 黄绿） |
| 主地图 | `#87CEEB` → `#B0E0E6` → `#98FB98`（天空到草地） |
| 收藏 | `#FF9FF3` → `#FECA57` → `#48DBFB` → `#98FB98`（彩虹糖果色） |
| 欢迎页 | `#87CEEB` → `#4ECDC4` → `#2A9D8F`（海洋渐变） |
| 家长模式 | `#1A5F7A` → `#159895` → `#57C5B6`（成熟蓝绿） |

### 语义色

`Success #2ECC71` · `Warning #FFA726` · `Error #FF6B6B` · `Info #4ECDC4` · `LockedOverlay #99CCCCCC`
徽章：`Gold #FFD700` · `Silver #C0C0C0` · `Bronze #CD7F32`

### 尺寸 token

| 类别 | 取值 |
|------|------|
| **触控目标** (`KidsTouchTarget`) | Minimum 100dp / Comfortable 120dp / Large 150dp |
| **字号** (`KidsTextSize`) | Tiny 18sp / Small 20sp / Medium 24sp / Large 32sp / Huge 48sp / Mega 64sp |
| **间距** (`KidsSpacing`) | 4 / 8 / 16 / 24 / 32 / 48 dp |
| **圆角** (`KidsShapes`) | 12 / 16 / 24 / 32 / 48 dp + Circle |
| **阴影** (`KidsShadows`) | 6 / 12 / 20 / 28 dp |
| **动画时长** (`AnimationDuration`) | Fast 200 / Normal 300 / Medium 500 / Slow 800 ms |

### 儿童友好约束

- 触控目标 ≥100dp（主要图标 ≥120dp）
- 正文字号 24sp，最小 18sp
- 警报红光最大透明度仅 0.15，闪烁周期 3 s（`AlertConfig`），避免视觉刺激
- 单点触控，忽略多点触控事件

---

## 已知问题与待办

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 1 | `defaultSceneStatuses()` 处于"测试模式"，三个场景全部返回 `UNLOCKED`，代码内带 TODO | `domain/model/GameProgress.kt` | 数据库种子数据是渐进式解锁，因此全新安装表现正常；但任何走该默认值的构造路径会绕过解锁逻辑 |
| 2 | `CrashTestActivity` 在所有构建类型中注册且 `exported="true"` | `androidMain/AndroidManifest.xml` | 正式包中可被外部 App 拉起，建议移入 debug source set |
| 3 | `FireStationViewModel` 中 `getVideoPath("firestation/${deviceId}.mp4")` 与实际资源路径不一致 | `presentation/firestation/FireStationViewModel.kt:162` | 实际播放路径由 Android UI 层的映射函数提供，ViewModel 中这条路径为历史遗留 |
| 4 | `document/UI_OPTIMIZATION_STATUS.md` 曾记录优化版文件不完整 | `document/` | 已在后续提交中补全，该文档已标注为历史记录 |
| 5 | iOS 部署目标 18.2 偏高 | `iosApp.xcodeproj` | 大幅限制可安装设备范围，发布前建议下调 |
| 6 | `versionCode = 1` / `versionName = "1.0"` 未随 Release 包更新 | `composeApp/build.gradle.kts` | 发布前需要建立版本号管理流程 |
| 7 | iOS 崩溃日志未安装全局异常处理器，且无处调用 `initialize()` | `iosMain/.../CrashLogger.ios.kt` | iOS 端实际不会自动记录崩溃；如需完整能力建议接入第三方崩溃上报库 |
| 8 | `Badge.sq` 注释中的 baseType 示例（`extinguisher`/`hydrant`/`sheep1`）与运行时实际值（`fire_extinguisher`/`fire_hydrant`/`forest_sheep1`）不一致 | `commonMain/sqldelight/.../Badge.sq` | 仅注释漂移，不影响运行，但易误导 |

---

## 常见问题 (FAQ)

**Q: 为什么选择 KMM 而不是 Flutter / React Native？**
A: KMM 让 Domain / Data / Presentation 三层完全共享，同时 UI 层使用各平台原生框架（Compose / SwiftUI），在动画与视频这类重体验场景下能拿到最好的性能与手感。

**Q: 为什么每个 Android 场景有三个 Kotlin 文件？**
A: 见 [UI 双轨制与编译开关](#ui-双轨制与编译开关)。`Screen` 是原始版，`ScreenOptimized` 是优化版，`ScreenSelector` 按 `BuildConfig.IS_USE_OPTIMIZED_UI` 分发。

**Q: 如何添加新的 Lottie 动画？**
A: Android 放入 `composeApp/src/androidMain/assets/lottie/`，iOS 放入 `iosApp/iosApp/Resources/lottie/`，通过 `ResourcePathProvider.getLottiePath(name)` 获取路径。Android 侧用 `LottieAnimationPlayer`，iOS 侧用 `LottieView`。

**Q: 数据存储在哪里？**
A: SQLDelight 本地数据库。Android 在 `/data/data/com.cryallen.tigerfire/databases/`，iOS 由 `NativeSqliteDriver` 存放在应用沙盒的默认数据库目录。

**Q: 应用是否需要网络连接？**
A: 不需要。完全离线运行，不访问任何网络资源，不收集任何用户数据，Android 侧甚至未申请网络权限。

**Q: 如何重置游戏进度？**
A: 家长模式 → "重置进度" → 二次确认。该操作不可撤销。

**Q: 为什么视频切后台后从头播放？**
A: 3-6 岁儿童注意力容易分散，中断后可能忘记前文。从头播放确保知识完整接收；单个视频最长 45 秒左右，重复观看负担小。

**Q: Intel Mac 能跑 iOS 模拟器吗？**
A: 不能。KMM 只配置了 `iosArm64` 与 `iosSimulatorArm64` 两个目标，未包含 `iosX64`。如需支持，需在 `composeApp/build.gradle.kts` 中补充 target。

---

## 项目文档索引

### 核心文档

| 文档 | 说明 |
|------|------|
| [`constitution.md`](constitution.md) | 项目宪法（不可变规则），最高优先级 |
| [`CLAUDE.md`](CLAUDE.md) | AI 开发指南 |
| [`specs/spec.md`](specs/spec.md) | 完整功能规格说明 |
| [`specs/plan.md`](specs/plan.md) | 技术实现方案 |
| [`specs/tasks.md`](specs/tasks.md) | 详细任务分解（8 个阶段） |

**冲突优先级**：`constitution.md` > `CLAUDE.md` > `specs/*` > 用户即时指令

### 构建与发布

| 文档 | 说明 |
|------|------|
| [`document/RELEASE_BUILD_GUIDE.md`](document/RELEASE_BUILD_GUIDE.md) | Release 签名打包完整流程 |
| [`document/APK_OPTIMIZATION_GUIDE.md`](document/APK_OPTIMIZATION_GUIDE.md) | APK 体积优化手段 |

### 测试文档

| 文档 | 说明 |
|------|------|
| [`document/E2E_TEST_GUIDE.md`](document/E2E_TEST_GUIDE.md) | 端到端测试指南 |
| [`document/E2E_TEST_OPTIMIZED_FEATURES.md`](document/E2E_TEST_OPTIMIZED_FEATURES.md) | 优化版功能的端到端测试 |
| [`document/E2E_TEST_REPORT.md`](document/E2E_TEST_REPORT.md) | 端到端测试报告 |
| [`document/E2E_TEST_VALIDATION_REPORT.md`](document/E2E_TEST_VALIDATION_REPORT.md) | 测试验证报告 |
| [`document/UI_AUTOMATION_TEST_GUIDE.md`](document/UI_AUTOMATION_TEST_GUIDE.md) | UI 自动化测试指南 |
| [`document/iOS_TEST_GUIDE.md`](document/iOS_TEST_GUIDE.md) | iOS 测试指南 |
| [`document/TESTING_CHECKLIST.md`](document/TESTING_CHECKLIST.md) | 测试清单 |
| [`document/TEST_VERIFICATION_CHECKLIST.md`](document/TEST_VERIFICATION_CHECKLIST.md) | 验证清单 |

### UI 优化

| 文档 | 说明 |
|------|------|
| [`document/UI_UX_OPTIMIZATION_PLAN.md`](document/UI_UX_OPTIMIZATION_PLAN.md) | UI/UX 优化总体方案 |
| [`document/UI_UX_OPTIMIZATION_SUMMARY.md`](document/UI_UX_OPTIMIZATION_SUMMARY.md) | 优化总结 |
| [`document/UI_UX_IMPLEMENTATION_COMPLETE.md`](document/UI_UX_IMPLEMENTATION_COMPLETE.md) | 优化实现完成报告 |
| [`document/UI_OPTIMIZATION_SWITCH_GUIDE.md`](document/UI_OPTIMIZATION_SWITCH_GUIDE.md) | 双轨 UI 切换指南 |
| [`document/QUICK_START_UI_SWITCH.md`](document/QUICK_START_UI_SWITCH.md) | 切换快速上手 |
| [`document/UI_OPTIMIZATION_STATUS.md`](document/UI_OPTIMIZATION_STATUS.md) | 优化文件完整性历史记录 |
| 场景专项 | `PHASE1_UI_OPTIMIZATIONS_SUMMARY.md`、`MAP_SCREEN_OPTIMIZATIONS.md`、`FOREST_SCREEN_OPTIMIZATIONS.md`、`WELCOME_COLLECTION_OPTIMIZATIONS.md` |

### 缺陷修复记录

`BADGE_FIX_SUMMARY.md`、`BADGE_DUPLICATE_FIX_REPORT.md`、`test_badge_transaction.md`、`WEEKLY_CHART_FIX_REPORT.md`、`PARENT_SCREEN_FIX_REPORT.md`、`BUGFIX_WHITSCREEN.md`、`CRITICAL_BUG_FOUND.md`、`QUICK_FIX_STRATEGY.md`、`FINAL_RECOMMENDATION.md`、`stage7_completion_report.md`

---

## 项目截图

实际运行截图位于 [`release/screenshot/`](release/screenshot/)：

| 场景 | 文件 |
|------|------|
| 欢迎页 | `Screenshot_welcome.png` |
| 主地图 | `Screenshot_map.png` |
| 消防站 | `Screenshot_firestation.png` |
| 学校 | `Screenshot_school.png` |
| 森林 | `Screenshot_forest.png` |
| 我的收藏 | `Screenshot_collection.png` |
| 家长模式 | `Screenshot_parent.png` |

---

## 开发指南

### 架构原则

项目严格遵循 **Clean Architecture**，分层边界不可违反：

1. **Domain 层**（`commonMain/domain`）：业务实体、用例、仓储接口；平台无关，不依赖 Data / Presentation
2. **Data 层**（`commonMain/data`）：仓储实现、SQLDelight 持久化、资源路径；仅依赖 Domain
3. **Presentation 层**（`commonMain/presentation`）：ViewModel（StateFlow 管理状态 + Channel 分发 Effect）
4. **UI 层**（`androidMain` / `iosApp`）：只做渲染与平台适配，不含业务逻辑

### 添加新功能的顺序

1. Domain 层：定义模型与业务规则（并补单元测试）
2. Data 层：扩展 `.sq` schema 与仓储实现
3. Presentation 层：新增 State / Event / Effect / ViewModel
4. Android：Compose UI（如需双轨，同时新增 `Optimized` 与 `Selector`）
5. iOS：SwiftUI View + `AppCoordinator` 路由

### 代码规范

- **Kotlin**：[Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Swift**：[Swift Style Guide](https://google.github.io/swift/)
- **提交信息**：[Conventional Commits](https://www.conventionalcommits.org/)

---

## 贡献指南

### 提交 Issue

- 使用清晰的标题描述问题
- 提供复现步骤和环境信息（设备型号、系统版本、构建类型）
- 功能请求请说明使用场景

### 提交 Pull Request

1. Fork 仓库并创建功能分支
2. 遵循现有的代码风格和分层边界
3. 确保 `./gradlew :composeApp:testDebugUnitTest` 通过
4. 更新相关文档
5. 提交 PR 并描述改动内容

---

## 相关资源

| 资源 | 链接 |
|------|------|
| Kotlin Multiplatform | https://kotlinlang.org/docs/multiplatform.html |
| Compose Multiplatform | https://www.jetbrains.com/compose-multiplatform/ |
| Jetpack Compose | https://developer.android.com/jetpack/compose |
| SwiftUI | https://developer.apple.com/documentation/swiftui |
| SQLDelight | https://sqldelight.github.io/sqldelight/ |
| Lottie iOS | https://github.com/airbnb/lottie-ios |
| Media3 / ExoPlayer | https://developer.android.com/media/media3 |

---

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

---

<div align="center">

**TigerFire** - 让消防安全教育变得有趣！🔥🐯

[⬆ 回到顶部](#目录)

</div>
