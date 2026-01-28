package com.cryallen.tigerfire.presentation.parent

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ParentViewModel Android 平台实现
 *
 * 提供 Android 特定的实际实现
 */

/**
 * 获取当前日期字符串（格式：yyyy-MM-dd）
 */
actual fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}
