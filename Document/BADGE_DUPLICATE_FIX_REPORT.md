# 徽章重复保存问题修复报告

## 📋 问题描述

**用户反馈：**
- 消防站场景播放1个视频，收藏页面却显示3个徽章
- 消防站场景没及时记录徽章数据，导致收藏页面显示与实际不符

**预期逻辑：**
- 消防站：4个设备视频 → 4个基础徽章
- 学校：1个视频 → 1个基础徽章
- 森林：2只小羊 → 2个基础徽章
- **总计：7个不同类型的基础徽章**

## 🔍 根本原因分析

### 核心问题：竞态条件导致徽章重复保存

**触发场景：**
1. 用户快速点击同一设备图标
2. 第一次点击触发视频播放 → 视频完成 → 开始保存徽章（异步操作）
3. **在保存完成前**，用户再次点击同一设备
4. 第二次检查时，数据库还未更新，检查通过
5. 第二次也开始保存徽章
6. **结果：同一个设备的徽章被保存了2-3次**

### 代码层面的问题

**FireStationViewModel.handleVideoCompleted():**
```kotlin
viewModelScope.launch {  // ❌ 异步操作，存在竞态条件
    val progress = repository.getGameProgressNow()
    val alreadyCompleted = device.deviceId in progress.fireStationCompletedItems

    if (alreadyCompleted) {
        return@launch  // 已完成则跳过
    }

    // ❌ 问题：从检查到保存之间有时间差，可能重复保存
    progressRepository.saveProgressWithBadge(finalProgress, deviceBadge)
}
```

**时间线示意：**
```
时间轴：  T1      T2      T3      T4      T5      T6
点击1:   [点击] → [视频] → [检查✓] → [保存中...]
点击2:            [点击] → [视频] → [检查✓] → [保存中...]
                                    ↑
                                  问题点：两次检查都通过了
```

## ✅ 修复方案

### 三层防护机制

#### 1. **视频播放期间禁用点击**
```kotlin
private fun handleDeviceClicked(device: FireStationDevice) {
    // ✅ 防止在视频播放期间点击
    if (currentState.isPlayingVideo) {
        return
    }
    // ...
}
```

#### 2. **保存徽章时添加锁**
```kotlin
// 添加保存状态集合
private val savingDevices = mutableSetOf<FireStationDevice>()

private fun handleVideoCompleted(device: FireStationDevice) {
    viewModelScope.launch {
        // ✅ 检查并添加保存锁
        val shouldSave = synchronized(savingDevices) {
            if (device in savingDevices) {
                false  // 已经在保存中
            } else {
                savingDevices.add(device)
                true
            }
        }

        if (!shouldSave) {
            return@launch  // 跳过重复保存
        }

        try {
            // 保存逻辑...
        } finally {
            // ✅ 保存完成后移除锁
            synchronized(savingDevices) {
                savingDevices.remove(device)
            }
        }
    }
}
```

#### 3. **保存完成后立即释放锁**
使用 `try-finally` 确保无论保存成功或失败，都会释放锁。

## 📝 修改的文件

### 1. FireStationViewModel.kt
- ✅ 添加 `savingDevices` 保存锁
- ✅ `handleDeviceClicked`: 检查视频播放和保存状态
- ✅ `handleVideoCompleted`: 使用 try-finally 包裹保存逻辑

### 2. ForestViewModel.kt
- ✅ 添加 `savingSheepIndices` 保存锁
- ✅ `handleSheepClicked`: 检查保存状态
- ✅ `handleRescueVideoCompleted`: 使用 try-finally 包裹保存逻辑

### 3. SchoolViewModel.kt
- ✅ 添加 `isSavingBadge` 保存标志
- ✅ `handleVideoCompleted`: 使用 try-finally 包裹保存逻辑

## 🎯 修复后的时间线

```
时间轴：  T1      T2      T3      T4      T5      T6
点击1:   [点击] → [视频] → [加锁] → [保存中...]
点击2:            [点击✗] ← 被拒绝（保存中）
                   ↑
                 防护点1：视频播放中拒绝

点击1:   [点击] → [视频] → [加锁] → [保存] → [释放锁]
点击2:                     [点击✗] ← 被拒绝（锁定中）
                            ↑
                         防护点2：保存锁拒绝
```

## 🧪 测试验证

### 测试场景
1. **单次播放测试**
   - 点击灭火器 → 观看视频 → 检查收藏页面
   - 预期：显示 1 个徽章

2. **重复播放测试**
   - 再次点击灭火器 → 观看视频 → 检查收藏页面
   - 预期：显示 2 个徽章（不同变体）

3. **快速点击测试**
   - 快速连续点击同一设备多次
   - 预期：只播放一个视频，只保存一次徽章

4. **完整流程测试**
   - 依次观看4个设备视频
   - 预期：收藏页面显示 4 个基础徽章

### 运行测试
```bash
# 1. 清空数据库
adb shell run-as com.cryallen.tigerfire rm -rf /data/data/com.cryallen.tigerfire/databases/

# 2. 重新安装应用
./gradlew installDebug

# 3. 手动测试上述场景

# 4. 查看日志
adb logcat | grep -E 'DEBUG|Badge|saveProgressWithBadge'
```

## 📊 业务逻辑验证

| 场景 | 徽章baseType | 变体数量 | 保存逻辑 | 验证逻辑 | 修复后状态 |
|------|-------------|---------|---------|---------|-----------|
| 消防站-消防栓 | `fire_hydrant` | 4 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |
| 消防站-云梯 | `ladder_truck` | 4 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |
| 消防站-灭火器 | `fire_extinguisher` | 4 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |
| 消防站-水枪 | `water_hose` | 4 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |
| 学校 | `school` | 3 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |
| 森林-小羊1 | `forest_sheep1` | 2 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |
| 森林-小羊2 | `forest_sheep2` | 2 | ✅ 正确 | ✅ 正确 | ✅ 已修复 |

## 🔧 其他发现

### 数据一致性
1. **徽章ID与baseType匹配**：经检查，设备ID与徽章baseType完全一致
2. **验证逻辑正确**：CollectionViewModel 的验证逻辑正确使用 `badge.baseType in progress.fireStationCompletedItems`
3. **数据库事务正常**：`saveProgressWithBadge` 使用了事务确保原子性

### 不存在的问题（已排除）
- ❌ 设备ID不匹配（实际匹配正确）
- ❌ 验证逻辑错误（实际逻辑正确）
- ❌ 数据库事务问题（实际使用正常）

## 💡 后续建议

### 1. 数据库层面加强
考虑在数据库表中添加唯一约束：
```sql
CREATE TABLE Badge(
    id TEXT PRIMARY KEY NOT NULL,
    scene TEXT NOT NULL,
    baseType TEXT NOT NULL,
    variant INTEGER NOT NULL,
    earnedAt INTEGER NOT NULL,
    UNIQUE(baseType, variant)  -- 添加联合唯一约束
);
```

### 2. 监控和日志
- 保留现有的 DEBUG 日志，便于追踪问题
- 添加徽章保存成功/失败的统计

### 3. 用户体验优化
- 视频播放期间禁用返回按钮
- 添加保存进度提示（可选）

## ✅ 修复完成

**修复时间：** 2026-01-30

**修复范围：**
- ✅ 消防站场景徽章重复保存问题
- ✅ 森林场景徽章重复保存问题
- ✅ 学校场景徽章重复保存问题

**测试建议：**
1. 清空数据库重新测试
2. 重点测试快速点击场景
3. 验证完整的 7 个基础徽章收集流程

**预期结果：**
- 每个设备/场景只保存一次基础徽章
- 重复观看可以获得不同变体徽章
- 收藏页面显示的徽章数量与实际一致
