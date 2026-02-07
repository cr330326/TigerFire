# TigerFire Android APK 优化指南

## 📊 当前资源分析

### 资源大小分布
| 资源类型 | 大小 | 占比 |
|---------|------|------|
| 视频文件 | ~107M | ~93% |
| 音频文件 | ~7.6M | ~6% |
| Lottie 动画 | ~24K | <1% |

### 主要视频文件
| 文件 | 大小 | 场景 |
|------|------|------|
| School_Fire_Safety_Knowledge.mp4 | 37M | 学校场景 |
| firefighter_cartoon.mp4 | 14M | 消防站 |
| firehydrant_cartoon.mp4 | 14M | 消防站 |
| fireladder_truck_cartoon.mp4 | 12M | 消防站 |
| rescue_sheep_2.mp4 | 11M | 森林场景 |
| firenozzle_cartoon.mp4 | 11M | 消防站 |
| rescue_sheep_1.mp4 | 8.6M | 森林场景 |

---

## ✅ 已启用的优化

### 1. 代码混淆与压缩
```kotlin
isMinifyEnabled = true       // R8 混淆
isShrinkResources = true     // 资源压缩
```

### 2. ProGuard 优化
- 移除未使用的代码
- 混淆类名和方法名
- 优化字节码

### 3. 资源配置优化
```kotlin
resourceConfigurations += setOf("zh", "zh-rCN")  // 只打包中文资源
```

### 4. 构建优化
- 移除调试日志
- 移除 LeakCanary（仅 Debug）
- 启用增量编译

---

## 🚀 进一步优化方案

### 方案 1: 视频压缩（推荐）

当前视频占用 93% 的 APK 大小，压缩视频是最有效的优化方式。

#### 使用 FFmpeg 压缩视频
```bash
# 安装 FFmpeg (macOS)
brew install ffmpeg

# 压缩学校场景视频 (37M -> ~15M)
ffmpeg -i composeApp/src/androidMain/assets/videos/School_Fire_Safety_Knowledge.mp4 \
  -c:v libx264 -crf 28 -preset medium -c:a aac -b:a 96k \
  composeApp/src/androidMain/assets/videos/School_Fire_Safety_Knowledge_compressed.mp4

# 批量压缩所有视频
for file in composeApp/src/androidMain/assets/videos/*.mp4; do
  ffmpeg -i "$file" \
    -c:v libx264 -crf 28 -preset medium -c:a aac -b:a 96k \
    "${file%.mp4}_compressed.mp4"
done
```

**预期效果**: 107M → ~45M（节省约 60MB）

---

### 方案 2: 音频优化

#### 转换为 AAC 格式
```bash
# 转换 WAV 为 AAC (高质量)
ffmpeg -i composeApp/src/androidMain/assets/audio/sound_effects/alert.wav \
  -c:a aac -b:a 128k \
  composeApp/src/androidMain/assets/audio/sound_effects/alert.aac

# 转换 MP3 为 AAC (更低码率)
ffmpeg -i composeApp/src/androidMain/assets/audio/music/fire_engine.mp3 \
  -c:a aac -b:a 96k \
  composeApp/src/androidMain/assets/audio/music/fire_engine.aac
```

**预期效果**: 7.6M → ~4M（节省约 3.6MB）

---

### 方案 3: 使用 App Bundle (AAB)

App Bundle 是 Google 推荐的发布格式，可根据设备自动下载所需资源。

```bash
# 构建 AAB
./gradlew :composeApp:bundleRelease

# 生成的文件位置
# composeApp/build/outputs/bundle/release/composeApp-release.aab
```

**优势**:
- 用户下载更小的 APK
- 自动按架构分包
- 支持按语言分包

---

### 方案 4: 动态下载资源

将大文件（视频）改为首次启动时下载。

#### 实现步骤
1. 将视频上传到 CDN
2. 应用启动时检查并下载视频
3. 下载过程中显示进度动画

**预期效果**: APK 大小降至 ~20MB

---

### 方案 5: 启用 APK 分割

为不同 CPU 架构生成不同的 APK。

```kotlin
splits {
    abi {
        isEnable = true
        reset()
        include("armeabi-v7a", "arm64-v8a")
        isUniversalApk = false
    }
}
```

**预期效果**: 用户下载更小的架构专用 APK

---

## 📋 构建前检查清单

- [ ] 代码混淆规则已配置
- [ ] 资源压缩已启用
- [ ] 签名配置已完成
- [ ] 视频文件已优化
- [ ] 音频文件已优化
- [ ] 测试所有功能正常
- [ ] 验证 APK 可正常安装

---

## 🔧 构建命令

### 生成 Keystore
```bash
keytool -genkey -v -keystore release.keystore \
  -alias release -keyalg RSA -keysize 2048 -validity 10000
```

### 配置签名
```bash
cp keystore.properties.example keystore.properties
# 编辑 keystore.properties 填入密码
```

### 构建 Release APK
```bash
# 使用构建脚本 (推荐)
./scripts/build_release.sh

# 或直接使用 Gradle
./gradlew :composeApp:assembleRelease
```

### 构建 App Bundle
```bash
./gradlew :composeApp:bundleRelease
```

---

## 📏 大小目标

| 目标 | 当前 | 优化后 | 状态 |
|------|------|--------|------|
| APK 大小 | ~120MB | ~80MB | 进行中 |
| 资源优化 | - | 压缩视频+音频 | 待处理 |
| App Bundle | - | ~50MB | 待测试 |

---

## 🔍 验证 APK

### 验证签名
```bash
apksigner verify --print-certs composeApp/build/outputs/apk/release/composeApp-release.apk
```

### 分析 APK 内容
```bash
# 使用 Android SDK
aapt dump badging composeApp/build/outputs/apk/release/composeApp-release.apk

# 或使用 APK Analyzer (Android Studio)
# Build -> Analyze APK...
```

---

## 📝 注意事项

1. **不要混淆模型类**: 数据模型需要保持序列化兼容
2. **测试混淆后功能**: 某些反射操作可能受影响
3. **保留崩溃日志**: 使用 ProGuard mapping.txt 还原堆栈
4. **备份 Keystore**: 丢失后无法更新应用

---

## 🎯 推荐优化顺序

1. **立即执行**: 代码混淆 + 资源压缩 (已完成)
2. **高优先级**: 视频压缩 (节省最大)
3. **中优先级**: 音频优化
4. **长期方案**: 动态资源下载
