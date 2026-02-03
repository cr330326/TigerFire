package com.cryallen.tigerfire.domain.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS 平台的时间工具实现
 */
actual object TimeUtils {
    actual fun getCurrentTimeMillis(): Long {
        val currentTime = NSDate().timeIntervalSince1970
        return (currentTime * 1000).toLong()
    }
}
