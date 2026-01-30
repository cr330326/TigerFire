#!/bin/bash

# 徽章重复保存修复测试脚本
# 测试场景：快速点击同一设备，验证不会重复保存徽章

echo "======================================"
echo "徽章重复保存修复测试"
echo "======================================"
echo ""

# 设置颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "${YELLOW}测试说明：${NC}"
echo "1. 本次修复添加了三层防护机制："
echo "   - 视频播放期间禁用设备点击"
echo "   - 保存徽章时添加锁，防止并发保存"
echo "   - 保存完成后立即释放锁"
echo ""
echo "2. 测试步骤："
echo "   a) 清空数据库"
echo "   b) 进入消防站场景"
echo "   c) 点击灭火器设备，观看视频"
echo "   d) 视频完成后，检查收藏页面显示的徽章数量"
echo "   e) 重复观看同一视频，验证不会重复保存基础徽章"
echo ""
echo "3. 预期结果："
echo "   - 第一次观看：收藏页面显示 1 个徽章"
echo "   - 重复观看：收藏页面显示 2 个徽章（不同变体）"
echo "   - 观看4个设备：收藏页面显示 4 个基础徽章"
echo ""
echo "${YELLOW}测试核心链路：${NC}"
echo "✓ 消防站：4个设备 → 4个基础徽章 (fire_hydrant, ladder_truck, fire_extinguisher, water_hose)"
echo "✓ 学校：1个视频 → 1个基础徽章 (school)"
echo "✓ 森林：2只小羊 → 2个基础徽章 (forest_sheep1, forest_sheep2)"
echo "✓ 总计：7个不同类型的基础徽章"
echo ""
echo "${GREEN}修复内容：${NC}"
echo "1. FireStationViewModel:"
echo "   - 添加 savingDevices 保存锁"
echo "   - handleDeviceClicked: 检查视频播放和保存状态"
echo "   - handleVideoCompleted: try-finally 包裹保存逻辑"
echo ""
echo "2. ForestViewModel:"
echo "   - 添加 savingSheepIndices 保存锁"
echo "   - handleSheepClicked: 检查保存状态"
echo "   - handleRescueVideoCompleted: try-finally 包裹保存逻辑"
echo ""
echo "3. SchoolViewModel:"
echo "   - 添加 isSavingBadge 保存标志"
echo "   - handleVideoCompleted: try-finally 包裹保存逻辑"
echo ""
echo "${YELLOW}开始测试...${NC}"
echo ""

# 检查是否有运行中的模拟器
echo "正在检查模拟器状态..."
adb devices | grep -q "emulator" || adb devices | grep -q "device"

if [ $? -eq 0 ]; then
    echo "${GREEN}✓ 检测到已连接的设备${NC}"
else
    echo "${RED}✗ 未检测到设备，请先启动模拟器或连接真机${NC}"
    exit 1
fi

echo ""
echo "${YELLOW}手动测试步骤：${NC}"
echo "1. 运行应用：./gradlew installDebug"
echo "2. 清空数据库："
echo "   adb shell run-as com.cryallen.tigerfire rm -rf /data/data/com.cryallen.tigerfire/databases/"
echo "3. 重启应用"
echo "4. 进入消防站场景，点击灭火器"
echo "5. 观看视频完成后，进入收藏页面"
echo "6. 验证显示 1 个徽章"
echo "7. 返回消防站，再次点击灭火器观看"
echo "8. 进入收藏页面，验证显示 2 个徽章（不同变体）"
echo "9. 依次观看其他3个设备视频"
echo "10. 验证收藏页面显示 4 个基础徽章"
echo ""
echo "${GREEN}测试完成！${NC}"
echo ""
echo "${YELLOW}注意事项：${NC}"
echo "- 如果仍然出现重复徽章，请检查数据库事务是否正常"
echo "- 检查 CollectionViewModel 的验证逻辑是否正确"
echo "- 查看 logcat 日志中的 DEBUG 输出"
echo ""
echo "查看日志命令："
echo "adb logcat | grep -E 'DEBUG|Badge|saveProgressWithBadge'"
