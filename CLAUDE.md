# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

**TigerFire (老虎消防车)** — an offline educational mobile app for preschool children (ages 3-6) about fire safety, hosted by "小火" (Xiao Huo / Little Fire), a tiger firefighter character.

Kotlin Multiplatform Mobile project. Domain / Data / Presentation layers are fully shared; UI is native per platform (Jetpack Compose on Android, SwiftUI on iOS).

**Status: feature-complete on both platforms.** A signed Android release APK exists at `release/composeApp-release_v1.0.0.apk` (43 MB). Treat this as a maintenance codebase, not a greenfield one — prefer additive changes over rewrites (see `constitution.md`).

---

## Toolchain (pinned in `gradle/libs.versions.toml`)

| Item | Version |
|------|---------|
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.10.0 |
| AGP | 8.11.2 |
| Gradle wrapper | 8.14.3 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 |
| JVM target (bytecode) | Java 11 (JDK 17+ required to *run* Gradle) |
| SQLDelight | 2.1.0 |
| kotlinx.coroutines / serialization | 1.10.1 / 1.8.0 |
| Lottie Compose (Android) | 6.6.2 |
| Media3 / ExoPlayer | 1.5.0 |
| AndroidX Navigation Compose | 2.8.5 |
| LeakCanary (debug only) | 2.13 |
| lottie-ios (SPM) | 4.6.0 |
| iOS deployment target | **18.2** |
| KMM iOS targets | `iosArm64`, `iosSimulatorArm64` (**no** `iosX64`) |

The iOS framework is `ComposeApp` (static, `isStatic = true`).

---

## Build and Development Commands

### Android

```bash
./gradlew :composeApp:assembleDebug
```

```bash
./gradlew :composeApp:installDebug
```

```bash
./gradlew :composeApp:testDebugUnitTest
```

```bash
./gradlew :composeApp:connectedDebugAndroidTest
```

### Android Release

Release builds enable R8 (`isMinifyEnabled`) and resource shrinking (`isShrinkResources`), and read signing config from `keystore.properties` at the repo root (absent → build succeeds but is unsigned).

```bash
./scripts/build_release.sh
```

### iOS

Fill in `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` first.

```bash
open iosApp/iosApp.xcodeproj
```

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16' build
```

---

## Repository Structure

```
TigerFire/
├── composeApp/                       # Shared KMM module AND the Android app module
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/com/cryallen/tigerfire/
│   │   │   │   ├── domain/{model,usecase,repository,utils}/
│   │   │   │   ├── data/{local,repository,resource}/
│   │   │   │   ├── presentation/{welcome,map,firestation,school,forest,
│   │   │   │   │                collection,parent,audio,common}/
│   │   │   │   ├── factory/UseCaseFactory.kt
│   │   │   │   └── ui/theme/KidsTheme.kt
│   │   │   ├── sqldelight/com/cryallen/tigerfire/database/*.sq
│   │   │   └── composeResources/
│   │   ├── androidMain/
│   │   │   ├── kotlin/.../ui/<scene>/         # 3 files per scene, see below
│   │   │   ├── kotlin/.../ui/components/KidsComponents.kt
│   │   │   ├── kotlin/.../ui/debug/           # CrashTestActivity, CrashLogDebugScreen
│   │   │   ├── kotlin/.../navigation/{AppNavigation,Route}.kt
│   │   │   ├── kotlin/.../component/{VideoPlayer,LottieAnimationPlayer,
│   │   │   │                         AndroidAudioManager,HapticManager}.kt
│   │   │   ├── kotlin/.../factory/ViewModelFactory.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── assets/{videos,audio/{music,sound_effects,voices},lottie}/
│   │   ├── iosMain/kotlin/.../{data,domain,presentation,factory}/
│   │   ├── commonTest/                        # 90 unit tests
│   │   └── androidTest/                       # 18 instrumented tests
│   ├── proguard-rules.pro
│   └── build.gradle.kts
├── iosApp/
│   ├── Configuration/Config.xcconfig
│   ├── iosApp/{Navigation,UI,Component,Audio,Resources}/
│   └── iosAppUITests/                         # 11 UI tests
├── specs/{spec,plan,tasks}.md
├── document/                                  # 31 markdown process docs & test reports
├── scripts/                                   # 20 build/test/diagnostic scripts
├── release/                                   # signed APK + real screenshots
├── constitution.md
├── CLAUDE.md
└── README.md
```

---

## Architecture (Immutable Rules)

Clean Architecture with enforceable boundaries. These come from `constitution.md` and **cannot be violated**.

| Layer | Location | Responsibility | Constraints |
|-------|----------|----------------|-------------|
| **Domain** | `commonMain/domain/` | Entities, use cases, repository interfaces | Platform-independent; no dependency on Data/Presentation/UI |
| **Data** | `commonMain/data/` | Repository impls, SQLDelight, resource paths | Depends only on Domain |
| **Presentation** | `commonMain/presentation/` | ViewModels, state, effects | Depends on Domain (+ Data via DI) |
| **UI (Android)** | `androidMain/` | Compose screens | No business logic; delegates to shared ViewModels |
| **UI (iOS)** | `iosApp/` | SwiftUI screens | No business logic; delegates to shared ViewModels |

### MVI-flavoured presentation pattern

Every scene has exactly four files in `presentation/<scene>/`:

- `XxxState.kt` — immutable UI state data class
- `XxxEvent.kt` — user intents (sealed)
- `XxxEffect.kt` — one-shot side effects (sealed)
- `XxxViewModel.kt` — exposes `StateFlow<XxxState>` + `Channel<XxxEffect>`

Scenes: `welcome`, `map`, `firestation`, `school`, `forest`, `collection`, `parent`.

### expect/actual surfaces

When adding platform behaviour, extend one of these rather than leaking platform APIs into `commonMain`:

| expect declaration | commonMain location |
|--------------------|---------------------|
| `ResourcePathProvider` | `data/resource/` |
| `PlatformSqlDriver` | `data/local/` |
| `LogFileManager` | `data/local/` |
| `CrashLogger` | `domain/repository/` |
| `TimeUtils` | `domain/utils/` |
| `PlatformDateTime` | `presentation/common/` |
| `AudioManager` (interface, not expect) | `presentation/audio/` |

---

## Android UI: the dual-track Selector pattern

**This is the single most important local convention.** Each Android scene has three files:

```
androidMain/.../ui/<scene>/
├── XxxScreen.kt            # original implementation
├── XxxScreenOptimized.kt   # optimized visuals/animation implementation
└── XxxScreenSelector.kt    # dispatches on BuildConfig.IS_USE_OPTIMIZED_UI
```

`AppNavigation.kt` registers **only the Selectors**. The switch lives in `composeApp/build.gradle.kts`:

```kotlin
buildConfigField("boolean", "IS_USE_OPTIMIZED_UI", "false")   // currently: original UI
```

Consequences for any UI change:

- Changing a screen usually means changing **both** implementations, or explicitly stating which track you touched.
- Never bypass the Selector by calling `XxxScreen` / `XxxScreenOptimized` directly from navigation.
- New scenes should follow the same three-file shape.
- `./scripts/verify_ui_switch.sh` verifies which track a build actually shipped.

Both tracks are complete for all 7 scenes. (`document/UI_OPTIMIZATION_STATUS.md` describes an earlier, now-resolved state where the optimized files were partial — do not treat it as current.)

---

## Core Domain Facts

### Scenes and unlocking

```
Fire Station (seeded UNLOCKED) → 4 devices learned
  → School (auto-unlock)       → narrative watched
  → Forest (auto-unlock)       → 2 sheep rescued
```

DB seed row: `{"FIRE_STATION":"UNLOCKED","SCHOOL":"LOCKED","FOREST":"LOCKED"}`.

⚠️ `GameProgress.defaultSceneStatuses()` currently returns **all three UNLOCKED** ("test mode", carries a TODO). The DB seed governs a fresh install, so runtime behaviour is progressive — but don't rely on the Kotlin default.

### Fire station devices

`FireStationDevice` (`presentation/firestation/FireStationState.kt`):

| enum | deviceId | display | video |
|------|----------|---------|-------|
| `FIRE_EXTINGUISHER` | `fire_extinguisher` | 灭火器 | `videos/firefighter_cartoon.mp4` |
| `FIRE_HYDRANT` | `fire_hydrant` | 消防栓 | `videos/firehydrant_cartoon.mp4` |
| `LADDER_TRUCK` | `ladder_truck` | 云梯 | `videos/fireladder_truck_cartoon.mp4` |
| `WATER_HOSE` | `water_hose` | 水枪 | `videos/firenozzle_cartoon.mp4` |

### Badges

`TOTAL_UNIQUE_BADGES = 7`. Variant caps are **per baseType and NOT uniform** — `getMaxVariantsForBaseType()` in `domain/model/GameProgressExtensions.kt`:

```kotlin
"fire_hydrant", "ladder_truck", "fire_extinguisher", "water_hose" -> 4
"school"                                                          -> 3
"forest_sheep1", "forest_sheep2"                                  -> 2
else                                                              -> 1
```

Max obtainable badges: 4×4 + 3 + 2×2 = **23**. Variant assignment is `existingCount % maxVariants`.
Completion (`hasCollectedAllBadges()`) counts **distinct baseTypes ≥ 7**, not total badges.

### Parent mode

- Durations: `ParentSettings.AVAILABLE_DURATIONS = listOf(5, 10, 15, 30)`, default 15
- Reminder: 2 minutes before expiry
- Math-question gate for entry and for extending time
- `dailyUsageStats: Map<"yyyy-MM-dd", Long /* millis */>`
- `ResetProgressUseCase` clears all progress and badges

### Child-safety guards (`presentation/common/`)

| Class | Behaviour |
|-------|-----------|
| `RapidClickGuard` | 3 clicks within 500 ms → play `voices/slow_down.mp3` |
| `IdleTimer` | 30 s idle (range 5 s–300 s) → play `voices/hint_idle.mp3` |
| `SessionTimer` | Counts down the parent-configured session, fires the early reminder |
| `AppSessionManager` | Singleton wiring the three together plus usage recording |

---

## Persistence

SQLDelight database `TigerFireDatabase`, generated into `com.cryallen.tigerfire.database`. Schema in `commonMain/sqldelight/com/cryallen/tigerfire/database/`:

| Table | Notes |
|-------|-------|
| `GameProgress.sq` | Single-row (`id = 1`). `sceneStatuses` and `fireStationCompletedItems` are JSON TEXT columns |
| `Badge.sq` | `id` (PK, `baseType_v{variant}`), `scene`, `baseType`, `variant`, `earnedAt`; indexed on `scene` and `baseType` |
| `ParentSettings.sq` | Single-row. `dailyUsageStats` is JSON TEXT |

Changing a `.sq` file changes the on-device schema. Per `constitution.md`, **do not modify data models without a migration plan.**

Diagnostics: `scripts/verify_database.sh`, `scripts/verify_badge_database.sh`, `scripts/monitor_badge_realtime.sh`.

---

## Resources

Android assets are **flat**, not nested per scene:

```
composeApp/src/androidMain/assets/
├── videos/          # 7 MP4s, flat directory
├── audio/
│   ├── music/          fire_engine.mp3
│   ├── sound_effects/  click, success, collect, alert, hint, helicopter, water, truck_horn (.wav)
│   └── voices/         welcome_greeting, school_fire, school_praise, forest_start,
│                       forest_complete, collection_egg, hint_idle, slow_down, time_up (.mp3)
└── lottie/          anim_truck_enter.json, anim_xiaohuo_wave.json
```

iOS mirrors these under `iosApp/iosApp/Resources/{videos,audio,lottie}/`.

Paths are resolved through `ResourcePathProvider`:
- Android → `file:///android_asset/<kind>/<name>.<ext>`
- iOS → `NSBundle.mainBundle.pathForResource("<kind>/<name>", ofType:)`

⚠️ `FireStationViewModel.kt:162` builds `getVideoPath("firestation/${deviceId}.mp4")`, which does not match any real asset — the Android UI layer supplies the actual mapping. Leave it alone unless deliberately fixing it.

Video compression: `scripts/compress_videos.sh`.

---

## Crash Logging

`CrashLogger` is an `expect class`. **The two platforms are not at parity:**

- **Android (complete)** — installs a `Thread.UncaughtExceptionHandler`, initialised in `TigerFireApplication.onCreate()`
- **iOS (stub)** — `initialize()` only prints; Kotlin/Native makes installing `NSSetUncaughtExceptionHandler` impractical, so **no global handler is installed**, and nothing on the iOS side calls `initialize()` today. Manual `logCrash`/`logError` still work.

- Logs: JSON. Android `filesDir/crash_logs/`; iOS `NSTemporaryDirectory()/crash_logs/`
- `LogFileManager.MAX_LOG_FILES = 20`, `MAX_FILE_SIZE = 100 KB`
- Context hooks: `setCurrentScene(...)`, `setLastAction(...)`
- Non-fatal path: `NonFatalError` + `ErrorType` (e.g. `VIDEO_LOAD_FAILED`)
- Debug surfaces: `ui/debug/CrashTestActivity.kt`, `ui/debug/CrashLogDebugScreen.kt`, `scripts/debug_crash.sh`

⚠️ `CrashTestActivity` is declared in the main manifest with `exported="true"` — it ships in release builds. Flag this if release hardening comes up.

---

## Design Tokens (`commonMain/.../ui/theme/KidsTheme.kt`)

Do not hardcode colors or sizes in screens; use these objects.

| Object | Values |
|--------|--------|
| `ThemeGradients` | Per-scene 3–4 stop vertical gradients (FireStation, School, Forest, Map, Collection, Welcome, Parent) |
| `KidsTouchTarget` | Minimum 100dp / Comfortable 120dp / Large 150dp |
| `KidsTextSize` | Tiny 18sp / Small 20sp / Medium 24sp / Large 32sp / Huge 48sp / Mega 64sp |
| `KidsSpacing` | 4 / 8 / 16 / 24 / 32 / 48 dp |
| `KidsShapes` | 12 / 16 / 24 / 32 / 48 dp + Circle |
| `KidsShadows` | 6 / 12 / 20 / 28 dp |
| `AnimationDuration` | Fast 200 / Normal 300 / Medium 500 / Slow 800 ms |
| `AlertConfig` | Red-flash `MaxAlpha = 0.15f`, `FlashPeriod = 3000L` (deliberately gentle) |
| `SemanticColors` | Success/Warning/Error/Info/LockedOverlay + Badge Gold/Silver/Bronze |

Child-friendly constraints: touch targets ≥100dp (≥120dp for primary icons), body text 24sp (18sp floor), single-touch only.

---

## Testing

| Suite | Location | Count |
|-------|----------|-------|
| KMM unit tests | `composeApp/src/commonTest/` | 90 |
| Android instrumented UI tests | `composeApp/src/androidTest/` | 18 |
| iOS UI tests | `iosApp/iosAppUITests/` | 11 |

Android test classes: `AppNavigationTest` (6), `PerformanceTest` (5), `ParentModeTest` (4), `BadgeCollectionTest` (3).
Test libs: Espresso 3.7.0, UI Automator 2.3.0, Compose UI Test 1.8.0.

Domain logic is the well-covered part (`GameProgressTest` 17, `ParentSettingsTest` 15, `RapidClickGuardTest` 14, `BadgeExtensionsTest` 13, `GameProgressExtensionsTest` 11). **New domain logic should come with unit tests in `commonTest`.**

Key scripts in `scripts/`: `e2e_test.sh`, `run_e2e_test_optimized.sh`, `run_ui_tests.sh`, `test_android.sh`, `test_on_device.sh`, `test_badge_fix.sh`, `test_parent_screen.sh`, `test_weekly_chart.sh`, `test_forest_fixes.sh`, `test_back_button.sh`, `verify_ui_switch.sh`, `verify_database.sh`, `verify_badge_database.sh`, `verify_fix.sh`, `monitor_badge_realtime.sh`, `debug_crash.sh`, `build_release.sh`, `compress_videos.sh`, `add_test_usage_data.sh`, `generate_images.py`.

### Regression-sensitive areas

Historically fragile — check these when touching progress or badges (see the fix reports in `document/`):

- Badge duplication / transaction consistency (`BADGE_DUPLICATE_FIX_REPORT.md`, `test_badge_transaction.md`)
- Scene-unlock state synchronisation
- Parent-mode weekly chart including today's data (`WEEKLY_CHART_FIX_REPORT.md`)
- Navigation white-screen on transition (`BUGFIX_WHITSCREEN.md`)

---

## Privacy & Permissions

Fully offline. Android declares only `android.permission.VIBRATE`. No network permission, no analytics, no third-party data collection. Do not introduce network calls or telemetry.

---

## Development Workflow

### Specs-driven order

1. `specs/spec.md` — requirements
2. `specs/plan.md` — technical approach
3. `specs/tasks.md` — task breakdown (8 phases)
4. Implement within architectural boundaries

### Conflict resolution priority

1. `constitution.md` (highest, immutable)
2. `CLAUDE.md` (this file)
3. `specs/*`
4. User instructions

### AI must NOT

- Violate Clean Architecture boundaries
- Introduce dependencies without justifying motivation, benefit, and alternatives
- Modify data models / `.sq` schema without a migration plan
- Refactor code unrelated to the current request
- Delete existing functionality without saying so
- Fabricate APIs or platform behaviour
- Bypass the Screen Selector pattern

### AI must

- Keep `commonMain` platform-independent
- Use explicit types in public APIs
- Use design tokens instead of magic numbers
- Add `commonTest` coverage for new domain logic
- Say so explicitly when uncertain, and ask when specs are ambiguous

---

## Known Issues

| # | Issue | Location |
|---|-------|----------|
| 1 | `defaultSceneStatuses()` in "test mode" — returns all scenes UNLOCKED (TODO in code) | `domain/model/GameProgress.kt` |
| 2 | `CrashTestActivity` exported in the main manifest, ships in release | `androidMain/AndroidManifest.xml` |
| 3 | `getVideoPath("firestation/…")` doesn't match real assets | `presentation/firestation/FireStationViewModel.kt:162` |
| 4 | `document/UI_OPTIMIZATION_STATUS.md` describes a resolved historical state | `document/` |
| 5 | iOS deployment target 18.2 sharply limits installable devices | `iosApp/iosApp.xcodeproj` |
| 6 | `versionCode = 1` / `versionName = "1.0"` not bumped for the shipped release APK | `composeApp/build.gradle.kts` |
| 7 | iOS crash logging installs no global handler and `initialize()` is never called | `iosMain/.../CrashLogger.ios.kt` |
| 8 | `Badge.sq` comments list stale baseTypes (`extinguisher`, `hydrant`, `sheep1`) vs runtime values (`fire_extinguisher`, `fire_hydrant`, `forest_sheep1`) | `commonMain/sqldelight/.../Badge.sq` |

---

## Communication Style

- Structured, clear, concise; lists over long paragraphs
- Include the "why" when proposing a solution
- Language: Chinese (matching project context) or English
- Code comments: Chinese or English, matching the surrounding file
