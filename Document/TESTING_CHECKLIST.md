# TigerFire App真机测试清单

## 📱 测试环境
- **设备**：M2105K81AC（小米）
- **系统**：Android 13
- **安装状态**：✅ APK已安装（2026-01-30 20:58）
- **包名**：com.cryallen.tigerfire

---

## 🧪 优化验证测试清单

### 1. P0安全性优化验证

#### 1.1 SchoolScreen警报效果（✅ 已优化）
**优化点**：
- 警报强度：0.25 → 0.15（降低40%）
- 警报周期：2000ms → 3000ms（减缓50%）

**测试步骤**：
1. [ ] 从MapScreen进入SchoolScreen
2. [ ] 观察红色警报闪烁效果
3. [ ] 验证闪烁频率变慢（每3秒而非2秒）
4. [ ] 验证闪烁强度变弱（透明度更低）

**预期结果**：
- ✅ 警报更柔和，不刺眼
- ✅ 闪烁速度明显变慢
- ✅ 儿童不会被惊吓

---

#### 1.2 ForestScreen火焰卡通化（✅ 已优化）
**优化点**：
- 火焰效果：粒子系统 → emoji 🔥
- 动画：跳跃+左右摇摆

**测试步骤**：
1. [ ] 从MapScreen进入ForestScreen
2. [ ] 观察小羊周围火焰效果
3. [ ] 验证火焰为emoji 🔥（非粒子效果）
4. [ ] 观察火焰跳跃和摇摆动画

**预期结果**：
- ✅ 火焰为可爱emoji，无真实火焰粒子
- ✅ 动画有趣不恐怖
- ✅ 儿童感觉有趣而非害怕

---

### 2. P1一致性优化验证

#### 2.1 统一主题渐变（✅ 已优化）
**优化点**：所有Screen应用ThemeGradients，统一3层渐变结构

**测试清单**：
1. [ ] **WelcomeScreen**：天空蓝系（导入已就绪）
2. [ ] **MapScreen**：天空蓝→粉蓝→嫩绿（3层）
3. [ ] **FireStationScreen**：柔和红→温暖橙→明亮黄（3层）
4. [ ] **SchoolScreen**：青绿蓝→天空蓝→淡蓝（3层）
5. [ ] **ForestScreen**：翠绿→嫩绿→黄绿（3层）
6. [ ] **CollectionScreen**：粉紫→金黄→天蓝→嫩绿（4层彩虹糖果）

**预期结果**：
- ✅ 所有渐变明亮、饱和
- ✅ 层次统一（除Collection为4层）
- ✅ 视觉风格一致

---

#### 2.2 统一返回按钮（✅ 已优化）
**优化点**：KidsBackButton（64dp、emoji 🔙、Spring动画）

**测试清单**：
1. [ ] **FireStationScreen**：左上角返回按钮
2. [ ] **SchoolScreen**：左上角返回按钮
3. [ ] **ForestScreen**：左上角返回按钮
4. [ ] **CollectionScreen**：左上角返回按钮
5. [ ] **ParentScreen**：部分优化

**交互测试**：
- [ ] 按钮显示为🔙 emoji
- [ ] 按钮尺寸为64dp（比之前更大）
- [ ] 点击有Spring回弹动画
- [ ] 点击后正确返回MapScreen

**预期结果**：
- ✅ 所有返回按钮视觉统一
- ✅ 触摸目标更大（儿童易点击）
- ✅ 动画流畅有趣

---

### 3. P2增强优化验证

#### 3.1 SchoolScreen播放按钮（✅ 已优化）
**优化点**：简化PlayButtonArea（182行 → 60行），应用CartoonPlayButton

**测试步骤**：
1. [ ] 进入SchoolScreen
2. [ ] 观察播放按钮样式
3. [ ] 验证按钮尺寸≥180dp
4. [ ] 观察脉冲动画（1.0 → 1.05）
5. [ ] 点击播放按钮
6. [ ] 验证视频正常播放

**预期结果**：
- ✅ 播放按钮超大（易点击）
- ✅ 脉冲动画吸引注意
- ✅ 星星闪烁、光晕扩散
- ✅ 点击反馈明显
- ✅ 视频播放流畅

---

#### 3.2 ForestScreen小羊组件（✅ 已优化）
**优化点**：现有SheepClickable已包含优秀设计，保持原样

**测试步骤**：
1. [ ] 进入ForestScreen
2. [ ] 观察2只小羊
3. [ ] 验证小羊尺寸≥110dp
4. [ ] 观察求救摇晃动画
5. [ ] 观察脉冲光晕（未救援时）
6. [ ] 点击小羊触发救援

**预期结果**：
- ✅ 小羊emoji 🐑清晰可见
- ✅ 摇晃动画有趣
- ✅ 光晕引导儿童点击
- ✅ 点击响应灵敏
- ✅ 火焰环绕效果（emoji 🔥）

---

#### 3.3 CollectionScreen徽章浮动（✅ 已优化）
**优化点**：BadgeCard新增浮动、旋转、动态阴影

**测试步骤**：
1. [ ] 进入CollectionScreen
2. [ ] 观察已获得的徽章卡片
3. [ ] 验证徽章上下浮动（0→12dp）
4. [ ] 观察轻微旋转（-2°→2°）
5. [ ] 检查闪光效果（shimmer）
6. [ ] 观察动态阴影跟随浮动

**预期结果**：
- ✅ 徽章有明显浮动效果
- ✅ 旋转增强3D感
- ✅ 闪光吸引注意
- ✅ 动画流畅不卡顿

---

## 🎯 关键性能验证

### 帧率测试
1. [ ] MapScreen滚动场景图标：保持60fps
2. [ ] SchoolScreen警报闪烁：保持60fps
3. [ ] ForestScreen火焰动画：保持60fps
4. [ ] CollectionScreen徽章列表滚动：保持60fps

**测试方法**：
```bash
# 启用GPU渲染模式分析
adb shell setprop debug.hwui.profile visual_bars

# 查看帧率
adb shell dumpsys gfxinfo com.cryallen.tigerfire
```

---

### 内存测试
1. [ ] 启动App内存占用 < 150MB
2. [ ] 各Screen切换后内存增长 < 50MB
3. [ ] 长时间使用（5分钟）内存稳定

**测试方法**：
```bash
# 查看内存占用
adb shell dumpsys meminfo com.cryallen.tigerfire
```

---

### 触摸目标测试
1. [ ] 所有返回按钮：64dp（✅ 超过48dp最小标准）
2. [ ] SchoolScreen播放按钮：180dp（✅ 超大）
3. [ ] ForestScreen小羊：110dp（✅ 超过100dp儿童标准）
4. [ ] CollectionScreen徽章卡片：110dp宽（✅ 合适）

**验证方法**：
- 让3-6岁儿童尝试点击
- 观察误触率是否降低

---

## 🐛 已知问题检查

### 编译警告（非阻塞）
- [ ] ViewModel deprecated warning
- [ ] LocalLifecycleOwner deprecated warning
- [ ] expect/actual classes Beta warning

**状态**：⚠️ 警告不影响功能，后续版本修复

---

### 代码优化建议
- [ ] Division by zero in CrashLogDebugScreen（故意触发）
- [ ] Redundant conversion in ForestScreen
- [ ] Deprecated Divider in ParentScreen

**状态**：⚠️ 低优先级，不影响核心功能

---

## 📊 测试结果记录

### P0安全性（2项）
- [ ] SchoolScreen警报柔和化：通过 / 失败 / 未测试
- [ ] ForestScreen火焰卡通化：通过 / 失败 / 未测试

### P1一致性（2项）
- [ ] 统一主题渐变：通过 / 失败 / 未测试
- [ ] 统一返回按钮：通过 / 失败 / 未测试

### P2增强体验（3项）
- [ ] SchoolScreen播放按钮：通过 / 失败 / 未测试
- [ ] ForestScreen小羊组件：通过 / 失败 / 未测试
- [ ] CollectionScreen徽章浮动：通过 / 失败 / 未测试

### 性能指标
- [ ] 帧率≥60fps：通过 / 失败 / 未测试
- [ ] 内存<200MB：通过 / 失败 / 未测试
- [ ] 无崩溃：通过 / 失败 / 未测试

---

## 🎥 测试记录

### 截图清单
建议记录以下关键截图：
1. MapScreen（主导航）
2. SchoolScreen警报效果
3. SchoolScreen播放按钮
4. ForestScreen火焰效果
5. ForestScreen小羊组件
6. CollectionScreen徽章浮动
7. 返回按钮统一效果

### 视频记录
建议录制以下操作视频：
1. 完整App流程（3-5分钟）
2. 各Screen切换流畅度
3. 动画效果演示
4. 儿童实际使用测试

---

## ✅ 完成标准

**通过条件**：
- ✅ P0安全性优化2项全部通过
- ✅ P1一致性优化2项全部通过
- ✅ P2增强体验3项至少2项通过
- ✅ 帧率≥60fps
- ✅ 内存占用<200MB
- ✅ 无崩溃、无阻塞性bug

**下一步**：
- 收集儿童和家长反馈
- 记录改进建议
- 规划下一轮迭代

---

**测试日期**：2026-01-30
**测试设备**：M2105K81AC（小米，Android 13）
**测试版本**：Debug Build
**测试人员**：开发团队 + GitHub Copilot

---

## 🚀 快速测试命令

```bash
# 重新安装APK
cd /Users/vsh9p8q/Personal/Project/TigerTruck/TigerFire
./gradlew installDebug

# 启动App
adb shell am start -n com.cryallen.tigerfire/.MainActivity

# 查看日志
adb logcat -c && adb logcat | grep TigerFire

# 查看帧率
adb shell dumpsys gfxinfo com.cryallen.tigerfire

# 查看内存
adb shell dumpsys meminfo com.cryallen.tigerfire

# 截屏
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# 录屏
adb shell screenrecord /sdcard/demo.mp4
# Ctrl+C停止录制
adb pull /sdcard/demo.mp4
```
