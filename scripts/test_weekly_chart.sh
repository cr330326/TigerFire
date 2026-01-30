#!/bin/bash

echo "=== 本周使用数据验证测试 ==="
echo ""
echo "修复内容："
echo "  - 修改 getLast7DaysMinutes() 函数"
echo "  - 从 for (i in 1..7) 改为 for (i in 0..6)"
echo "  - 现在包括今天的数据"
echo ""

# 获取今天的日期
TODAY=$(date +"%Y-%m-%d")
echo "今天日期: $TODAY"
echo ""

echo "📋 详细测试步骤："
echo ""
echo "1️⃣ 进入家长模式页面"
echo "   - 点击主页面地图上的「家长模式」图标"
echo ""

echo "2️⃣ 观察「本周使用」柱状图"
echo "   - 位置：在「使用统计」卡片下方"
echo "   - 标题：📈 本周使用"
echo "   - 右上角：总计 Xh Xm"
echo ""

echo "3️⃣ 验证柱状图数据（7个柱子，从左到右）"
echo "   假设今天是周四（$TODAY）："
echo ""
echo "   柱子1 (周五) - 6天前的数据"
echo "   柱子2 (周六) - 5天前的数据"
echo "   柱子3 (周日) - 4天前的数据"
echo "   柱子4 (周一) - 3天前的数据"
echo "   柱子5 (周二) - 2天前的数据"
echo "   柱子6 (周三) - 昨天的数据"
echo "   柱子7 (周四) - 【今天的数据 - 应该显示！】"
echo ""

echo "4️⃣ 检查柱子颜色"
echo "   ✅ 有数据的日期："
echo "      - 柱子高度 > 4dp"
echo "      - 绿色渐变 (#159895 → #57C5B6)"
echo "      - 有阴影效果"
echo ""
echo "   ⬜ 无数据的日期："
echo "      - 柱子高度 = 4dp (最小高度)"
echo "      - 灰色 (#E0E0E0 → #EEEEEE)"
echo "      - 无阴影"
echo ""

echo "5️⃣ 检查总计时长"
echo "   - 如果今天有使用数据，总计应该 > 0"
echo "   - 格式：总计 Xh Xm"
echo "   - 如果全部为0，显示：暂无数据"
echo ""

read -p "按Enter键启动应用并开始测试..."

# 启动应用
adb shell am force-stop com.cryallen.tigerfire
sleep 1
adb shell am start -n com.cryallen.tigerfire/com.cryallen.tigerfire.MainActivity
sleep 2

echo ""
echo "✅ 应用已启动"
echo ""
echo "请在设备上完成测试，然后按Enter查看日志..."
read

echo ""
echo "📊 检查应用日志（最近50行）..."
adb logcat -d -v time | grep -i "parent\|usage\|weekly" | tail -50

echo ""
echo "=== 验证结果确认 ==="
echo ""
read -p "本周柱状图最后一个柱子（今天）是否有数据？(y/n): " ANSWER

if [ "$ANSWER" = "y" ] || [ "$ANSWER" = "Y" ]; then
    echo "✅ 测试通过！本周使用数据显示正常"
else
    echo "❌ 测试失败！需要进一步排查"
    echo ""
    echo "可能的问题："
    echo "  1. 今天还没有使用数据（dailyUsageStats为空）"
    echo "  2. 日期格式不匹配"
    echo "  3. 数据未正确从数据库读取"
fi

echo ""
