package com.cryallen.tigerfire.domain.utils

/**
 * Android 平台的时间工具实现
 */
actual object TimeUtils {
    actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
}
