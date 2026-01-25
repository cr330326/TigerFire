package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 崩溃日志文件信息
 *
 * 表示存储在设备上的崩溃日志文件
 *
 * @property fileName 文件名（如 "crash_1735123456789_abc123.log"）
 * @property filePath 文件完整路径
 * @property timestamp 时间戳（从文件名解析）
 * @property size 文件大小（字节）
 */
@Serializable
data class CrashLogFile(
    val fileName: String,
    val filePath: String,
    val timestamp: Long,
    val size: Long
) {
    /**
     * 获取人类可读的文件大小
     */
    fun getReadableSize(): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            else -> "${size / (1024 * 1024)}MB"
        }
    }

    /**
     * 判断是否为崩溃日志（以 "crash_" 开头）
     */
    fun isCrashLog(): Boolean = fileName.startsWith("crash_")

    /**
     * 判断是否为错误日志（以 "error_" 开头）
     */
    fun isErrorLog(): Boolean = fileName.startsWith("error_")
}
