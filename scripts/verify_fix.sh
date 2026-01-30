#!/bin/bash

# 快速验证修复效果脚本

echo "========================================"
echo "  验证修复后的徽章数据"
echo "========================================"
echo ""

# 导出数据库
echo "📥 导出数据库..."
cd /Users/vsh9p8q/Personal/Project/TigerTruck/TigerFire
adb exec-out run-as com.cryallen.tigerfire cat databases/TigerFire.db > exported_db_fixed.db

echo "✅ 数据库已导出"
echo ""

# 查询徽章数量
echo "📊 徽章统计："
BADGE_COUNT=$(sqlite3 exported_db_fixed.db "SELECT COUNT(*) FROM Badge;")
echo "总数: $BADGE_COUNT"
echo ""

# 查询徽章详情
echo "📋 徽章详情："
sqlite3 exported_db_fixed.db "
SELECT
    scene,
    baseType,
    COUNT(*) as count,
    GROUP_CONCAT(variant) as variants
FROM Badge
GROUP BY scene, baseType
ORDER BY scene, baseType;" | column -t -s '|'

echo ""

# 查询游戏进度
echo "🎮 游戏进度："
echo "消防站已完成项:"
COMPLETED_ITEMS=$(sqlite3 exported_db_fixed.db "SELECT fireStationCompletedItems FROM GameProgress;")
echo "$COMPLETED_ITEMS"

# 解析并计数
ITEM_COUNT=$(echo "$COMPLETED_ITEMS" | grep -o '"[^"]*"' | wc -l)
echo "数量: $ITEM_COUNT"

echo ""
echo "森林已救援小羊:"
sqlite3 exported_db_fixed.db "SELECT forestRescuedSheep FROM GameProgress;"

echo ""
echo "场景状态:"
sqlite3 exported_db_fixed.db "SELECT sceneStatuses FROM GameProgress;"

echo ""
echo "========================================"

# 验证结果
echo ""
echo "🔍 验证结果："

if [ "$ITEM_COUNT" -eq "$BADGE_COUNT" ] && [ "$ITEM_COUNT" -gt 0 ]; then
    echo "✅ 成功！徽章数量($BADGE_COUNT)与完成项数量($ITEM_COUNT)一致"
elif [ "$ITEM_COUNT" -eq 1 ] && [ "$BADGE_COUNT" -gt 1 ]; then
    echo "❌ 失败！完成项只有1个，但徽章有$BADGE_COUNT个（Bug仍存在）"
else
    echo "⚠️  请检查数据：完成项=$ITEM_COUNT, 徽章=$BADGE_COUNT"
fi

echo ""
echo "========================================"
