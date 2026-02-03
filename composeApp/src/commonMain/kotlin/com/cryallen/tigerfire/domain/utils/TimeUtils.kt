package com.cryallen.tigerfire.domain.utils

/**
 * 时间工具类 - 跨平台实现
 *
 * 使用 expect/actual 模式获取当前时间戳（毫秒）
 * 用于替代 System.currentTimeMillis() 在 Kotlin/Native 中不可用的问题
 */
expect object TimeUtils {
    /**
     * 获取当前时间戳（毫秒）
     */
    fun getCurrentTimeMillis(): Long
}
