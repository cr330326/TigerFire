package com.cryallen.tigerfire.data.local

import com.cryallen.tigerfire.domain.model.CrashLogFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.fclose

/**
 * iOS 平台的日志文件管理器实现（简化版）
 *
 * 日志目录：临时目录/crash_logs/
 */
@OptIn(ExperimentalForeignApi::class)
actual class LogFileManager {

    companion object {
        /** 最大日志文件数量 */
        const val MAX_LOG_FILES = 20

        /** 单个日志文件最大大小 (10MB) */
        const val MAX_FILE_SIZE = 10 * 1024 * 1024L
    }

    /**
     * 日志目录路径
     * iOS: 临时目录/crash_logs/
     */
    actual val logsDirectory: String
        get() {
            val tempDir = NSTemporaryDirectory() ?: "/tmp"
            val logsDir = "$tempDir/crash_logs"
            val fileManager = NSFileManager.defaultManager

            // 创建目录（如果不存在）
            if (!fileManager.fileExistsAtPath(logsDir)) {
                fileManager.createDirectoryAtPath(
                    path = logsDir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }

            return logsDir
        }

    actual fun writeLog(fileName: String, content: String) {
        try {
            val filePath = "$logsDirectory/$fileName"
            val fileManager = NSFileManager.defaultManager

            // 检查文件大小
            if (fileManager.fileExistsAtPath(filePath)) {
                val attrs = fileManager.attributesOfItemAtPath(filePath, null)
                val fileSize = (attrs as? Map<*, *>)?.get("NSFileSize") as? Long ?: 0L

                if (fileSize > MAX_FILE_SIZE) {
                    println("Log file too large, skipping: $fileName ($fileSize bytes)")
                    return
                }
            }

            // 写入文件 - 使用 POSIX 文件 API
            val file = fopen(filePath, "w")
            if (file != null) {
                fputs(content, file)
                fclose(file)
                println("Log written successfully: $filePath")
                // 异步清理旧日志
                cleanupOldLogs()
            } else {
                println("Failed to open file for writing: $fileName")
            }
        } catch (e: Exception) {
            println("Failed to write log: ${e.message}")
        }
    }

    actual fun cleanupOldLogs() {
        try {
            val fileManager = NSFileManager.defaultManager
            val logsDir = logsDirectory

            if (!fileManager.fileExistsAtPath(logsDir)) {
                return
            }

            val files = fileManager.contentsOfDirectoryAtPath(logsDir, null)
            if (files == null) return

            val sortedFiles = (files as List<*>)
                .filterIsInstance<String>()
                .sortedByDescending { file ->
                    val filePath = "$logsDir/$file"
                    val attrs = fileManager.attributesOfItemAtPath(filePath, null) as? Map<*, *>
                    val modificationDate = attrs?.get("NSFileModificationDate") as? Double
                    modificationDate ?: 0.0
                }

            // 删除超过 MAX_LOG_FILES 的旧日志
            val filesToDelete = sortedFiles.drop(MAX_LOG_FILES)
            filesToDelete.forEach { fileName ->
                val filePath = "$logsDir/$fileName"
                fileManager.removeItemAtPath(filePath, null)
            }

            if (filesToDelete.isNotEmpty()) {
                println("Cleaned up ${filesToDelete.size} old log(s)")
            }
        } catch (e: Exception) {
            println("Failed to cleanup logs: ${e.message}")
        }
    }

    actual fun getLogFiles(): List<CrashLogFile> {
        return try {
            val fileManager = NSFileManager.defaultManager
            val logsDir = logsDirectory

            if (!fileManager.fileExistsAtPath(logsDir)) {
                return emptyList()
            }

            val files = fileManager.contentsOfDirectoryAtPath(logsDir, null)
                ?: return emptyList()

            (files as List<*>)
                .filterIsInstance<String>()
                .mapNotNull { fileName ->
                    val filePath = "$logsDir/$fileName"
                    val attrs = fileManager.attributesOfItemAtPath(filePath, null) as? Map<*, *>

                    CrashLogFile(
                        fileName = fileName,
                        filePath = filePath,
                        timestamp = parseTimestampFromFileName(fileName),
                        size = (attrs?.get("NSFileSize") as? Long) ?: 0L
                    )
                }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            println("Failed to get log files: ${e.message}")
            emptyList()
        }
    }

    actual fun clearAllLogs() {
        try {
            val fileManager = NSFileManager.defaultManager
            val logsDir = logsDirectory

            val files = fileManager.contentsOfDirectoryAtPath(logsDir, null)
            files?.forEach { fileName ->
                val filePath = "$logsDir/$fileName"
                fileManager.removeItemAtPath(filePath, null)
            }

            println("All logs cleared")
        } catch (e: Exception) {
            println("Failed to clear logs: ${e.message}")
        }
    }

    /**
     * 从文件名解析时间戳
     *
     * 文件名格式：crash_1735123456789_abc123.log 或 error_1735123456789.log
     */
    private fun parseTimestampFromFileName(fileName: String): Long {
        return try {
            val parts = fileName.split("_")
            if (parts.size >= 2) {
                val timestampStr = parts[1]
                timestampStr.toLongOrNull() ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}
