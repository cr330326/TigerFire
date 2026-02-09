#!/bin/bash
#
# UI 优化版本切换验证脚本
#
# 用法: ./scripts/verify_ui_switch.sh [check|switch-original|switch-optimized]
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_GRADLE="$PROJECT_ROOT/composeApp/build.gradle.kts"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查当前配置
check_current_config() {
    print_info "检查当前 UI 配置..."

    if [ ! -f "$BUILD_GRADLE" ]; then
        print_error "找不到 build.gradle.kts: $BUILD_GRADLE"
        exit 1
    fi

    # 检查 BuildConfig 字段
    if grep -q "IS_USE_OPTIMIZED_UI" "$BUILD_GRADLE"; then
        print_success "找到 IS_USE_OPTIMIZED_UI 配置"

        # 提取当前值
        local value=$(grep -o 'IS_USE_OPTIMIZED_UI.*=.*"\(true\|false\)"' "$BUILD_GRADLE" | grep -o '"\(true\|false\)"' | tr -d '"')

        if [ "$value" = "true" ]; then
            print_info "当前配置: 使用优化版本 (Optimized UI)"
        else
            print_info "当前配置: 使用原始版本 (Original UI)"
        fi
    else
        print_warning "未找到 IS_USE_OPTIMIZED_UI 配置"
        print_info "请运行以下命令添加配置:"
        print_info "  ./gradlew :composeApp:generateDebugBuildConfig"
    fi

    # 检查优化文件是否存在
    print_info "检查优化文件状态..."

    local files=(
        "$PROJECT_ROOT/composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/collection/CollectionScreenOptimized.kt"
        "$PROJECT_ROOT/composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/welcome/WelcomeScreenOptimized.kt"
        "$PROJECT_ROOT/composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/map/MapScreenOptimized.kt"
    )

    for file in "${files[@]}"; do
        local filename=$(basename "$file")
        if [ -f "$file" ]; then
            local lines=$(wc -l < "$file" | tr -d ' ')
            print_success "$filename 存在 ($lines 行)"
        else
            print_warning "$filename 不存在"
        fi
    done

    # 检查 Selector 文件
    print_info "检查 Selector 文件..."

    local selectors=(
        "$PROJECT_ROOT/composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/collection/CollectionScreenSelector.kt"
        "$PROJECT_ROOT/composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/welcome/WelcomeScreenSelector.kt"
        "$PROJECT_ROOT/composeApp/src/androidMain/kotlin/com/cryallen/tigerfire/ui/map/MapScreenSelector.kt"
    )

    for file in "${selectors[@]}"; do
        local filename=$(basename "$file")
        if [ -f "$file" ]; then
            print_success "$filename 已创建"
        else
            print_warning "$filename 未创建"
        fi
    done
}

# 切换到原始版本
switch_to_original() {
    print_info "切换到原始版本..."

    if [ ! -f "$BUILD_GRADLE" ]; then
        print_error "找不到 build.gradle.kts"
        exit 1
    fi

    # 使用 sed 替换配置值
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' 's/IS_USE_OPTIMIZED_UI.*=.*"true"/IS_USE_OPTIMIZED_UI", "false"/' "$BUILD_GRADLE"
    else
        # Linux
        sed -i 's/IS_USE_OPTIMIZED_UI.*=.*"true"/IS_USE_OPTIMIZED_UI", "false"/' "$BUILD_GRADLE"
    fi

    print_success "已切换到原始版本"
    print_info "请重新构建项目: ./gradlew :composeApp:assembleDebug"
}

# 切换到优化版本
switch_to_optimized() {
    print_info "切换到优化版本..."

    if [ ! -f "$BUILD_GRADLE" ]; then
        print_error "找不到 build.gradle.kts"
        exit 1
    fi

    # 使用 sed 替换配置值
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' 's/IS_USE_OPTIMIZED_UI.*=.*"false"/IS_USE_OPTIMIZED_UI", "true"/' "$BUILD_GRADLE"
    else
        # Linux
        sed -i 's/IS_USE_OPTIMIZED_UI.*=.*"false"/IS_USE_OPTIMIZED_UI", "true"/' "$BUILD_GRADLE"
    fi

    print_success "已切换到优化版本"
    print_info "请重新构建项目: ./gradlew :composeApp:assembleDebug"
}

# 显示帮助信息
show_help() {
    cat << EOF
UI 优化版本切换验证脚本

用法: $0 [命令]

命令:
  check              检查当前配置和文件状态 (默认)
  switch-original  切换到原始 UI 版本
  switch-optimized 切换到优化 UI 版本
  help             显示此帮助信息

示例:
  $0 check                    # 检查当前配置
  $0 switch-original          # 切换到原始版本
  $0 switch-optimized         # 切换到优化版本

注意:
  - 切换后需要重新构建项目
  - 优化版本文件需要完整实现才能正常使用

EOF
}

# 主函数
main() {
    case "${1:-check}" in
        check)
            check_current_config
            ;;
        switch-original)
            switch_to_original
            ;;
        switch-optimized)
            switch_to_optimized
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
