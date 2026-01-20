package com.cryallen.tigerfire.presentation.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android 平台的日期/时间工具实现
 */
actual object PlatformDateTime {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 获取当前时间戳（毫秒）
     */
    actual fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 获取今日日期字符串（格式 "yyyy-MM-dd"）
     */
    actual fun getTodayDate(): String {
        return dateFormat.format(Date())
    }
}
