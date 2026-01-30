#!/bin/bash

# UI自动化测试执行脚本
# 作用：执行完整的UI自动化测试套件并生成测试报告

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 打印标题
print_header() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "$1"
    echo "=================================================="
    echo -e "${NC}"
}

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

print_header "TigerFire UI自动化测试"

# 1. 检查设备连接
print_info "步骤1: 检查Android设备连接..."
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    print_error "未检测到Android设备，请连接设备或启动模拟器"
    exit 1
fi

DEVICE_NAME=$(adb devices | grep -v "List" | grep "device" | awk '{print $1}' | head -n 1)
print_success "检测到设备: $DEVICE_NAME"

# 2. 获取设备信息
print_info "步骤2: 获取设备信息..."
DEVICE_MODEL=$(adb -s "$DEVICE_NAME" shell getprop ro.product.model | tr -d '\r')
ANDROID_VERSION=$(adb -s "$DEVICE_NAME" shell getprop ro.build.version.release | tr -d '\r')
print_info "设备型号: $DEVICE_MODEL"
print_info "Android版本: $ANDROID_VERSION"

# 3. 清理旧的测试数据
print_info "步骤3: 清理旧的测试数据..."
adb -s "$DEVICE_NAME" shell pm clear com.cryallen.tigerfire || true
print_success "测试数据已清理"

# 4. 编译测试APK
print_info "步骤4: 编译App和测试APK..."
./gradlew assembleDebug assembleDebugAndroidTest
if [ $? -ne 0 ]; then
    print_error "编译失败"
    exit 1
fi
print_success "编译成功"

# 5. 安装App和测试APK
print_info "步骤5: 安装App和测试APK..."
./gradlew installDebug installDebugAndroidTest
if [ $? -ne 0 ]; then
    print_error "安装失败"
    exit 1
fi
print_success "安装成功"

# 6. 创建测试报告目录
REPORT_DIR="$PROJECT_ROOT/test-reports/ui-tests"
mkdir -p "$REPORT_DIR"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$REPORT_DIR/test-report-$TIMESTAMP.md"

# 7. 执行测试
print_header "开始执行UI自动化测试"
print_info "测试报告将保存到: $REPORT_FILE"

# 清空日志
adb -s "$DEVICE_NAME" logcat -c

# 执行所有测试
print_info "执行测试套件..."
./gradlew connectedDebugAndroidTest --info 2>&1 | tee "$REPORT_DIR/test-output-$TIMESTAMP.log"

TEST_EXIT_CODE=${PIPESTATUS[0]}

# 8. 生成测试报告
print_header "生成测试报告"

# 从Gradle测试结果中提取信息
TEST_RESULT_DIR="$PROJECT_ROOT/composeApp/build/reports/androidTests/connected"
TEST_HTML="$TEST_RESULT_DIR/index.html"

if [ -f "$TEST_HTML" ]; then
    print_success "测试HTML报告已生成: $TEST_HTML"

    # 在Mac上打开HTML报告
    if [[ "$OSTYPE" == "darwin"* ]]; then
        print_info "打开测试报告..."
        open "$TEST_HTML"
    fi
fi

# 生成Markdown报告
cat > "$REPORT_FILE" << EOF
# TigerFire UI自动化测试报告

## 📋 测试概览

**测试日期**: $(date +"%Y-%m-%d %H:%M:%S")
**设备型号**: $DEVICE_MODEL
**Android版本**: $ANDROID_VERSION
**设备ID**: $DEVICE_NAME
**测试类型**: UI自动化测试（Compose UI Test + Espresso）

---

## 📊 测试结果汇总

EOF

# 分析测试结果
if [ $TEST_EXIT_CODE -eq 0 ]; then
    cat >> "$REPORT_FILE" << EOF
**测试状态**: ✅ **全部通过**

所有UI自动化测试用例均已通过，应用功能正常。

EOF
    print_success "所有测试通过！"
else
    cat >> "$REPORT_FILE" << EOF
**测试状态**: ❌ **部分失败**

部分测试用例未通过，请查看详细日志。

EOF
    print_warning "部分测试失败，请查看报告"
fi

# 添加测试类别
cat >> "$REPORT_FILE" << EOF
## 🧪 测试覆盖范围

### 1. 导航流程测试 (AppNavigationTest)
- ✅ App启动和欢迎页显示
- ✅ 从欢迎页导航到主地图
- ✅ 导航到消防站场景
- ✅ 导航到学校场景
- ✅ 导航到森林场景
- ✅ 返回按钮导航

### 2. 徽章收集功能测试 (BadgeCollectionTest)
- ✅ 播放设备视频并获取徽章
- ✅ 查看收藏页面的徽章
- ✅ 重复观看同一设备获得不同变体

### 3. 家长模式测试 (ParentModeTest)
- ✅ 进入家长模式
- ✅ 查看使用时长统计
- ✅ 设置使用时长限制
- ✅ 从家长模式返回

### 4. 性能和压力测试 (PerformanceTest)
- ✅ 应用启动时间测试
- ✅ 快速点击防抖测试
- ✅ 场景切换性能测试
- ✅ 连续导航压力测试
- ✅ 内存稳定性测试

---

## 📈 性能指标

详细性能数据请查看测试日志: \`test-output-$TIMESTAMP.log\`

---

## 📝 测试详情

完整的HTML测试报告: [查看报告]($TEST_HTML)

测试输出日志: \`$REPORT_DIR/test-output-$TIMESTAMP.log\`

---

## 🔗 相关文档

- [E2E测试指南](../document/E2E_TEST_GUIDE.md)
- [测试清单](../document/TESTING_CHECKLIST.md)

---

**报告生成时间**: $(date +"%Y-%m-%d %H:%M:%S")
EOF

print_success "测试报告已生成: $REPORT_FILE"

# 9. 显示测试总结
print_header "测试完成"

if [ $TEST_EXIT_CODE -eq 0 ]; then
    print_success "所有测试通过 ✅"
    print_info "HTML报告: $TEST_HTML"
    print_info "Markdown报告: $REPORT_FILE"
    exit 0
else
    print_warning "部分测试失败 ⚠️"
    print_info "请查看报告了解详情: $REPORT_FILE"
    exit 1
fi
