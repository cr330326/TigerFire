package com.cryallen.tigerfire.component

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 震动反馈管理器
 *
 * 为用户提供触觉反馈，增强交互体验
 * 专为儿童友好设计，使用温和的震动强度
 *
 * @param context Android Context
 */
class HapticManager(private val context: Context) {

    companion object {
        /** 单例实例 */
        @Volatile
        private var INSTANCE: HapticManager? = null

        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): HapticManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HapticManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * 检查设备是否支持震动
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() == true
    }

    /**
     * 点击反馈 - 轻触震动
     *
     * 用于：按钮点击、场景图标点击等常规交互
     */
    fun click() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+ 使用 VibrationEffect
            val effect = VibrationEffect.createOneShot(
                10, // 10ms 持续时间（温和）
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }

    /**
     * 成功反馈 - 双轻震动
     *
     * 用于：获得徽章、完成任务、解锁场景等
     */
    fun success() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 两次短震动，间隔 50ms
            val timings = longArrayOf(0, 30, 50, 30)
            val amplitudes = intArrayOf(
                0,
                VibrationEffect.DEFAULT_AMPLITUDE,
                0,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            // 旧版本使用简单的震动模式
            vibrator?.vibrate(longArrayOf(0, 30, 50, 30), -1)
        }
    }

    /**
     * 错误反馈 - 长震动
     *
     * 用于：验证失败、操作错误等
     */
    fun error() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 较长的震动
            val effect = VibrationEffect.createOneShot(
                50, // 50ms 持续时间
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    /**
     * 徽章获得反馈 - 庆祝震动模式
     *
     * 用于：获得新徽章时
     */
    fun badge() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 三次短震动，营造庆祝感
            val timings = longArrayOf(0, 25, 40, 25, 40, 40)
            val amplitudes = intArrayOf(
                0,
                VibrationEffect.DEFAULT_AMPLITUDE,
                0,
                VibrationEffect.DEFAULT_AMPLITUDE,
                0,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 25, 40, 25, 40, 40), -1)
        }
    }

    /**
     * 拖拽反馈 - 持续轻震动
     *
     * 用于：拖拽操作时
     */
    fun drag() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 非常短的震动，用于拖拽反馈
            val effect = VibrationEffect.createOneShot(
                8, // 8ms - 非常轻的震动
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(8)
        }
    }

    /**
     * 吸附成功反馈 - 双击震动
     *
     * 用于：拖拽物体吸附到位时
     */
    fun snap() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 两次快速短震动
            val timings = longArrayOf(0, 15, 20, 15)
            val amplitudes = intArrayOf(
                0,
                VibrationEffect.DEFAULT_AMPLITUDE,
                0,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 15, 20, 15), -1)
        }
    }

    /**
     * 全部完成反馈 - 庆祝震动模式
     *
     * 用于：收集齐所有徽章、完成所有任务时
     */
    fun allCompleted() {
        if (!hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 渐强式震动模式
            val timings = longArrayOf(0, 20, 30, 30, 30, 40, 30, 50)
            val amplitudes = intArrayOf(
                0,
                80,
                0,
                120,
                0,
                160,
                0,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 20, 30, 30, 30, 40, 30, 50), -1)
        }
    }

    /**
     * 取消所有震动
     */
    fun cancel() {
        vibrator?.cancel()
    }
}

/**
 * Compose 记忆辅助函数
 *
 * 在 Composable 中使用 remember 获取 HapticManager 实例
 */
@Composable
fun rememberHapticManager(): HapticManager {
    val context = LocalContext.current
    return remember { HapticManager.getInstance(context) }
}

/**
 * Context 扩展函数
 *
 * 便捷获取 HapticManager 实例
 */
fun Context.getHapticManager(): HapticManager {
    return HapticManager.getInstance(this)
}

/**
 * Android Haptic 扩展函数
 */
fun Context.performHapticFeedback(type: HapticType) {
    val hapticManager = getHapticManager()
    when (type) {
        HapticType.CLICK -> hapticManager.click()
        HapticType.SUCCESS -> hapticManager.success()
        HapticType.ERROR -> hapticManager.error()
        HapticType.BADGE -> hapticManager.badge()
        HapticType.DRAG -> hapticManager.drag()
        HapticType.SNAP -> hapticManager.snap()
        HapticType.ALL_COMPLETED -> hapticManager.allCompleted()
    }
}

/**
 * 震动反馈类型
 */
enum class HapticType {
    /** 点击反馈 */
    CLICK,
    /** 成功反馈 */
    SUCCESS,
    /** 错误反馈 */
    ERROR,
    /** 徽章获得反馈 */
    BADGE,
    /** 拖拽反馈 */
    DRAG,
    /** 吸附成功反馈 */
    SNAP,
    /** 全部完成反馈 */
    ALL_COMPLETED
}
