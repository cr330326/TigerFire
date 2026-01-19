# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

**TigerTruck (老虎消防车)** is an educational mobile application for preschool children (ages 3-6) focused on fire safety education. The app features "Xiao Huo" (Little Fire), a tiger firefighter character, who guides children through interactive learning scenarios.

### Supported Platforms
- **Android**: Jetpack Compose
- **iOS**: SwiftUI (via KMM shared business logic)

### Project Type
Kotlin Multiplatform Mobile (KMM) project maximizing shared logic while maintaining native UI experiences.

---

## Build and Development Commands

### Android Build
```bash
cd Code/TigerFire
./gradlew :composeApp:assembleDebug
```

### iOS Build
Open `Code/TigerFire/iosApp/iosApp` directory in Xcode and build from there.

---

## Repository Structure

```
TigerTruck/
├── Code/TigerFire/           # KMM project (Android Studio)
│   ├── composeApp/           # Shared Kotlin code
│   │   ├── src/commonMain/  # Platform-independent logic
│   │   ├── src/androidMain/ # Android-specific code
│   │   └── src/iosMain/     # iOS-specific code
│   └── iosApp/              # iOS app entry point
├── Design/                  # UI/UX specifications
│   ├── design-guide.md      # Page layouts, dimensions, colors
│   ├── component-specs.md   # UI component specs
│   └── assets/             # Images, videos, Lottie animations
├── Document/                # Project documentation
│   ├── spec.md             # Feature specifications
│   ├── claude.md           # AI guidelines (Chinese)
│   └── constitution.md     # Immutable architectural rules
└── Publish/                # Deployment artifacts
```

---

## Architecture (Immutable Rules)

This project strictly follows **Clean Architecture** with enforceable boundaries:

### Layer Boundaries (Cannot Be Violated)
1. **Domain Layer** (`commonMain`): Business logic, use cases, entities - must remain platform-independent
2. **Data Layer** (`commonMain`): Repositories, data sources, network services (Ktor), database (SQLDelight)
3. **Presentation Layer**: Android (Jetpack Compose) / iOS (SwiftUI) - NO business logic in UI

### Module Structure
```
shared/
├── domain/    # Core business rules
├── data/      # Repository implementations
└── presentation/  # ViewModels where possible
androidApp/    # Android-specific UI
iosApp/       # iOS-specific UI
```

### Platform Constraints
- **Shared module**: No platform-specific APIs (Android/iOS), no UI code, no lifecycle awareness
- **Android**: Jetpack Compose only (no XML layouts)
- **iOS**: SwiftUI with lightweight UI, business logic delegated to shared ViewModels

---

## Core Application Features

### Three Learning Scenarios
1. **Fire Station** (消防站): Interactive device learning - click 4 devices to watch educational MP4 videos
2. **School** (学校): Animated narrative - auto-play 45-second emergency response animation
3. **Forest** (森林): Gesture-based rescue - drag helicopter to save sheep

### Key Systems
- **Badge Collection**: 7 badges total (4 from Fire Station, 1 from School, 2 from Forest) with variant rewards
- **Scene Unlocking**: Progressive unlock (Fire Station → School → Forest)
- **Parental Controls**: Time management with math verification, usage statistics, progress reset
- **Voice Guidance**: Xiao Huo character provides all guidance via voice (normal speed with pauses)

---

## Technology Stack

### Core Technologies
- **Kotlin Multiplatform Mobile**: Shared business logic
- **Kotlin Coroutines + Flow**: Async operations
- **Jetpack Compose** (Android UI)
- **SwiftUI** (iOS UI)
- **Lottie**: UI animations (transitions, micro-interactions)
- **MP4**: Educational videos (preloaded assets)

### Shared State Model
```kotlin
enum class SceneStatus { LOCKED, UNLOCKED, COMPLETED }

data class Badge(
    val id: String,
    val scene: SceneType,
    val variant: Int = 0  // For color variants
)

data class GameProgress(
    val fireStation: SceneStatus,
    val school: SceneStatus,
    val forest: SceneStatus,
    val badges: List<Badge>,
    val totalPlayTime: Long
)
```

---

## Development Workflow

### Specs-Driven Development
AI must follow this order:
1. **spec.md**: Understand requirements
2. **plan.md**: Plan implementation approach (if needed)
3. **tasks.md**: Break down complex features
4. **Implementation**: Code following architectural boundaries

### Conflict Resolution Priority
1. `Document/constitution.md` (highest authority)
2. `Document/claude.md`
3. `Document/spec.md`
4. User instructions (lowest)

### Coding Constraints
- Shared module: Platform-independent code only
- UI layer: No business logic
- Kotlin: Prefer immutable structures, explicit types
- Naming: Domain-driven, avoid UI semantics in shared code

---

## Key Constraints and Forbidden Actions

### AI Must NOT
- Violate Clean Architecture boundaries
- Introduce new dependencies without justification (benefits vs existing alternatives)
- Modify public APIs without migration plans
- Refactor code unrelated to current requirements
- Make premature optimizations

### AI Must
- Maintain platform independence in shared module
- Use explicit types in public APIs
- Avoid magic numbers and strings
- Ask clarifying questions when specs are unclear

---

## Design Specifications

### Child-Friendly UI Constraints
- **Touch targets**: ≥100pt (≥120pt for main icons)
- **Spacing**: ≥40pt between adjacent elements
- **Text size**: ≥24pt for children's content
- **Single-touch only**: Ignore multi-touch gestures

### Performance Requirements
- Cold start: ≤1.2 seconds
- Lottie animation: ≥30 FPS
- Single scene memory: ≤120 MB
- App size: ≤300 MB

### Color Palette
- Primary Red: `#E63946` (Fire Station, alerts)
- Primary Blue: `#457B9D` (School, water)
- Primary Green: `#2A9D8F` (Forest, safety)
- Accent Yellow: `#F4A261` (Stars, badges)

---

## Important Documentation Files

| File | Purpose |
|------|---------|
| `Document/spec.md` | Complete feature specifications with interaction rules |
| `Document/constitution.md` | Project constitution with immutable architectural rules |
| `Document/claude.md` | AI assistant guidelines (Chinese) |
| `Design/design-guide.md` | Page layouts, animations, interaction flows |
| `Code/TigerFire/README.md` | KMM project setup overview |

---

## Resource Management

### Asset Organization
- Lottie JSON files: By scene (`/welcome`, `/firestation`, `/school`, `/forest`)
- MP4 videos: Preloaded in `assets/videos/`
- Shared layer provides `ResourcePathProvider` interface for platform-specific path mapping

### Video Content
- 4 equipment videos (15 sec each) for Fire Station
- 1 narrative animation (45 sec) for School
- 2 rescue clips (10 sec each) for Forest
- 1 celebration animation (20 sec) for badge collection milestone

---

## Testing Considerations

### Target User Testing
- Usability: Can 3-4 year olds complete tasks without guidance?
- Can 5 year olds complete forest rescue independently?
- Compatibility: Verify on low-end Android (1GB RAM) and iPhone 8

### Regression Testing
- Scene unlock logic synchronization
- MP4 resource replacement handling
- Badge collection variant logic

---

## Communication Style

- Output: Structured, clear, concise
- Lists preferred over long paragraphs
- Explain "why" when proposing solutions
- Default language: Chinese (as per original docs)
- Code comments: Chinese or English
