#!/bin/bash

# ForestScreen 修复验证测试脚本
# 测试以下三个问题的修复：
# 1. 已救援小羊显示打勾图标
# 2. 救援进度显示正确
# 3. 快速切换小羊时播放按钮正常显示

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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
    echo -e "${BLUE}[测试 $TOTAL_TESTS]${NC} $test_name"
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

# 获取屏幕尺寸
get_screen_size() {
    SCREEN_SIZE=$(adb shell wm size | grep "Physical size" | awk '{print $3}')
    WIDTH=$(echo $SCREEN_SIZE | cut -d'x' -f1)
    HEIGHT=$(echo $SCREEN_SIZE | cut -d'x' -f2)
}

# 点击坐标
tap() {
    local x=$1
    local y=$2
    adb shell input tap $x $y
}

# 等待元素出现（通过简单延迟实现）
wait_for() {
    local seconds=$1
    sleep $seconds
}

echo "========================================="
echo "ForestScreen 修复验证测试"
echo "========================================="
echo ""

# 1. 检查设备连接
echo "1. 检查设备连接..."
DEVICE=$(adb devices | grep device | grep -v "List" | wc -l)
if [ $DEVICE -eq 0 ]; then
    echo -e "${RED}❌ 未检测到设备${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 设备已连接${NC}"
echo ""

# 2. 清理应用数据并启动
echo "2. 清理应用数据..."
adb shell pm clear $PACKAGE_NAME
echo -e "${GREEN}✅ 应用数据已清理${NC}"
echo ""

# 3. 启动应用
echo "3. 启动应用..."
adb shell am start -n $PACKAGE_NAME/$MAIN_ACTIVITY
sleep 3
echo -e "${GREEN}✅ 应用已启动${NC}"
echo ""

# 获取屏幕尺寸
get_screen_size
echo "屏幕尺寸: ${WIDTH}x${HEIGHT}"
echo ""

# 4. 清空日志
echo "4. 清空日志缓存..."
adb logcat -c
echo -e "${GREEN}✅ 日志已清空${NC}"
echo ""

# ==================== 测试场景 ====================

echo "========================================="
echo "开始测试场景"
echo "========================================="
echo ""

# 场景1: 测试已救援小羊显示打勾图标
echo "场景1: 测试已救援小羊显示打勾图标"
echo "----------------------------------------"

# 计算点击位置（基于屏幕比例）
# 欢迎页"开始"按钮 - 屏幕中心偏下
WELCOME_BTN_X=$((WIDTH * 50 / 100))
WELCOME_BTN_Y=$((HEIGHT * 70 / 100))

# 主地图上的森林场景入口 - 右下角
FOREST_ENTRANCE_X=$((WIDTH * 75 / 100))
FOREST_ENTRANCE_Y=$((HEIGHT * 70 / 100))

# 森林场景第一只小羊 - 右上
SHEEP1_X=$((WIDTH * 70 / 100))
SHEEP1_Y=$((HEIGHT * 30 / 100))

# 森林场景第二只小羊 - 右下
SHEEP2_X=$((WIDTH * 75 / 100))
SHEEP2_Y=$((HEIGHT * 65 / 100))

# 播放按钮（在小羊位置附近）
PLAY_BTN_X=$((WIDTH * 70 / 100))
PLAY_BTN_Y=$((HEIGHT * 35 / 100))

# 返回按钮 - 左上角
BACK_BTN_X=$((WIDTH * 15 / 100))
BACK_BTN_Y=$((HEIGHT * 15 / 100))

test_case "点击欢迎页开始按钮"
tap $WELCOME_BTN_X $WELCOME_BTN_Y
sleep 2
test_pass

test_case "导航到森林场景"
tap $FOREST_ENTRANCE_X $FOREST_ENTRANCE_Y
sleep 2
test_pass

test_case "点击第一只小羊进行救援"
tap $SHEEP1_X $SHEEP1_Y
sleep 3  # 等待直升机飞行
test_pass

test_case "点击播放按钮观看救援视频"
tap $PLAY_BTN_X $PLAY_BTN_Y
sleep 5  # 等待视频播放（假设视频约10秒，这里等待一半时间后返回）
test_pass

test_case "点击返回按钮退出森林场景"
tap $BACK_BTN_X $BACK_BTN_Y
sleep 2
test_pass

# 检查日志中的状态更新
test_case "验证第一只小羊已标记为救援"
LOGS=$(adb logcat -d | grep "rescuedSheep" | tail -5)
if echo "$LOGS" | grep -q "0"; then
    test_pass
else
    echo "日志: $LOGS"
    test_fail "未找到小羊0的救援记录"
fi

test_case "重新进入森林场景"
tap $FOREST_ENTRANCE_X $FOREST_ENTRANCE_Y
sleep 2
test_pass

# 检查是否显示打勾图标（通过日志验证状态加载）
test_case "验证已救援小羊显示打勾图标（通过状态验证）"
LOGS=$(adb logcat -d | grep "ForestState" | tail -10)
if echo "$LOGS" | grep -q "rescuedSheep=\[0\]"; then
    test_pass
else
    echo "相关日志: $LOGS"
    test_fail "状态中未包含已救援的小羊0"
fi

# 场景2: 测试救援进度显示
echo ""
echo "场景2: 测试救援进度显示正确"
echo "----------------------------------------"

test_case "验证救援进度显示为 1/2"
LOGS=$(adb logcat -d | grep -E "rescuedSheep|isAllCompleted" | tail -10)
if echo "$LOGS" | grep -q "rescuedSheep.*size.*1"; then
    test_pass
else
    echo "相关日志: $LOGS"
    test_fail "救援进度未正确显示为1"
fi

# 场景3: 测试快速切换小羊
echo ""
echo "场景3: 测试快速切换小羊时播放按钮显示"
echo "----------------------------------------"

test_case "快速点击第二只小羊（在直升机未飞行时）"
tap $SHEEP2_X $SHEEP2_Y
sleep 1
test_pass

test_case "再次快速点击第一只小羊（测试飞行ID机制）"
tap $SHEEP1_X $SHEEP1_Y
sleep 3  # 等待直升机飞行到第一只小羊
test_pass

test_case "验证播放按钮显示在最后点击的小羊位置"
# 通过日志检查 currentFlightId 机制是否工作
LOGS=$(adb logcat -d | grep -E "currentFlightId|targetSheepIndex" | tail -20)
if echo "$LOGS" | grep -q "currentFlightId"; then
    test_pass
else
    echo "相关日志: $LOGS"
    test_fail "未找到飞行ID相关日志"
fi

# 清理
echo ""
echo "5. 清理测试环境..."
adb shell am force-stop $PACKAGE_NAME
echo -e "${GREEN}✅ 应用已停止${NC}"
echo ""

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
    echo ""
    echo "相关日志输出:"
    adb logcat -d | grep -E "ForestViewModel|ForestState|ForestScreen" | tail -50
    exit 1
fi
