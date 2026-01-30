#!/bin/bash

# 徽章数据库验证脚本
# 用于检查数据库中的徽章数据是否正确

echo "======================================"
echo "徽章数据库验证工具"
echo "======================================"
echo ""

# 设置颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}正在连接到设备...${NC}"
adb devices | grep -q "device$" || {
    echo -e "${RED}错误: 未检测到设备${NC}"
    exit 1
}

echo -e "${GREEN}✓ 设备已连接${NC}"
echo ""

# 获取徽章数据
echo -e "${BLUE}正在查询数据库...${NC}"
echo ""

# 查询所有徽章
echo -e "${YELLOW}=== 所有徽章 ===${NC}"
adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT id, scene, baseType, variant, datetime(earnedAt/1000, \"unixepoch\", \"localtime\") as earnedTime FROM Badge ORDER BY earnedAt;'" 2>/dev/null || echo "数据库为空或应用未安装"

echo ""
echo -e "${YELLOW}=== 徽章统计 ===${NC}"

# 统计总数
TOTAL=$(adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT COUNT(*) FROM Badge;'" 2>/dev/null | tr -d '\r')
echo -e "总徽章数: ${GREEN}${TOTAL}${NC}"

# 统计不同类型的徽章
UNIQUE=$(adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT COUNT(DISTINCT baseType) FROM Badge;'" 2>/dev/null | tr -d '\r')
echo -e "不同类型数: ${GREEN}${UNIQUE}${NC}"

echo ""
echo -e "${YELLOW}=== 按场景分组 ===${NC}"

# 消防站徽章
FIRE_STATION=$(adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT COUNT(*) FROM Badge WHERE scene=\"FIRE_STATION\";'" 2>/dev/null | tr -d '\r')
echo -e "消防站徽章: ${GREEN}${FIRE_STATION}${NC}"

# 学校徽章
SCHOOL=$(adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT COUNT(*) FROM Badge WHERE scene=\"SCHOOL\";'" 2>/dev/null | tr -d '\r')
echo -e "学校徽章: ${GREEN}${SCHOOL}${NC}"

# 森林徽章
FOREST=$(adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT COUNT(*) FROM Badge WHERE scene=\"FOREST\";'" 2>/dev/null | tr -d '\r')
echo -e "森林徽章: ${GREEN}${FOREST}${NC}"

echo ""
echo -e "${YELLOW}=== 按类型分组 ===${NC}"
adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT baseType, COUNT(*) as count FROM Badge GROUP BY baseType ORDER BY baseType;'" 2>/dev/null | while read line; do
    echo -e "  ${GREEN}$line${NC}"
done

echo ""
echo -e "${YELLOW}=== 游戏进度 ===${NC}"
adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT * FROM GameProgress;'" 2>/dev/null || echo "无进度数据"

echo ""
echo -e "${YELLOW}=== 重复徽章检查 ===${NC}"
DUPLICATES=$(adb shell "run-as com.cryallen.tigerfire sqlite3 /data/data/com.cryallen.tigerfire/databases/tigerfire.db 'SELECT baseType, variant, COUNT(*) as count FROM Badge GROUP BY baseType, variant HAVING count > 1;'" 2>/dev/null)

if [ -z "$DUPLICATES" ]; then
    echo -e "${GREEN}✓ 没有发现重复徽章${NC}"
else
    echo -e "${RED}✗ 发现重复徽章:${NC}"
    echo "$DUPLICATES"
fi

echo ""
echo -e "${BLUE}======================================"
echo "验证完成"
echo -e "======================================${NC}"
