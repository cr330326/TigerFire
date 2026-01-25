package com.cryallen.tigerfire.data.local

import com.cryallen.tigerfire.domain.model.CrashLogFile
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.fileExistsAtPath
import platform.Foundation.attributesOfItemAtPath
import platform.Foundation.createDirectoryAtURL
import platform.Foundation.URLsForDirectory
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.contentsOfDirectoryAtPath
import platform.Foundation.removeItemAtPath
import platform.Foundation.NSModificationDate
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileModificationDate
import platform.darwin.os_log
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringUsingEncoding
import platform.Foundation.create

/**
 * iOS 平台的日志文件管理器实现
 *
 * 日志目录：Application Support/crash_logs/
 */
actual class LogFileManager {

    /**
     * 日志目录路径
     * iOS: Application Support/crash_logs/
     */
    actual val logsDirectory: String
        get() {
            val fileManager = NSFileManager.defaultManager
            val urls = fileManager.URLsForDirectory(
                NSApplicationSupportDirectory,
                NSUserDomainMask
            )
            val appSupportDir = urls.first() as NSURL

            val logsDir = appSupportDir.URLByAppendingPathComponent("crash_logs")

            // 创建目录（如果不存在）
            if (!fileManager.fileExistsAtPath(logsDir!!.path!!)) {
                fileManager.createDirectoryAtURL(
                    logsDir,
                    true,
                    null,
                    null
                )
            }

            return logsDir.path!!
        }

    actual fun writeLog(fileName: String, content: String) {
        try {
            val fileManager = NSFileManager.defaultManager
            val filePath = "$logsDirectory/$fileName"

            // 检查文件大小
            if (fileManager.fileExistsAtPath(filePath)) {
                val attrs = fileManager.attributesOfItemAtPath(filePath, null)
                val fileSize = attrs?.objectForKey(NSFileSize) as? Long ?: 0L

                if (fileSize > LogFileManager.MAX_FILE_SIZE) {
                    os_log(OS_LOG_TYPE_DEFAULT, "Log file too large, skipping: $fileName ($fileSize bytes)")
                    return
                }
            }

            // 写入文件
            val success = (content as NSString).writeToFile(
                filePath,
                true,
                NSUTF8StringEncoding
            )

            if (success) {
                os_log(OS_LOG_TYPE_DEFAULT, "Log written successfully: $filePath")
                // 异步清理旧日志
                cleanupOldLogs()
            } else {
                os_log(OS_LOG_TYPE_ERROR, "Failed to write log: $fileName")
            }
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to write log: ${e.message}")
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
                ?.sortedByDescending { file ->
                    val filePath = "$logsDir/$file"
                    val attrs = fileManager.attributesOfItemAtPath(filePath, null)
                    val modificationDate = attrs?.objectForKey(NSFileModificationDate) as? NSDate
                    modificationDate?.timeIntervalSince1970
                        ?: 0.0
                } ?: return

            // 删除超过 MAX_LOG_FILES 的旧日志
            val filesToDelete = files.drop(LogFileManager.MAX_LOG_FILES)
            filesToDelete.forEach { fileName ->
                val filePath = "$logsDir/$fileName"
                val success = fileManager.removeItemAtPath(filePath, null)
                if (success) {
                    os_log(OS_LOG_TYPE_DEFAULT, "Deleted old log: $fileName")
                }
            }

            if (filesToDelete.isNotEmpty()) {
                os_log(OS_LOG_TYPE_DEFAULT, "Cleaned up ${filesToDelete.size} old log(s)")
            }
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to cleanup logs: ${e.message}")
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

            files.mapNotNull { fileName ->
                val filePath = "$logsDir/$fileName"
                val attrs = fileManager.attributesOfItemAtPath(filePath, null)

                CrashLogFile(
                    fileName = fileName as String,
                    filePath = filePath,
                    timestamp = parseTimestampFromFileName(fileName as String),
                    size = (attrs?.objectForKey(NSFileSize) as? Long) ?: 0L
                )
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to get log files: ${e.message}")
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

            os_log(OS_LOG_TYPE_DEFAULT, "All logs cleared")
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to clear logs: ${e.message}")
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
