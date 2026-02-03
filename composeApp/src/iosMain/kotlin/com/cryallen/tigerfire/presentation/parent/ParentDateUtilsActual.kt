package com.cryallen.tigerfire.presentation.parent

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

/**
 * ParentViewModel iOS 平台实现
 *
 * 提供 iOS 特定的实际实现
 */

/**
 * 获取当前日期字符串（格式：yyyy-MM-dd）
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getCurrentDate(): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    return dateFormatter.stringFromDate(NSDate())
}
