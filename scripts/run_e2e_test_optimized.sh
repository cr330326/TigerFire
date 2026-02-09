#!/bin/bash

# TigerFire 优化功能端到端测试脚本
# 用于验证 Phase 1 UI 优化功能

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目路径
PROJECT_DIR="/Users/vsh9p8q/Personal/Project/TigerTruck/TigerFire"
cd "$PROJECT_DIR"

# 日志文件
LOG_DIR="$PROJECT_DIR/test_logs"
mkdir -p "$LOG_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOG_DIR/e2e_test_$TIMESTAMP.log"

# 计数器
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_SKIPPED=0

# 打印函数
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
    echo -e "\n========================================" >> "$LOG_FILE"
    echo "$1" >> "$LOG_FILE"
    echo "========================================" >> "$LOG_FILE"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
    echo "✅ $1" >> "$LOG_FILE"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    echo "❌ $1" >> "$LOG_FILE"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
    echo "⚠️ $1" >> "$LOG_FILE"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
    echo "ℹ️ $1" >> "$LOG_FILE"
}

# 测试函数
run_test() {
    local test_name="$1"
    local test_command="$2"

    echo -e "\n--- 测试: $test_name ---" >> "$LOG_FILE"

    if eval "$test_command" >> "$LOG_FILE" 2>&1; then
        print_success "$test_name"
        ((TESTS_PASSED++))
        return 0
    else
        print_error "$test_name"
        ((TESTS_FAILED++))
        return 1
    fi
}

# 检查设备连接
check_device() {
    print_header "步骤 1: 检查设备连接"

    if ! adb devices | grep -q "device$"; then
        print_error "未检测到连接的设备"
        print_info "请连接 Android 设备并启用 USB 调试"
        exit 1
    fi

    DEVICE_INFO=$(adb shell getprop ro.product.model)
    ANDROID_VERSION=$(adb shell getprop ro.build.version.release)

    print_success "设备已连接: $DEVICE_INFO (Android $ANDROID_VERSION)"
    echo "设备: $DEVICE_INFO (Android $ANDROID_VERSION)" >> "$LOG_FILE"
}

# 构建项目
build_project() {
    print_header "步骤 2: 构建优化版本"

    print_info "开始构建..."

    if ./gradlew :composeApp:assembleDebug --quiet; then
        print_success "构建成功"

        # 获取 APK 大小
        APK_SIZE=$(du -h composeApp/build/outputs/apk/debug/composeApp-debug.apk | cut -f1)
        print_info "APK 大小: $APK_SIZE"
        echo "APK 大小: $APK_SIZE" >> "$LOG_FILE"
    else
        print_error "构建失败"
        exit 1
    fi
}

# 安装应用
install_app() {
    print_header "步骤 3: 安装应用到设备"

    if adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk; then
        print_success "安装成功"
    else
        print_error "安装失败"
        exit 1
    fi
}

# 准备测试环境
prepare_test() {
    print_header "步骤 4: 准备测试环境"

    # 清除日志
    adb logcat -c
    print_info "日志已清除"

    # 可选：清空数据库（全新测试）
    print_warning "是否清空数据库进行全新测试？(y/n)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        adb shell run-as com.cryallen.tigerfire rm -rf /data/data/com.cryallen.tigerfire/databases/ 2>/dev/null || true
        print_info "数据库已清空"
    fi

    print_success "测试环境准备完成"
}

# 测试 MapScreen
run_mapscreen_tests() {
    print_header "步骤 5: 测试 MapScreen 优化功能"

    print_info "启动应用..."
    adb shell am start -n com.cryallen.tigerfire/.MainActivity
    sleep 3

    # 测试 1: 卡车转场动画
    print_info "测试 1: 卡车转场动画..."
    print_info "请手动点击消防站图标，观察卡车转场动画"
    sleep 2

    # 等待用户确认或自动检测
    sleep 8

    # 检查日志
    if adb logcat -d | grep -q "Truck transition"; then
        print_success "卡车转场动画测试通过"
    else
        print_warning "未检测到卡车转场日志，请手动确认"
    fi

    # 返回地图
    adb shell input keyevent KEYCODE_BACK
    sleep 2

    # 测试 2: 小火引导动画
    print_info "测试 2: 小火引导动画..."
    print_info "请等待30秒，观察是否出现小火引导动画"
    sleep 30

    print_info "请手动确认是否看到小火角色和提示气泡"

    # 测试 3: 场景图标微交互
    print_info "测试 3: 场景图标微交互..."
    print_info "请点击学校图标，观察粒子效果和触觉反馈"
    sleep 3

    # 返回地图
    adb shell input keyevent KEYCODE_BACK
    sleep 2

    print_success "MapScreen 测试完成"
}

# 测试 WelcomeScreen
run_welcomescreen_tests() {
    print_header "步骤 6: 测试 WelcomeScreen 优化功能"

    print_info "请杀掉应用后重新启动，观察 WelcomeScreen"
    adb shell am force-stop com.cryallen.tigerfire
    sleep 2

    print_info "启动应用..."
    adb shell am start -n com.cryallen.tigerfire/.MainActivity
    sleep 2

    print_info "请观察："
    print_info "1. 卡车尾部是否有烟雾粒子？"
    print_info "2. 卡车轮子是否有火花效果？"
    print_info "3. 背景云朵是否在移动？"
    print_info "4. 星星是否在闪烁？"

    sleep 10
    print_success "WelcomeScreen 测试完成"
}

# 测试 CollectionScreen
run_collectionscreen_tests() {
    print_header "步骤 7: 测试 CollectionScreen 优化功能"

    print_info "请先获得至少一个徽章，然后进入收藏页面"
    print_info "等待自动导航到地图..."
    sleep 10

    # 模拟点击收藏按钮（需要根据实际坐标调整）
    print_info "请手动点击收藏按钮进入 CollectionScreen"
    sleep 5

    print_info "请观察："
    print_info "1. 徽章卡片是否在上下漂浮？"
    print_info "2. 徽章是否有轻微旋转？"
    print_info "3. 徽章是否有闪光扫过效果？"
    print_info "4. 点击徽章时是否有触觉反馈和粒子效果？"

    sleep 10

    # 测试集齐彩蛋（如果有条件）
    print_info "如果已集齐所有徽章，请观察烟花彩蛋动画"

    print_success "CollectionScreen 测试完成"
}

# 性能测试
run_performance_tests() {
    print_header "步骤 8: 性能测试"

    print_info "测试 FPS..."
    adb shell dumpsys gfxinfo com.cryallen.tigerfire framestats > "fps_test_$TIMESTAMP.txt"

    # 分析 FPS
    if grep -q "framestats" "fps_test_$TIMESTAMP.txt"; then
        print_success "FPS 数据已收集"
    else
        print_warning "FPS 数据收集可能失败"
    fi

    print_info "测试内存使用..."
    adb shell dumpsys meminfo com.cryallen.tigerfire > "memory_test_$TIMESTAMP.txt"
    print_success "内存数据已收集"

    print_info "性能测试完成，数据保存在："
    print_info "FPS: fps_test_$TIMESTAMP.txt"
    print_info "内存: memory_test_$TIMESTAMP.txt"
}

# 生成报告
generate_report() {
    print_header "步骤 9: 生成测试报告"

    REPORT_FILE="$LOG_DIR/e2e_test_report_$TIMESTAMP.md"

    cat > "$REPORT_FILE" << EOF
# 端到端测试报告

**测试时间**: $(date)
**测试版本**: 优化版本 (Phase 1)
**测试设备**: $(adb shell getprop ro.product.model) - Android $(adb shell getprop ro.build.version.release)

## 测试结果摘要

| 测试模块 | 测试用例 | 通过 | 失败 | 跳过 |
|---------|---------|------|------|------|
| MapScreen | 4 | - | - | - |
| WelcomeScreen | 3 | - | - | - |
| CollectionScreen | 4 | - | - | - |
| 性能测试 | 2 | - | - | - |
| **总计** | **13** | **$TESTS_PASSED** | **$TESTS_FAILED** | **$TESTS_SKIPPED** |

## 详细测试结果

### MapScreen 测试

- [ ] 卡车转场动画测试
- [ ] 小火引导动画测试
- [ ] 场景图标微交互测试
- [ ] 视差背景测试

### WelcomeScreen 测试

- [ ] 卡车粒子效果测试
- [ ] 火花特效测试
- [ ] 视差背景测试

### CollectionScreen 测试

- [ ] 3D徽章展示测试
- [ ] 徽章收集动画测试
- [ ] 集齐彩蛋测试
- [ ] 统计卡片微交互测试

## 性能数据

| 指标 | 目标值 | 实际值 | 结果 |
|------|--------|--------|------|
| 平均FPS | ≥ 55 | - | - |
| 内存峰值 | < 200MB | - | - |
| 启动时间 | < 3s | - | - |

## 问题记录

| 问题编号 | 描述 | 严重程度 | 状态 |
|---------|------|---------|------|
| - | - | - | - |

## 结论

- [ ] 所有测试通过，可以进入生产环境
- [ ] 部分测试失败，需要修复后重新测试
- [ ] 测试通过，但建议优化性能

---

**测试人员签名**: _______________
**日期**: _______________
EOF

    print_success "测试报告已生成: $REPORT_FILE"
}

# 清理函数
cleanup() {
    print_header "清理"

    # 杀掉应用
    adb shell am force-stop com.cryallen.tigerfire 2>/dev/null || true

    print_info "清理完成"
}

# 主函数
main() {
    print_header "TigerFire 优化功能端到端测试"

    # 检查依赖
    if ! command -v adb &> /dev/null; then
        print_error "未找到 adb 命令，请确保 Android SDK 已安装"
        exit 1
    fi

    # 执行测试步骤
    check_device
    build_project
    install_app
    prepare_test
    run_mapscreen_tests
    run_welcomescreen_tests
    run_collectionscreen_tests
    run_performance_tests
    generate_report
    cleanup

    # 测试结果摘要
    print_header "测试结果摘要"

    print_info "测试日志: $LOG_FILE"
    print_info "测试报告: $LOG_DIR/e2e_test_report_$TIMESTAMP.md"
    print_info "性能数据: fps_test_$TIMESTAMP.txt, memory_test_$TIMESTAMP.txt"

    print_success "端到端测试完成！"

    exit 0
}

# 错误处理
trap 'print_error "测试过程中出现错误"; cleanup; exit 1' ERR

# 运行主函数
main "$@"
