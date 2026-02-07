#!/bin/bash

# ============================================
# TigerFire 视频压缩脚本
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

VIDEOS_DIR="composeApp/src/androidMain/assets/videos"
BACKUP_DIR="composeApp/src/androidMain/assets/videos_backup"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  TigerFire 视频压缩工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# ============================================
# 检查 FFmpeg
# ============================================
echo -e "${YELLOW}[1/6] 检查 FFmpeg...${NC}"

if ! command -v ffmpeg &> /dev/null; then
    echo -e "${RED}❌ 未找到 FFmpeg${NC}"
    echo ""
    echo "请先安装 FFmpeg:"
    echo "  macOS: brew install ffmpeg"
    echo "  Ubuntu: sudo apt install ffmpeg"
    echo "  Windows: 从 https://ffmpeg.org 下载"
    echo ""
    exit 1
fi

FFMPEG_VERSION=$(ffmpeg -version | head -1)
echo -e "${GREEN}✓ $FFMPEG_VERSION${NC}"
echo ""

# ============================================
# 检查视频目录
# ============================================
echo -e "${YELLOW}[2/6] 检查视频文件...${NC}"

if [ ! -d "$VIDEOS_DIR" ]; then
    echo -e "${RED}❌ 未找到视频目录: $VIDEOS_DIR${NC}"
    exit 1
fi

VIDEO_FILES=("$VIDEOS_DIR"/*.mp4)
VIDEO_COUNT=${#VIDEO_FILES[@]}

if [ "$VIDEO_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ 未找到视频文件${NC}"
    exit 1
fi

TOTAL_SIZE=$(du -sh "$VIDEOS_DIR" | cut -f1)
echo "  找到 $VIDEO_COUNT 个视频文件"
echo "  当前总大小: $TOTAL_SIZE"
echo ""

# ============================================
# 备份原始视频
# ============================================
echo -e "${YELLOW}[3/6] 备份原始视频...${NC}"

if [ -d "$BACKUP_DIR" ]; then
    echo -e "${YELLOW}  ⚠️  备份目录已存在，跳过备份${NC}"
else
    mkdir -p "$BACKUP_DIR"
    cp -r "$VIDEOS_DIR"/* "$BACKUP_DIR"/
    BACKUP_SIZE=$(du -sh "$BACKUP_DIR" | cut -f1)
    echo -e "${GREEN}✓ 已备份到: $BACKUP_DIR ($BACKUP_SIZE)${NC}"
fi
echo ""

# ============================================
# 压缩配置
# ============================================
echo -e "${YELLOW}[4/6] 压缩视频文件...${NC}"
echo ""

# 压缩参数配置
# CRF: 恒定质量因子 (18-28, 值越小质量越高)
# PRESET: 压缩速度 (fast, medium, slow)
# AUDIO_BITRATE: 音频码率
CRF=${1:-28}
PRESET="medium"
AUDIO_BITRATE="96k"

echo "  压缩参数:"
echo "    - CRF (质量): $CRF (推荐 28)"
echo "    - Preset: $PRESET"
echo "    - 音频码率: $AUDIO_BITRATE"
echo ""

# 压缩统计
TOTAL_ORIGINAL_SIZE=0
TOTAL_COMPRESSED_SIZE=0

for video in "${VIDEO_FILES[@]}"; do
    if [ -f "$video" ]; then
        filename=$(basename "$video")
        original_size=$(stat -f%z "$video" 2>/dev/null || stat -c%s "$video" 2>/dev/null)
        TOTAL_ORIGINAL_SIZE=$((TOTAL_ORIGINAL_SIZE + original_size))

        original_size_mb=$(echo "scale=1; $original_size / 1048576" | bc)
        echo -e "${BLUE}压缩: $filename (${original_size_mb}M)${NC}"

        # 临时文件
        temp_file="${video%.mp4}_temp.mp4"

        # 执行压缩
        ffmpeg -i "$video" \
            -c:v libx264 \
            -crf "$CRF" \
            -preset "$PRESET" \
            -c:a aac \
            -b:a "$AUDIO_BITRATE" \
            -movflags +faststart \
            -y \
            "$temp_file" \
            -loglevel warning \
            -stats

        # 获取压缩后大小
        compressed_size=$(stat -f%z "$temp_file" 2>/dev/null || stat -c%s "$temp_file" 2>/dev/null)
        TOTAL_COMPRESSED_SIZE=$((TOTAL_COMPRESSED_SIZE + compressed_size))

        compressed_size_mb=$(echo "scale=1; $compressed_size / 1048576" | bc)
        saved_percent=$(echo "scale=1; ($original_size - $compressed_size) * 100 / $original_size" | bc)

        echo -e "  → 压缩后: ${compressed_size_mb}M (节省 ${saved_percent}%)"

        # 替换原文件
        mv "$temp_file" "$video"
        echo ""
    fi
done

echo -e "${GREEN}✓ 视频压缩完成${NC}"
echo ""

# ============================================
# 压缩结果统计
# ============================================
echo -e "${YELLOW}[5/6] 压缩结果统计...${NC}"
echo ""

SAVED_BYTES=$((TOTAL_ORIGINAL_SIZE - TOTAL_COMPRESSED_SIZE))
SAVED_MB=$(echo "scale=1; $SAVED_BYTES / 1048576" | bc)
SAVED_PERCENT=$(echo "scale=1; $SAVED_BYTES * 100 / $TOTAL_ORIGINAL_SIZE" | bc)

TOTAL_ORIGINAL_MB=$(echo "scale=1; $TOTAL_ORIGINAL_SIZE / 1048576" | bc)
TOTAL_COMPRESSED_MB=$(echo "scale=1; $TOTAL_COMPRESSED_SIZE / 1048576" | bc)

echo "  原始大小: ${TOTAL_ORIGINAL_MB}M"
echo "  压缩后大小: ${TOTAL_COMPRESSED_MB}M"
echo -e "  ${GREEN}节省: ${SAVED_MB}M (${SAVED_PERCENT}%)${NC}"
echo ""

# ============================================
# APK 大小预估
# ============================================
echo -e "${YELLOW}[6/6] APK 大小预估...${NC}"
echo ""

# 假设 APK 其他部分约 20MB
OTHER_SIZE_MB=20
ORIGINAL_APK_MB=$(echo "$TOTAL_ORIGINAL_MB + $OTHER_SIZE_MB" | bc)
NEW_APK_MB=$(echo "$TOTAL_COMPRESSED_MB + $OTHER_SIZE_MB" | bc)

echo "  预计 APK 大小变化:"
echo "    压缩前: ~${ORIGINAL_APK_MB}M"
echo "    压缩后: ~${NEW_APK_MB}M"
echo ""

# ============================================
# 完成
# ============================================
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✓ 视频压缩完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "备份位置: $BACKUP_DIR"
echo ""
echo "下一步:"
echo "  1. 测试应用确保视频播放正常"
echo "  2. 如果一切正常，可删除备份目录"
echo "  3. 运行 ./scripts/build_release.sh 构建 APK"
echo ""
echo "如需恢复原始视频:"
echo -e "${GREEN}  cp -r $BACKUP_DIR/* $VIDEOS_DIR/${NC}"
echo ""
