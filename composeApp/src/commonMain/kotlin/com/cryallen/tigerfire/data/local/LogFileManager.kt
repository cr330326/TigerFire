package com.cryallen.tigerfire.data.local

import com.cryallen.tigerfire.domain.model.CrashLogFile

/**
 * 日志文件管理器
 *
 * 负责崩溃日志文件的读写、清理等操作
 * 使用 expect/actual 模式实现平台特定的文件操作
 *
 * 各平台实现职责：
 * - Android: 将日志写入 /data/data/com.cryallen.tigerfire/files/crash_logs/
 * - iOS: 将日志写入 Application Support/crash_logs/
 */
expect class LogFileManager {

    /**
     * 获取日志目录路径
     */
    val logsDirectory: String

    /**
     * 写入日志文件
     *
     * @param fileName 文件名（如 "crash_1735123456789_abc123.log"）
     * @param content 日志内容（JSON 格式）
     */
    fun writeLog(fileName: String, content: String)

    /**
     * 清理旧日志
     *
     * 删除超过保留数量的旧日志文件（默认保留最新 20 个）
     */
    fun cleanupOldLogs()

    /**
     * 获取所有日志文件列表
     *
     * @return 日志文件列表，按时间戳降序排列
     */
    fun getLogFiles(): List<CrashLogFile>

    /**
     * 清空所有日志
     */
    fun clearAllLogs()
}

/**
 * 日志文件管理器常量
 */
object LogFileManagerConstants {
    /**
     * 最大日志文件数量
     */
    const val MAX_LOG_FILES = 20

    /**
     * 单文件最大大小（100 KB）
     */
    const val MAX_FILE_SIZE = 100 * 1024L
}
