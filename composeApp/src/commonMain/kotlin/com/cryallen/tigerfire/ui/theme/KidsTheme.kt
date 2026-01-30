package com.cryallen.tigerfire.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 儿童友好主题系统
 *
 * 设计原则：
 * - 明亮、可爱、圆润、卡通
 * - 色彩温暖不刺眼
 * - 触控目标≥100pt
 * - 字体大而清晰
 */

/**
 * 场景主题渐变色（统一3层）
 */
object ThemeGradients {
    /**
     * 消防站：暖色系（柔和红-温暖橙-明亮黄）
     */
    val FireStation = listOf(
        Color(0xFFFF6B6B),  // 柔和红（降低饱和度）
        Color(0xFFFFAA66),  // 温暖橙
        Color(0xFFFFE066)   // 明亮黄
    )

    /**
     * 学校：蓝色系（青绿蓝-天空蓝-淡蓝）
     */
    val School = listOf(
        Color(0xFF4ECDC4),  // 青绿蓝
        Color(0xFF7FCDFF),  // 天空蓝
        Color(0xFFB4E7FF)   // 淡蓝
    )

    /**
     * 森林：绿色系（翠绿-嫩绿-黄绿）
     */
    val Forest = listOf(
        Color(0xFF2ECC71),  // 翠绿
        Color(0xFF7FD98E),  // 嫩绿
        Color(0xFFB8F5A4)   // 黄绿
    )

    /**
     * 主地图：天空到草地过渡（天空蓝-粉蓝-嫩绿）
     */
    val Map = listOf(
        Color(0xFF87CEEB),  // 天空蓝
        Color(0xFFB0E0E6),  // 粉蓝
        Color(0xFF98FB98)   // 嫩绿
    )

    /**
     * 收藏：彩虹糖果色（粉紫-金黄-天蓝-嫩绿）
     */
    val Collection = listOf(
        Color(0xFFFF9FF3),  // 粉紫
        Color(0xFFFECA57),  // 金黄
        Color(0xFF48DBFB),  // 天蓝
        Color(0xFF98FB98)   // 嫩绿
    )

    /**
     * 欢迎页：海洋渐变（天蓝-青蓝-海绿）
     */
    val Welcome = listOf(
        Color(0xFF87CEEB),  // 天空蓝
        Color(0xFF4ECDC4),  // 青绿蓝
        Color(0xFF2A9D8F)   // 海洋绿
    )

    /**
     * 家长模式：成熟蓝绿（深蓝-青绿-浅青绿）
     */
    val Parent = listOf(
        Color(0xFF1A5F7A),  // 深蓝色
        Color(0xFF159895),  // 青绿色
        Color(0xFF57C5B6)   // 浅青绿色
    )
}

/**
 * 儿童友好字号系统（比标准字号大10-15%）
 */
object KidsTextSize {
    val Tiny = 18.sp      // 最小文字（提示信息）
    val Small = 20.sp     // 小字（副标题）
    val Medium = 24.sp    // 中字（正文）
    val Large = 32.sp     // 大字（按钮文字）
    val Huge = 48.sp      // 特大（场景标题）
    val Mega = 64.sp      // 超大（主标题）
}

/**
 * 圆角系统（统一圆润风格）
 */
object KidsShapes {
    val ExtraSmall = RoundedCornerShape(12.dp)   // 小按钮、标签
    val Small = RoundedCornerShape(16.dp)        // 普通按钮
    val Medium = RoundedCornerShape(24.dp)       // 卡片
    val Large = RoundedCornerShape(32.dp)        // 大卡片
    val ExtraLarge = RoundedCornerShape(48.dp)   // 特大卡片
    val Circle = CircleShape                     // 圆形按钮
}

/**
 * 阴影系统（增强立体感）
 */
object KidsShadows {
    val Small = 6.dp       // 小组件
    val Medium = 12.dp     // 普通按钮
    val Large = 20.dp      // 重要按钮
    val ExtraLarge = 28.dp // 浮动元素
}

/**
 * 间距系统
 */
object KidsSpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
    val Huge = 48.dp
}

/**
 * 触控目标最小尺寸（儿童手指精度）
 */
object KidsTouchTarget {
    val Minimum = 100.dp    // 最小触控目标
    val Comfortable = 120.dp // 舒适触控目标
    val Large = 150.dp      // 大触控目标（重要按钮）
}

/**
 * 语义化颜色
 */
object SemanticColors {
    // 成功色（翠绿）
    val Success = Color(0xFF2ECC71)

    // 警告色（温暖橙）
    val Warning = Color(0xFFFFA726)

    // 错误色（柔和红）
    val Error = Color(0xFFFF6B6B)

    // 信息色（天空蓝）
    val Info = Color(0xFF4ECDC4)

    // 锁定态遮罩（半透明灰）
    val LockedOverlay = Color(0x99CCCCCC)

    // 徽章金色
    val BadgeGold = Color(0xFFFFD700)

    // 徽章银色
    val BadgeSilver = Color(0xFFC0C0C0)

    // 徽章铜色
    val BadgeBronze = Color(0xFFCD7F32)
}

/**
 * 警报效果配置（降低刺激性）
 */
object AlertConfig {
    // 红光闪烁最大透明度（降低至0.15，避免刺眼）
    const val MaxAlpha = 0.15f

    // 闪烁周期（ms）
    const val FlashPeriod = 3000L

    // 渐入渐出步长
    const val FadeSteps = 10

    // 每步延迟（ms）
    const val StepDelay = 50L
}

/**
 * 动画时长配置（统一流畅体验）
 */
object AnimationDuration {
    // 快速动画（点击反馈）
    const val Fast = 200

    // 标准动画（过渡效果）
    const val Normal = 300

    // 中速动画（复杂过渡）
    const val Medium = 500

    // 慢速动画（页面进入）
    const val Slow = 800
}

/**
 * 创建垂直渐变Brush（统一接口）
 */
fun createVerticalGradient(colors: List<Color>): Brush {
    return Brush.verticalGradient(
        colors = colors,
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )
}

/**
 * 创建径向渐变Brush（用于圆形元素）
 */
fun createRadialGradient(colors: List<Color>): Brush {
    return Brush.radialGradient(colors = colors)
}

/**
 * 创建线性渐变Brush（用于按钮等）
 */
fun createLinearGradient(colors: List<Color>): Brush {
    return Brush.linearGradient(colors = colors)
}
