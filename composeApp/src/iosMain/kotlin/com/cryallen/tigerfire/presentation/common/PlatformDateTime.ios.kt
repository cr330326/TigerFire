package com.cryallen.tigerfire.presentation.common

import platform.Foundation.*

/**
 * iOS 平台的日期/时间工具实现
 */
actual object PlatformDateTime {

    /**
     * 获取当前时间戳（毫秒）
     * 使用简单的 NSDate 方法获取时间戳
     */
    actual fun getCurrentTimeMillis(): Long {
        val date = NSDate()
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd HH:mm:ss"
        }
        val dateString = dateFormatter.stringFromDate(date)
        println("iOS Debug: Current date string = $dateString")

        // 简化实现：返回一个基于秒数的近似值
        // 注意：完整实现需要在 Swift 代码中使用 Date().timeIntervalSince1970
        return (NSDate().timeIntervalSinceReferenceDate * 1000).toLong() + 978307200000L
    }

    /**
     * 获取今日日期字符串（格式 "yyyy-MM-dd"）
     */
    actual fun getTodayDate(): String {
        val date = NSDate()
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd"
            locale = NSLocale("en_US")
        }
        return dateFormatter.stringFromDate(date) ?: ""
    }
}
