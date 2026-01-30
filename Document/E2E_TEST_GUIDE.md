# 徽章收集功能端到端测试指南

## 🎯 测试目标

验证修复后的徽章收集功能是否正常工作，确保：
1. ✅ 每个设备/场景只保存一次基础徽章
2. ✅ 重复观看可以获得不同变体
3. ✅ 收藏页面显示的徽章数量与实际一致
4. ✅ 不会出现重复徽章

## 📋 测试前准备

### 1. 应用已安装
```bash
cd /Users/vsh9p8q/Personal/Project/TigerTruck/TigerFire
bash gradlew installDebug
```

### 2. 清空数据库（可选，用于全新测试）
```bash
adb shell run-as com.cryallen.tigerfire rm -rf /data/data/com.cryallen.tigerfire/databases/
```

### 3. 启动日志监控（在单独终端窗口）
```bash
adb logcat -c
adb logcat | grep -E "DEBUG handleVideoCompleted|saveProgressWithBadge|Badge" --line-buffered
```

## 🧪 测试场景

### 测试1: 消防站单个设备（消防栓）

**步骤：**
1. 打开应用，进入欢迎页面
2. 点击"开始游戏"进入主地图
3. 点击"消防站"图标
4. 在消防站场景中，点击**消防栓（fire_hydrant）**图标
5. 观看视频完成（约5-10秒）
6. 等待徽章动画显示
7. 点击返回，进入"我的收藏"页面

**预期结果：**
- ✅ 收藏页面显示 **1个徽章**（消防栓）
- ✅ 日志显示：
  ```
  DEBUG handleVideoCompleted: device = fire_hydrant
  DEBUG saveProgressWithBadge: badge.baseType = fire_hydrant
  DEBUG saveProgressWithBadge: badge.id = fire_hydrant_v0_xxx
  ```

**验证点：**
- [ ] 收藏页面显示1个徽章
- [ ] 徽章名称为"消防栓"
- [ ] 变体编号为 v0（默认）

---

### 测试2: 重复观看同一设备

**步骤：**
1. 从收藏页面返回主地图
2. 再次进入消防站场景
3. 再次点击**消防栓（fire_hydrant）**图标
4. 观看视频完成
5. 进入"我的收藏"页面

**预期结果：**
- ✅ 收藏页面显示 **2个徽章**（消防栓 v0 和 v1）
- ✅ 日志显示：
  ```
  DEBUG saveProgressWithBadge: badge.id = fire_hydrant_v1_xxx
  ```

**验证点：**
- [ ] 收藏页面显示2个徽章（都是消防栓，但变体不同）
- [ ] 第一个徽章：v0
- [ ] 第二个徽章：v1

---

### 测试3: 快速点击同一设备（竞态测试）

**步骤：**
1. 从收藏页面返回主地图
2. 进入消防站场景
3. **快速连续点击**消防栓图标3-5次（在视频播放期间）
4. 观看视频完成
5. 进入"我的收藏"页面

**预期结果：**
- ✅ 只播放1个视频
- ✅ 收藏页面显示 **3个徽章**（v0, v1, v2，没有重复）
- ✅ 日志显示只保存了1次：
  ```
  DEBUG saveProgressWithBadge: badge.id = fire_hydrant_v2_xxx
  ```

**验证点：**
- [ ] 收藏页面显示3个徽章（v0, v1, v2）
- [ ] 没有出现重复的变体（如两个v2）
- [ ] 日志中没有多次保存同一变体的记录

---

### 测试4: 完整流程测试（所有设备）

**步骤：**
1. 清空数据库，重启应用
2. 依次观看消防站的4个设备视频：
   - 消防栓（fire_hydrant）
   - 云梯（ladder_truck）
   - 灭火器（fire_extinguisher）
   - 水枪（water_hose）
3. 进入学校场景，观看视频
4. 进入森林场景，救援2只小羊
5. 进入"我的收藏"页面

**预期结果：**
- ✅ 收藏页面显示 **7个徽章**
  - 消防站：4个（fire_hydrant, ladder_truck, fire_extinguisher, water_hose）
  - 学校：1个（school）
  - 森林：2个（forest_sheep1, forest_sheep2）

**验证点：**
- [ ] 总徽章数：7个
- [ ] 消防站徽章：4个
- [ ] 学校徽章：1个
- [ ] 森林徽章：2个
- [ ] 每个类型都是 v0（首次获得）

---

## 📊 日志关键信息

### 正常保存日志示例
```
DEBUG handleVideoCompleted: device = fire_hydrant
DEBUG handleVideoCompleted: progress.fireStationCompletedItems = []
DEBUG handleVideoCompleted: updatedProgress.fireStationCompletedItems = [fire_hydrant]
DEBUG handleVideoCompleted: isAllCompleted = false
DEBUG saveProgressWithBadge: START TRANSACTION
DEBUG saveProgressWithBadge: badge.id = fire_hydrant_v0_1769781888095
DEBUG saveProgressWithBadge: badge.baseType = fire_hydrant
DEBUG saveProgressWithBadge: fireStationCompletedItems = ["fire_hydrant"]
DEBUG saveProgressWithBadge: COMMIT TRANSACTION
```

### 重复保存检测（修复后应该不出现）
如果看到以下日志，说明有问题：
```
DEBUG saveProgressWithBadge: badge.id = fire_hydrant_v0_xxx
DEBUG saveProgressWithBadge: badge.id = fire_hydrant_v0_yyy  ← 相同变体被保存两次！
```

---

## ✅ 测试检查清单

### 功能测试
- [ ] 单个设备观看 → 收藏页面显示1个徽章
- [ ] 重复观看 → 获得不同变体徽章
- [ ] 快速点击 → 不会重复保存
- [ ] 所有设备 → 收藏7个基础徽章

### 性能测试
- [ ] 徽章保存速度正常（< 100ms）
- [ ] UI响应流畅，无卡顿
- [ ] 日志无异常或错误

### 数据一致性
- [ ] 收藏页面显示数量 = 实际数据库数量
- [ ] 每个基础类型只有一个 v0 徽章
- [ ] 变体编号连续（v0, v1, v2, v3）

---

## 🐛 已知问题排查

### 问题1: 收藏页面显示的徽章比预期多

**可能原因：**
- 重复保存了相同的徽章

**排查方法：**
1. 查看日志，搜索 `saveProgressWithBadge`
2. 检查是否有相同的 `badge.id` 被保存多次
3. 检查 `badge.variant` 是否有重复

**预期修复：**
- 添加了保存锁机制，应该不会再出现

---

### 问题2: 收藏页面显示的徽章比预期少

**可能原因：**
- 数据库保存失败
- `CollectionViewModel` 验证逻辑过滤了某些徽章

**排查方法：**
1. 查看日志中的 `COMMIT TRANSACTION` 是否成功
2. 检查 `fireStationCompletedItems` 是否包含设备ID
3. 使用 `verify_badge_database.sh` 脚本检查数据库

---

### 问题3: 快速点击导致重复徽章

**可能原因：**
- 保存锁机制失效

**排查方法：**
1. 查看日志，检查是否有多个 `START TRANSACTION` 同时执行
2. 检查是否有 `synchronized(savingDevices)` 相关的错误

**预期修复：**
- 三层防护机制应该阻止这种情况

---

## 📝 测试报告模板

```markdown
# 徽章收集功能测试报告

**测试日期：** 2026-01-30
**测试设备：** [设备型号]
**应用版本：** [版本号]

## 测试结果总结

- ✅/❌ 测试1: 单个设备观看
- ✅/❌ 测试2: 重复观看
- ✅/❌ 测试3: 快速点击
- ✅/❌ 测试4: 完整流程

## 详细测试结果

### 测试1: 单个设备观看
- 预期徽章数: 1
- 实际徽章数: __
- 结果: ✅/❌

### 测试2: 重复观看
- 预期徽章数: 2
- 实际徽章数: __
- 结果: ✅/❌

### 测试3: 快速点击
- 预期徽章数: 3
- 实际徽章数: __
- 是否有重复: 是/否
- 结果: ✅/❌

### 测试4: 完整流程
- 预期徽章数: 7
- 实际徽章数: __
- 各场景徽章数:
  - 消防站: __/4
  - 学校: __/1
  - 森林: __/2
- 结果: ✅/❌

## 发现的问题

[列出测试中发现的任何问题]

## 结论

[总体评价修复效果]
```

---

## 🎯 成功标准

修复被认为成功，当且仅当：
1. ✅ 所有测试场景都通过
2. ✅ 日志中没有重复保存记录
3. ✅ 收藏页面显示的徽章数量准确
4. ✅ 快速点击不会导致重复徽章

---

## 📞 支持

如有问题，请查看：
- 详细修复报告: `document/BADGE_DUPLICATE_FIX_REPORT.md`
- 数据库验证脚本: `scripts/verify_badge_database.sh`
- 测试脚本: `scripts/test_badge_fix.sh`
