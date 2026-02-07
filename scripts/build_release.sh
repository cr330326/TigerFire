#!/bin/bash

# ============================================
# TigerFire Android Release 构建脚本
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  TigerFire Release 构建工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# ============================================
# 1. 检查 Keystore 配置
# ============================================
echo -e "${YELLOW}[1/6] 检查签名配置...${NC}"

if [ ! -f "keystore.properties" ]; then
    echo -e "${RED}❌ 未找到 keystore.properties 文件${NC}"
    echo ""
    echo "请按以下步骤配置签名："
    echo ""
    echo "1. 生成 Keystore 文件："
    echo -e "${GREEN}   keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000${NC}"
    echo ""
    echo "2. 复制配置文件模板："
    echo -e "${GREEN}   cp keystore.properties.example keystore.properties${NC}"
    echo ""
    echo "3. 编辑 keystore.properties 并填入正确的密码"
    echo ""
    exit 1
fi

if [ ! -f "release.keystore" ] && ! grep -q "^storeFile=.*/" keystore.properties; then
    STORE_FILE=$(grep "^storeFile=" keystore.properties | cut -d'=' -f2)
    if [ ! -f "$STORE_FILE" ]; then
        echo -e "${RED}❌ 未找到 Keystore 文件: $STORE_FILE${NC}"
        echo ""
        echo "请先生成 Keystore 文件："
        echo -e "${GREEN}   keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000${NC}"
        echo ""
        exit 1
    fi
fi

echo -e "${GREEN}✓ 签名配置检查通过${NC}"
echo ""

# ============================================
# 2. 清理之前的构建
# ============================================
echo -e "${YELLOW}[2/6] 清理之前的构建...${NC}"
./gradlew clean
echo -e "${GREEN}✓ 清理完成${NC}"
echo ""

# ============================================
# 3. 分析资源大小
# ============================================
echo -e "${YELLOW}[3/6] 分析资源大小...${NC}"

ASSETS_DIR="composeApp/src/androidMain/assets"
if [ -d "$ASSETS_DIR" ]; then
    TOTAL_SIZE=$(du -sh "$ASSETS_DIR" | cut -f1)
    VIDEO_SIZE=$(du -sh "$ASSETS_DIR/videos" 2>/dev/null | cut -f1)
    AUDIO_SIZE=$(du -sh "$ASSETS_DIR/audio" 2>/dev/null | cut -f1)
    LOTTIE_SIZE=$(du -sh "$ASSETS_DIR/lottie" 2>/dev/null | cut -f1)

    echo "  资源目录总大小: $TOTAL_SIZE"
    echo "    - 视频文件: $VIDEO_SIZE"
    echo "    - 音频文件: $AUDIO_SIZE"
    echo "    - Lottie动画: $LOTTIE_SIZE"
fi
echo -e "${GREEN}✓ 资源分析完成${NC}"
echo ""

# ============================================
# 4. 构建 Release APK
# ============================================
echo -e "${YELLOW}[4/6] 构建 Release APK...${NC}"
echo "  这可能需要几分钟时间..."

./gradlew :composeApp:assembleRelease \
    --no-daemon \
    --stacktrace

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ APK 构建成功${NC}"
else
    echo -e "${RED}❌ APK 构建失败${NC}"
    exit 1
fi
echo ""

# ============================================
# 5. 查找并分析生成的 APK
# ============================================
echo -e "${YELLOW}[5/6] 分析生成的 APK...${NC}"

APK_PATH=$(find composeApp/build/outputs/apk/release -name "*.apk" | head -1)

if [ -z "$APK_PATH" ]; then
    echo -e "${RED}❌ 未找到生成的 APK 文件${NC}"
    exit 1
fi

APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
APK_SIZE_BYTES=$(stat -f%z "$APK_PATH" 2>/dev/null || stat -c%s "$APK_PATH" 2>/dev/null)

echo "  APK 路径: $APK_PATH"
echo "  APK 大小: $APK_SIZE"

# 检查 APK 是否超过目标大小 (300MB)
if [ "$APK_SIZE_BYTES" -gt 314572800 ]; then
    echo -e "${YELLOW}  ⚠️  警告: APK 超过 300MB 目标大小${NC}"
else
    echo -e "${GREEN}  ✓ APK 大小在目标范围内 (≤300MB)${NC}"
fi
echo ""

# ============================================
# 6. APK 信息摘要
# ============================================
echo -e "${YELLOW}[6/6] 生成 APK 信息摘要...${NC}"

OUTPUT_DIR="composeApp/build/outputs/apk/release"
OUTPUT_FILE="$OUTPUT_DIR/TigerFire-Release-Info.txt"

cat > "$OUTPUT_FILE" << EOF
========================================
TigerFire Android Release APK 信息
========================================

构建时间: $(date '+%Y-%m-%d %H:%M:%S')
版本号: 1.0 (versionCode: 1)

APK 信息:
  文件名: $(basename "$APK_PATH")
  大小: $APK_SIZE
  路径: $(pwd)/$APK_PATH

构建配置:
  - 代码混淆: ✓ 启用
  - 资源压缩: ✓ 启用
  - 签名: ✓ 已配置

资源大小:
  总计: $TOTAL_SIZE
  视频: $VIDEO_SIZE
  音频: $AUDIO_SIZE
  Lottie: $LOTTIE_SIZE

安装说明:
1. 将 APK 传输到 Android 设备
2. 在设备上启用"未知来源"安装
3. 点击 APK 文件进行安装
4. 如需验证签名，可使用:
   apksigner verify --print-certs $(basename "$APK_PATH")

优化建议:
- 如需进一步减小 APK 大小，可考虑:
  1. 使用 App Bundle (AAB) 格式
  2. 压缩视频文件 (降低分辨率/比特率)
  3. 将部分资源改为在线下载
  4. 使用 PNG/WebP 优化工具

EOF

cat "$OUTPUT_FILE"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✓ Release APK 构建完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "APK 文件位置:"
echo -e "${GREEN}  $APK_PATH${NC}"
echo ""
echo "详细信息已保存到:"
echo -e "${GREEN}  $OUTPUT_FILE${NC}"
echo ""
