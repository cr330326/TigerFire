# 🚨 紧急问题发现：数据覆盖Bug

## 真机测试结果

### ✅ 好消息
1. 应用成功安装并运行
2. 事务功能正常工作
3. 徽章数据正确保存（共7个）

### ❌ 发现的严重问题

#### 问题现象
```
fireStationCompletedItems: ["fire_hydrant"]  ❌ 只有1项
实际徽章数: 5个消防站徽章                    ✅ 但有5个徽章
```

#### 徽章详情
| 场景 | baseType | 数量 | 变体 |
|------|----------|------|------|
| 消防站 | fire_hydrant | 2 | 0,1 |
| 消防站 | fire_extinguisher | 1 | 0 |
| 消防站 | ladder_truck | 1 | 0 |
| 消防站 | water_hose | 1 | 0 |
| 学校 | school | 2 | 0,1 |

## 🔍 根本原因

### 数据竞态条件

在 `RecordUsageUseCase.kt` 和 `UnlockSceneUseCase.kt` 中：

```kotlin
// ❌ 问题代码
val currentProgress = repository.getGameProgress().first()
val updatedProgress = currentProgress.addPlayTime(durationMillis)
repository.updateGameProgress(updatedProgress)  // 覆盖了fireStationCompletedItems
```

**执行流程**：
1. RecordUsageUseCase 从数据库读取progress（此时可能fireStationCompletedItems为空）
2. 只更新了 totalPlayTime
3. 调用 updateGameProgress()，**把其他字段也一起写入**
4. 结果：fireStationCompletedItems 被覆盖成旧值！

## 💡 解决方案

### 方案A：拆分更新方法（推荐）

在 `ProgressRepository` 中添加专门的更新方法：

```kotlin
interface ProgressRepository {
    // 现有方法
    suspend fun updateGameProgress(progress: GameProgress)
    suspend fun saveProgressWithBadge(progress: GameProgress, badge: Badge)

    // 🆕 新增：只更新单个字段的方法
    suspend fun updateTotalPlayTime(playTime: Long)
    suspend fun updateSceneStatus(scene: SceneType, status: SceneStatus)
}
```

在 `ProgressRepositoryImpl` 中实现：

```kotlin
override suspend fun updateTotalPlayTime(playTime: Long) {
    database.gameProgressQueries.updateTotalPlayTime(playTime)
}

override suspend fun updateSceneStatus(scene: SceneType, status: SceneStatus) {
    val progress = getGameProgress().first()
    val updated = progress.updateSceneStatus(scene, status)
    database.gameProgressQueries.updateSceneStatuses(
        json.encodeToString(updated.sceneStatuses)
    )
}
```

然后修改 `RecordUsageUseCase`：

```kotlin
suspend operator fun invoke(durationMillis: Long): Result<Unit> {
    return try {
        val todayDate = PlatformDateTime.getTodayDate()
        repository.recordUsage(todayDate, durationMillis)

        // ✅ 只更新总时长，不覆盖其他字段
        val currentProgress = repository.getGameProgress().first()
        val newTotalTime = currentProgress.totalPlayTime + durationMillis
        repository.updateTotalPlayTime(newTotalTime)

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 方案B：使用数据库级别的增量更新

直接在SQL层面做增量更新，避免读-改-写的竞态：

```sql
-- 在 GameProgress.sq 中添加
updateTotalPlayTimeIncrement:
UPDATE GameProgress
SET totalPlayTime = totalPlayTime + ?
WHERE id = 1;
```

## 📊 影响评估

### 受影响的功能
1. ✅ 徽章保存 - **已修复**（通过事务）
2. ❌ 游戏进度字段 - **受影响**（被UseCase覆盖）
3. ❌ 消防站完成项 - **严重受影响**
4. ❌ 森林救援小羊 - **可能受影响**

### 测试数据证明
- 用户完成了4个消防站设备（有5个徽章为证）
- 但 `fireStationCompletedItems` 只记录了1个
- 学校场景状态正确（COMPLETED）
- 事务功能正常（徽章与进度同时保存）

## 🎯 修复优先级

**P0 - 紧急**: 必须立即修复
- [ ] RecordUsageUseCase 的数据覆盖问题
- [ ] UnlockSceneUseCase 的数据覆盖问题

**P1 - 高**: 尽快修复
- [ ] 添加专用的字段更新方法
- [ ] 重构所有UseCase使用新方法

## 🧪 验证步骤

修复后需要验证：
1. 清空数据库
2. 依次完成4个消防站设备
3. 检查 `fireStationCompletedItems` 应包含全部4个
4. 完成学校和森林场景
5. 验证所有字段都正确

---

**发现时间**: 2026年1月30日 18:10
**严重程度**: 🔴 高危 - 数据完整性问题
**状态**: 待修复
