# plan.md：老虎消防车 App 技术实现方案

---

## 文档信息

| 属性 | 值 |
|------|-----|
| **文档版本** | v1.1 |
| **创建日期** | 2026-01-19 |
| **更新日期** | 2026-01-21 |
| **更新内容** | 完善启动页流程，增加语音播放、全屏点击、首次使用初始化逻辑
| **适用范围** | 完整 App 功能实现 |
| **技术栈** | Kotlin Multiplatform Mobile (KMM) |
| **架构模式** | Clean Architecture + MVVM |

---

## 1. 实现目标与范围说明

### 1.1 本方案覆盖的功能范围

本技术方案涵盖以下核心功能模块的完整实现：

1. **启动页场景**：Lottie 动画播放 + 全屏点击进入
2. **主地图场景**：场景图标状态管理、解锁逻辑、转场动画
3. **消防站场景**：4 个教学设备点击交互 + MP4 视频播放 + 徽章获得
4. **学校场景**：MP4 剧情动画播放 + 场景解锁 + 徽章获得
5. **森林场景**：手势拖拽交互 + 小羊救援 + 徽章获得
6. **我的收藏**：徽章展示系统 + 变体管理 + 隐藏彩蛋
7. **家长模式**：时间控制、使用统计、进度管理
8. **全局系统**：语音引导、音效系统、进度持久化

### 1.2 不包含的内容（明确边界）

以下内容**不在本方案范围内**：

- 网络功能（App 完全离线运行）
- 多语言国际化（首版仅支持中文）
- 无障碍功能增强（遵循系统默认）
- 社交分享功能
- 内购或广告系统
- 后台数据分析或埋点

---

## 2. 模块与职责拆分

### 2.1 总体模块架构

```
TigerFire/
├── shared/                          # Kotlin Multiplatform 共享模块
│   ├── domain/                     # 领域层（业务规则）
│   │   ├── model/                  # 领域模型
│   │   │   ├── SceneType.kt
│   │   │   ├── SceneStatus.kt
│   │   │   ├── Badge.kt
│   │   │   ├── GameProgress.kt
│   │   │   └── ParentSettings.kt
│   │   ├── usecase/                # 用例层
│   │   │   ├── UnlockSceneUseCase.kt
│   │   │   ├── AwardBadgeUseCase.kt
│   │   │   ├── CheckTimeLimitUseCase.kt
│   │   │   └── ResetProgressUseCase.kt
│   │   └── repository/             # 仓储接口
│   │       ├── ProgressRepository.kt
│   │       └── SettingsRepository.kt
│   ├── data/                       # 数据层
│   │   ├── repository/             # 仓储实现
│   │   ├── local/                  # 本地存储
│   │   └── resource/               # 资源路径管理
│   └── presentation/               # 表现层（ViewModel）
│       ├── welcome/
│       ├── map/
│       ├── firestation/
│       ├── school/
│       ├── forest/
│       ├── collection/
│       └── parent/
├── androidApp/                      # Android 平台层
│   └── src/main/java/com.tigertruck/
│       ├── ui/                     # Jetpack Compose UI
│       │   ├── welcome/
│       │   ├── map/
│       │   ├── firestation/
│       │   ├── school/
│       │   ├── forest/
│       │   ├── collection/
│       │   └── parent/
│       ├── component/              # 可复用组件
│       │   ├── LottiePlayer.kt
│       │   ├── VideoPlayer.kt
│       │   └── AudioManager.kt
│       └── navigation/
│           └── AppNavigation.kt
└── iosApp/                         # iOS 平台层
    ├── UI/                         # SwiftUI Views
    │   ├── WelcomeView/
    │   ├── MapView/
    │   ├── FireStationView/
    │   ├── SchoolView/
    │   ├── ForestView/
    │   ├── CollectionView/
    │   └── ParentView/
    ├── Component/                  # 可复用组件
    │   ├── LottieView.swift
    │   ├── VideoPlayerView.swift
    │   └── AudioManager.swift
    └── Navigation/
        └── AppCoordinator.swift
```

### 2.2 各层职责边界

#### 2.2.1 Shared 模块职责

**Domain 层**：
- 定义所有业务规则：场景解锁条件、徽章获得规则、时间控制逻辑
- 保持完全平台无关，不引用任何 Android/iOS API
- 所有模型为不可变数据类（`data class`）

**Data 层**：
- 实现进度持久化（使用 SQLDelight）
- 提供资源路径抽象接口（具体实现由平台层提供）
- 管理数据访问逻辑

**Presentation 层**：
- 提供 ViewModel 接口定义
- 实现核心状态管理逻辑（State / Event / Effect）
- 暴露 `StateFlow` 供 UI 订阅

#### 2.2.2 AndroidApp 职责

- 使用 Jetpack Compose 实现 UI 渲染
- 实现 Lottie 动画播放（`lottie-compose` 库）
- 实现 MP4 视频播放（`ExoPlayer` 或 `MediaPlayer`）
- 实现音频播放与管理
- 提供资源路径实现（`ResourcePathProvider`）
- 处理 Android 生命周期事件

#### 2.2.3 iOSApp 职责

- 使用 SwiftUI 实现 UI 渲染
- 实现 Lottie 动画播放（`Lottie-iOS` 库）
- 实现 MP4 视频播放（`AVPlayer`）
- 实现音频播放与管理
- 提供资源路径实现
- 处理 iOS 生命周期事件

### 2.3 Feature 模块划分

为确保模块独立性与职责清晰，建议在 `shared/presentation/` 下按场景拆分：

| Feature 模块 | 职责 | 输入 | 输出 |
|-------------|------|------|------|
| **welcome** | 启动页动画播放、全屏点击检测 | 无 | 导航至主地图 |
| **map** | 场景图标状态管理、解锁判定、转场触发 | GameProgress | 导航至具体场景 |
| **firestation** | 设备点击状态、视频播放控制、徽章颁发 | GameProgress | 更新 Progress + Badge |
| **school** | 剧情动画播放、场景解锁触发 | GameProgress | 解锁森林 + Badge |
| **forest** | 手势拖拽控制、救援判定、徽章颁发 | GameProgress | 更新 Progress + Badge |
| **collection** | 徽章展示、变体管理、彩蛋解锁 | Badge List | 展示 + 彩蛋触发 |
| **parent** | 时间控制、统计展示、进度重置 | Settings + Progress | 更新设置 / 重置进度 |

**模块依赖关系**：
- 所有 feature 模块依赖 `domain` 层
- Feature 之间**不可直接相互依赖**
- 通过 `domain/usecase` 协调跨场景逻辑

---

## 3. 核心状态与数据模型设计（Shared）

### 3.1 关键领域模型

#### 3.1.1 场景类型与状态

```kotlin
// SceneType.kt
enum class SceneType {
    FIRE_STATION,  // 消防站
    SCHOOL,        // 学校
    FOREST         // 森林
}

// SceneStatus.kt
enum class SceneStatus {
    LOCKED,        // 锁定态（灰色 + 锁图标）
    UNLOCKED,      // 已解锁（彩色 + 光效）
    COMPLETED      // 已完成（彩色 + 勾图标）
}
```

#### 3.1.2 徽章模型

```kotlin
// Badge.kt
data class Badge(
    val id: String,              // 唯一 ID（如 "firestation_extinguisher_red"）
    val scene: SceneType,        // 所属场景
    val baseType: String,        // 基础类型（如 "extinguisher"）
    val variant: Int = 0,        // 变体编号（0=默认, 1=红色, 2=黄色...）
    val earnedAt: Long           // 获得时间戳
)

// BadgeVariant.kt
data class BadgeVariant(
    val variantId: Int,
    val colorName: String,       // 如 "red", "yellow", "blue"
    val iconPath: String         // 资源路径
)
```

**徽章变体规则说明**：
- 每个基础类型初次获得时为变体 0（默认）
- 重复通关同一项目时，按顺序获得变体 1, 2, 3...
- 变体总数由资源文件数量决定（消防站 4 色，学校 3 色，森林 2 色）

#### 3.1.3 游戏进度模型

```kotlin
// GameProgress.kt
data class GameProgress(
    val sceneStatuses: Map<SceneType, SceneStatus> = mapOf(
        SceneType.FIRE_STATION to SceneStatus.UNLOCKED,
        SceneType.SCHOOL to SceneStatus.LOCKED,
        SceneType.FOREST to SceneStatus.LOCKED
    ),
    val badges: List<Badge> = emptyList(),
    val totalPlayTime: Long = 0L,           // 累计游玩时长（毫秒）
    val fireStationCompletedItems: Set<String> = emptySet(), // 已学设备集合
    val forestRescuedSheep: Int = 0         // 已救小羊数量
)
```

#### 3.1.4 家长设置模型

```kotlin
// ParentSettings.kt
data class ParentSettings(
    val sessionDurationMinutes: Int = 15,   // 单次使用时长（分钟）
    val reminderMinutesBefore: Int = 2,     // 提前提醒时间
    val dailyUsageStats: Map<String, Long> = emptyMap() // 日期 -> 时长（毫秒）
)
```

### 3.2 状态流转规则

#### 3.2.1 场景解锁流转图

```
[启动] → FIRE_STATION: UNLOCKED
           |
           | 完成 4 个设备学习
           ↓
       SCHOOL: UNLOCKED
           |
           | 观看完剧情动画
           ↓
       FOREST: UNLOCKED
```

**判定逻辑**：
- 消防站完成条件：`fireStationCompletedItems.size == 4`
- 学校完成条件：视频播放至结束（通过回调事件触发）
- 森林完成条件：`forestRescuedSheep == 2`

#### 3.2.2 徽章获得触发点

| 场景 | 触发时机 | 徽章数量 |
|------|---------|---------|
| 消防站 | 每个设备视频播放完毕 | 4 枚（可重复获得变体） |
| 学校 | 剧情动画播放完毕 | 1 枚（可重复获得变体） |
| 森林 | 每只小羊救援成功 | 2 枚（可重复获得变体） |

**变体分配算法**：
```kotlin
fun calculateNextVariant(badges: List<Badge>, baseType: String): Int {
    val existingCount = badges.count { it.baseType == baseType }
    return existingCount % MAX_VARIANTS_PER_TYPE
}
```

### 3.3 与全局状态的关系

**全局状态管理器**（`GameProgressManager`）：
- 单一数据源（Single Source of Truth）
- 暴露 `StateFlow<GameProgress>` 供所有 ViewModel 订阅
- 所有修改通过 UseCase 进行，确保业务规则一致性

**通信方式**：
- UI 层 → ViewModel → UseCase → Repository → 更新 GameProgress
- 状态变更 → StateFlow 发射 → UI 层自动响应

---

## 4. 核心业务流程说明

### 4.1 启动页流程（欢迎场景）

**步骤**：
1. App 启动 → 显示启动页界面（全屏布局，无状态栏）
2. 自动播放 Lottie 动画（`anim_truck_enter.json`，3-5 秒）
   - 消防车从屏幕底部驶入
   - 同时播放鸣笛音效（`sfx_truck_horn.mp3`）
   - 轻快活泼背景音乐（`bgm_welcome.mp3`）开始播放
3. 动画结束 → 小火探头挥手（`anim_xiaohuo_wave.json`）
4. 播放语音（`voice_welcome_greeting.mp3`）："HI！今天和我一起救火吧！"
   - 正常语速，带停顿（适配 3-6 岁儿童理解）
5. 语音播放完毕后，屏幕全域可点击
6. 用户点击屏幕任意位置 → 导航至主地图

**首次使用处理**：
- 首次启动时，自动初始化默认家长设置：
  - `sessionDurationMinutes = 15`（默认 15 分钟）
  - `reminderMinutesBefore = 2`（提前 2 分钟提醒）
- 不显示任何引导提示，直接进入启动页动画流程

**关键触发点**：
- 卡车入场动画完成 → 触发挥手动画 + 语音播放
- 语音播放完成 → 启用全屏点击响应
- 屏幕任意位置点击 → 发送导航事件

**主流启动页设计参考**：
- **品牌展示 + 功能引导结合**：通过小火角色建立 IP 认知
- **极简交互**：无按钮，全屏可点，降低 3-6 岁儿童操作门槛
- **感官多维度反馈**：视觉（Lottie 动画）+ 听觉（音效 + 语音）+ 触觉（点击反馈）
- **节奏控制**：总时长控制在 5-8 秒，避免儿童等待焦虑

### 4.1.1 资源缺失降级方案

**当前状态**：Lottie 动画资源尚未制作完成

**降级实现**（参考主流做法）：

1. **纯色背景 + Logo 方案**（最简化）：
   ```kotlin
   // Android Compose 代码示例
   @Composable
   fun WelcomeScreen(viewModel: WelcomeViewModel) {
       val state by viewModel.state.collectAsState()

       Box(
           modifier = Modifier
               .fillMaxSize()
               .background(Color(0xFFE63946)) // 品牌红色背景
               .clickable(enabled = state.isClickEnabled) {
                   viewModel.onEvent(WelcomeEvent.ScreenClicked)
               }
       ) {
           // 中心显示小火 Logo 或静态图
           Image(
               painter = painterResource(R.drawable.xiaohuo_logo),
               contentDescription = null,
               modifier = Modifier.align(Alignment.Center)
           )

           // 底部显示文字提示
           if (state.isClickEnabled) {
               Text(
                   text = "点击屏幕开始游戏",
                   color = Color.White,
                   fontSize = 24.sp,
                   modifier = Modifier.align(Alignment.BottomCenter)
                       .padding(bottom = 80.dp)
               )
           }
       }
   }
   ```

2. **渐变背景 + 简单动画方案**（无 Lottie）：
   ```kotlin
   // 使用 Compose 内置动画替代 Lottie
   val infiniteTransition = rememberInfiniteTransition(label = "pulse")
   val scale by infiniteTransition.animateFloat(
       initialValue = 1f,
       targetValue = 1.1f,
       animationSpec = infiniteRepeatable(
           animation = tween(1000, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse
       ), label = "scale"
   )

   // Logo 缩放动画模拟"呼吸"效果
   Image(
       painter = painterResource(R.drawable.xiaohuo_logo),
       contentDescription = null,
       modifier = Modifier
           .scale(scale)
           .align(Alignment.Center)
   )
   ```

3. **完整 Lottie 方案**（资源就绪后）：
   - 使用 `lottie-compose` 库
   - 按上述完整流程实现
   - 保留降级方案作为备用

**渐进式实现建议**：
```
阶段 1（当前）：纯色背景 + Logo + 点击进入 → 快速验证导航流程
阶段 2（资源就绪）：添加 Lottie 动画 → 完整启动页体验
阶段 3（优化打磨）：添加音效 + 语音 → 多感官体验
```

### 4.2 消防站学习流程

**步骤**：
1. 从主地图点击消防站图标 → 播放转场动画 → 进入消防站场景
2. 显示 4 个设备图标（灭火器、消防栓、云梯、水枪）
3. 用户点击未学习设备 → 图标缩放动画 + 点击音效
4. 播放对应 MP4 教学视频（15 秒）
5. 视频播放完毕 → 设备图标变为"星星点亮"状态（金色闪烁）
6. 弹出徽章获得动画 + 播放成功音效
7. 记录已完成设备 → 更新 GameProgress
8. 重复 3-7 直至 4 个设备全部完成
9. 全部完成 → 小火欢呼语音 + 解锁学校场景

**关键判定逻辑**：
- 设备可点击条件：`device not in fireStationCompletedItems`
- 视频播放中：禁止其他设备点击（图标变灰）
- 解锁判定：`fireStationCompletedItems.size == 4` 时触发 `UnlockSceneUseCase(SCHOOL)`

### 4.3 森林救援流程

**步骤**：
1. 进入森林场景 → 播放警报音效 + 小火语音提示
2. 显示直升机图标（屏幕中央，超大）
3. 显示 2 只小羊位置（屏幕上方不同位置）
4. 用户单指拖拽直升机 → 直升机慢速跟随手指移动
5. 直升机与小羊距离 ≤80pt → 自动吸附 + 显示"放下梯子"按钮
6. 点击"放下梯子"按钮 → 播放救援 MP4 片段（小羊爬梯子）
7. 视频结束 → 小羊消失 + 弹出徽章 + 成功音效
8. 重复 4-7 直至救出 2 只小羊
9. 全部完成 → 播放庆祝动画 + 小火语音总结

**手势交互细节**：
- 拖拽速度：直升机移动速度限制为手指速度的 60%（增加可控性）
- 吸附判定：每帧检测直升机中心与小羊中心距离，触发阈值 80pt
- 中断处理：拖拽过程中抬手 → 直升机停在当前位置，不回退

### 4.4 徽章收集与彩蛋解锁流程

**徽章展示**：
1. 点击主地图"我的收藏"按钮 → 进入徽章展示页
2. 按场景分组显示已获得徽章（消防站 4 个槽位，学校 1 个，森林 2 个）
3. 未获得徽章显示为灰色轮廓
4. 已获得徽章显示完整图标 + 变体颜色标识

**隐藏彩蛋解锁**：
- 判定条件：`badges.distinctBy { it.baseType }.size == 7`（集齐 7 种基础徽章）
- 触发时机：进入收藏页时检测，满足条件自动播放彩蛋动画
- 彩蛋内容：小火跳舞 + 放烟花（Lottie 动画，20 秒）

### 4.5 家长模式时间控制流程

**时间追踪**：
1. App 进入前台 → 开始计时
2. 每秒更新 `currentSessionElapsedTime`
3. 达到设定时长前 2 分钟 → 弹出小火提醒："还有 2 分钟哦"
4. 达到设定时长 → 暂停所有交互 + 弹出家长验证界面

**家长验证流程**：
1. 显示数学题（如 "5 + 3 = ?"）+ 数字输入框
2. 用户输入答案 → 验证
3. 答对 → 显示"再玩 5 分钟"按钮 + "退出"按钮
4. 答错 → 提示"答案不正确" + 重新出题
5. 取消验证 → App 退出至桌面

**统计记录**：
- 每次 App 退出时，记录当日累计时长至 `dailyUsageStats`
- 图表展示：读取最近 7 天数据，生成柱状图

---

## 5. ViewModel 设计思路

### 5.1 ViewModel 职责边界

ViewModel 负责：
- **状态管理**：维护 UI 所需的完整状态（State）
- **事件处理**：接收 UI 事件（Event），调用 UseCase 执行业务逻辑
- **副作用触发**：通过 Effect 通知 UI 执行一次性操作（如导航、播放音效）
- **状态订阅**：订阅 Domain 层的数据流，转换为 UI 状态

ViewModel **不负责**：
- UI 渲染细节（如颜色、字体、布局）
- 平台特定功能（如动画实现、音频播放细节）
- 直接操作 Repository（必须通过 UseCase）

### 5.2 State / Event / Effect 划分原则

#### 5.2.1 State（状态）

**定义**：UI 当前需要展示的所有数据快照，必须完整且自包含。

**示例**（消防站场景）：
```kotlin
data class FireStationState(
    val completedDevices: Set<String> = emptySet(),
    val currentPlayingVideo: String? = null,
    val isVideoPlaying: Boolean = false,
    val showBadgeAnimation: Boolean = false,
    val isAllCompleted: Boolean = false
)
```

**原则**：
- State 必须为不可变数据类
- 所有字段应有合理默认值
- State 应完整描述 UI，UI 不应维护额外状态

#### 5.2.2 Event（事件）

**定义**：用户或系统触发的动作，表达"发生了什么"。

**示例**（消防站场景）：
```kotlin
sealed class FireStationEvent {
    data class DeviceClicked(val deviceId: String) : FireStationEvent()
    object VideoPlaybackCompleted : FireStationEvent()
    object BadgeAnimationCompleted : FireStationEvent()
    object BackPressed : FireStationEvent()
}
```

**原则**：
- Event 使用 `sealed class` 定义，便于穷举处理
- Event 应携带必要参数（如 `deviceId`）
- Event 命名使用过去式或动作型动词

#### 5.2.3 Effect（副作用）

**定义**：一次性执行的操作，不影响 State 持久状态。

**示例**（消防站场景）：
```kotlin
sealed class FireStationEffect {
    data class PlayVideo(val videoPath: String) : FireStationEffect()
    object PlaySuccessSound : FireStationEffect()
    object NavigateToMap : FireStationEffect()
    data class ShowBadgeReward(val badge: Badge) : FireStationEffect()
}
```

**原则**：
- Effect 通过 `Channel` 或 `SharedFlow` 发送，确保只消费一次
- 典型场景：导航、播放音效、显示 Toast、触发动画
- UI 层订阅 Effect 并执行对应操作后即丢弃

### 5.3 UI 订阅与事件触发模式

**订阅模式**：
```kotlin
// ViewModel
class FireStationViewModel(...) : ViewModel() {
    private val _state = MutableStateFlow(FireStationState())
    val state: StateFlow<FireStationState> = _state.asStateFlow()
    
    private val _effect = Channel<FireStationEffect>()
    val effect: Flow<FireStationEffect> = _effect.receiveAsFlow()
    
    fun onEvent(event: FireStationEvent) {
        when (event) {
            is FireStationEvent.DeviceClicked -> handleDeviceClick(event.deviceId)
            // ...
        }
    }
}

// UI Layer (Compose)
@Composable
fun FireStationScreen(viewModel: FireStationViewModel) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FireStationEffect.PlayVideo -> videoPlayer.play(effect.videoPath)
                // ...
            }
        }
    }
    
    // UI 组件响应 state
    DeviceButton(
        onClick = { viewModel.onEvent(FireStationEvent.DeviceClicked("extinguisher")) }
    )
}
```

**关键点**：
- State 通过 `StateFlow` 暴露，UI 使用 `collectAsState()` 订阅
- Effect 通过 `Channel` 暴露，UI 使用 `LaunchedEffect` 收集
- UI 通过调用 `onEvent()` 向 ViewModel 发送事件

---

## 6. 平台层实现要点

### 6.1 Android（Jetpack Compose）侧关注点

#### 6.1.1 Lottie 动画集成

- 使用 `lottie-compose` 库（版本 6.x+）
- 组件封装：
  ```kotlin
  @Composable
  fun LottieAnimationPlayer(
      animationRes: String,
      onAnimationEnd: () -> Unit
  ) {
      val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationRes))
      val progress by animateLottieCompositionAsState(
          composition = composition,
          iterations = 1
      )
      
      LaunchedEffect(progress) {
          if (progress == 1f) onAnimationEnd()
      }
      
      LottieAnimation(composition = composition, progress = progress)
  }
  ```

#### 6.1.2 MP4 视频播放

- 使用 `ExoPlayer`（推荐）或 `MediaPlayer`
- 关键实现：
  - 视频播放完毕回调 → 发送 `VideoPlaybackCompleted` 事件
  - 切后台处理：暂停播放，恢复时从头开始
  - 资源路径：`file:///android_asset/videos/firestation_extinguisher.mp4`

#### 6.1.3 音频系统

- 使用 `MediaPlayer` 管理音效与语音
- 音效预加载：App 启动时加载常用音效至内存
- 场景差异化音效管理：
  ```kotlin
  object SoundEffectManager {
      private val sceneSounds = mapOf(
          SceneType.FIRE_STATION to R.raw.click_firestation,
          SceneType.SCHOOL to R.raw.click_school,
          SceneType.FOREST to R.raw.click_forest
      )
      
      fun playClickSound(scene: SceneType) { /* ... */ }
  }
  ```

#### 6.1.4 导航管理

- 使用 Jetpack Compose Navigation
- 导航图定义：
  ```kotlin
  NavHost(navController, startDestination = "welcome") {
      composable("welcome") { WelcomeScreen(...) }
      composable("map") { MapScreen(...) }
      composable("firestation") { FireStationScreen(...) }
      // ...
  }
  ```

#### 6.1.5 触控区域优化

- 所有可点击元素使用 `Modifier.size(min = 100.dp)`
- 相邻元素使用 `Modifier.padding(min = 20.dp)` 分隔
- 防重复点击：使用 `Modifier.clickable(enabled = !isProcessing)`

### 6.2 iOS（SwiftUI）侧关注点

#### 6.2.1 Lottie 动画集成

- 使用 `Lottie-iOS` 库
- SwiftUI 包装器：
  ```swift
  struct LottieView: UIViewRepresentable {
      let animationName: String
      let onAnimationEnd: () -> Void
      
      func makeUIView(context: Context) -> LottieAnimationView {
          let animationView = LottieAnimationView(name: animationName)
          animationView.play { finished in
              if finished { onAnimationEnd() }
          }
          return animationView
      }
  }
  ```

#### 6.2.2 MP4 视频播放

- 使用 `AVPlayer` + `VideoPlayer` (SwiftUI)
- 关键实现：
  - 监听播放完成通知：`AVPlayerItemDidPlayToEndTime`
  - 资源路径：`Bundle.main.path(forResource: "firestation_extinguisher", ofType: "mp4")`

#### 6.2.3 状态桥接

- 使用 `ObservableObject` 包装 Shared ViewModel：
  ```swift
  class FireStationViewModelWrapper: ObservableObject {
      private let viewModel: FireStationViewModel
      @Published var state: FireStationState
      
      init(viewModel: FireStationViewModel) {
          self.viewModel = viewModel
          self.state = viewModel.state.value
          
          viewModel.state.collect { [weak self] newState in
              self?.state = newState
          }
      }
  }
  ```

#### 6.2.4 触控区域优化

- 使用 `.frame(minWidth: 100, minHeight: 100)` 确保点击区域
- 防疯狂点击：使用 `@State private var isProcessing = false` 控制

### 6.3 动画与 Shared 状态协作方式

**协作模式**：
1. Shared ViewModel 维护逻辑状态（如 `isVideoPlaying`）
2. 平台层根据状态控制动画播放
3. 动画完成回调 → 发送事件至 ViewModel → 更新状态

**示例流程**（消防站设备点击）：
```
用户点击设备
   ↓
UI 发送 DeviceClicked 事件
   ↓
ViewModel 处理：
   - 更新 state.currentPlayingVideo
   - 发送 PlayVideo Effect
   ↓
UI 收到 Effect → 播放视频
   ↓
视频播放完毕 → UI 发送 VideoPlaybackCompleted 事件
   ↓
ViewModel 处理：
   - 调用 AwardBadgeUseCase
   - 更新 state.completedDevices
   - 发送 ShowBadgeReward Effect
   ↓
UI 收到 Effect → 播放徽章动画
```

---

## 7. 异常与边界场景处理

### 7.1 快速点击防护

**问题**：3-6 岁儿童可能疯狂点击同一元素。

**解决方案**：
- 点击触发后立即禁用元素（`enabled = false`）
- 操作完成后恢复（如视频播放结束、动画结束）
- 连续 3 次快速点击（间隔 <500ms）→ 触发小火语音提示："慢慢来，不着急~"

**实现**：
```kotlin
// ViewModel
private var lastClickTime = 0L
private var rapidClickCount = 0

fun onDeviceClick(deviceId: String) {
    val now = System.currentTimeMillis()
    if (now - lastClickTime < 500) {
        rapidClickCount++
        if (rapidClickCount >= 3) {
            _effect.send(Effect.PlaySlowDownVoice)
            rapidClickCount = 0
        }
    } else {
        rapidClickCount = 0
    }
    lastClickTime = now
    // 正常处理点击
}
```

### 7.2 动画/视频中断处理

**场景 1：用户切后台**
- Lottie 动画：暂停动画，恢复时从暂停位置继续
- MP4 视频：暂停播放，恢复时**从头播放**（确保知识完整接收）

**场景 2：用户中途点击返回**
- 弹出确认对话框："确定要离开吗？进度会保存哦"
- 确认离开 → 保存当前已完成项，导航回主地图
- 取消 → 继续停留

**实现**：
```kotlin
// Android - 视频播放器生命周期
LaunchedEffect(lifecycleState) {
    when (lifecycleState) {
        Lifecycle.State.PAUSED -> videoPlayer.pause()
        Lifecycle.State.RESUMED -> videoPlayer.seekTo(0).play() // 从头播放
    }
}
```

### 7.3 资源加载失败降级策略

**问题**：Lottie 文件损坏、MP4 文件缺失等。

**降级方案**：

| 资源类型 | 失败表现 | 降级方案 |
|---------|---------|---------|
| Lottie 动画 | 加载超时（>3 秒）或解析失败 | 显示静态替代图 + 自动播放语音描述 |
| MP4 视频 | 播放器初始化失败 | 显示静态教学图 + 播放语音讲解（预录） |
| 音频文件 | 加载失败 | 静默跳过，不中断流程 |

**实现示例**：
```kotlin
fun loadLottieAnimation(path: String): LottieComposition? {
    return try {
        withTimeout(3000) {
            LottieCompositionFactory.fromAsset(context, path).await()
        }
    } catch (e: Exception) {
        Log.e("Lottie", "Failed to load $path", e)
        null // UI 层检测到 null 后显示降级 UI
    }
}
```

### 7.4 无操作超时提示

**逻辑**：
- 进入任意场景后，启动 30 秒计时器
- 30 秒内无任何点击/拖拽操作 → 触发提示
- 提示内容：小火弹出语音 + 可点击元素轻轻晃动动画
- 任何操作触发后重置计时器

**实现**：
```kotlin
// ViewModel
private var idleTimer: Job? = null

fun onUserInteraction() {
    idleTimer?.cancel()
    idleTimer = viewModelScope.launch {
        delay(30_000)
        _effect.send(Effect.ShowIdleHint)
    }
}

fun onEvent(event: SceneEvent) {
    onUserInteraction()
    // 处理事件...
}
```

### 7.5 进度恢复与数据一致性

**问题**：App 崩溃或被系统杀死后，用户重新打开。

**解决方案**：
- 所有关键进度（场景解锁、徽章获得）实时写入 SQLDelight 本地数据库
- App 启动时从数据库读取 `GameProgress`，恢复状态
- 使用事务确保数据一致性（如颁发徽章 + 更新场景状态必须同时成功）

**数据库表设计**：
```sql
-- progress.sq
CREATE TABLE GameProgress (
    id INTEGER PRIMARY KEY DEFAULT 1,
    fireStationStatus TEXT NOT NULL,
    schoolStatus TEXT NOT NULL,
    forestStatus TEXT NOT NULL,
    fireStationCompletedItems TEXT NOT NULL, -- JSON 数组
    forestRescuedSheep INTEGER NOT NULL,
    totalPlayTime INTEGER NOT NULL
);

CREATE TABLE Badge (
    id TEXT PRIMARY KEY,
    scene TEXT NOT NULL,
    baseType TEXT NOT NULL,
    variant INTEGER NOT NULL,
    earnedAt INTEGER NOT NULL
);
```

---

## 8. 技术风险与约束说明

### 8.1 潜在风险点

| 风险项 | 描述 | 影响 | 缓解措施 |
|-------|------|------|---------|
| **Lottie 性能** | 复杂动画在低端设备可能掉帧 | 用户体验下降 | 1. 简化动画层级 <br>2. 限制关键帧数量 <br>3. 低端设备检测 + 降级 |
| **视频包体积** | 7 个 MP4 文件可能超 200MB | 安装包超标 | 1. 使用 H.264 编码 + 中等码率 <br>2. 分辨率限制为 720p <br>3. 按需下载（暂不支持） |
| **音视频同步** | 视频播放与状态更新可能不同步 | 徽章过早/过晚弹出 | 严格使用播放完成回调 + 状态机管理 |
| **时间控制绕过** | 儿童可能通过系统设置绕过时间限制 | 家长管控失效 | 仅依赖 App 内计时，不与系统时间绑定；文档说明限制 |

### 8.2 明确不在本阶段解决的问题

以下问题**不在当前实现范围**，可在后续迭代中考虑：

1. **多语言国际化**：首版仅支持中文，UI 文本硬编码可接受
2. **云端进度同步**：完全离线运行，无跨设备进度同步
3. **自定义徽章设计**：徽章样式固定，不支持用户自定义
4. **高级手势支持**：仅支持单指点击/拖拽，不支持多指或复杂手势
5. **直升机拖拽物理仿真**：森林场景拖拽采用简化"慢速跟随"逻辑，
   不实现惯性、碰撞检测、物理反弹等效果（符合 spec.md"极度简化操作"原则）。
   **实现方式**：直升机位置 = 手指位置 × 速度衰减系数（约 0.6），
   确保低龄儿童操作可控性
6. **动态内容更新**：所有资源内置，不支持在线下载新场景
7. **无障碍增强**：依赖系统默认无障碍支持，不做额外优化
8. **性能监控与崩溃上报**：无网络权限，不集成第三方分析工具

### 8.3 关键技术约束

**必须遵守**：
- 安装包体积 ≤300 MB（含所有资源）
- 冷启动时间 ≤1.2 秒（中端设备）
- 单场景内存占用 ≤120 MB
- 零网络请求（完全离线）
- 不访问任何敏感权限（位置、通讯录、相册等）

**推荐约束**：
- 视频时长单个 ≤45 秒（学校场景）
- Lottie 动画文件 ≤500 KB/个
- 音频文件总大小 ≤20 MB

---

## 9. 后续工作指引

### 9.1 基于本方案的下一步行动

1. **UI/UE 设计**：
   - 设计师根据本方案创建高保真原型
   - 确定 Lottie 动画关键帧与时长
   - 产出完整 Design Spec

2. **资源制作**：
   - 外部动画工作室根据视频脚本制作 7 个 MP4 文件
   - UI 设计师产出 Lottie 动画源文件
   - 录制小火语音音频（约 20 段）

3. **任务拆解**（tasks.md）：
   - 按 feature 模块拆解具体开发任务
   - 制定 Milestone 与优先级
   - 分配 Android / iOS 并行开发任务

4. **技术验证**：
   - 搭建 KMM 项目骨架
   - 验证 Lottie / ExoPlayer / AVPlayer 集成
   - 验证 SQLDelight 数据持久化

### 9.2 关键里程碑建议

| Milestone | 交付内容 | 验收标准 |
|-----------|---------|---------|
| M1：架构搭建 | KMM 项目 + Domain 模型 + 导航框架 | 编译通过 + 空页面导航正常 |
| M2：核心流程 | 启动页 + 主地图 + 消防站完整流程 | 可完成一次完整学习 + 徽章获得 |
| M3：完整场景 | 学校 + 森林场景实现 | 3 个场景可连贯游玩 |
| M4：收集系统 | 我的收藏 + 徽章变体 + 彩蛋 | 徽章展示正常 + 彩蛋可解锁 |
| M5：家长功能 | 时间控制 + 统计 + 进度重置 | 家长模式完整可用 |
| M6：打磨优化 | 音效 + 边界处理 + 性能优化 | 通过验收标准测试 |

---

## 附录：关键设计决策记录

### A1. 为什么选择 Clean Architecture？

**理由**：
- 业务逻辑与 UI 完全分离，便于 Android / iOS 复用
- 依赖倒置原则确保 Domain 层稳定，不受平台变化影响
- 测试友好，可对 UseCase 进行单元测试

### A2. 为什么使用 StateFlow 而非 LiveData？

**理由**：
- StateFlow 是 Kotlin 标准库，跨平台支持
- LiveData 仅限 Android，不适用于 KMM
- Flow 生态更完善（操作符丰富）

### A3. 为什么视频切后台后从头播放？

**理由**：
- 3-6 岁儿童注意力分散，中断后可能忘记前文
- 从头播放确保知识完整接收
- 视频时长 ≤45 秒，重复观看负担小

### A4. 为什么徽章系统使用变体而非单一徽章？

**理由**：
- 增加重复游玩动力（收集不同颜色）
- 避免"玩一遍就结束"的问题
- 符合儿童收集心理（类似贴纸册）

### A5. 为什么家长验证使用数学题而非密码？

**理由**：
- 密码容易被儿童观察记住
- 数学题对 3-6 岁儿童有明确认知门槛（大概率不会）
- 简单直观，家长理解成本低

---

**文档结束**

> 本方案已完整覆盖 spec.md 所有核心需求，严格遵循 constitution.md 与 CLAUDE.md 的架构约束与行为规范。可直接用于指导后续 tasks.md 拆解与代码实现，所有设计决策均已说明理由，确保方案稳定、清晰、可维护。
