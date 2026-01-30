#!/bin/bash

echo "=== ParentScreen 功能验证测试 ==="
echo ""

# 检查设备连接
if ! adb devices | grep -q "device$"; then
    echo "❌ 未检测到设备，请连接设备后重试"
    exit 1
fi

echo "✅ 设备已连接"
echo ""

# 启动应用
echo "1️⃣ 启动应用..."
adb shell am force-stop com.cryallen.tigerfire
sleep 1
adb shell am start -n com.cryallen.tigerfire/com.cryallen.tigerfire.MainActivity
sleep 3

if ! adb shell pidof com.cryallen.tigerfire > /dev/null; then
    echo "❌ 应用启动失败"
    exit 1
fi
echo "✅ 应用启动成功"
echo ""

echo "📋 测试步骤："
echo ""
echo "步骤1: 验证使用统计数据"
echo "   ✓ 进入主页面，点击地图上的家长模式图标"
echo "   ✓ 查看「使用统计」卡片："
echo "     - 今日使用时长：应显示今天的实际使用时长"
echo "     - 总使用时长：应显示所有历史数据的累计值（不应该为0）"
echo "     - 已收藏徽章：应显示已获得的徽章数量"
echo ""
echo "步骤2: 验证本周使用数据"
echo "   ✓ 在使用统计卡片下方查看「本周使用」柱状图"
echo "   ✓ 应该显示最近7天的使用时长柱状图"
echo "   ✓ 如果有使用数据，应该显示彩色柱子和总计时长"
echo ""
echo "步骤3: 验证重置游戏进度功能"
echo "   ✓ 点击「重置游戏进度」按钮"
echo "   ✓ 完成数学题验证（观察题目并输入正确答案）"
echo "   ✓ 确认重置对话框"
echo "   ✓ 验证重置后："
echo "     - 今日使用时长 = 0"
echo "     - 总使用时长 = 0"
echo "     - 已收藏徽章 = 0"
echo "     - 本周使用柱状图全部为灰色（暂无数据）"
echo ""

read -p "请在设备上完成上述测试步骤后按Enter继续..."

echo ""
echo "4️⃣ 检查崩溃日志..."
adb logcat -d | grep -E "FATAL|AndroidRuntime.*com.cryallen.tigerfire|Exception.*ParentScreen|Exception.*ParentViewModel" | tail -20

if adb logcat -d | grep -q "FATAL.*com.cryallen.tigerfire"; then
    echo "❌ 检测到崩溃日志"
    echo ""
    echo "完整崩溃信息："
    adb logcat -d | grep -A 30 "FATAL.*com.cryallen.tigerfire"
else
    echo "✅ 未检测到崩溃"
fi

echo ""
echo "5️⃣ 检查应用状态..."
if adb shell pidof com.cryallen.tigerfire > /dev/null; then
    echo "✅ 应用仍在运行"

    # 获取内存使用
    MEM=$(adb shell dumpsys meminfo com.cryallen.tigerfire | grep "TOTAL PSS" | awk '{print $3}')
    if [ -n "$MEM" ]; then
        echo "📊 内存使用: ${MEM}K"
    fi
else
    echo "❌ 应用已停止运行"
fi

echo ""
echo "=== 测试完成 ==="
echo ""
echo "请确认以下功能是否正常："
echo "  [ ] 总使用时长显示正确（不为0）"
echo "  [ ] 本周使用数据显示正确"
echo "  [ ] 重置游戏进度功能正常工作"
echo "  [ ] 重置后所有统计数据清零"
echo ""
