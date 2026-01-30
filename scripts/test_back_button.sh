#!/bin/bash

# 测试返回按钮样式修复
echo "=== 返回按钮样式测试 ==="
echo ""

# 启动应用
echo "1️⃣ 启动应用..."
adb shell am start -n com.cryallen.tigerfire/com.cryallen.tigerfire.MainActivity
sleep 3

# 检查是否启动
if adb shell pidof com.cryallen.tigerfire > /dev/null; then
    echo "✅ 应用启动成功"
else
    echo "❌ 应用启动失败"
    exit 1
fi

echo ""
echo "2️⃣ 测试返回按钮（请在设备上手动测试）："
echo "   - 进入消防站、学校、森林等场景"
echo "   - 检查返回按钮是否显示为 ← 箭头（不是emoji 🔙）"
echo "   - 测试返回按钮点击动画和功能"
echo ""
echo "3️⃣ 预期效果："
echo "   ✓ 返回按钮显示为简洁的蓝色箭头 ←"
echo "   ✓ 白色渐变背景 + 金色描边"
echo "   ✓ 点击时有缩放动画"
echo "   ✓ 不再使用 🔙 emoji"
echo ""

# 等待用户确认
read -p "按Enter键继续监控日志，或Ctrl+C退出..."

# 监控日志
echo ""
echo "4️⃣ 监控崩溃日志..."
adb logcat -c
adb logcat | grep -E "FATAL|AndroidRuntime|CrashReport|TigerFire"
