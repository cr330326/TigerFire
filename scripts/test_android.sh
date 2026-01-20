#!/bin/bash
# TigerFire Android Test Script
# Run automated checks and manual test prompts

echo "=========================================="
echo "TigerFire Android Testing Script"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project root
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "📱 Project Root: $PROJECT_ROOT"
echo ""

# ========================================
# 1. Compilation Test
# ========================================
echo "=========================================="
echo "1. Compilation Test"
echo "=========================================="
./gradlew :composeApp:assembleDebug --quiet
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Android compilation successful${NC}"
else
    echo -e "${RED}✗ Android compilation failed${NC}"
    exit 1
fi
echo ""

# ========================================
# 2. APK Size Check
# ========================================
echo "=========================================="
echo "2. APK Size Check"
echo "=========================================="
APK_PATH="composeApp/build/outputs/apk/debug/composeApp-debug.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    APK_SIZE_BYTES=$(ls -l "$APK_PATH" | awk '{print $5}')
    APK_SIZE_MB=$((APK_SIZE_BYTES / 1024 / 1024))

    echo "APK Size: $APK_SIZE ($APK_SIZE_MB MB)"

    if [ $APK_SIZE_MB -le 300 ]; then
        echo -e "${GREEN}✓ APK size within target (≤300 MB)${NC}"
    else
        echo -e "${YELLOW}⚠ APK size exceeds 300 MB target${NC}"
    fi
else
    echo -e "${RED}✗ APK not found at $APK_PATH${NC}"
fi
echo ""

# ========================================
# 3. Asset Verification
# ========================================
echo "=========================================="
echo "3. Asset Verification"
echo "=========================================="

ASSETS_DIR="composeApp/src/androidMain/assets"
TOTAL_ASSETS=0
MISSING_ASSETS=0

# Check videos
echo "📹 Videos:"
for video in "firehydrant_cartoon.mp4" "fireladder_truck_cartoon.mp4" "firefighter_cartoon.mp4" "firenozzle_cartoon.mp4" "School_Fire_Safety_Knowledge.mp4" "rescue_sheep_1.mp4" "rescue_sheep_2.mp4"; do
    if [ -f "$ASSETS_DIR/videos/$video" ]; then
        echo -e "  ${GREEN}✓${NC} $video"
        ((TOTAL_ASSETS++))
    else
        echo -e "  ${RED}✗${NC} $video (missing)"
        ((MISSING_ASSETS++))
    fi
done

# Check Lottie
echo "🎨 Lottie Animations:"
for lottie in "anim_truck_enter.json" "anim_xiaohuo_wave.json"; do
    if [ -f "$ASSETS_DIR/lottie/$lottie" ]; then
        echo -e "  ${GREEN}✓${NC} $lottie"
        ((TOTAL_ASSETS++))
    else
        echo -e "  ${RED}✗${NC} $lottie (missing)"
        ((MISSING_ASSETS++))
    fi
done

# Check images
echo "🖼️ Images:"
for image in "bg_map.png" "bg_firestation.png" "bg_school.png" "bg_forest.png" "icon_badge_base.png" "btn_parent.png" "btn_collection.png"; do
    if [ -f "$ASSETS_DIR/images/$image" ]; then
        echo -e "  ${GREEN}✓${NC} $image"
        ((TOTAL_ASSETS++))
    else
        echo -e "  ${RED}✗${NC} $image (missing)"
        ((MISSING_ASSETS++))
    fi
done

echo ""
echo "Asset Summary: $TOTAL_ASSETS found, $MISSING_ASSETS missing"
echo ""

# ========================================
# 4. Database Schema Check
# ========================================
echo "=========================================="
echo "4. Database Schema Verification"
echo "=========================================="
SCHEMA_DIR="composeApp/src/commonMain/sqldelight/com/cryallen/tigerfire/database"
SCHEMAS=0

for schema in "GameProgress.sq" "Badge.sq" "ParentSettings.sq"; do
    if [ -f "$SCHEMA_DIR/$schema" ]; then
        echo -e "${GREEN}✓${NC} $schema"
        ((SCHEMAS++))
    else
        echo -e "${RED}✗${NC} $schema (missing)"
    fi
done

if [ $SCHEMAS -eq 3 ]; then
    echo -e "${GREEN}✓ All database schemas present${NC}"
else
    echo -e "${RED}✗ Missing database schemas${NC}"
fi
echo ""

# ========================================
# 5. Code Statistics
# ========================================
echo "=========================================="
echo "5. Code Statistics"
echo "=========================================="

# Count Kotlin files
KOTLIN_FILES=$(find composeApp/src -name "*.kt" | wc -l | tr -d ' ')
echo "Kotlin files: $KOTLIN_FILES"

# Count Swift files
SWIFT_FILES=$(find iosApp -name "*.swift" | wc -l | tr -d ' ')
echo "Swift files: $SWIFT_FILES"

# Total lines of code
LOC=$(find composeApp/src iosApp -name "*.kt" -o -name "*.swift" | xargs wc -l | tail -1 | awk '{print $1}')
echo "Total LOC: $LOC"
echo ""

# ========================================
# 6. Manual Test Instructions
# ========================================
echo "=========================================="
echo "6. Manual Testing Required"
echo "=========================================="
echo -e "${YELLOW}⚠ Automated checks complete. Manual testing required:${NC}"
echo ""
echo "To install and test on Android device:"
echo ""
echo "1. Enable Developer Mode and USB Debugging on device"
echo "2. Connect device via USB"
echo "3. Install APK:"
echo "   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk"
echo ""
echo "4. Launch app and test:"
echo "   adb shell am start -n com.cryallen.tigerfire/.MainActivity"
echo ""
echo "5. View logs:"
echo "   adb logcat | grep TigerFire"
echo ""
echo "See Document/stage7_completion_report.md for full test checklist."
echo ""

# ========================================
# 7. Summary
# ========================================
echo "=========================================="
echo "Summary"
echo "=========================================="
echo "✓ Compilation: Successful"
echo "✓ Assets: $TOTAL_ASSETS present"
if [ $MISSING_ASSETS -gt 0 ]; then
    echo "⚠ Missing Assets: $MISSING_ASSETS"
fi
echo "✓ Database Schemas: $SCHEMAS/3"
echo "✓ Code Files: $((KOTLIN_FILES + SWIFT_FILES))"
echo ""
echo -e "${GREEN}=========================================="
echo "Stage 7.1-7.4: Ready for Manual Testing"
echo "==========================================${NC}"
