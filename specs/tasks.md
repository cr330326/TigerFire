# tasks.md：老虎消防车 App 开发任务拆解

---

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档版本** | v1.0 |
| **创建日期** | 2026-01-19 |
| **基于方案** | plan.md v1.0 |
| **遵循规范** | constitution.md > CLAUDE.md > plan.md > spec.md |

---

## 1. 任务拆解原则说明

### 1.1 拆解依据

本 tasks.md 基于 `plan.md v1.0` 中定义的技术方案进行拆解，严格遵循以下原则：

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
- 定义 `WelcomeState`（无额外状态，仅标记动画完成）
- 定义 `WelcomeEvent`：
  - `AnimationCompleted`
  - `ScreenClicked`
- 定义 `WelcomeEffect`：
  - `NavigateToMap`
- 实现 ViewModel：
  - 监听动画完成事件
  - 处理屏幕点击事件
  - 发送导航 Effect
- 验证：ViewModel 可正确响应事件并发送 Effect

**新增文件**：
- `shared/presentation/welcome/WelcomeViewModel.kt`
- `shared/presentation/welcome/WelcomeState.kt`
- `shared/presentation/welcome/WelcomeEvent.kt`
- `shared/presentation/welcome/WelcomeEffect.kt`

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
  - `isVideoPlaying: Boolean`
  - `showBadgeAnimation: Boolean`
  - `isCompleted: Boolean`
- 定义 `SchoolEvent`：
  - `VideoPlaybackCompleted`
  - `BadgeAnimationCompleted`
  - `BackPressed`
- 定义 `SchoolEffect`：
  - `PlayVideo(videoPath: String)`
  - `ShowBadgeReward(badge: Badge)`
  - `UnlockForestScene`
  - `NavigateToMap`
- 实现 ViewModel：
  - 进入场景自动播放视频
  - 视频播放完毕后颁发徽章
  - 自动解锁森林场景
- 验证：ViewModel 可正确管理学校场景流程

**新增文件**：
- `shared/presentation/school/SchoolViewModel.kt`
- `shared/presentation/school/SchoolState.kt`
- `shared/presentation/school/SchoolEvent.kt`
- `shared/presentation/school/SchoolEffect.kt`

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
  - `helicopterPosition: Offset`（仅相对位置，不涉及平台 UI 类型）
  - `rescuedSheepCount: Int`
  - `showLadderButton: Boolean`
  - `currentRescueVideo: String?`
  - `showBadgeAnimation: Boolean`
  - `isAllCompleted: Boolean`
- 定义 `ForestEvent`：
  - `HelicopterDragged(x: Float, y: Float)`
  - `LadderButtonClicked`
  - `RescueVideoCompleted`
  - `BadgeAnimationCompleted`
  - `BackPressed`
- 定义 `ForestEffect`：
  - `ShowLadderButton`
  - `HideLadderButton`
  - `PlayRescueVideo(videoPath: String)`
  - `ShowBadgeReward(badge: Badge)`
  - `PlaySuccessSound`
  - `NavigateToMap`
- 实现 ViewModel：
  - 处理拖拽位置（应用速度衰减系数 0.6）
  - 判定与小羊距离（≤80pt 触发吸附）
  - 救援视频播放完毕后更新小羊计数
- 验证：ViewModel 可正确管理森林救援流程

**新增文件**：
- `shared/presentation/forest/ForestViewModel.kt`
- `shared/presentation/forest/ForestState.kt`
- `shared/presentation/forest/ForestEvent.kt`
- `shared/presentation/forest/ForestEffect.kt`

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
- 使用 `LottieAnimationPlayer` 播放 `anim_truck_enter.json`
- 动画完成后播放 `anim_xiaohuo_wave.json`
- 使用 `Modifier.clickable` 实现全屏点击
- 订阅 `viewModel.effect` 处理导航
- 验证：启动页动画可正常播放，点击可跳转

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/welcome/WelcomeScreen.kt`

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
- 自动播放剧情视频（45 秒）
- 播放警报音效与屏幕红光闪烁
- 视频播放完毕发送 `VideoPlaybackCompleted` 事件
- 显示小火点赞动画（Lottie）
- 订阅 `viewModel.effect` 处理徽章、解锁、导航
- 验证：学校场景可正常播放

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/school/SchoolScreen.kt`

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
- 绘制森林火灾背景
- 绘制 2 只小羊位置
- 使用 `Modifier.pointerInput` 实现拖拽手势
- 根据 `state.helicopterPosition` 更新直升机位置
- 当 `showLadderButton` 为 true 时显示"放下梯子"按钮（≥100pt）
- 点击按钮播放救援视频
- 视频播放完毕发送 `RescueVideoCompleted` 事件
- 订阅 `viewModel.effect` 处理音效与导航
- 验证：森林场景可正常救援

**新增文件**：
- `androidApp/src/main/java/com/tigertruck/ui/forest/ForestScreen.kt`

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
- 使用 `LottieView` 播放 `anim_truck_enter.json`
- 动画完成后播放 `anim_xiaohuo_wave.json`
- 使用 `.contentShape(Rectangle())` + `.onTapGesture` 实现全屏点击
- 创建 `WelcomeViewModelWrapper` 桥接 Shared ViewModel
- 订阅 Effect 处理导航
- 验证：启动页动画可正常播放，点击可跳转

**新增文件**：
- `iosApp/TigerFire/UI/WelcomeView/WelcomeView.swift`

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
- 自动播放剧情视频（45 秒）
- 播放警报音效与屏幕红光闪烁
- 视频播放完毕发送 Event
- 显示小火点赞动画（Lottie）
- 订阅 Effect 处理徽章、解锁、导航
- 验证：学校场景可正常播放

**新增文件**：
- `iosApp/TigerFire/UI/SchoolView/SchoolView.swift`

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
- 绘制森林火灾背景
- 绘制 2 只小羊位置
- 使用 `.gesture(DragGesture())` 实现拖拽手势
- 根据 `state.helicopterPosition` 更新直升机位置
- 当 `showLadderButton` 为 true 时显示"放下梯子"按钮（≥100pt）
- 点击按钮播放救援视频
- 视频播放完毕发送 Event
- 订阅 Effect 处理音效与导航
- 验证：森林场景可正常救援

**新增文件**：
- `iosApp/TigerFire/UI/ForestView/ForestView.swift`

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

### 7.4 性能验证

| 属性 | 说明 |
|------|------|
| **任务编号** | 7.4 |
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
| 阶段 6：辅助与保障 | 5 | 中 |
| 阶段 7：联调与测试 | 4 | 中 |
| **总计** | **55** | - |

---

**文档结束**

> 本 tasks.md 基于 plan.md v1.0 严格拆解，每条任务均可独立完成、独立测试、独立验收。
> 请按照阶段顺序执行，确保依赖关系正确。
