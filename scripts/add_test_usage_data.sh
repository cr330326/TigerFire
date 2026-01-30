#!/bin/bash
##################################
# 添加测试使用数据
# 向设备中的应用数据库添加今天的使用数据用于测试
##################################

echo "=== 添加测试使用数据 ==="
echo ""

# 获取今天的日期
TODAY=$(date +"%Y-%m-%d")
echo "今天日期: $TODAY"
echo ""

# 包名
PACKAGE_NAME="com.cryallen.tigerfire"

echo "📋 测试步骤："
echo ""
echo "1️⃣ 启动应用并进入任一场景（消防站/学校/森林）"
echo "   - 在场景中停留至少 2-3 分钟"
echo "   - 确保 SessionTimer 正在运行"
echo ""
echo "2️⃣ 返回主页面（触发 SessionTimer.endSessionAndRecord()）"
echo "   - 这会将使用时长记录到数据库"
echo "   - 日期格式：$TODAY"
echo ""
echo "3️⃣ 进入家长模式查看统计"
echo "   - 今日使用时长应该显示刚才的游玩时间"
echo "   - 本周柱状图最后一个柱子（今天）应该有数据"
echo ""
echo "⚠️ 注意："
echo "   - 只有退出场景时才会记录使用时长"
echo "   - 如果还在场景中，数据不会实时更新"
echo "   - 日期格式必须为 yyyy-MM-dd (如: $TODAY)"
echo ""

echo "按Enter键启动应用..."
read

adb shell am start -n $PACKAGE_NAME/.MainActivity

echo ""
echo "✅ 应用已启动"
echo ""
echo "请完成以下操作:"
echo "  1. 进入任一场景（消防站/学校/森林）"
echo "  2. 停留 2-3 分钟"
echo "  3. 返回主页面"
echo "  4. 进入家长模式"
echo "  5. 查看「本周使用」柱状图"
echo ""
echo "完成后按Enter键继续..."
read

echo ""
echo "📊 检查应用日志（查找数据库记录）..."
adb logcat -d | grep -E "recordUsage|dailyUsageStats|$TODAY" | tail -20

echo ""
echo "=== 验证结果 ==="
echo ""
echo "本周柱状图最后一个柱子（今天 $TODAY）是否有数据？(y/n): "
read has_data

if [ "$has_data" = "y" ]; then
    echo "✅ 测试成功！今天的数据已正确显示"
    echo ""
    echo "验证要点："
    echo "  ✓ 最后一个柱子有绿色渐变（表示有数据）"
    echo "  ✓ 柱子高度 > 4dp（不是灰色最小高度）"
    echo "  ✓ 总计时长 > 0"
else
    echo "❌ 测试失败！需要进一步排查"
    echo ""
    echo "可能的问题："
    echo "  1. 游玩时间太短（需要至少1分钟）"
    echo "  2. 未正确退出场景（数据未记录）"
    echo "  3. SessionTimer 未启动"
    echo "  4. 数据库写入失败"
    echo ""
    echo "建议操作："
    echo "  - 重新进入场景并停留更长时间（3-5分钟）"
    echo "  - 确保通过返回按钮正常退出场景"
    echo "  - 检查应用日志确认 recordUsage 被调用"
fi
