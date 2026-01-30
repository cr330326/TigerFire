#!/bin/bash

# 徽章收集系统真机测试脚本
# 作者：AI Assistant
# 日期：2026年1月30日

set -e

PACKAGE_NAME="com.cryallen.tigerfire"
DB_NAME="TigerFire.db"

echo "=========================================="
echo "  TigerFire 徽章收集系统真机测试"
echo "=========================================="
echo ""

# 检查设备连接
echo "📱 检查设备连接..."
DEVICE=$(adb devices | grep -w "device" | head -1 | awk '{print $1}')
if [ -z "$DEVICE" ]; then
    echo "❌ 未找到连接的设备"
    exit 1
fi
echo "✅ 设备已连接: $DEVICE"
echo ""

# 检查应用是否安装
echo "📦 检查应用安装状态..."
if adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "✅ 应用已安装: $PACKAGE_NAME"
else
    echo "❌ 应用未安装"
    exit 1
fi
echo ""

# 提供测试选项
echo "请选择操作:"
echo "1) 清空数据库（重新开始测试）"
echo "2) 查看当前徽章数据"
echo "3) 查看游戏进度"
echo "4) 实时监控日志"
echo "5) 导出数据库"
echo "6) 退出"
echo ""
read -p "请输入选项 (1-6): " choice

case $choice in
    1)
        echo ""
        echo "🗑️  清空数据库..."
        adb shell run-as $PACKAGE_NAME rm -f databases/$DB_NAME
        adb shell run-as $PACKAGE_NAME rm -f databases/${DB_NAME}-journal
        echo "✅ 数据库已清空"
        echo "📝 请重启应用以重新初始化数据库"
        ;;

    2)
        echo ""
        echo "📋 查询徽章数据..."
        adb shell run-as $PACKAGE_NAME sqlite3 databases/$DB_NAME "SELECT COUNT(*) as total FROM Badge;" 2>&1 | grep -v "^$" | tail -1 | while read count; do
            echo "徽章总数: $count"
        done

        echo ""
        echo "徽章详情:"
        adb shell run-as $PACKAGE_NAME sqlite3 databases/$DB_NAME "SELECT id, scene, baseType, variant FROM Badge ORDER BY earnedAt;" 2>&1 | grep -v "^$"
        ;;

    3)
        echo ""
        echo "📊 查询游戏进度..."
        echo "消防站已完成项:"
        adb shell run-as $PACKAGE_NAME sqlite3 databases/$DB_NAME "SELECT fireStationCompletedItems FROM GameProgress;" 2>&1 | grep -v "^$"

        echo ""
        echo "森林已救援小羊数:"
        adb shell run-as $PACKAGE_NAME sqlite3 databases/$DB_NAME "SELECT forestRescuedSheep FROM GameProgress;" 2>&1 | grep -v "^$"

        echo ""
        echo "场景状态:"
        adb shell run-as $PACKAGE_NAME sqlite3 databases/$DB_NAME "SELECT sceneStatuses FROM GameProgress;" 2>&1 | grep -v "^$"
        ;;

    4)
        echo ""
        echo "📡 实时监控日志 (Ctrl+C 退出)..."
        echo "----------------------------------------"
        adb logcat | grep --line-buffered -E "System.out.*DEBUG|saveProgressWithBadge|handleVideoCompleted|FireStationViewModel|SchoolViewModel|ForestViewModel"
        ;;

    5)
        echo ""
        echo "💾 导出数据库..."
        EXPORT_PATH="./exported_${DB_NAME}"
        adb shell run-as $PACKAGE_NAME cat databases/$DB_NAME > "$EXPORT_PATH"
        echo "✅ 数据库已导出到: $EXPORT_PATH"
        echo ""
        echo "查看导出的数据:"
        sqlite3 "$EXPORT_PATH" "SELECT * FROM Badge;"
        ;;

    6)
        echo "👋 退出"
        exit 0
        ;;

    *)
        echo "❌ 无效选项"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "  测试完成"
echo "=========================================="
