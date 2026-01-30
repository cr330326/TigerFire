#!/bin/bash

echo "=== 白屏崩溃调试 ==="
echo ""
echo "准备工作："
echo "1. 清空日志缓存"
adb logcat -c

echo "2. 强制停止应用"
adb shell am force-stop com.cryallen.tigerfire

echo "3. 启动应用"
adb shell am start -n com.cryallen.tigerfire/com.cryallen.tigerfire.MainActivity
sleep 2

echo ""
echo "📱 请在设备上执行以下操作："
echo "   1️⃣ 进入消防车场景"
echo "   2️⃣ 点击返回按钮回到主页面"
echo "   3️⃣ 进入收藏页面"
echo "   4️⃣ 多次点击返回按钮（尝试复现白屏）"
echo ""
echo "按Enter键开始捕获日志..."
read

echo ""
echo "📋 捕获日志中（Ctrl+C停止）..."
echo "================================================"
adb logcat -v time | grep -E "AndroidRuntime|FATAL|Exception|Error|TigerFire" | grep -v "SignalStrength\|FileUtil\|com.mi.health"
