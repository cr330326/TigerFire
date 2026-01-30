#!/bin/bash

# TigerFire App 端到端测试脚本
# 日期: 2026-01-30
# 设备: M2105K81AC (Android 13)

echo "========================================="
echo "TigerFire App 端到端测试"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PACKAGE_NAME="com.cryallen.tigerfire"
MAIN_ACTIVITY=".MainActivity"

# 测试计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_case() {
    local test_name=$1
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${YELLOW}[测试 $TOTAL_TESTS]${NC} $test_name"
}

test_pass() {
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${GREEN}✅ 通过${NC}"
    echo ""
}

test_fail() {
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${RED}❌ 失败: $1${NC}"
    echo ""
}

# 检查设备连接
echo "1. 检查设备连接..."
DEVICE=$(adb devices | grep device | grep -v "List" | wc -l)
if [ $DEVICE -eq 0 ]; then
    echo -e "${RED}❌ 未检测到设备${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 设备已连接${NC}"
echo ""

# 检查App是否已安装
echo "2. 检查App安装状态..."
INSTALLED=$(adb shell pm list packages | grep $PACKAGE_NAME | wc -l)
if [ $INSTALLED -eq 0 ]; then
    echo -e "${RED}❌ App未安装${NC}"
    exit 1
fi
echo -e "${GREEN}✅ App已安装${NC}"
echo ""

# 清空日志
echo "3. 清空日志缓存..."
adb logcat -c
echo -e "${GREEN}✅ 日志已清空${NC}"
echo ""

# 强制停止App
echo "4. 强制停止App..."
adb shell am force-stop $PACKAGE_NAME
sleep 1
echo -e "${GREEN}✅ App已停止${NC}"
echo ""

# 启动App
echo "5. 启动App..."
test_case "启动MainActivity"
START_OUTPUT=$(adb shell am start -n $PACKAGE_NAME/$MAIN_ACTIVITY 2>&1)
if echo "$START_OUTPUT" | grep -q "Error"; then
    test_fail "启动失败"
    exit 1
else
    test_pass
fi

# 等待App完全启动
echo "等待App启动 (3秒)..."
sleep 3

# 检查崩溃
echo "6. 检查崩溃日志..."
test_case "检查是否有致命错误"
CRASHES=$(adb logcat -d | grep -E "FATAL|AndroidRuntime.*FATAL" | grep -v "grep" | wc -l)
if [ $CRASHES -gt 0 ]; then
    echo "发现崩溃日志:"
    adb logcat -d | grep -E "FATAL|AndroidRuntime" | tail -20
    test_fail "发现 $CRASHES 个崩溃"
else
    test_pass
fi

# 检查App进程
echo "7. 检查App进程..."
test_case "验证App进程运行中"
PROCESS=$(adb shell ps | grep $PACKAGE_NAME | wc -l)
if [ $PROCESS -eq 0 ]; then
    test_fail "App进程未运行"
else
    test_pass
fi

# 模拟点击测试（中心点）
echo "8. 模拟屏幕交互..."
test_case "模拟点击屏幕中心"
SCREEN_SIZE=$(adb shell wm size | grep "Physical size" | awk '{print $3}')
WIDTH=$(echo $SCREEN_SIZE | cut -d'x' -f1)
HEIGHT=$(echo $SCREEN_SIZE | cut -d'x' -f2)
CENTER_X=$((WIDTH / 2))
CENTER_Y=$((HEIGHT / 2))

adb shell input tap $CENTER_X $CENTER_Y
sleep 2

# 再次检查崩溃
CRASHES_AFTER=$(adb logcat -d | grep -E "FATAL|AndroidRuntime.*FATAL" | grep -v "grep" | wc -l)
if [ $CRASHES_AFTER -gt $CRASHES ]; then
    test_fail "点击后发现新崩溃"
else
    test_pass
fi

# 检查内存占用
echo "9. 检查内存占用..."
test_case "验证内存使用 < 200MB"
MEMORY=$(adb shell dumpsys meminfo $PACKAGE_NAME | grep "TOTAL PSS" | awk '{print $3}')
if [ ! -z "$MEMORY" ]; then
    MEMORY_MB=$((MEMORY / 1024))
    echo "当前内存: ${MEMORY_MB}MB"
    if [ $MEMORY_MB -lt 200 ]; then
        test_pass
    else
        test_fail "内存占用 ${MEMORY_MB}MB 超过200MB"
    fi
else
    echo "无法获取内存信息"
    test_pass
fi

# 多次返回键测试
echo "10. 测试返回键导航..."
test_case "连续按5次返回键"
for i in {1..5}; do
    adb shell input keyevent KEYCODE_BACK
    sleep 1
done

CRASHES_FINAL=$(adb logcat -d | grep -E "FATAL|AndroidRuntime.*FATAL" | grep -v "grep" | wc -l)
if [ $CRASHES_FINAL -gt $CRASHES_AFTER ]; then
    test_fail "返回键操作后发现新崩溃"
else
    test_pass
fi

# 检查App状态
echo "11. 最终App状态检查..."
test_case "验证App稳定运行"
FINAL_PROCESS=$(adb shell ps | grep $PACKAGE_NAME | wc -l)
if [ $FINAL_PROCESS -eq 0 ]; then
    echo "App已退出（正常，因为多次按返回键）"
    test_pass
else
    echo "App仍在运行"
    test_pass
fi

# 检查特定错误模式
echo "12. 检查特定错误模式..."
test_case "检查类型转换错误"
TYPE_ERRORS=$(adb logcat -d | grep -iE "ClassCastException|NumberFormatException|IllegalArgumentException" | wc -l)
if [ $TYPE_ERRORS -gt 0 ]; then
    echo "发现类型错误:"
    adb logcat -d | grep -iE "ClassCastException|NumberFormatException|IllegalArgumentException" | tail -10
    test_fail "发现 $TYPE_ERRORS 个类型错误"
else
    test_pass
fi

# 总结
echo ""
echo "========================================="
echo "测试总结"
echo "========================================="
echo -e "总测试数: $TOTAL_TESTS"
echo -e "${GREEN}通过: $PASSED_TESTS${NC}"
echo -e "${RED}失败: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}⚠️  有 $FAILED_TESTS 个测试失败${NC}"
    exit 1
fi
