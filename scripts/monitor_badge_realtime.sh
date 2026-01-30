#!/bin/bash

# 实时徽章验证脚本
# 监控应用运行时的徽章保存行为

echo "======================================"
echo "徽章收集实时验证工具"
echo "======================================"
echo ""

# 设置颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}正在启动实时监控...${NC}"
echo ""
echo -e "${YELLOW}监控说明：${NC}"
echo "- 绿色: 徽章保存成功"
echo "- 红色: 可能的重复保存"
echo "- 蓝色: 进度更新"
echo "- 黄色: 重要信息"
echo ""
echo -e "${CYAN}请在手机上进行以下操作：${NC}"
echo "1. 进入消防站场景"
echo "2. 点击任意设备观看视频"
echo "3. 观察日志输出"
echo ""
echo "按 Ctrl+C 停止监控"
echo "======================================"
echo ""

# 清空日志
adb logcat -c 2>/dev/null

# 定义变量追踪徽章
declare -A badge_count
total_badges=0
last_badge=""

# 监控日志
adb logcat | grep -E "DEBUG handleVideoCompleted|saveProgressWithBadge|Badge" --line-buffered | while read line; do
    # 检测设备ID
    if echo "$line" | grep -q "DEBUG handleVideoCompleted: device ="; then
        device=$(echo "$line" | sed -n 's/.*device = \(.*\)/\1/p')
        echo -e "${BLUE}📹 检测到视频完成: ${device}${NC}"
    fi

    # 检测进度更新
    if echo "$line" | grep -q "fireStationCompletedItems ="; then
        items=$(echo "$line" | sed -n 's/.*fireStationCompletedItems = \(.*\)/\1/p')
        echo -e "${BLUE}📊 进度更新: ${items}${NC}"
    fi

    # 检测徽章保存开始
    if echo "$line" | grep -q "START TRANSACTION"; then
        echo -e "${CYAN}🔄 开始保存徽章...${NC}"
    fi

    # 检测徽章ID
    if echo "$line" | grep -q "badge.id ="; then
        badge_id=$(echo "$line" | sed -n 's/.*badge.id = \(.*\)/\1/p')

        # 提取baseType和variant
        base_type=$(echo "$badge_id" | cut -d'_' -f1-2)
        variant=$(echo "$badge_id" | sed -n 's/.*_v\([0-9]\+\)_.*/\1/p')

        # 检查是否重复
        key="${base_type}_v${variant}"
        if [ "${badge_count[$key]}" ]; then
            echo -e "${RED}⚠️  警告: 检测到可能的重复徽章!${NC}"
            echo -e "${RED}   徽章: ${badge_id}${NC}"
            echo -e "${RED}   之前已保存: ${badge_count[$key]}${NC}"
        else
            badge_count[$key]="$badge_id"
            ((total_badges++))
            echo -e "${GREEN}✅ 徽章保存: ${badge_id}${NC}"
            echo -e "${GREEN}   类型: ${base_type}, 变体: v${variant}${NC}"
            echo -e "${YELLOW}   当前总数: ${total_badges}${NC}"
        fi

        last_badge="$badge_id"
    fi

    # 检测baseType
    if echo "$line" | grep -q "badge.baseType ="; then
        base_type=$(echo "$line" | sed -n 's/.*badge.baseType = \(.*\)/\1/p')
        echo -e "${CYAN}   基础类型: ${base_type}${NC}"
    fi

    # 检测事务提交
    if echo "$line" | grep -q "COMMIT TRANSACTION"; then
        echo -e "${GREEN}✓ 徽章保存完成${NC}"
        echo ""
    fi
done
