#!/bin/bash

echo "=== 数据库数据验证 ==="
echo ""

# 获取数据库文件路径
DB_PATH="/data/data/com.cryallen.tigerfire/databases/tigerfire.db"

echo "1️⃣ 检查ParentSettings表数据..."
echo ""
adb shell "su -c 'sqlite3 $DB_PATH \"SELECT * FROM ParentSettings;\"'" 2>/dev/null || \
adb shell "run-as com.cryallen.tigerfire sqlite3 databases/tigerfire.db 'SELECT * FROM ParentSettings;'" 2>/dev/null || \
echo "⚠️  无法访问数据库（需要root权限或调试版本）"

echo ""
echo "2️⃣ 检查dailyUsageStats数据..."
echo ""
adb shell "su -c 'sqlite3 $DB_PATH \"SELECT dailyUsageStats FROM ParentSettings;\"'" 2>/dev/null || \
adb shell "run-as com.cryallen.tigerfire sqlite3 databases/tigerfire.db 'SELECT dailyUsageStats FROM ParentSettings;'" 2>/dev/null || \
echo "⚠️  无法访问数据库"

echo ""
echo "3️⃣ 检查GameProgress表数据..."
echo ""
adb shell "su -c 'sqlite3 $DB_PATH \"SELECT totalPlayTime FROM GameProgress;\"'" 2>/dev/null || \
adb shell "run-as com.cryallen.tigerfire sqlite3 databases/tigerfire.db 'SELECT totalPlayTime FROM GameProgress;'" 2>/dev/null || \
echo "⚠️  无法访问数据库"

echo ""
echo "4️⃣ 检查Badge表数据..."
echo ""
adb shell "su -c 'sqlite3 $DB_PATH \"SELECT COUNT(*) FROM Badge;\"'" 2>/dev/null || \
adb shell "run-as com.cryallen.tigerfire sqlite3 databases/tigerfire.db 'SELECT COUNT(*) FROM Badge;'" 2>/dev/null || \
echo "⚠️  无法访问数据库"

echo ""
echo "=== 数据验证完成 ==="
