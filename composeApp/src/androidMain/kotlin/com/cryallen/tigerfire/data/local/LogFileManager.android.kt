package com.cryallen.tigerfire.data.local

import android.content.Context
import android.util.Log
import com.cryallen.tigerfire.domain.model.CrashLogFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Android 平台的日志文件管理器实现
 *
 * 日志目录：/data/data/com.cryallen.tigerfire/files/crash_logs/
 *
 * @property context Android 应用上下文
 */
actual class LogFileManager(private val context: Context) {

    /**
     * 日志目录路径
     * Android: /data/data/com.cryallen.tigerfire/files/crash_logs/
     */
    actual val logsDirectory: String
        get() = File(context.filesDir, "crash_logs").absolutePath

    /**
     * 写锁，用于保护并发写入
     */
    private val writeLock = ReentrantLock()

    actual fun writeLog(fileName: String, content: String) {
        writeLock.withLock {
            try {
                val logsDir = File(logsDirectory).apply { mkdirs() }
                val logFile = File(logsDir, fileName)

                // 检查文件大小
                if (logFile.exists() && logFile.length() > LogFileManagerConstants.MAX_FILE_SIZE) {
                    Log.w(TAG, "Log file too large, skipping: ${logFile.name} (${logFile.length()} bytes)")
                    return
                }

                // 写入日志内容
                logFile.writeText(content, Charsets.UTF_8)
                Log.i(TAG, "Log written successfully: ${logFile.absolutePath} (${logFile.length()} bytes)")

                // 异步清理旧日志（不阻塞当前线程）
                CoroutineScope(Dispatchers.IO).launch {
                    cleanupOldLogs()
                }
            } catch (e: Exception) {
                // 日志写入失败，静默处理（避免递归崩溃）
                Log.e(TAG, "Failed to write log: $fileName", e)
            }
        }
    }

    actual fun cleanupOldLogs() {
        try {
            val logsDir = File(logsDirectory)
            if (!logsDir.exists()) {
                return
            }

            val logFiles = logsDir.listFiles()
                ?.sortedByDescending { it.lastModified() }
                ?: return

            // 删除超过 MAX_LOG_FILES 的旧日志
            val filesToDelete = logFiles.drop(LogFileManagerConstants.MAX_LOG_FILES)
            filesToDelete.forEach { file ->
                if (file.delete()) {
                    Log.i(TAG, "Deleted old log: ${file.name}")
                } else {
                    Log.w(TAG, "Failed to delete old log: ${file.name}")
                }
            }

            if (filesToDelete.isNotEmpty()) {
                Log.i(TAG, "Cleaned up ${filesToDelete.size} old log(s)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup logs", e)
        }
    }

    actual fun getLogFiles(): List<CrashLogFile> {
        return try {
            val logsDir = File(logsDirectory)
            if (!logsDir.exists()) {
                return emptyList()
            }

            logsDir.listFiles()?.map { file ->
                CrashLogFile(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    timestamp = parseTimestampFromFileName(file.name),
                    size = file.length()
                )
            }?.sortedByDescending { it.timestamp } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get log files", e)
            emptyList()
        }
    }

    actual fun clearAllLogs() {
        try {
            val logsDir = File(logsDirectory)
            logsDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Log.i(TAG, "Deleted log: ${file.name}")
                }
            }
            Log.i(TAG, "All logs cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear logs", e)
        }
    }

    /**
     * 从文件名解析时间戳
     *
     * 文件名格式：crash_1735123456789_abc123.log 或 error_1735123456789.log
     */
    private fun parseTimestampFromFileName(fileName: String): Long {
        return try {
            // 提取时间戳部分
            val parts = fileName.split("_")
            if (parts.size >= 2) {
                val timestampStr = parts[1]
                timestampStr.toLong()
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse timestamp from filename: $fileName")
            0L
        }
    }

    companion object {
        private const val TAG = "LogFileManager"
    }
}
