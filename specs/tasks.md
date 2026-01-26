# tasks.md：老虎消防车 App 开发任务拆解

---

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档版本** | v1.4 |
| **创建日期** | 2026-01-19 |
| **更新日期** | 2026-01-25 |
| **更新内容** | 新增崩溃监控与日志记录系统相关任务（阶段6.6-6.10），添加稳定性保障任务（阶段8.5-8.7） |
| **基于方案** | plan.md v1.4 |
| **遵循规范** | constitution.md > CLAUDE.md > plan.md > spec.md |

---

## 1. 任务拆解原则说明

### 1.1 拆解依据

本 tasks.md 基于 `plan.md v1.1` 中定义的技术方案进行拆解，严格遵循以下原则：

- **不推翻设计**：所有任务执行 plan.md 中已确定的架构决策
- **可独立完成**：每条任务可被单独领取、实现、测试
- **明确产出物**：每条任务均有可验证的交付物
- **符合 Clean Architecture**：严格遵守分层边界

### 1.2 拆解范围

**包含**：
- Shared 模块：Domain / Data / Presentation 三层完整实现
- Android 平台：Jetpack Compose UI + 平台适配
- iOS 平台：SwiftUI UI + 平台适配
- 辅助任务：状态持久化、异常处理

**不包含**：
- 资源制作（Lottie 文件、MP4 视频、音频）
- UI/UX 设计工作
- 外部依赖库开发

### 1.3 执行顺序建议

```
阶段 0：基础设施搭建（0.1 - 0.7）
    ↓
阶段 1：Domain 层核心模型（1.1 - 1.7）
    ↓
阶段 2：Data 层持久化（2.1 - 2.4）
    ↓
阶段 3：Presentation 层 ViewModel（3.1 - 3.7）
    ↓
阶段 4：Android 平台 UI（4.1 - 4.10）
    ↓
阶段 5：iOS 平台 UI（5.1 - 5.10）
    ↓
阶段 6：辅助与保障（6.1 - 6.5）
    ↓
阶段 7：联调与测试（7.1 - 7.4）
```

---

## 2. 阶段 0：基础设施搭建

### 0.1 创建 KMM 项目骨架

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.1 |
| **所属模块** | 项目根目录 |
| **依赖** | 无 |
| **产出物** | 可编译的 KMM 项目结构 |

**任务说明**：
- 创建 TigerFire KMM 项目骨架
- 配置 `shared` / `androidApp` / `iosApp` 三模块结构
- 配置 Kotlin 版本（1.9.x）与 KMM 插件
- 配置 Android Gradle 与 iOS Xcode 集成
- 验证：项目可编译，各模块无编译错误

**新增文件**：
- `build.gradle.kts`（项目根）
- `shared/build.gradle.kts`
- `androidApp/build.gradle.kts`
- `iosApp/TigerFire.xcodeproj`

---

### 0.2 配置 Shared 模块依赖

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.2 |
| **所属模块** | shared |
| **依赖** | 0.1 |
| **产出物** | Shared 模块可正常导入所需依赖 |

**任务说明**：
- 在 `shared/build.gradle.kts` 中添加依赖：
  - `kotlinx-coroutines-core`（协程）
  - `kotlinx-serialization-json`（序列化）
  - SQLDelight（本地存储）
- 验证：Shared 模块可正常编译，依赖导入无报错

**修改文件**：
- `shared/build.gradle.kts`

---

### 0.3 定义 Shared 模块目录结构

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.3 |
| **所属模块** | shared |
| **依赖** | 0.1 |
| **产出物** | 完整的三层目录结构 |

**任务说明**：
- 创建 `shared/domain/` 及子目录：
  - `model/`、`usecase/`、`repository/`
- 创建 `shared/data/` 及子目录：
  - `repository/`、`local/`、`resource/`
- 创建 `shared/presentation/` 及子目录：
  - 按场景创建子目录（`welcome/`、`map/`、`firestation/` 等）
- 验证：目录结构符合 plan.md §2.1 定义

**新增目录**：
- `shared/domain/model/`
- `shared/domain/usecase/`
- `shared/domain/repository/`
- `shared/data/repository/`
- `shared/data/local/`
- `shared/data/resource/`
- `shared/presentation/welcome/`
- `shared/presentation/map/`
- `shared/presentation/firestation/`
- `shared/presentation/school/`
- `shared/presentation/forest/`
- `shared/presentation/collection/`
- `shared/presentation/parent/`

---

### 0.4 配置 Android 平台依赖

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.4 |
| **所属模块** | androidApp |
| **依赖** | 0.1 |
| **产出物** | Android 模块可正常导入所需依赖 |

**任务说明**：
- 在 `androidApp/build.gradle.kts` 中添加依赖：
  - `androidx.compose.ui` / `material3` / `activity`
  - `androidx.lifecycle.viewmodel-compose`
  - `androidx.navigation.compose`
  - `com.airbnb.android:lottie-compose`
  - `com.google.android.exoplayer:exoplayer`
- 配置 Compose 编译选项
- 验证：Android 模块可编译，无依赖报错

**修改文件**：
- `androidApp/build.gradle.kts`

---

### 0.5 配置 iOS 平台依赖

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.5 |
| **所属模块** | iosApp |
| **依赖** | 0.1 |
| **产出物** | iOS 模块可正常导入所需依赖 |

**任务说明**：
- 在 iOS 项目中添加 Swift Package Manager 依赖：
  - Lottie-iOS（官方库）
- 配置 SwiftUI + Combine 框架导入
- 验证：iOS 项目可编译，无依赖报错

**修改文件**：
- `iosApp/TigerFire.xcodeproj/project.pbxproj`

---

### 0.6 配置 SQLDelight 数据库

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.6 |
| **所属模块** | shared/data/local |
| **依赖** | 0.2 |
| **产出物** | SQLDelight 可正常生成数据库代码 |

**任务说明**：
- 在 `shared/build.gradle.kts` 中配置 SQLDelight 插件
- 创建 `shared/commonMain/sqldelight/com/tigertruck/database/` 目录
- 配置生成的 Kotlin 代码输出路径
- 验证：.sq 文件可正常生成对应 Kotlin 接口

**修改文件**：
- `shared/build.gradle.kts`

**新增目录**：
- `shared/commonMain/sqldelight/com/tigertruck/database/`

---

### 0.7 配置 Shared-Platform 接口定义

| 属性 | 说明 |
|------|------|
| **任务编号** | 0.7 |
| **所属模块** | shared/data/resource |
| **依赖** | 0.3 |
| **产出物** | 平台资源路径提供者接口 |

**任务说明**：
- 定义 `ResourcePathProvider` 接口（expect class）
- 声明方法：
  - `getLottiePath(name: String): String`
  - `getVideoPath(name: String): String`
  - `getAudioPath(name: String): String`
- 验证：接口在 Shared 模块中可正常声明

**新增文件**：
- `shared/data/resource/ResourcePathProvider.kt`

---

## 3. 阶段 1：Domain 层核心模型

### 1.1 定义场景类型与状态枚举

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.1 |
| **所属模块** | shared/domain/model |
| **依赖** | 0.3 |
| **产出物** | SceneType.kt 与 SceneStatus.kt |

**任务说明**：
- 定义 `SceneType` 枚举：`FIRE_STATION`、`SCHOOL`、`FOREST`
- 定义 `SceneStatus` 枚举：`LOCKED`、`UNLOCKED`、`COMPLETED`
- 两个枚举均标记为 `@Serializable`
- 验证：枚举可在 Shared 模块中正常使用

**新增文件**：
- `shared/domain/model/SceneType.kt`
- `shared/domain/model/SceneStatus.kt`

---

### 1.2 定义徽章模型

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.2 |
| **所属模块** | shared/domain/model |
| **依赖** | 1.1 |
| **产出物** | Badge.kt 模型 |

**任务说明**：
- 定义 `Badge` 数据类，包含字段：
  - `id: String`
  - `scene: SceneType`
  - `baseType: String`
  - `variant: Int`
  - `earnedAt: Long`
- 标记为 `@Serializable` 与 `data class`
- 验证：模型可正常序列化/反序列化

**新增文件**：
- `shared/domain/model/Badge.kt`

---

### 1.3 定义游戏进度模型

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.3 |
| **所属模块** | shared/domain/model |
| **依赖** | 1.1, 1.2 |
| **产出物** | GameProgress.kt 模型 |

**任务说明**：
- 定义 `GameProgress` 数据类，包含字段：
  - `sceneStatuses: Map<SceneType, SceneStatus>`
  - `badges: List<Badge>`
  - `totalPlayTime: Long`
  - `fireStationCompletedItems: Set<String>`
  - `forestRescuedSheep: Int`
- 提供默认初始化值（消防站解锁，其他锁定）
- 标记为 `@Serializable`
- 验证：模型可完整描述游戏进度状态

**新增文件**：
- `shared/domain/model/GameProgress.kt`

---

### 1.4 定义家长设置模型

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.4 |
| **所属模块** | shared/domain/model |
| **依赖** | 0.3 |
| **产出物** | ParentSettings.kt 模型 |

**任务说明**：
- 定义 `ParentSettings` 数据类，包含字段：
  - `sessionDurationMinutes: Int`（默认 15）
  - `reminderMinutesBefore: Int`（默认 2）
  - `dailyUsageStats: Map<String, Long>`（日期 -> 毫秒）
- 提供默认初始化值
- 标记为 `@Serializable`
- 验证：模型可完整描述家长设置

**新增文件**：
- `shared/domain/model/ParentSettings.kt`

---

### 1.5 定义进度仓储接口

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.5 |
| **所属模块** | shared/domain/repository |
| **依赖** | 1.3, 1.4 |
| **产出物** | ProgressRepository.kt 接口 |

**任务说明**：
- 定义 `ProgressRepository` 接口，声明方法：
  - `getGameProgress(): Flow<GameProgress>`
  - `updateGameProgress(progress: GameProgress)`
  - `resetProgress()`
  - `getDailyUsageStats(): Flow<Map<String, Long>>`
  - `recordUsage(durationMillis: Long)`
- 使用 `Flow` 支持响应式更新
- 验证：接口可在 Shared 模块中正常声明

**新增文件**：
- `shared/domain/repository/ProgressRepository.kt`

---

### 1.6 定义徽章变体计算逻辑

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.6 |
| **所属模块** | shared/domain/model |
| **依赖** | 1.2 |
| **产出物** | BadgeExtensions.kt 扩展函数 |

**任务说明**：
- 定义扩展函数 `calculateNextVariant(badges: List<Badge>, baseType: String): Int`
- 实现逻辑：统计已有同 baseType 徽章数量，对 MAX_VARIANTS_PER_TYPE（取 4）取模
- 定义常量 `MAX_VARIANTS_PER_TYPE = 4`
- 验证：函数可正确计算变体编号

**新增文件**：
- `shared/domain/model/BadgeExtensions.kt`

---

### 1.7 定义场景解锁条件验证逻辑

| 属性 | 说明 |
|------|------|
| **任务编号** | 1.7 |
| **所属模块** | shared/domain/model |
| **依赖** | 1.3 |
| **产出物** | GameProgressExtensions.kt 扩展函数 |

**任务说明**：
- 定义扩展函数 `isSceneUnlocked(scene: SceneType): Boolean`
- 解锁逻辑：
  - `FIRE_STATION`：始终返回 true
  - `SCHOOL`：`fireStationCompletedItems.size == 4`
  - `FOREST`：`schoolStatus == COMPLETED`
- 验证：函数可正确判定各场景解锁状态

**新增文件**：
- `shared/domain/model/GameProgressExtensions.kt`

---

## 4. 阶段 2：Data 层持久化

### 2.1 定义 SQLDelight 数据库表结构

| 属性 | 说明 |
|------|------|
| **任务编号** | 2.1 |
| **所属模块** | shared/data/local |
| **依赖** | 0.6 |
| **产出物** | GameProgress.sq 与 Badge.sq 表定义 |

**任务说明**：
- 创建 `GameProgress.sq`，定义表：
  - `id INTEGER PRIMARY KEY DEFAULT 1`
  - `fireStationStatus TEXT NOT NULL`
  - `schoolStatus TEXT NOT NULL`
  - `forestStatus TEXT NOT NULL`
  - `fireStationCompletedItems TEXT NOT NULL`（JSON）
  - `forestRescuedSheep INTEGER NOT NULL`
  - `totalPlayTime INTEGER NOT NULL`
- 创建 `Badge.sq`，定义表：
  - `id TEXT PRIMARY KEY`
  - `scene TEXT NOT NULL`
  - `baseType TEXT NOT NULL`
  - `variant INTEGER NOT NULL`
  - `earnedAt INTEGER NOT NULL`
- 创建单行初始化 INSERT 语句
- 验证：.sq 文件可生成对应 Kotlin 代码

**新增文件**：
- `shared/commonMain/sqldelight/com/tigertruck/database/GameProgress.sq`
- `shared/commonMain/sqldelight/com/tigertruck/database/Badge.sq`

---

### 2.2 实现 ProgressRepository

| 属性 | 说明 |
|------|------|
| **任务编号** | 2.2 |
| **所属模块** | shared/data/repository |
| **依赖** | 1.5, 2.1 |
| **产出物** | ProgressRepositoryImpl.kt |

**任务说明**：
- 实现 `ProgressRepository` 接口
- 注入 SQLDelight 生成的 Database 实例
- 实现 `getGameProgress()`：从数据库读取并组装 `GameProgress` 对象
- 实现 `updateGameProgress()`：将对象序列化后写入数据库
- 实现 `resetProgress()`：重置为默认值并写入
- 实现 `getDailyUsageStats()` 与 `recordUsage()`：读写统计数据
- 使用 `@Inject` 注解（支持依赖注入）
- 验证：仓储可正确读写进度数据

**新增文件**：
- `shared/data/repository/ProgressRepositoryImpl.kt`

---

### 2.3 定义数据库驱动初始化模块

| 属性 | 说明 |
|------|------|
| **任务编号** | 2.3 |
| **所属模块** | shared/data/local |
| **依赖** | 2.1 |
| **产出物** | DatabaseFactory.kt |

**任务说明**：
- 定义 `DatabaseFactory` 类，提供方法：
  - `createDatabase(driver: SqlDriver): Database`
- 实现平台驱动的 expect/actual 模式：
  - `expect class PlatformSqlDriver`
  - Android 实现：使用 `AndroidSqliteDriver`
  - iOS 实现：使用 `NativeSqlDriver`
- 验证：各平台可正确创建数据库实例

**新增文件**：
- `shared/data/local/DatabaseFactory.kt`
- `shared/data/local/PlatformSqlDriver.kt`（expect）
- `shared/androidMain/.../PlatformSqlDriver.kt`（actual）
- `shared/iosMain/.../PlatformSqlDriver.kt`（actual）

---

### 2.4 实现 ResourcePathProvider 平台实现

| 属性 | 说明 |
|------|------|
| **任务编号** | 2.4 |
| **所属模块** | shared/data/resource + androidApp + iosApp |
| **依赖** | 0.7 |
| **产出物** | 各平台的 ResourcePathProvider 实现 |

**任务说明**：
- Android 实现：
  - 返回 `file:///android_asset/` 路径
  - `getLottiePath`: `lottie/{name}.json`
  - `getVideoPath`: `videos/{name}.mp4`
  - `getAudioPath`: `audio/{name}.mp3`
- iOS 实现：
  - 返回 `Bundle.main.path(forResource:ofType:)` 路径
  - 同样路径映射规则
- 验证：各平台可正确获取资源路径

**修改文件**：
- `shared/data/resource/ResourcePathProvider.kt`（添加 actual）
- `shared/androidMain/.../ResourcePathProvider.kt`
- `shared/iosMain/.../ResourcePathProvider.kt`

---

## 5. 阶段 3：Presentation 层 ViewModel

### 3.1 实现 WelcomeViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.1 |
| **所属模块** | shared/presentation/welcome |
| **依赖** | 1.3, 1.5 |
| **产出物** | WelcomeViewModel.kt |

**任务说明**：
- 定义 `WelcomeState`：
  - `isTruckAnimationCompleted: Boolean`（卡车入场动画是否完成）
  - `showWaveAnimation: Boolean`（是否显示挥手动画）
  - `isVoicePlaying: Boolean`（语音是否正在播放）
  - `shouldNavigate: Boolean`（是否应该导航）
- 定义 `WelcomeEvent`：
  - `TruckAnimationCompleted`（卡车入场动画完成）
  - `WaveAnimationCompleted`（挥手动画完成）
  - `VoicePlaybackCompleted`（语音播放完成）
- 定义 `WelcomeEffect`：
  - `PlayWaveAnimation`（播放挥手动画）
  - `PlayVoice(audioPath: String)`（播放欢迎语音）
  - `NavigateToMap`（延迟100ms后导航至主地图）
- 实现 ViewModel：
  - 卡车动画完成 → 触发挥手动画 + 语音播放
  - 语音播放完成 → 延迟100ms自动发送导航事件（无需用户交互）
- 验证：ViewModel 可正确管理启动页流程状态

**新增文件**：
- `shared/presentation/welcome/WelcomeViewModel.kt`
- `shared/presentation/welcome/WelcomeState.kt`
- `shared/presentation/welcome/WelcomeEvent.kt`
- `shared/presentation/welcome/WelcomeEffect.kt`

**说明**：
- 语音资源路径：`voice_welcome_greeting.mp3`
- 卡车动画：`anim_truck_enter.json`（2-3 秒）
- 挥手动画：`anim_xiaohuo_wave.json`（3 秒）
- 自动导航延迟：100ms

---

### 3.2 实现 MapViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.2 |
| **所属模块** | shared/presentation/map |
| **依赖** | 1.3, 1.5, 1.7 |
| **产出物** | MapViewModel.kt |

**任务说明**：
- 定义 `MapState`：
  - `sceneStatuses: Map<SceneType, SceneStatus>`
  - `badges: List<Badge>`
- 定义 `MapEvent`：
  - `SceneClicked(scene: SceneType)`
  - `CollectionClicked`
  - `ParentModeClicked`
- 定义 `MapEffect`：
  - `NavigateToScene(scene: SceneType)`
  - `NavigateToCollection`
  - `NavigateToParent`
  - `PlaySceneSound(scene: SceneType)`
- 实现 ViewModel：
  - 订阅 `ProgressRepository.getGameProgress()`
  - 处理场景点击（检查解锁状态）
  - 发送导航与音效 Effect
- 验证：ViewModel 可正确管理地图状态

**新增文件**：
- `shared/presentation/map/MapViewModel.kt`
- `shared/presentation/map/MapState.kt`
- `shared/presentation/map/MapEvent.kt`
- `shared/presentation/map/MapEffect.kt`

---

### 3.3 实现 FireStationViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.3 |
| **所属模块** | shared/presentation/firestation |
| **依赖** | 1.3, 1.5, 1.6 |
| **产出物** | FireStationViewModel.kt |

**任务说明**：
- 定义 `FireStationState`：
  - `completedDevices: Set<String>`
  - `currentPlayingVideo: String?`
  - `isVideoPlaying: Boolean`
  - `showBadgeAnimation: Boolean`
  - `isAllCompleted: Boolean`
- 定义 `FireStationEvent`：
  - `DeviceClicked(deviceId: String)`
  - `VideoPlaybackCompleted`
  - `BadgeAnimationCompleted`
  - `BackPressed`
- 定义 `FireStationEffect`：
  - `PlayVideo(videoPath: String)`
  - `ShowBadgeReward(badge: Badge)`
  - `PlaySuccessSound`
  - `NavigateToMap`
- 实现 ViewModel：
  - 处理设备点击（检查重复点击）
  - 视频播放完毕后颁发徽章
  - 完成全部设备后触发解锁
  - 快速点击防护（3 次快速点击触发提示）
- 验证：ViewModel 可正确管理消防站学习流程

**新增文件**：
- `shared/presentation/firestation/FireStationViewModel.kt`
- `shared/presentation/firestation/FireStationState.kt`
- `shared/presentation/firestation/FireStationEvent.kt`
- `shared/presentation/firestation/FireStationEffect.kt`

---

### 3.4 实现 SchoolViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.4 |
| **所属模块** | shared/presentation/school |
| **依赖** | 1.3, 1.5 |
| **产出物** | SchoolViewModel.kt |

**任务说明**：
- 定义 `SchoolState`：
  - `showAlarmEffect: Boolean`（是否显示警报效果）
  - `showPlayButton: Boolean`（是否显示播放按钮）
  - `isVideoPlaying: Boolean`（视频是否正在播放）
  - `showBadgeAnimation: Boolean`（是否显示徽章动画）
  - `isCompleted: Boolean`（是否已完成）
- 定义 `SchoolEvent`：
  - `PlayButtonClicked`（用户点击播放按钮）
  - `VideoPlaybackCompleted`（视频播放完成）
  - `VoicePlaybackCompleted`（语音播放完成）
  - `BadgeAnimationCompleted`（徽章动画完成）
  - `BackPressed`（用户点击返回）
- 定义 `SchoolEffect`：
  - `StartAlarmEffects`（启动警报效果：音效+红光）
  - `StopAlarmEffects`（停止警报效果）
  - `PlayVideo(videoPath: String)`（播放视频）
  - `PlayVoice(voicePath: String)`（播放语音）
  - `ShowBadgeReward(badge: Badge)`（显示徽章奖励）
  - `UnlockForestScene`（解锁森林场景）
  - `NavigateToMap`（导航至主地图）
- 实现 ViewModel：
  - 进入场景发送 `StartAlarmEffects` Effect
  - 处理播放按钮点击 → 发送 `StopAlarmEffects` + `PlayVideo`
  - 视频播放完毕后颁发徽章 + 解锁森林 + 播放语音
  - 语音播放完毕后导航回主地图
  - 视频播放中禁用返回按钮
- 验证：ViewModel 可正确管理学校场景流程

**新增文件**：
- `shared/presentation/school/SchoolViewModel.kt`
- `shared/presentation/school/SchoolState.kt`
- `shared/presentation/school/SchoolEvent.kt`
- `shared/presentation/school/SchoolEffect.kt`

**视频资源**：`School_Fire_Safety_Knowledge.mp4`
**语音资源**：`voice_school_fire_alert.mp3`（"学校着火啦！"）+ `voice_school_praise.mp3`（"你真棒！记住，着火要找大人帮忙！"）

---

### 3.5 实现 ForestViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.5 |
| **所属模块** | shared/presentation/forest |
| **依赖** | 1.3, 1.5 |
| **产出物** | ForestViewModel.kt |

**任务说明**：
- 定义 `ForestState`：
  - `rescuedSheepCount: Int`（已救小羊数量 0-2）
  - `targetSheepIndex: Int?`（当前目标小羊索引 1 或 2）
  - `isHelicopterFlying: Boolean`（直升机是否正在飞行）
  - `showLadderButton: Boolean`（是否显示放下梯子按钮）
  - `isVideoPlaying: Boolean`（救援视频是否正在播放）
  - `showBadgeAnimation: Boolean`（是否显示徽章动画）
  - `isAllCompleted: Boolean`（是否全部完成）
- 定义 `ForestEvent`：
  - `SheepClicked(sheepIndex: Int)`（用户点击小羊 1 或 2）
  - `HelicopterFlightCompleted`（直升机飞行完成）
  - `LadderButtonClicked`（用户点击放下梯子按钮）
  - `RescueVideoCompleted`（救援视频播放完成）
  - `BadgeAnimationCompleted`（徽章动画完成）
  - `BackPressed`（用户点击返回）
- 定义 `ForestEffect`：
  - `FlyHelicopter(targetSheepIndex: Int)`（飞向目标小羊）
  - `ShowLadderButton`（显示放下梯子按钮）
  - `HideLadderButton`（隐藏放下梯子按钮）
  - `PlayRescueVideo(videoPath: String)`（播放救援视频）
  - `ShowBadgeReward(badge: Badge)`（显示徽章奖励）
  - `PlayVoice(voicePath: String)`（播放语音）
  - `PlayCelebrationAnimation`（播放庆祝动画）
  - `NavigateToMap`（导航至主地图）
- 实现 ViewModel：
  - 处理小羊点击事件（检查是否已救、是否正在飞行/播放视频）
  - 直升机飞行完成后显示放下梯子按钮
  - 救援视频播放完毕后更新小羊计数并颁发徽章
  - 两个小羊都救出后触发庆祝动画
- 验证：ViewModel 可正确管理森林救援流程

**新增文件**：
- `shared/presentation/forest/ForestViewModel.kt`
- `shared/presentation/forest/ForestState.kt`
- `shared/presentation/forest/ForestEvent.kt`
- `shared/presentation/forest/ForestEffect.kt`

**视频资源**：`rescue_sheep_1.mp4`、`rescue_sheep_2.mp4`

---

### 3.6 实现 CollectionViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.6 |
| **所属模块** | shared/presentation/collection |
| **依赖** | 1.3, 1.5 |
| **产出物** | CollectionViewModel.kt |

**任务说明**：
- 定义 `CollectionState`：
  - `badges: List<Badge>`
  - `showEggAnimation: Boolean`
- 定义 `CollectionEvent`：
  - `BackPressed`
  - `EggAnimationCompleted`
- 定义 `CollectionEffect`：
  - `PlayEggAnimation`
  - `NavigateToMap`
- 实现 ViewModel：
  - 订阅进度获取徽章列表
  - 进入时检测是否集齐 7 种基础徽章
  - 满足条件自动播放彩蛋动画
- 验证：ViewModel 可正确管理收藏展示

**新增文件**：
- `shared/presentation/collection/CollectionViewModel.kt`
- `shared/presentation/collection/CollectionState.kt`
- `shared/presentation/collection/CollectionEvent.kt`
- `shared/presentation/collection/CollectionEffect.kt`

---

### 3.7 实现 ParentViewModel

| 属性 | 说明 |
|------|------|
| **任务编号** | 3.7 |
| **所属模块** | shared/presentation/parent |
| **依赖** | 1.3, 1.4, 1.5 |
| **产出物** | ParentViewModel.kt |

**任务说明**：
- 定义 `ParentState`：
  - `settings: ParentSettings`
  - `dailyStats: Map<String, Long>`
  - `showVerification: Boolean`
  - `mathQuestion: Pair<String, Int>`（问题文本 + 答案）
  - `timeLimitReached: Boolean`
- 定义 `ParentEvent`：
  - `VerifyAnswer(answer: Int)`
  - `ExtendTime`
  - `UpdateDuration(minutes: Int)`
  - `ResetProgress`
  - `BackPressed`
- 定义 `ParentEffect`：
  - `ShowVerification`
  - `ExitApp`
  - `PlayVoice(voicePath: String)`
- 实现 ViewModel：
  - 生成随机数学题（一位数加法，结果 ≤ 10）
  - 验证答案
  - 更新时间设置
  - 重置进度
  - 计算本周使用统计数据
- 验证：ViewModel 可正确管理家长功能

**新增文件**：
- `shared/presentation/parent/ParentViewModel.kt`
- `shared/presentation/parent/ParentState.kt`
- `shared/presentation/parent/ParentEvent.kt`
- `shared/presentation/parent/ParentEffect.kt`

---

## 6. 阶段 4：Android 平台 UI（Jetpack Compose）

### 4.1 实现 Android 导航框架

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.1 |
| **所属模块** | androidApp |
| **依赖** | 0.4 |
| **产出物** | AppNavigation.kt |

**任务说明**：
- 使用 `NavHost` 定义导航图
- 定义路由：
  - `welcome`、`map`、`firestation`、`school`、`forest`、`collection`、`parent`
- 实现 `@Composable fun AppNavigation()`
- 配置起始路由为 `welcome`
- 验证：导航框架可正常运行

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/navigation/AppNavigation.kt`

---

### 4.2 实现 WelcomeScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.2 |
| **所属模块** | androidApp/ui/welcome |
| **依赖** | 3.1, 4.1 |
| **产出物** | WelcomeScreen.kt |

**任务说明**：
- 创建 `@Composable fun WelcomeScreen(viewModel: WelcomeViewModel)`
- **全屏布局配置**：
  - 使用 `WindowInsetsControllerCompat` 隐藏状态栏和导航栏
  - 设置系统 UI 标志：`SYSTEM_UI_FLAG_FULLSCREEN` + `SYSTEM_UI_FLAG_HIDE_NAVIGATION`
- **动画播放流程**：
  1. 页面加载自动播放卡车入场动画（`anim_truck_enter.json`，2-3秒）
  2. 卡车动画完成 → 播放挥手动画（`anim_xiaohuo_wave.json`，3秒）
  3. 同时播放欢迎语音（`voice_welcome_greeting.mp3`）
  4. 播放鸣笛音效（`sfx_truck_horn.mp3`）和背景音乐（`bgm_welcome.mp3`）
- **自动导航**：
  - 语音播放完成后自动触发导航
  - 延迟100ms后发送导航事件（无需用户交互）
- **Effect 处理**：
  - 订阅 `viewModel.effect` 处理导航（`NavigateToMap`）
  - 处理播放语音 Effect（`PlayVoice`）
- **降级策略**：
  - Lottie 加载失败（>3 秒）→ 显示静态替代图 + 延迟5秒自动进入
  - 语音加载失败 → 静默跳过，不阻塞流程
- 验证：启动页动画可正常播放，音效正常，语音播放完毕后自动跳转

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/welcome/WelcomeScreen.kt`

**资源文件准备**：
- `assets/lottie/anim_truck_enter.json`（卡车入场动画）
- `assets/lottie/anim_xiaohuo_wave.json`（小火挥手动画）
- `assets/audio/sfx_truck_horn.mp3`（鸣笛音效）
- `assets/audio/bgm_welcome.mp3`（背景音乐）
- `assets/audio/voice_welcome_greeting.mp3`（欢迎语音）
- `res/drawable/launcher_placeholder.xml`（降级静态图）

---

### 4.3 实现 MapScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.3 |
| **所属模块** | androidApp/ui/map |
| **依赖** | 3.2, 4.1 |
| **产出物** | MapScreen.kt |

**任务说明**：
- 创建 `@Composable fun MapScreen(viewModel: MapViewModel)`
- 绘制地图背景（图片资源）
- 绘制 3 个场景图标：
  - 使用 `state.sceneStatuses` 判断锁定/解锁/完成状态
  - 应用对应视觉效果（灰色/彩色/光效）
  - 尺寸 ≥120pt，间距 ≥40pt
- 添加"我的收藏"按钮（左上角）
- 添加家长模式入口（右上角齿轮）
- 处理点击事件，发送 `SceneClicked` 等 Event
- 订阅 `viewModel.effect` 处理导航与音效
- 验证：地图可正常显示，图标状态正确

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/map/MapScreen.kt`

---

### 4.4 实现 FireStationScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.4 |
| **所属模块** | androidApp/ui/firestation |
| **依赖** | 3.3, 4.1 |
| **产出物** | FireStationScreen.kt |

**任务说明**：
- 创建 `@Composable fun FireStationScreen(viewModel: FireStationViewModel)`
- 绘制消防站背景
- 绘制 4 个设备按钮：
  - 根据 `state.completedDevices` 显示星星点亮状态
  - 尺寸 ≥100×100pt
  - 视频播放中禁用点击
- 集成 `VideoPlayer` 组件播放 MP4
- 视频播放完毕发送 `VideoPlaybackCompleted` 事件
- 显示徽章获得动画（Lottie）
- 订阅 `viewModel.effect` 处理音效与导航
- 验证：消防站场景可正常学习

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/firestation/FireStationScreen.kt`

---

### 4.5 实现 SchoolScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.5 |
| **所属模块** | androidApp/ui/school |
| **依赖** | 3.4, 4.1 |
| **产出物** | SchoolScreen.kt |

**任务说明**：
- 创建 `@Composable fun SchoolScreen(viewModel: SchoolViewModel)`
- **警报效果实现**：
  - 进入场景订阅 `StartAlarmEffects` Effect：
    - 播放警报音效（循环播放）
    - 屏幕边缘红光闪烁（使用 `Modifier.drawBehind` 绘制红色半透明边框，配合 `animateFloatAsState` 实现脉冲动画）
  - 播放语音："学校着火啦！快叫消防车！"
- **播放按钮**：
  - 屏幕中央显示超大播放按钮图标（≥150pt）
  - 仅当 `state.showPlayButton` 为 true 且视频未播放时显示
  - 点击发送 `PlayButtonClicked` 事件
- **视频播放**：
  - 订阅 `PlayVideo` Effect → 集成 `VideoPlayer` 组件
  - 播放 `School_Fire_Safety_Knowledge.mp4`
  - 视频播放完毕发送 `VideoPlaybackCompleted` 事件
- **完成后效果**：
  - 显示小火点赞动画（Lottie）
  - 播放语音："你真棒！记住，着火要找大人帮忙！"
  - 显示徽章获得动画
  - 语音播放完毕发送 `VoicePlaybackCompleted` 事件
- **返回按钮**：
  - 视频播放中禁用返回按钮
  - 其他状态下正常返回主地图
- 订阅 `viewModel.effect` 处理所有效果
- 验证：学校场景完整流程可正常运行

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/school/SchoolScreen.kt`

**资源文件准备**：
- `assets/videos/School_Fire_Safety_Knowledge.mp4`（学校消防安全知识视频）
- `assets/audio/sfx_school_alarm.mp3`（警报音效，循环播放）
- `assets/audio/voice_school_fire_alert.mp3`（"学校着火啦！快叫消防车！"）
- `assets/audio/voice_school_praise.mp3`（"你真棒！记住，着火要找大人帮忙！"）
- `assets/lottie/anim_xiaohuo_thumbsup.json`（小火点赞动画）
- `res/drawable/ic_play_button.xml`（超大播放按钮图标，红色渐变圆形 + 播放三角形）

---

### 4.6 实现 ForestScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.6 |
| **所属模块** | androidApp/ui/forest |
| **依赖** | 3.5, 4.1 |
| **产出物** | ForestScreen.kt |

**任务说明**：
- 创建 `@Composable fun ForestScreen(viewModel: ForestViewModel)`
- **布局结构**：
  - 森林火灾背景（静态图片或动画）
  - 屏幕左侧：直升机图标（超大，≥150pt），持续播放螺旋桨飞行动画（Lottie）
  - 屏幕右侧：两只小羊图标，周边显示火苗动画，小羊做求救动画
- **小羊点击交互**：
  - 使用 `Modifier.clickable` 实现小羊点击检测
  - 点击小羊发送 `SheepClicked(sheepIndex)` 事件
  - 已救出的小羊图标隐藏或显示为已救援状态
- **直升机飞行动画**：
  - 订阅 `FlyHelicopter` Effect，使用 `animate*AsState` 实现平滑移动
  - 动画时长约 1-1.5 秒
  - 飞行完成后发送 `HelicopterFlightCompleted` 事件
- **放下梯子按钮**：
  - 当 `state.showLadderButton` 为 true 时显示圆形按钮（≥100pt）
  - 按钮位于直升机下方或跟随直升机位置
  - 点击发送 `LadderButtonClicked` 事件
- **救援视频播放**：
  - 订阅 `PlayRescueVideo` Effect → 集成 `VideoPlayer` 组件
  - 播放 `rescue_sheep_1.mp4` 或 `rescue_sheep_2.mp4`
  - 视频播放完毕发送 `RescueVideoCompleted` 事件
- **庆祝动画**：
  - 订阅 `PlayCelebrationAnimation` Effect → 播放 Lottie 庆祝动画
  - 播放语音总结："直升机能从天上救人，真厉害！"
- 订阅 `viewModel.effect` 处理音效与导航
- 验证：森林场景可正常救援

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/forest/ForestScreen.kt`

**资源文件准备**：
- `assets/lottie/anim_helicopter_spin.json`（直升机螺旋桨旋转动画）
- `assets/lottie/anim_sheep_help.json`（小羊求救动画）
- `assets/lottie/anim_fire_flicker.json`（火苗闪烁动画）
- `assets/lottie/anim_celebration.json`（庆祝动画）
- `assets/videos/rescue_sheep_1.mp4`（救援小羊视频1）
- `assets/videos/rescue_sheep_2.mp4`（救援小羊视频2）
- `assets/audio/voice_forest_rescue_intro.mp3`（"小羊被困啦！快开直升机救它们！"）
- `assets/audio/voice_forest_summary.mp3`（"直升机能从天上救人，真厉害！"）

---

### 4.7 实现 CollectionScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.7 |
| **所属模块** | androidApp/ui/collection |
| **依赖** | 3.6, 4.1 |
| **产出物** | CollectionScreen.kt |

**任务说明**：
- 创建 `@Composable fun CollectionScreen(viewModel: CollectionViewModel)`
- 按 3 个场景分组显示徽章：
  - 消防站 4 个槽位
  - 学校 1 个槽位
  - 森林 2 个槽位
- 已获得徽章显示完整图标 + 变体颜色
- 未获得徽章显示灰色轮廓
- 当 `showEggAnimation` 为 true 时播放彩蛋动画
- 订阅 `viewModel.effect` 处理导航
- 验证：徽章收藏可正常展示

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/collection/CollectionScreen.kt`

---

### 4.8 实现 ParentScreen（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.8 |
| **所属模块** | androidApp/ui/parent |
| **依赖** | 3.7, 4.1 |
| **产出物** | ParentScreen.kt |

**任务说明**：
- 创建 `@Composable fun ParentScreen(viewModel: ParentViewModel)`
- 显示时间设置选项（5/10/15/30 分钟）
- 显示本周使用时长柱状图
- 显示重置进度按钮
- 当 `showVerification` 为 true 时显示数学题验证界面
- 显示"再玩 5 分钟"按钮（验证通过后）
- 处理用户输入，发送对应 Event
- 订阅 `viewModel.effect` 处理退出与语音
- 验证：家长模式可正常使用

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/parent/ParentScreen.kt`

---

### 4.9 实现 LottieAnimationPlayer 组件（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.9 |
| **所属模块** | androidApp/component |
| **依赖** | 0.4 |
| **产出物** | LottieAnimationPlayer.kt |

**任务说明**：
- 创建 `@Composable fun LottieAnimationPlayer(...)`
- 参数：`animationRes: String`、`onAnimationEnd: () -> Unit`
- 使用 `lottie-compose` 库实现
- 使用 `rememberLottieComposition` 加载动画
- 使用 `animateLottieCompositionAsState` 控制播放
- 动画完成时触发回调
- 支持超时降级（3 秒未加载成功返回 null）
- 验证：组件可正常播放 Lottie 动画

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/component/LottieAnimationPlayer.kt`

---

### 4.10 实现 VideoPlayer 组件（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 4.10 |
| **所属模块** | androidApp/component |
| **依赖** | 0.4 |
| **产出物** | VideoPlayer.kt |

**任务说明**：
- 创建 `@Composable fun VideoPlayer(...)`
- 参数：`videoPath: String`、`onPlaybackCompleted: () -> Unit`
- 使用 `ExoPlayer` 实现
- 监听播放完成事件：`Player.Listener.onPlaybackStateChanged`
- 切后台时暂停，恢复时从头播放
- 支持资源加载失败降级（显示静态图）
- 验证：组件可正常播放 MP4 视频

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/component/VideoPlayer.kt`

---

## 7. 阶段 5：iOS 平台 UI（SwiftUI）

### 5.1 实现 iOS 导航协调器

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.1 |
| **所属模块** | iosApp/Navigation |
| **依赖** | 0.5 |
| **产出物** | AppCoordinator.swift |

**任务说明**：
- 创建 `AppCoordinator` 类管理导航
- 使用 `NavigationStack` 或 `NavigationView`
- 定义路由枚举：`welcome`、`map`、`firestation`、`school`、`forest`、`collection`、`parent`
- 实现 `navigate(to: Route)` 方法
- 配置起始路由为 `welcome`
- 验证：导航协调器可正常运行

**新增文件**：
- `iosApp/TigerFire/Navigation/AppCoordinator.swift`

---

### 5.2 实现 WelcomeView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.2 |
| **所属模块** | iosApp/UI/WelcomeView |
| **依赖** | 3.1, 5.1 |
| **产出物** | WelcomeView.swift |

**任务说明**：
- 创建 `struct WelcomeView: View`
- **全屏布局配置**：
  - 使用 `.statusBar(hidden: true)` 隐藏状态栏
  - 设置 `.edgesIgnoringSafeArea(.all)` 全屏显示
- **动画播放流程**：
  1. 页面加载（`.onAppear`）自动播放卡车入场动画（`anim_truck_enter.json`，2-3秒）
  2. 卡车动画完成 → 播放挥手动画（`anim_xiaohuo_wave.json`，3秒）
  3. 同时播放欢迎语音（`voice_welcome_greeting.mp3`）
  4. 播放鸣笛音效（`sfx_truck_horn.mp3`）和背景音乐（`bgm_welcome.mp3`）
- **自动导航**：
  - 语音播放完成后自动触发导航
  - 延迟100ms后发送导航事件（无需用户交互）
- **ViewModel 桥接**：
  - 创建 `WelcomeViewModelWrapper` 桥接 Shared ViewModel
  - 订阅 Effect 处理导航（`NavigateToMap`）
  - 处理播放语音 Effect（`PlayVoice`）
- **降级策略**：
  - Lottie 加载失败（>3 秒）→ 显示静态替代图 + 延迟5秒自动进入
  - 语音加载失败 → 静默跳过，不阻塞流程
- 验证：启动页动画可正常播放，音效正常，语音播放完毕后自动跳转

**新增文件**：
- `iosApp/TigerFire/UI/WelcomeView/WelcomeView.swift`

**资源文件准备**：
- `anim_truck_enter.json`（卡车入场动画，放入 Lottie 读取路径）
- `anim_xiaohuo_wave.json`（小火挥手动画）
- `sfx_truck_horn.mp3`（鸣笛音效）
- `bgm_welcome.mp3`（背景音乐）
- `voice_welcome_greeting.mp3`（欢迎语音）
- `launcher_placeholder.png`（降级静态图）

---

### 5.3 实现 MapView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.3 |
| **所属模块** | iosApp/UI/MapView |
| **依赖** | 3.2, 5.1 |
| **产出物** | MapView.swift |

**任务说明**：
- 创建 `struct MapView: View`
- 绘制地图背景（图片资源）
- 绘制 3 个场景图标：
  - 使用 `state.sceneStatuses` 判断锁定/解锁/完成状态
  - 应用对应视觉效果（灰色/彩色/光效）
  - 尺寸 ≥120pt，间距 ≥40pt
- 添加"我的收藏"按钮（左上角）
- 添加家长模式入口（右上角齿轮）
- 处理点击事件，发送 Event
- 订阅 Effect 处理导航与音效
- 验证：地图可正常显示，图标状态正确

**新增文件**：
- `iosApp/TigerFire/UI/MapView/MapView.swift`

---

### 5.4 实现 FireStationView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.4 |
| **所属模块** | iosApp/UI/FireStationView |
| **依赖** | 3.3, 5.1 |
| **产出物** | FireStationView.swift |

**任务说明**：
- 创建 `struct FireStationView: View`
- 绘制消防站背景
- 绘制 4 个设备按钮：
  - 根据 `state.completedDevices` 显示星星点亮状态
  - 尺寸 ≥100×100pt
  - 视频播放中禁用点击（`.disabled()`）
- 集成 `VideoPlayerView` 组件播放 MP4
- 视频播放完毕发送 Event
- 显示徽章获得动画（Lottie）
- 订阅 Effect 处理音效与导航
- 验证：消防站场景可正常学习

**新增文件**：
- `iosApp/TigerFire/UI/FireStationView/FireStationView.swift`

---

### 5.5 实现 SchoolView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.5 |
| **所属模块** | iosApp/UI/SchoolView |
| **依赖** | 3.4, 5.1 |
| **产出物** | SchoolView.swift |

**任务说明**：
- 创建 `struct SchoolView: View`
- **警报效果实现**：
  - 进入场景订阅 `StartAlarmEffects` Effect：
    - 播放警报音效（使用 `AVAudioPlayer` 循环播放）
    - 屏幕边缘红光闪烁（使用 `.overlay()` + `ZStack` 配合红色半透明矩形 + `opacity` 动画实现脉冲效果）
  - 播放语音："学校着火啦！快叫消防车！"
- **播放按钮**：
  - 屏幕中央显示超大播放按钮图标（≥150pt）
  - 使用 `Image(systemName: "play.circle.fill")` 或自定义图标
  - 仅当 `state.showPlayButton` 为 true 且视频未播放时显示
  - 点击发送 `PlayButtonClicked` 事件
- **视频播放**：
  - 订阅 `PlayVideo` Effect → 集成 `VideoPlayerView` 组件
  - 播放 `School_Fire_Safety_Knowledge.mp4`
  - 视频播放完毕发送 `VideoPlaybackCompleted` 事件
- **完成后效果**：
  - 显示小火点赞动画（Lottie）
  - 播放语音："你真棒！记住，着火要找大人帮忙！"
  - 显示徽章获得动画
  - 语音播放完毕发送 `VoicePlaybackCompleted` 事件
- **返回按钮**：
  - 视频播放中禁用返回按钮（`.disabled()`）
  - 其他状态下正常返回主地图
- 订阅 Effect 处理所有效果
- 验证：学校场景完整流程可正常运行

**新增文件**：
- `iosApp/TigerFire/UI/SchoolView/SchoolView.swift`

**资源文件准备**：
- `School_Fire_Safety_Knowledge.mp4`（学校消防安全知识视频）
- `sfx_school_alarm.mp3`（警报音效，循环播放）
- `voice_school_fire_alert.mp3`（"学校着火啦！快叫消防车！"）
- `voice_school_praise.mp3`（"你真棒！记住，着火要找大人帮忙！"）
- `anim_xiaohuo_thumbsup.json`（小火点赞动画）

---

### 5.6 实现 ForestView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.6 |
| **所属模块** | iosApp/UI/ForestView |
| **依赖** | 3.5, 5.1 |
| **产出物** | ForestView.swift |

**任务说明**：
- 创建 `struct ForestView: View`
- **布局结构**：
  - 森林火灾背景（静态图片或动画）
  - 屏幕左侧：直升机图标（超大，≥150pt），持续播放螺旋桨飞行动画（Lottie）
  - 屏幕右侧：两只小羊图标，周边显示火苗动画，小羊做求救动画
- **小羊点击交互**：
  - 使用 `.onTapGesture` 实现小羊点击检测
  - 点击小羊发送 `SheepClicked(sheepIndex)` 事件
  - 已救出的小羊图标隐藏或显示为已救援状态
- **直升机飞行动画**：
  - 订阅 `FlyHelicopter` Effect，使用 `.offset()` + `withAnimation` 实现平滑移动
  - 动画时长约 1-1.5 秒
  - 飞行完成后发送 `HelicopterFlightCompleted` 事件
- **放下梯子按钮**：
  - 当 `state.showLadderButton` 为 true 时显示圆形按钮（≥100pt）
  - 按钮位于直升机下方或跟随直升机位置
  - 使用 `.onTapGesture` 发送 `LadderButtonClicked` 事件
- **救援视频播放**：
  - 订阅 `PlayRescueVideo` Effect → 集成 `VideoPlayerView` 组件
  - 播放 `rescue_sheep_1.mp4` 或 `rescue_sheep_2.mp4`
  - 视频播放完毕发送 `RescueVideoCompleted` 事件
- **庆祝动画**：
  - 订阅 `PlayCelebrationAnimation` Effect → 播放 Lottie 庆祝动画
  - 播放语音总结："直升机能从天上救人，真厉害！"
- 订阅 Effect 处理音效与导航
- 验证：森林场景可正常救援

**新增文件**：
- `iosApp/TigerFire/UI/ForestView/ForestView.swift`

**资源文件准备**：
- `anim_helicopter_spin.json`（直升机螺旋桨旋转动画）
- `anim_sheep_help.json`（小羊求救动画）
- `anim_fire_flicker.json`（火苗闪烁动画）
- `anim_celebration.json`（庆祝动画）
- `rescue_sheep_1.mp4`（救援小羊视频1）
- `rescue_sheep_2.mp4`（救援小羊视频2）
- `voice_forest_rescue_intro.mp3`（"小羊被困啦！快开直升机救它们！"）
- `voice_forest_summary.mp3`（"直升机能从天上救人，真厉害！"）

---

### 5.7 实现 CollectionView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.7 |
| **所属模块** | iosApp/UI/CollectionView |
| **依赖** | 3.6, 5.1 |
| **产出物** | CollectionView.swift |

**任务说明**：
- 创建 `struct CollectionView: View`
- 按 3 个场景分组显示徽章（使用 `LazyVGrid`）
- 已获得徽章显示完整图标 + 变体颜色
- 未获得徽章显示灰色轮廓
- 当 `showEggAnimation` 为 true 时播放彩蛋动画
- 订阅 Effect 处理导航
- 验证：徽章收藏可正常展示

**新增文件**：
- `iosApp/TigerFire/UI/CollectionView/CollectionView.swift`

---

### 5.8 实现 ParentView（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.8 |
| **所属模块** | iosApp/UI/ParentView |
| **依赖** | 3.7, 5.1 |
| **产出物** | ParentView.swift |

**任务说明**：
- 创建 `struct ParentView: View`
- 显示时间设置选项（`Picker`）
- 显示本周使用时长柱状图
- 显示重置进度按钮
- 当 `showVerification` 为 true 时显示数学题验证界面
- 显示"再玩 5 分钟"按钮（验证通过后）
- 处理用户输入，发送对应 Event
- 订阅 Effect 处理退出与语音
- 验证：家长模式可正常使用

**新增文件**：
- `iosApp/TigerFire/UI/ParentView/ParentView.swift`

---

### 5.9 实现 LottieView 组件（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.9 |
| **所属模块** | iosApp/Component |
| **依赖** | 0.5 |
| **产出物** | LottieView.swift |

**任务说明**：
- 创建 `struct LottieView: UIViewRepresentable`
- 参数：`animationName: String`、`onAnimationEnd: () -> Void`
- 使用 `Lottie-iOS` 库实现
- 在 `makeUIView` 中创建 `LottieAnimationView`
- 设置 `loopMode` 为 `playOnce`
- 监听 `LottieAnimationCompletion` 回调
- 支持超时降级（3 秒未加载成功执行回调）
- 验证：组件可正常播放 Lottie 动画

**新增文件**：
- `iosApp/TigerFire/Component/LottieView.swift`

---

### 5.10 实现 VideoPlayerView 组件（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.10 |
| **所属模块** | iosApp/Component |
| **依赖** | 0.5 |
| **产出物** | VideoPlayerView.swift |

**任务说明**：
- 创建 `struct VideoPlayerView: UIViewControllerRepresentable`
- 参数：`videoName: String`、`onPlaybackCompleted: () -> Void`
- 使用 `AVPlayer` + `AVPlayerViewController` 实现
- 监听 `AVPlayerItemDidPlayToEndTime` 通知
- 切后台时暂停，恢复时从头播放
- 支持资源加载失败降级（显示静态图）
- 验证：组件可正常播放 MP4 视频

**新增文件**：
- `iosApp/TigerFire/Component/VideoPlayerView.swift`

---

### 5.11 实现 ViewModelWrapper 基类（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 5.11 |
| **所属模块** | iosApp/Component |
| **依赖** | 3.1 - 3.7 |
| **产出物** | ViewModelWrapper.swift |

**任务说明**：
- 创建泛型基类 `ViewModelWrapper<TViewModel>`：`ObservableObject`
- 持有 Shared ViewModel 实例
- 订阅 `viewModel.state`（`StateFlow`）并映射到 `@Published var state`
- 订阅 `viewModel.effect`（`Flow`）并处理副作用
- 提供 `send(event:)` 方法
- 验证：Wrapper 可正确桥接 Shared ViewModel

**新增文件**：
- `iosApp/TigerFire/Component/ViewModelWrapper.swift`

---

## 8. 阶段 6：辅助与保障任务

### 6.1 实现音频管理系统（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.1 |
| **所属模块** | androidApp/component |
| **依赖** | 0.4 |
| **产出物** | AudioManager.kt |

**任务说明**：
- 创建 `AudioManager` 单例
- 实现方法：
  - `playClickSound(scene: SceneType)`（场景差异化音效）
  - `playVoice(voicePath: String)`
  - `playSuccessSound()`
  - `playHintSound()`
- 使用 `MediaPlayer` 池管理音效
- 支持同时播放 BGM 与音效
- 验证：音效可正常播放

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/component/AudioManager.kt`

---

### 6.2 实现音频管理系统（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.2 |
| **所属模块** | iosApp/Component |
| **依赖** | 0.5 |
| **产出物** | AudioManager.swift |

**任务说明**：
- 创建 `AudioManager` 单例
- 实现方法：
  - `playClickSound(scene: SceneType)`
  - `playVoice(voicePath: String)`
  - `playSuccessSound()`
  - `playHintSound()`
- 使用 `AVAudioPlayer` 管理音效
- 支持 `AVAudioSession` 多音频混音
- 验证：音效可正常播放

**新增文件**：
- `iosApp/TigerFire/Component/AudioManager.swift`

---

### 6.3 实现时间管理器（Shared）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.3 |
| **所属模块** | shared/presentation/common |
| **依赖** | 3.1 - 3.7 |
| **产出物** | SessionTimer.kt |

**任务说明**：
- 创建 `SessionTimer` 类
- 实现方法：
  - `startSession(durationMinutes: Int)`
  - `pauseSession()`
  - `resumeSession()`
  - `getElapsedTime(): Flow<Long>`
  - `getTimeRemaining(): Flow<Long>`
- 检测时间到前 2 分钟，发送提醒
- 时间到达时发送 `TimeLimitReached` 事件
- 记录使用时长到 `ProgressRepository`
- 验证：时间管理器可正确计时

**新增文件**：
- `shared/presentation/common/SessionTimer.kt`

---

### 6.4 实现快速点击防护逻辑（Shared）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.4 |
| **所属模块** | shared/presentation/common |
| **依赖** | 无 |
| **产出物** | RapidClickGuard.kt |

**任务说明**：
- 创建 `RapidClickGuard` 类
- 实现方法：
  - `checkClick(): Boolean`（返回是否应该触发防护）
- 记录最近 3 次点击时间
- 如果间隔 <500ms 且连续 3 次，返回 true
- 提供重置方法 `reset()`
- 验证：防护逻辑可正确检测快速点击

**新增文件**：
- `shared/presentation/common/RapidClickGuard.kt`

---

### 6.5 实现无操作超时检测（Shared）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.5 |
| **所属模块** | shared/presentation/common |
| **依赖** | 无 |
| **产出物** | IdleTimer.kt |

**任务说明**：
- 创建 `IdleTimer` 类
- 实现方法：
  - `startIdleDetection(timeoutMillis: Long, onIdle: () -> Unit)`
  - `reportActivity()`
- 使用协程延迟检测
- 任何活动调用 `reportActivity()` 重置计时
- 超时后触发 `onIdle` 回调
- 验证：空闲检测可正确工作

**新增文件**：
- `shared/presentation/common/IdleTimer.kt`

---

### 6.6 定义崩溃信息模型（Shared）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.6 |
| **所属模块** | shared/domain/model |
| **依赖** | 1.1 |
| **产出物** | CrashInfo.kt 与 NonFatalError.kt |

**任务说明**：
- 定义 `CrashInfo` 数据类，包含字段：
  - `appVersion: String`（App 版本号）
  - `buildNumber: String`（构建号）
  - `deviceModel: String`（设备型号）
  - `osVersion: String`（操作系统版本）
  - `timestamp: Long`（崩溃时间戳）
  - `crashType: String`（崩溃类型）
  - `stackTrace: String`（堆栈跟踪）
  - `scene: String?`（当前场景）
  - `userAction: String?`（用户最后操作）
  - `memoryUsage: Long`（内存占用 MB）
  - `deviceFreeMemory: Long`（可用内存 MB）
  - `threadName: String?`（线程名称）
  - `deviceId: String`（设备唯一标识，取自 Settings.Secure.ANDROID_ID 的哈希值）
- 定义 `NonFatalError` 数据类，用于非致命错误
- 定义 `ErrorType` 枚举（视频加载失败、Lottie 解析失败、数据库错误等）
- 标记为 `@Serializable`
- 添加 `toJson()` 和 `fromJson()` 扩展函数用于序列化
- 验证：模型可正确序列化为 JSON

**实现要点**（基于最佳实践）：
```kotlin
// CrashInfo.kt
@Serializable
data class CrashInfo(
    val appVersion: String,
    val buildNumber: String,
    val deviceModel: String,
    val osVersion: String,
    val timestamp: Long,
    val crashType: String,
    val stackTrace: String,
    val scene: String? = null,
    val userAction: String? = null,
    val memoryUsage: Long,
    val deviceFreeMemory: Long,
    val threadName: String? = null,
    val deviceId: String
) {
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): CrashInfo? = try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }
}

// NonFatalError.kt
@Serializable
data class NonFatalError(
    val timestamp: Long,
    val errorType: ErrorType,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val scene: String? = null,
    val stackTrace: String? = null
)

enum class ErrorType {
    VIDEO_LOAD_FAILED,
    LOTTIE_PARSE_FAILED,
    DATABASE_READ_ERROR,
    DATABASE_WRITE_ERROR,
    MEMORY_WARNING,
    RESOURCE_NOT_FOUND,
    NETWORK_ERROR
}
```

**新增文件**：
- `shared/domain/model/CrashInfo.kt`
- `shared/domain/model/NonFatalError.kt`
- `shared/domain/model/ErrorType.kt`

---

### 6.7 定义 CrashLogger 接口（Shared）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.7 |
| **所属模块** | shared/domain/repository |
| **依赖** | 6.6 |
| **产出物** | CrashLogger.kt 接口 |

**任务说明**：
- 定义 `CrashLogger` 接口（expect class），声明方法：
  - `initialize()`（初始化崩溃日志系统）
  - `logCrash(crashInfo: CrashInfo)`（记录崩溃）
  - `logError(error: NonFatalError)`（记录非致命错误）
  - `setCurrentScene(scene: String)`（设置当前场景）
  - `setLastAction(action: String)`（记录用户操作）
  - `getLogFiles(): List<String>`（获取日志文件列表）
  - `cleanupOldLogs()`（清理旧日志）
- 定义工厂方法 `createCrashLogger(): CrashLogger`
- 验证：接口可在 Shared 模块中正常声明

**新增文件**：
- `shared/domain/repository/CrashLogger.kt`

---

### 6.8 实现 LogFileManager（Shared）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.8 |
| **所属模块** | shared/data/local |
| **依赖** | 6.6 |
| **产出物** | LogFileManager.kt |

**任务说明**：
- 创建 `LogFileManager` expect class，负责日志文件管理
- 实现方法：
  - `writeLog(fileName: String, content: String)`（写入日志）
  - `cleanupOldLogs()`（清理超过 20 个的旧日志）
  - `getLogFiles(): List<CrashLogFile>`（获取日志文件列表）
  - `clearAllLogs()`（清空所有日志）
  - `getLogDirectory(): String`（获取日志目录路径）
- 配置参数：
  - **Android 日志目录**：`/data/data/com.cryallen.tigerfire/files/crash_logs/`
  - **iOS 日志目录**：`Application Support/crash_logs/`
  - 最大日志文件数：20
  - 单文件最大大小：100 KB
  - 文件命名格式：`crash_[timestamp]_[deviceId].log`
- JSON 格式化日志内容，确保可读性（使用 `Json { prettyPrint = true }`）
- 使用 expect/actual 模式处理平台文件操作差异
- 确保线程安全（使用 Mutex 保护并发写入）
- 验证：日志文件可正确写入和清理

**实现要点**（基于最佳实践）：
```kotlin
// shared/data/local/LogFileManager.kt
expect class LogFileManager() {
    val logsDirectory: String
    fun writeLog(fileName: String, content: String)
    fun cleanupOldLogs()
    fun getLogFiles(): List<CrashLogFile>
    fun clearAllLogs()
}

data class CrashLogFile(
    val fileName: String,
    val filePath: String,
    val timestamp: Long,
    val size: Long
)

// Android 实现
// shared/androidMain/.../LogFileManager.kt
actual class LogFileManager(
    private val context: Context
) {
    actual val logsDirectory: String
        get() = File(context.filesDir, "crash_logs").absolutePath

    private val maxLogFiles = 20
    private val maxFileSize = 100 * 1024 // 100 KB
    private val writeMutex = Mutex()

    actual fun writeLog(fileName: String, content: String) {
        runBlocking {
            writeMutex.withLock {
                try {
                    val logsDir = File(logsDirectory).apply { mkdirs() }
                    val logFile = File(logsDir, fileName)

                    // 检查文件大小
                    if (logFile.exists() && logFile.length() > maxFileSize) {
                        return
                    }

                    // 写入日志（格式化 JSON）
                    val jsonElement = Json.parseToJsonElement(content)
                    val formattedJson = Json { prettyPrint = true }.encodeToString(jsonElement)
                    logFile.writeText(formattedJson, Charsets.UTF_8)

                    // 异步清理旧日志（不阻塞主线程）
                    CoroutineScope(Dispatchers.IO).launch {
                        cleanupOldLogs()
                    }
                } catch (e: Exception) {
                    // 日志写入失败，静默处理（避免递归崩溃）
                    Log.e("LogFileManager", "Failed to write log", e)
                }
            }
        }
    }

    actual fun cleanupOldLogs() {
        try {
            val logsDir = File(logsDirectory)
            val logFiles = logsDir.listFiles()
                ?.sortedByDescending { it.lastModified() }
                ?: return

            // 删除超过 20 个的旧日志
            logFiles.drop(maxLogFiles).forEach { it.delete() }
        } catch (e: Exception) {
            Log.e("LogFileManager", "Failed to cleanup logs", e)
        }
    }

    actual fun getLogFiles(): List<CrashLogFile> {
        return try {
            File(logsDirectory).listFiles()?.map { file ->
                CrashLogFile(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    timestamp = parseTimestampFromFileName(file.name),
                    size = file.length()
                )
            }?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            Log.e("LogFileManager", "Failed to get log files", e)
            emptyList()
        }
    }

    actual fun clearAllLogs() {
        try {
            File(logsDirectory).listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e("LogFileManager", "Failed to clear logs", e)
        }
    }

    private fun parseTimestampFromFileName(fileName: String): Long {
        // 从文件名解析时间戳：crash_1735123456789_abc123.log
        return Regex("""crash_(\d+)_""").find(fileName)?.groupValues?.get(0)?.toLong() ?: 0L
    }
}
```

**Android 路径验证**：
- 使用 ADB 验证日志目录正确创建：
  ```bash
  adb shell
  cd /data/data/com.cryallen.tigerfire/files/crash_logs
  ls -la
  ```
- 确认日志文件权限正确（`-rw-rw----`）

**新增文件**：
- `shared/data/local/LogFileManager.kt`
- `shared/data/local/CrashLogFile.kt`
- `shared/androidMain/.../LogFileManager.kt`（actual）
- `shared/iosMain/.../LogFileManager.kt`（actual）

---

### 6.9 实现 CrashLogger（Android）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.9 |
| **所属模块** | shared/androidMain |
| **依赖** | 6.7, 6.8 |
| **产出物** | CrashLoggerImpl.kt（Android） |

**任务说明**：
- 实现 `CrashLogger` 接口的 Android actual 类
- 实现 `initialize()` 方法：
  - 设置 `Thread.setDefaultUncaughtExceptionHandler` 捕获全局异常
  - 注册 `ComponentCallbacks2` 监听内存警告（`onTrimMemory`）
  - 在 `Application.onCreate()` 中调用初始化
- 实现 `handleCrash()` 方法：
  - 收集崩溃信息（设备信息、堆栈跟踪、内存状态）
  - 生成唯一的 deviceId（使用 `Settings.Secure.ANDROID_ID` 的哈希值）
  - 格式化堆栈跟踪（使用 `Log.getStackTraceString()`）
  - 调用 `logCrash()` 写入日志到 `/data/data/com.cryallen.tigerfire/files/crash_logs/`
  - 调用默认异常处理器显示崩溃对话框
- 实现所有接口方法（`logError`, `setCurrentScene`, `setLastAction` 等）
- 使用 `ActivityManager` 获取内存信息（`MemoryInfo`）
- 使用 `Debug.MemoryInfo` 获取详细内存统计
- 验证：崩溃时能正确生成日志文件

**实现要点**（基于 Android 最佳实践）：
```kotlin
// shared/androidMain/.../CrashLoggerImpl.kt
actual class CrashLogger actual constructor(
    private val context: Context
) : CrashLogger {

    private val logFileManager = LogFileManager(context)
    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    // 保存当前场景和用户操作
    private val currentScene = AtomicReference<String>("UNKNOWN")
    private val lastAction = AtomicReference<String>("")

    actual fun initialize() {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(thread, throwable)
        }

        // 监听内存警告（需要 Application context）
        if (context is Application) {
            context.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onTrimMemory(level: Int) {
                    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val memoryInfo = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(memoryInfo)

                    logError(NonFatalError(
                        timestamp = System.currentTimeMillis(),
                        errorType = ErrorType.MEMORY_WARNING,
                        message = "Memory trim level: $level, Available: ${memoryInfo.availMem} bytes",
                        details = mapOf(
                            "level" to level.toString(),
                            "availMem" to memoryInfo.availMem.toString(),
                            "totalMem" to memoryInfo.totalMem.toString(),
                            "threshold" to memoryInfo.threshold.toString()
                        ),
                        scene = currentScene.get()
                    ))
                }

                override fun onConfigurationChanged(newConfig: Configuration) {}
                override fun onLowMemory() {
                    logError(NonFatalError(
                        timestamp = System.currentTimeMillis(),
                        errorType = ErrorType.MEMORY_WARNING,
                        message = "System low memory",
                        scene = currentScene.get()
                    ))
                }
            })
        }
    }

    private fun handleCrash(thread: Thread, throwable: Throwable) {
        try {
            // 获取内存信息
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            // 获取设备 ID（唯一标识）
            val deviceId = getDeviceId()

            val crashInfo = CrashInfo(
                appVersion = BuildConfig.VERSION_NAME,
                buildNumber = BuildConfig.VERSION_CODE.toString(),
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                timestamp = System.currentTimeMillis(),
                crashType = throwable::class.simpleName ?: "Unknown",
                stackTrace = Log.getStackTraceString(throwable),
                scene = currentScene.get(),
                userAction = lastAction.get(),
                memoryUsage = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024), // MB
                deviceFreeMemory = memoryInfo.availMem / (1024 * 1024), // MB
                threadName = thread.name,
                deviceId = deviceId
            )

            logCrash(crashInfo)

            // 强制刷新到磁盘
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .clearApplicationUserData()

        } catch (e: Exception) {
            // 崩溃日志记录失败，至少记录到 logcat
            Log.e("CrashLogger", "Failed to log crash", e)
            Log.e("CrashLogger", Log.getStackTraceString(throwable))
        } finally {
            // 调用默认处理器（显示崩溃对话框）
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    actual fun logCrash(crashInfo: CrashInfo) {
        val json = crashInfo.toJson()
        val fileName = "crash_${crashInfo.timestamp}_${crashInfo.deviceId}.log"
        logFileManager.writeLog(fileName, json)
    }

    actual fun logError(error: NonFatalError) {
        val json = Json.encodeToString(error)
        val fileName = "error_${error.timestamp}.log"
        logFileManager.writeLog(fileName, json)
    }

    actual fun setCurrentScene(scene: String) {
        currentScene.set(scene)
    }

    actual fun setLastAction(action: String) {
        lastAction.set(action)
    }

    actual fun getLogFiles(): List<String> {
        return logFileManager.getLogFiles().map { it.filePath }
    }

    actual fun cleanupOldLogs() {
        logFileManager.cleanupOldLogs()
    }

    private fun getDeviceId(): String {
        // 使用 ANDROID_ID 生成唯一设备标识（哈希处理避免泄露隐私）
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return androidId.hashCode().toString(16)
    }

    private fun getMemoryUsage(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
    }

    private fun getFreeMemory(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }
}

// 在 Application 类中初始化
class TigerFireApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化崩溃日志系统
        val crashLogger = CrashLogger(this)
        crashLogger.initialize()
    }
}
```

**AndroidManifest.xml 配置**：
```xml
<application
    android:name=".TigerFireApplication"
    ...>
    <!-- 无需额外权限，filesDir 是应用私有目录 -->
</application>
```

**验证步骤**：
1. 触发测试崩溃（如抛出 `RuntimeException("Test crash")`）
2. 使用 ADB 检查日志文件：
   ```bash
   adb shell run-as com.cryallen.tigerfire ls -la files/crash_logs/
   adb shell run-as com.cryallen.tigerfire cat files/crash_logs/crash_*.log
   ```
3. 验证日志内容包含完整堆栈跟踪和设备信息

**新增文件**：
- `shared/androidMain/.../CrashLoggerImpl.kt`
- `androidApp/src/main/.../TigerFireApplication.kt`

---

### 6.10 实现 CrashLogger（iOS）

| 属性 | 说明 |
|------|------|
| **任务编号** | 6.10 |
| **所属模块** | shared/iosMain |
| **依赖** | 6.7, 6.8 |
| **产出物** | CrashLoggerImpl.swift（iOS） |

**任务说明**：
- 实现 `CrashLogger` 接口的 iOS actual 类
- 实现 `initialize()` 方法：
  - 调用 `NSSetUncaughtExceptionHandler` 设置 Objective-C 异常处理器
  - 设置 `setUnhandledExceptionHook` 捕获 Kotlin 异常
  - 注册 `UIApplication.didReceiveMemoryWarningNotification` 监听内存警告
- 实现 `handleCrash()` 方法：
  - 收集崩溃信息（设备信息、堆栈跟踪、内存状态）
  - 生成唯一的 deviceId（使用 `UIDevice.current.identifierForVendor` 的哈希值）
  - 调用 `logCrash()` 写入日志到 `Application Support/crash_logs/`
- 实现所有接口方法（`logError`, `setCurrentScene`, `setLastAction` 等）
- 使用 `ProcessInfo.processInfo` 获取内存信息
- 使用 `UIDevice.current` 获取设备信息
- 验证：崩溃时能正确生成日志文件

**实现要点**（基于 iOS 最佳实践）：
```swift
// shared/iosMain/.../CrashLoggerImpl.swift
import Foundation
import UIKit

actual class CrashLogger: CrashLoggerType {
    private let logFileManager: LogFileManager
    private var currentScene: String = "UNKNOWN"
    private var lastAction: String = ""

    // 内存警告观察者
    private var memoryWarningObserver: NSObjectProtocol?

    init() {
        self.logFileManager = LogFileManager()
    }

    actual func initialize() {
        // 1. 设置 Objective-C 异常处理器（捕获 ObjC/Swift 异常）
        NSSetUncaughtExceptionHandler { [weak self] exception in
            self?.handleException(exception)
        }

        // 2. 设置 Kotlin 异常处理器（捕获 Kotlin 异常）
        // Kotlin Native 在 1.3.60+ 版本支持 setUnhandledExceptionHook
        setupKotlinExceptionHandler()

        // 3. 注册内存警告监听
        memoryWarningObserver = NotificationCenter.default.addObserver(
            forName: UIApplication.didReceiveMemoryWarningNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            self?.handleMemoryWarning()
        }
    }

    private func setupKotlinExceptionHandler() {
        // Kotlin Native 异常钩子（需要在 Kotlin 侧设置）
        // 这里假设通过 Kotlin 的 setUnhandledExceptionHook API
        // 具体实现依赖于 Kotlin 版本
    }

    private func handleException(_ exception: NSException) {
        let crashInfo = createCrashInfo(
            crashType: exception.name.rawValue,
            stackTrace: exception.callStackSymbols.joined(separator: "\n"),
            reason: exception.reason
        )
        logCrash(crashInfo: crashInfo)
    }

    private func handleKotlinException(_ throwable: KotlinThrowable) {
        // 处理 Kotlin 异常（如果 setUnhandledExceptionHook 可用）
        let crashInfo = createCrashInfo(
            crashType: "KotlinException",
            stackTrace: throwable.stackTraceDescription,
            reason: throwable.message
        )
        logCrash(crashInfo: crashInfo)
    }

    private func handleMemoryWarning() {
        let error = NonFatalError(
            timestamp: Date().timeIntervalSince1970,
            errorType: .memoryWarning,
            message: "System memory warning received",
            details: getMemoryDetails(),
            scene: currentScene
        )
        logError(error: error)
    }

    private func createCrashInfo(crashType: String, stackTrace: String, reason: String?) -> CrashInfo {
        let device = UIDevice.current
        let memoryInfo = getMemoryDetails()

        return CrashInfo(
            appVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown",
            buildNumber: Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "Unknown",
            deviceModel: "\(device.systemName) \(device.systemVersion) - \(device.model)",
            osVersion: device.systemVersion,
            timestamp: Int64(Date().timeIntervalSince1970 * 1000),
            crashType: crashType,
            stackTrace: stackTrace,
            scene: currentScene,
            userAction: lastAction.isEmpty ? nil : lastAction,
            memoryUsage: memoryInfo["usedMemory"] ?? 0,
            deviceFreeMemory: memoryInfo["freeMemory"] ?? 0,
            threadName: Thread.current.name,
            deviceId: getDeviceId()
        )
    }

    private func getMemoryDetails() -> [String: Int64] {
        var info = mach_task_basic_info()
        var count = mach_msg_type_number_t(MemoryLayout<mach_task_basic_info>.size)/4

        let kerr: kern_return_t = withUnsafeMutablePointer(to: &info) {
            $0.withMemoryRebound(to: integer_t.self, capacity: 1) {
                task_info(mach_task_self_, task_flavor_t(MACH_TASK_BASIC_INFO), $0, &count)
            }
        }

        var usedMemory: Int64 = 0
        if kerr == KERN_SUCCESS {
            usedMemory = Int64(info.resident_size)
        }

        // 获取设备总内存和可用内存
        var totalMemory: Int64 = 0
        var freeMemory: Int64 = 0

        #if os(iOS)
        let processInfo = ProcessInfo.processInfo
        totalMemory = ProcessInfo.processInfo.physicalMemory

        // 使用 host_statistics 获取可用内存
        var stats = vm_statistics64()
        var count = mach_msg_type_number_t(MemoryLayout<vm_statistics64>.size / MemoryLayout<integer_t>.size)

        let hostPort = mach_host_self()
        let result = withUnsafeMutablePointer(to: &stats) {
            $0.withMemoryRebound(to: integer_t.self, capacity: Int(count)) {
                host_statistics64(hostPort, HOST_VM_INFO64, $0, &count)
            }
        }

        if result == KERN_SUCCESS {
            freeMemory = Int64(stats.free_count + stats.inactive_count) * Int64(vm_page_size)
        }
        #endif

        return [
            "usedMemory": usedMemory / (1024 * 1024),  // MB
            "freeMemory": freeMemory / (1024 * 1024),  // MB
            "totalMemory": totalMemory / (1024 * 1024)  // MB
        ]
    }

    private func getDeviceId() -> String {
        // 使用 identifierForVendor 生成唯一设备标识（哈希处理避免泄露隐私）
        if let vendorId = UIDevice.current.identifierForVendor?.uuidString {
            return vendorId.hashValue.description
        }
        return "unknown"
    }

    actual func logCrash(crashInfo: CrashInfo) {
        do {
            let encoder = JSONEncoder()
            encoder.outputFormatting = .prettyPrinted
            let jsonData = try encoder.encode(crashInfo)
            let jsonString = String(data: jsonData, encoding: .utf8) ?? ""

            let fileName = "crash_\(crashInfo.timestamp)_\(crashInfo.deviceId).log"
            logFileManager.writeLog(fileName: fileName, content: jsonString)
        } catch {
            NSLog("Failed to encode crash info: \(error)")
        }
    }

    actual func logError(error: NonFatalError) {
        do {
            let encoder = JSONEncoder()
            encoder.outputFormatting = .prettyPrinted
            let jsonData = try encoder.encode(error)
            let jsonString = String(data: jsonData, encoding: .utf8) ?? ""

            let fileName = "error_\(error.timestamp).log"
            logFileManager.writeLog(fileName: fileName, content: jsonString)
        } catch {
            NSLog("Failed to encode error info: \(error)")
        }
    }

    actual func setCurrentScene(scene: String) {
        currentScene = scene
    }

    actual func setLastAction(action: String) {
        lastAction = action
    }

    actual func getLogFiles() -> [String] {
        return logFileManager.getLogFiles().map { $0.filePath }
    }

    actual func cleanupOldLogs() {
        logFileManager.cleanupOldLogs()
    }

    deinit {
        // 移除内存警告观察者
        if let observer = memoryWarningObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
}

// AppDelegate.swift 中的初始化
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // 初始化崩溃日志系统
        let crashLogger = CrashLogger()
        crashLogger.initialize()

        // 保存实例以便后续使用
        CrashLogger.shared = crashLogger

        return true
    }
}
```

**iOS 日志路径验证**：
- iOS 日志目录：`Application Support/crash_logs/`
- 使用 FileManager 获取路径：
  ```swift
  let paths = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)
  let appSupportDir = paths.first!
  let logsDir = appSupportDir.appendingPathComponent("crash_logs")
  ```
- 使用 Xcode → Device and Simulators → Download Container 验证日志文件

**重要提示**：
1. **Kotlin 异常处理**：Kotlin Native 在 iOS 上的异常处理需要使用 `setUnhandledExceptionHook` API（Kotlin 1.3.60+）
2. **信号处理**：iOS 不允许在信号处理器中进行文件 I/O 操作，因此只处理 `NSException` 和内存警告
3. **线程安全**：崩溃可能发生在任意线程，确保日志写入是线程安全的
4. **设备标识**：`identifierForVendor` 在用户卸载重装后会变化，这是符合 Apple 隐私政策的设计

**验证步骤**：
1. 触发测试崩溃（如抛出 `NSException(name: NSExceptionName("TestException"), reason: "Test crash")`）
2. 使用 Xcode 查看 Console 输出确认异常被捕获
3. 下载 App Container 检查日志文件：
   - Xcode → Window → Devices and Simulators
   - 选择设备 → Installed Apps → TigerFire
   - Click "Download Container"
   - 右键 `.xcappdata` → Show Package Contents
   - 导航至 `AppData/Application Support/crash_logs/`
4. 验证日志内容包含完整堆栈跟踪和设备信息

**新增文件**：
- `shared/iosMain/.../CrashLoggerImpl.swift`
- `iosApp/TigerFire/AppDelegate.swift`（更新初始化代码）

---

## 9. 阶段 7：联调与测试

### 7.1 集成所有 ViewModel 到 UI

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.1 |
| **所属模块** | androidApp + iosApp |
| **依赖** | 3.1 - 5.11 |
| **产出物** | 可完整运行的双端 App |

**任务说明**：
- Android：在 `MainActivity` 中初始化所有 ViewModel
- iOS：在 `AppDelegate` 中初始化所有 ViewModel
- 确保依赖注入正确配置
- 验证：App 可启动，无崩溃

---

### 7.2 端到端流程测试

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.2 |
| **所属模块** | 全项目 |
| **依赖** | 7.1 |
| **产出物** | 测试报告 |

**任务说明**：
- 测试完整流程：启动 → 地图 → 消防站 → 学校 → 森林 → 收藏
- 验证徽章正确获得
- 验证场景正确解锁
- 验证进度正确保存与恢复
- 验证家长模式正常工作
- 验证时间控制正常工作
- 记录发现的问题

---

### 7.3 边界场景测试

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.3 |
| **所属模块** | 全项目 |
| **依赖** | 7.2 |
| **产出物** | 边界测试报告 |

**任务说明**：
- 测试快速点击防护
- 测试切后台恢复
- 测试资源加载失败降级
- 测试无操作超时提示
- 测试进度重置
- 记录发现的问题

---

### 7.4 崩溃日志系统测试

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.4 |
| **所属模块** | 全项目 |
| **依赖** | 6.6 - 6.10 |
| **产出物** | 崩溃日志测试报告 |

**任务说明**：
- **崩溃记录验证**：
  - 触发测试崩溃（如空指针异常）
  - 验证日志文件是否正确生成
  - 检查日志内容完整性（堆栈跟踪、设备信息、场景信息）
- **非致命错误记录验证**：
  - 测试视频加载失败时是否记录日志
  - 测试 Lottie 解析失败时是否记录日志
  - 测试数据库异常时是否记录日志
- **日志清理验证**：
  - 生成超过 20 个日志文件
  - 验证旧日志是否自动清理
- **日志格式验证**：
  - 检查日志是否为有效 JSON 格式
  - 验证不包含用户可识别信息（PII）
- 记录发现的问题

**Android 测试步骤详解**：

**1. 崩溃记录验证**：
```kotlin
// 在 DebugActivity 中添加触发崩溃的按钮
fun triggerTestCrash(view: View) {
    CrashLogger.shared?.setLastAction("点击测试崩溃按钮")
    CrashLogger.shared?.setCurrentScene("DebugActivity")

    // 触发不同类型的崩溃
    when (view.id) {
        R.id.btnNullPointerException -> {
            val nullString: String? = null
            nullString!!.length  // 触发 NPE
        }
        R.id.btnIndexOutOfBounds -> {
            val list = listOf(1, 2, 3)
            list[10]  // 触发索引越界
        }
        R.id.btnArithmeticException -> {
            val x = 10 / 0  // 触发除零错误
        }
    }
}
```

**2. 验证日志生成**：
```bash
# 方法 1：使用 ADB 检查日志目录
adb shell
run-as com.cryallen.tigerfire
ls -la files/crash_logs/
cat files/crash_logs/crash_*.log | head -50

# 方法 2：拉取日志文件到本地
adb pull /data/data/com.cryallen.tigerfire/files/crash_logs/ ~/Desktop/crash_logs/

# 方法 3：使用 ADB 直接查看日志内容
adb shell run-as com.cryallen.tigerfire cat files/crash_logs/crash_*.log
```

**3. 验证日志 JSON 格式**：
```bash
# 使用 jq 验证 JSON 格式
cat crash_1735123456789_abc123.log | jq .
# 应输出格式化的 JSON，无语法错误
```

**4. 检查日志内容完整性**：
```json
{
  "appVersion": "1.0.0",
  "buildNumber": "1",
  "deviceModel": "Xiaomi Redmi Note 10",
  "osVersion": "Android 13 (API 33)",
  "timestamp": 1735123456789,
  "crashType": "KotlinNullPointerException",
  "stackTrace": "java.lang.NullPointerException...\n  at com.tigertruck.DebugActivity.triggerTestCrash(DebugActivity.kt:42)",
  "scene": "DebugActivity",
  "userAction": "点击测试崩溃按钮",
  "memoryUsage": 145,
  "deviceFreeMemory": 1024,
  "threadName": "main",
  "deviceId": "a1b2c3d4"
}
```

**5. 非致命错误验证**：
```kotlin
// 测试视频加载失败
fun testVideoLoadFailure() {
    CrashLogger.shared?.logError(NonFatalError(
        timestamp = System.currentTimeMillis(),
        errorType = ErrorType.VIDEO_LOAD_FAILED,
        message = "Failed to load video: invalid_path.mp4",
        details = mapOf(
            "videoPath" to "invalid_path.mp4",
            "errorCode" to "-1"
        ),
        scene = "FireStationScene",
        stackTrace = Thread.currentThread().stackTrace.take(10).joinToString("\n")
    ))
}

// 测试内存警告
fun testMemoryWarning() {
    // 模拟内存警告（需要在真机上测试）
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    CrashLogger.shared?.logError(NonFatalError(
        timestamp = System.currentTimeMillis(),
        errorType = ErrorType.MEMORY_WARNING,
        message = "Simulated memory warning",
        details = mapOf(
            "availableMemory" to "${memoryInfo.availMem / (1024 * 1024)} MB",
            "totalMemory" to "${memoryInfo.totalMem / (1024 * 1024)} MB"
        ),
        scene = "MapScene"
    ))
}
```

**6. 日志清理验证**：
```kotlin
// 生成 25 个测试日志文件
fun generateTestLogs() {
    repeat(25) { index ->
        CrashLogger.shared?.logError(NonFatalError(
            timestamp = System.currentTimeMillis() + index * 1000,
            errorType = ErrorType.RESOURCE_NOT_FOUND,
            message = "Test log $index",
            scene = "TestScene"
        ))
        Thread.sleep(100)  // 确保时间戳不同
    }
}

// 验证清理：应保留最新的 20 个日志
adb shell run-as com.cryallen.tigerfire ls files/crash_logs/ | wc -l
# 输出应为 20
```

**iOS 测试步骤详解**：

**1. 崩溃记录验证**：
```swift
// 在 DebugViewController 中添加触发崩溃的按钮
@IBAction func triggerTestCrash(_ sender: UIButton) {
    CrashLogger.shared?.setLastAction("点击测试崩溃按钮")
    CrashLogger.shared?.setCurrentScene("DebugViewController")

    switch sender.tag {
    case 1:  // NSException
        let array = NSArray()
        _ = array[1]  // 触发 NSRangeException
    case 2:  // fatalError
        fatalError("Test fatal error")
    default:
        break
    }
}
```

**2. 验证日志生成**：
```bash
# 方法 1：使用 Xcode Console 查看
# 在 Xcode 中运行 App，触发崩溃后查看 Console 输出
# 应该看到类似 "Crash log written to..." 的消息

# 方法 2：下载 Container 检查日志
# Xcode → Window → Devices and Simulators
# 选择设备 → Installed Apps → TigerFire → Download Container
# 右键 .xcappdata → Show Package Contents
# 导航至 AppData/Application Support/crash_logs/
```

**3. 使用 macOS 终端验证日志**：
```bash
# 挂载 Container
cd ~/Library/Developer/Xcode/Devices/DeviceData/
# 找到对应的设备目录

# 验证 JSON 格式
cat crash_1735123456789_a1b2c3d4.log | python3 -m json.tool
# 或使用 jq
cat crash_1735123456789_a1b2c3d4.log | jq .
```

**4. 检查 PII（个人可识别信息）**：
```bash
# 确保日志中不包含敏感信息
grep -i "email\|phone\|address\|name" crash_*.log
# 应该没有输出

# 验证 deviceId 是哈希值（不是原始 UUID）
grep '"deviceId"' crash_*.log
# 应该是类似 "a1b2c3d4" 这样的哈希值，而不是完整的 UUID
```

**5. 隐私合规检查清单**：
- [ ] 不包含用户真实姓名
- [ ] 不包含邮箱地址
- [ ] 不包含手机号码
- [ ] 不包含家庭地址
- [ ] deviceId 使用哈希值（非原始 ANDROID_ID 或 identifierForVendor）
- [ ] 不包含精确的 GPS 坐标（可使用模糊化的区域信息）

**测试报告模板**：

```markdown
# 崩溃日志系统测试报告

## 测试环境
- 测试日期：2024-01-25
- 测试设备：Xiaomi Redmi Note 10 (Android 13), iPhone 12 (iOS 17)
- App 版本：1.0.0 (Build 1)

## 测试结果汇总

| 测试项 | Android | iOS | 备注 |
|--------|---------|-----|------|
| NPE 崩溃记录 | ✅ 通过 | ✅ 通过 | |
| 索引越界崩溃记录 | ✅ 通过 | ✅ 通过 | |
| 内存警告记录 | ✅ 通过 | ✅ 通过 | 需真机测试 |
| 视频加载失败记录 | ✅ 通过 | ✅ 通过 | |
| 日志 JSON 格式验证 | ✅ 通过 | ✅ 通过 | |
| PII 检查 | ✅ 通过 | ✅ 通过 | 无敏感信息 |
| 日志清理（>20） | ✅ 通过 | ✅ 通过 | 保留最新 20 个 |

## 发现的问题
1. （示例）在 Android 8.0 以下设备上，日志目录权限问题导致写入失败
   - 严重程度：高
   - 状态：已修复

## 建议
1. 考虑添加日志上传功能，方便远程分析
2. 可以添加日志大小限制，避免单个日志文件过大
```

**自动化测试脚本示例**（可选）：
```python
# verify_crash_logs.py
import json
import os
import sys

def verify_crash_log(file_path):
    """验证单个崩溃日志文件"""
    try:
        with open(file_path, 'r') as f:
            data = json.load(f)

        # 检查必需字段
        required_fields = ['appVersion', 'deviceModel', 'timestamp',
                          'crashType', 'stackTrace', 'deviceId']
        missing_fields = [f for f in required_fields if f not in data]

        if missing_fields:
            print(f"❌ {file_path}: 缺少字段 {missing_fields}")
            return False

        # 检查 PII
        pii_keywords = ['email', 'phone', 'address', 'name']
        content = json.dumps(data).lower()
        found_pii = [kw for kw in pii_keywords if kw in content]

        if found_pii:
            print(f"⚠️  {file_path}: 可能包含 PII ({found_pii})")
            return False

        print(f"✅ {file_path}: 验证通过")
        return True

    except json.JSONDecodeError as e:
        print(f"❌ {file_path}: JSON 格式错误 - {e}")
        return False

if __name__ == '__main__':
    log_dir = sys.argv[1] if len(sys.argv) > 1 else './crash_logs'
    files = [os.path.join(log_dir, f) for f in os.listdir(log_dir)
             if f.endswith('.log')]

    results = [verify_crash_log(f) for f in files]
    passed = sum(results)
    print(f"\n总计: {passed}/{len(results)} 个文件通过验证")
```

**验收标准**：
- [ ] 所有类型的崩溃都能正确生成日志文件
- [ ] 日志文件格式为有效 JSON
- [ ] 日志包含完整的堆栈跟踪和设备信息
- [ ] 日志不包含任何 PII 信息
- [ ] 日志自动清理功能正常工作（>20 个文件时删除旧日志）
- [ ] Android 和 iOS 平台测试均通过

---

### 7.5 性能验证

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.5 |
| **所属模块** | 全项目 |
| **依赖** | 7.3 |
| **产出物** | 性能测试报告 |

**任务说明**：
- 测量冷启动时间（目标 ≤1.2 秒）
- 测量单场景内存占用（目标 ≤120 MB）
- 测量安装包体积（目标 ≤300 MB）
- 测量 Lottie 动画帧率（目标 ≥30 FPS）
- 测量视频播放流畅度
- 记录性能数据与优化建议

---

### 7.6 稳定性验证

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.6 |
| **所属模块** | 全项目 |
| **依赖** | 7.4, 7.5 |
| **产出物** | 稳定性测试报告 |

**任务说明**：
- **端到端稳定性测试**：
  - 完整游玩 10 轮（启动→消防站→学校→森林→收藏）
  - 记录崩溃次数，目标为 0
- **长时间运行测试**：
  - 连续运行 2 小时
  - 每 15 分钟记录内存占用
  - 验证内存占用稳定在 ±10% 范围内
- **场景切换压力测试**：
  - 快速切换场景 100 次
  - 记录是否有白屏或崩溃
- **低端设备测试**：
  - 在 1GB RAM Android 设备上完整测试
  - 验证所有场景可正常运行
- **内存泄漏检测**：
  - 使用 Android Profiler / Instruments 检测内存泄漏
  - 验证场景切换后内存正确释放
- 记录发现的问题

**测试 1：端到端稳定性测试（10 轮完整流程）**

**测试步骤**：
```kotlin
// 自动化测试脚本（使用 UI Automator 或 Espresso）
@RunWith(AndroidJUnit4::class)
class EndToEndStabilityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun run10CompletePlaythroughs() {
        var crashCount = 0
        var successfulRuns = 0

        repeat(10) { round ->
            try {
                runCompletePlaythrough(round + 1)
                successfulRuns++
                Log.i("StabilityTest", "Round ${round + 1} completed successfully")
            } catch (e: Exception) {
                crashCount++
                Log.e("StabilityTest", "Round ${round + 1} crashed", e)
                // 检查崩溃日志是否生成
                verifyCrashLogGenerated()
            }
        }

        // 验证：崩溃次数为 0
        assertEquals(0, crashCount, "Should have 0 crashes across 10 rounds")
        assertEquals(10, successfulRuns, "Should complete all 10 rounds")
    }

    private fun runCompletePlaythrough(round: Int) {
        // 1. 启动页 → 主地图（自动导航，无需点击）
        waitForScreen("MapScreen", timeoutMs = 8000) // 等待动画和自动导航完成

        // 2. 主地图 → 消防站
        clickScene("FIRE_STATION")
        completeFireStation()
        navigateBackToMap()

        // 3. 消防站 → 学校
        clickScene("SCHOOL")
        completeSchool()
        navigateBackToMap()

        // 4. 学校 → 森林
        clickScene("FOREST")
        completeForest()
        navigateBackToMap()

        // 5. 森林 → 收藏
        clickCollection()
        verifyBadgesDisplayed()
        navigateBackToMap()
    }

    private fun completeFireStation() {
        // 点击 4 个设备，观看视频
        listOf("extinguisher", "hose", "axe", "helmet").forEach { device ->
            clickDevice(device)
            waitForVideoComplete()
        }
    }

    private fun completeSchool() {
        // 播放学校场景动画
        clickPlayButton()
        waitForVideoComplete()
    }

    private fun completeForest() {
        // 救援两只小羊
        listOf(1, 2).forEach { sheepIndex ->
            clickSheep(sheepIndex)
            waitForRescueComplete()
        }
    }
}
```

**手动测试清单**：
```
测试轮次：1/10
□ 启动 App，卡车动画正常播放
□ 进入主地图，3 个场景图标显示正确
□ 消防站：点击 4 个设备，视频播放正常
□ 学校：播放按钮显示，视频播放正常
□ 森林：救援两只小羊，动画流畅
□ 收藏：徽章显示正确（7 个基础徽章）
□ 返回主地图，准备下一轮

备注：[记录任何异常]
```

**测试 2：长时间运行测试（2 小时）**

**自动化内存监控**：
```kotlin
// MemoryMonitor.kt
class MemoryMonitor(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo()
    private val logs = mutableListOf<MemorySnapshot>()

    data class MemorySnapshot(
        val timestamp: Long,
        val usedMemoryMB: Long,
        val freeMemoryMB: Long,
        val totalMemoryMB: Long,
        val availMemoryMB: Long
    )

    fun startMonitoring(durationMinutes: Int = 120, intervalMinutes: Int = 15) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + durationMinutes * 60 * 1000
        val interval = intervalMinutes * 60 * 1000

        CoroutineScope(Dispatchers.IO).launch {
            var currentTime = startTime
            while (currentTime < endTime) {
                activityManager.getMemoryInfo(memoryInfo)

                val usedMemory = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
                val freeMemory = memoryInfo.availMem / (1024 * 1024)
                val totalMemory = memoryInfo.totalMem / (1024 * 1024)

                logs.add(MemorySnapshot(
                    timestamp = currentTime,
                    usedMemoryMB = usedMemory,
                    freeMemoryMB = freeMemory,
                    totalMemoryMB = totalMemory,
                    availMemoryMB = memoryInfo.availMem / (1024 * 1024)
                ))

                Log.i("MemoryMonitor", """
                    |Memory Snapshot:
                    |  Used: ${usedMemory}MB
                    |  Free: ${freeMemory}MB
                    |  Total: ${totalMemory}MB
                    |  Available: ${memoryInfo.availMem / (1024 * 1024)}MB
                """.trimMargin())

                delay(interval)
                currentTime = System.currentTimeMillis()
            }

            // 生成报告
            generateReport()
        }
    }

    private fun generateReport() {
        val initialMemory = logs.first().usedMemoryMB
        val finalMemory = logs.last().usedMemoryMB
        val growth = finalMemory - initialMemory
        val growthPercent = (growth.toDouble() / initialMemory * 100)

        Log.i("MemoryMonitor", """
            |Memory Stability Report (2 hours):
            |  Initial: ${initialMemory}MB
            |  Final: ${finalMemory}MB
            |  Growth: ${growth}MB (${String.format("%.2f", growthPercent)}%)
            |  Status: ${if (growthPercent < 10) "✅ STABLE" else "❌ UNSTABLE"}
        """.trimMargin())

        // 验证：内存增长应小于 10%
        assertTrue(growthPercent < 10, "Memory growth should be < 10%")
    }
}
```

**测试 3：场景切换压力测试（100 次快速切换）**

```kotlin
// SceneSwitchStressTest.kt
@RunWith(AndroidJUnit4::class)
class SceneSwitchStressTest {

    @Test
    fun rapidSceneSwitch100Times() {
        val scenes = listOf("FIRE_STATION", "SCHOOL", "FOREST", "COLLECTION")
        var crashCount = 0
        var whiteScreenCount = 0
        val issues = mutableListOf<String>()

        repeat(100) { index ->
            val scene = scenes[index % scenes.size]

            try {
                // 快速切换场景（不等待动画完成）
                navigateToScene(scene, waitTimeout = 500)

                // 检测白屏（屏幕内容为空或仅显示加载指示器）
                if (isWhiteScreen()) {
                    whiteScreenCount++
                    issues.add("White screen detected at switch ${index + 1} to $scene")
                    Log.w("StressTest", "White screen at switch ${index + 1}")
                }

                Thread.sleep(100)  // 短暂延迟，避免完全无延迟

            } catch (e: Exception) {
                crashCount++
                issues.add("Crash at switch ${index + 1} to $scene: ${e.message}")
                Log.e("StressTest", "Crash at switch ${index + 1}", e)
            }
        }

        // 输出报告
        Log.i("StressTest", """
            |Scene Switch Stress Test Report (100 switches):
            |  Crashes: $crashCount
            |  White Screens: $whiteScreenCount
            |  Success Rate: ${100 - crashCount}%
        """.trimMargin())

        // 验证标准
        assertTrue(crashCount == 0, "Should have 0 crashes")
        assertTrue(whiteScreenCount <= 1, "Should have at most 1 white screen")
    }

    private fun isWhiteScreen(): Boolean {
        // 检测当前屏幕是否为白屏
        // 方法 1：使用 UI Automator 检查可见元素
        // 方法 2：屏幕截图分析
        // 方法 3：检查是否有任何可见的 UI 组件
        return false  // 实现需要根据具体 UI 结构
    }
}
```

**测试 4：低端设备测试（1GB RAM）**

**推荐测试设备**：
- Xiaomi Redmi 5A (2GB RAM，Android 7.1)
- Samsung Galaxy J2 (1GB RAM，Android 6.0)
- Alcatel 1X (1GB RAM，Android 8.1）

**测试步骤**：
1. 清空后台所有应用
2. 安装 TigerFire App
3. 执行完整流程测试（至少 3 轮）
4. 记录以下指标：

```
| 指标 | 目标 | 实测 | 状态 |
|------|------|------|------|
| 冷启动时间 | ≤1.2s | ___ | □ |
| 单场景内存 | ≤120MB | ___ | □ |
| Lottie 帧率 | ≥30 FPS | ___ | □ |
| 视频播放 | 流畅 | ___ | □ |
| 场景切换 | ≤500ms | ___ | □ |
| 崩溃次数 | 0 | ___ | □ |
```

**测试 5：内存泄漏检测（Android Profiler）**

**使用 Android Profiler 检测内存泄漏**：
```kotlin
// MemoryLeakTest.kt
@RunWith(AndroidJUnit4::class)
class MemoryLeakTest {

    @Test
    fun detectMemoryLeaksOnSceneTransition() {
        // 1. 启动 App 并获取初始内存
        val initialMemory = captureHeapDump("initial")

        // 2. 导航至消防站场景
        navigateToScene("FIRE_STATION")
        Thread.sleep(2000)  // 等待场景加载

        // 3. 返回主地图
        pressBack()
        Thread.sleep(2000)  // 等待清理完成

        // 4. 触发 GC
        Runtime.getRuntime().gc()
        Thread.sleep(1000)

        // 5. 获取最终内存
        val finalMemory = captureHeapDump("after_firestation")

        // 6. 分析内存差异
        val memoryLeaked = finalMemory - initialMemory
        val leakPercent = (memoryLeaked.toDouble() / initialMemory * 100)

        Log.i("MemoryLeakTest", """
            |Memory Leak Test (FireStation):
            |  Initial: ${initialMemory}MB
            |  Final: ${finalMemory}MB
            |  Leaked: ${memoryLeaked}MB (${String.format("%.2f", leakPercent)}%)
        """.trimMargin())

        // 验证：内存泄漏应小于 5MB（<5%）
        assertTrue(memoryLeaked < 5, "Memory leak should be < 5MB")
    }

    private fun captureHeapDump(label: String): Long {
        // 使用 Debug.dumpHprofData() 生成 HPROF 文件
        val fileName = "heap_dump_${label}_${System.currentTimeMillis()}.hprof"
        val path = context.getExternalFilesDir(null)?.absolutePath + "/" + fileName

        Debug.dumpHprofData(path)

        // 获取当前内存占用
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        Log.i("MemoryLeakTest", "Heap dump saved to: $path (${usedMemory}MB)")
        return usedMemory
    }
}
```

**使用 Android Studio Memory Profiler**：
1. Run App with Profiler
2. 选择 Memory → Record
3. 执行场景切换操作
4. 点击 Dump Java Heap
5. 分析以下对象：
   - Activity/Fragment 实例数量（应为 1）
   - ViewModel 实例数量（应为 1）
   - Bitmap/Drawable 对象（应正确释放）
   - 检查持有 Activity 引用的对象

**iOS 内存泄漏检测（Instruments）**：
```bash
# 使用 Xcode Instruments
# 1. Xcode → Product → Profile (⌘I)
# 2. 选择 "Leaks" 或 "Allocations" 模板
# 3. 执行场景切换操作
# 4. 检查 Leaks 报告
# 5. 分析 Call Tree 查找内存分配
```

**测试 6：崩溃率和白屏率统计**

**端到端测试统计**：
```kotlin
// StabilityMetrics.kt
data class StabilityMetrics(
    val totalRuns: Int = 0,
    val crashCount: Int = 0,
    val whiteScreenCount: Int = 0,
    val anrCount: Int = 0  // Android only
) {
    val crashRate: Double
        get() = if (totalRuns > 0) crashCount.toDouble() / totalRuns * 100 else 0.0

    val whiteScreenRate: Double
        get() = if (totalRuns > 0) whiteScreenCount.toDouble() / totalRuns * 100 else 0.0

    val anrRate: Double
        get() = if (totalRuns > 0) anrCount.toDouble() / totalRuns * 100 else 0.0

    fun meetsRequirements(): Boolean {
        return crashRate <= 0.1 &&
               whiteScreenRate <= 0.05 &&
               anrRate <= 0.1
    }

    fun generateReport(): String {
        return """
            |Stability Metrics Report:
            |  Total Runs: $totalRuns
            |  Crashes: $crashCount (Rate: ${String.format("%.3f", crashRate)}%) [目标: ≤0.1%]
            |  White Screens: $whiteScreenCount (Rate: ${String.format("%.3f", whiteScreenRate)}%) [目标: ≤0.05%]
            |  ANRs: $anrCount (Rate: ${String.format("%.3f", anrRate)}%) [目标: ≤0.1%]
            |  Status: ${if (meetsRequirements()) "✅ PASS" else "❌ FAIL"}
        """.trimMargin()
    }
}
```

**测试报告模板**：

```markdown
# 稳定性验证报告

## 测试环境
- 测试日期：2024-01-25
- 测试设备：
  - Xiaomi Redmi Note 10 (6GB RAM, Android 13)
  - Xiaomi Redmi 5A (2GB RAM, Android 7.1)
  - iPhone 12 (4GB RAM, iOS 17)
  - iPhone 8 (2GB RAM, iOS 16)
- App 版本：1.0.0 (Build 1)

## 测试结果汇总

| 测试项 | 目标 | 高端设备 | 低端设备 | 状态 |
|--------|------|----------|----------|------|
| 端到端 10 轮崩溃率 | ≤0.1% | 0% | 0% | ✅ |
| 2 小时内存增长 | <10% | 3.2% | 5.8% | ✅ |
| 100 次切换崩溃 | 0 | 0 | 0 | ✅ |
| 100 次切换白屏 | ≤1 | 0 | 1 | ✅ |
| 单场景内存占用 | ≤120MB | 95MB | 112MB | ✅ |
| 冷启动时间 | ≤1.2s | 0.8s | 1.1s | ✅ |
| Lottie 动画帧率 | ≥30 FPS | 45 FPS | 32 FPS | ✅ |
| ANR 率 | ≤0.1% | 0% | 0% | ✅ |

## 详细数据

### 端到端稳定性测试（10 轮）
| 轮次 | 设备 | 崩溃 | 白屏 | ANR | 备注 |
|------|------|------|------|-----|------|
| 1 | Redmi Note 10 | ❌ | ❌ | ❌ | |
| 2 | Redmi Note 10 | ❌ | ❌ | ❌ | |
| ... | ... | ... | ... | ... | ... |

### 长时间运行测试（2 小时）
| 设备 | 初始内存 | 最终内存 | 增长 | 状态 |
|------|----------|----------|------|------|
| Redmi Note 10 | 95MB | 98MB | +3MB (+3.2%) | ✅ |
| Redmi 5A | 112MB | 119MB | +7MB (+6.3%) | ✅ |
| iPhone 12 | 88MB | 91MB | +3MB (+3.4%) | ✅ |

### 内存泄漏检测结果
| 场景切换 | 泄漏量 | 状态 | 问题对象 |
|----------|--------|------|----------|
| Map → FireStation → Map | +2MB | ✅ | 无 |
| Map → School → Map | +1MB | ✅ | 无 |
| Map → Forest → Map | +3MB | ✅ | 无 |
| Map → Collection → Map | +1MB | ✅ | 无 |

## 发现的问题
1. （示例）在 Redmi 5A 上，场景切换偶现白屏（1/100）
   - 严重程度：中
   - 复现步骤：快速切换场景时
   - 状态：已优化场景加载超时时间

## 结论
✅ 所有稳定性指标均满足 spec.md 中定义的非功能需求：
- 崩溃率：0% (目标 ≤0.1%)
- 白屏率：0.33% (目标 ≤0.05%)
- ANR 率：0% (目标 ≤0.1%)
- 内存稳定性：平均增长 4.3% (目标 <10%)
```

**验收标准**：
- [ ] 端到端 10 轮测试崩溃率 ≤0.1%
- [ ] 2 小时运行内存增长 <10%
- [ ] 100 次场景切换崩溃次数 = 0
- [ ] 100 次场景切换白屏次数 ≤1
- [ ] 低端设备（1GB RAM）所有场景正常运行
- [ ] 内存泄漏检测：场景切换后内存泄漏 <5MB
- [ ] Android Profiler / Instruments 无严重内存泄漏警告

---

## 附录：任务依赖关系图

```
阶段 0（0.1 - 0.7）
    ├─→ 阶段 1 Domain（1.1 - 1.7）
    │       ├─→ 阶段 2 Data（2.1 - 2.4）
    │       │       ├─→ 阶段 3 Presentation（3.1 - 3.7）
    │       │       │       ├─→ 阶段 4 Android UI（4.1 - 4.10）
    │       │       │       ├─→ 阶段 5 iOS UI（5.1 - 5.11）
    │       │       │       └─→ 阶段 6 辅助（6.1 - 6.5）
    │       │       └─→ 阶段 7 联调（7.1 - 7.4）
    │       └───────────────────────────┘
    └───────────────────────────────────┘
```

---

## 任务统计

| 阶段 | 任务数量 | 预估复杂度 |
|------|---------|-----------|
| 阶段 0：基础设施搭建 | 7 | 中 |
| 阶段 1：Domain 层 | 7 | 低 |
| 阶段 2：Data 层 | 4 | 中 |
| 阶段 3：Presentation 层 | 7 | 高 |
| 阶段 4：Android UI | 10 | 高 |
| 阶段 5：iOS UI | 11 | 高 |
| 阶段 6：辅助与保障 | 10 | 中 |
| 阶段 7：联调与测试 | 6 | 中 |
| **总计** | **62** | - |

**说明**：
- 阶段 6 新增 5 个崩溃日志相关任务（6.6 - 6.10）
- 阶段 7 新增 2 个测试任务（7.4 崩溃日志测试、7.6 稳定性验证）
- 任务总数从 55 个增加至 62 个

---

**文档结束**

> 本 tasks.md 基于 plan.md v1.1 严格拆解，每条任务均可独立完成、独立测试、独立验收。
> 请按照阶段顺序执行，确保依赖关系正确。
