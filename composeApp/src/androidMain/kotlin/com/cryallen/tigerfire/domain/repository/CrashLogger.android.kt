package com.cryallen.tigerfire.domain.repository

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Process
import android.util.Log
import com.cryallen.tigerfire.data.local.LogFileManager
import com.cryallen.tigerfire.domain.model.CrashInfo
import com.cryallen.tigerfire.domain.model.CrashLogFile
import com.cryallen.tigerfire.domain.model.NonFatalError
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import java.lang.ref.WeakReference

/**
 * Android 平台的崩溃日志记录器实现
 *
 * 功能：
 * - 使用 Thread.UncaughtExceptionHandler 捕获全局异常
 * - 注册 ComponentCallbacks2 监听内存警告
 * - 将崩溃日志写入 /data/data/com.cryallen.tigerfire/files/crash_logs/
 *
 * @property context Android 应用上下文（建议使用 Application Context）
 */
actual class CrashLogger(private val context: Context) {

    private val logFileManager = LogFileManager(context)
    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    // 保存当前场景和用户操作（使用 AtomicReference 保证线程安全）
    private val currentScene = AtomicReference<String>("UNKNOWN")
    private val lastAction = AtomicReference<String>("")

    // 设备 ID 缓存
    private val deviceId by lazy { retrieveDeviceId() }

    actual fun initialize() {
        Log.i(TAG, "Initializing CrashLogger...")

        // 1. 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(thread, throwable)
        }

        // 2. 注册内存警告监听（需要 Application Context）
        if (context is Application) {
            context.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onTrimMemory(level: Int) {
                    handleTrimMemory(level)
                }

                override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}
                override fun onLowMemory() {
                    handleLowMemory()
                }
            })
        }

        Log.i(TAG, "CrashLogger initialized successfully")
        Log.i(TAG, "Logs directory: ${logFileManager.logsDirectory}")
    }

    /**
     * 处理未捕获的异常
     */
    private fun handleCrash(thread: Thread, throwable: Throwable) {
        Log.e(TAG, "Handling crash in thread: ${thread.name}", throwable)

        try {
            // 获取内存信息
            val memoryInfo = getMemoryInfo()

            // 构建 CrashInfo
            val crashInfo = CrashInfo(
                appVersion = getAppVersion(),
                buildNumber = getBuildNumber(),
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                timestamp = System.currentTimeMillis(),
                crashType = throwable::class.simpleName ?: "Unknown",
                stackTrace = Log.getStackTraceString(throwable),
                scene = currentScene.get(),
                userAction = lastAction.get().takeIf { it.isNotEmpty() },
                memoryUsage = memoryInfo.usedMemory,
                deviceFreeMemory = memoryInfo.freeMemory,
                threadName = thread.name,
                deviceId = deviceId
            )

            // 记录崩溃
            logCrash(crashInfo)

            Log.i(TAG, "Crash log written successfully")

        } catch (e: Exception) {
            // 崩溃日志记录失败，至少记录到 logcat
            Log.e(TAG, "Failed to log crash", e)
            Log.e(TAG, "Original crash:", throwable)
        } finally {
            // 调用默认处理器（显示崩溃对话框）
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * 处理内存警告
     */
    private fun handleTrimMemory(level: Int) {
        val memoryInfo = getMemoryInfo()

        val levelDescription = when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            else -> "UNKNOWN($level)"
        }

        Log.w(TAG, "Memory trim: $levelDescription, Available: ${memoryInfo.freeMemory}MB")

        val error = NonFatalError(
            timestamp = System.currentTimeMillis(),
            errorType = com.cryallen.tigerfire.domain.model.ErrorType.MEMORY_WARNING,
            message = "Memory trim level: $levelDescription",
            details = mapOf(
                "level" to level.toString(),
                "availableMemory" to "${memoryInfo.freeMemory}MB",
                "totalMemory" to "${memoryInfo.totalMemory}MB",
                "usedMemory" to "${memoryInfo.usedMemory}MB"
            ),
            scene = currentScene.get()
        )

        logError(error)
    }

    /**
     * 处理低内存警告
     */
    private fun handleLowMemory() {
        Log.w(TAG, "System low memory")

        val memoryInfo = getMemoryInfo()

        val error = NonFatalError(
            timestamp = System.currentTimeMillis(),
            errorType = com.cryallen.tigerfire.domain.model.ErrorType.MEMORY_WARNING,
            message = "System low memory",
            details = mapOf(
                "availableMemory" to "${memoryInfo.freeMemory}MB",
                "totalMemory" to "${memoryInfo.totalMemory}MB",
                "usedMemory" to "${memoryInfo.usedMemory}MB"
            ),
            scene = currentScene.get()
        )

        logError(error)
    }

    actual fun logCrash(crashInfo: CrashInfo) {
        try {
            val json = crashInfo.toJson()
            val fileName = "crash_${crashInfo.timestamp}_${crashInfo.deviceId}.log"
            logFileManager.writeLog(fileName, json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log crash", e)
        }
    }

    actual fun logError(error: NonFatalError) {
        try {
            val json = Json {
                prettyPrint = true
            }.encodeToString(error)
            val fileName = "error_${error.timestamp}.log"
            logFileManager.writeLog(fileName, json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error", e)
        }
    }

    actual fun setCurrentScene(scene: String) {
        currentScene.set(scene)
        Log.d(TAG, "Scene changed to: $scene")
    }

    actual fun setLastAction(action: String) {
        lastAction.set(action)
        Log.d(TAG, "Last action: $action")
    }

    actual fun getLogFiles(): List<CrashLogFile> {
        return logFileManager.getLogFiles()
    }

    actual fun cleanupOldLogs() {
        logFileManager.cleanupOldLogs()
    }

    actual fun clearAllLogs() {
        logFileManager.clearAllLogs()
    }

    /**
     * 获取设备 ID（唯一标识）
     *
     * 使用 Settings.Secure.ANDROID_ID 的哈希值，避免泄露隐私
     */
    private fun retrieveDeviceId(): String {
        return try {
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            androidId.hashCode().toString(16)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get device ID", e)
            "unknown"
        }
    }

    /**
     * 获取 App 版本号
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * 获取构建号
     */
    private fun getBuildNumber(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * 获取内存信息
     */
    private fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return MemoryInfo(
            usedMemory = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024),
            freeMemory = memoryInfo.availMem / (1024 * 1024),
            totalMemory = memoryInfo.totalMem / (1024 * 1024),
            threshold = memoryInfo.threshold / (1024 * 1024)
        )
    }

    /**
     * 内存信息数据类
     */
    private data class MemoryInfo(
        val usedMemory: Long,    // 已使用内存（MB）
        val freeMemory: Long,    // 可用内存（MB）
        val totalMemory: Long,   // 总内存（MB）
        val threshold: Long      // 内存阈值（MB）
    )

    companion object {
        private const val TAG = "CrashLogger"

        /**
         * 初始化 CrashLogger 单例
         *
         * 应在 Application.onCreate() 中调用
         */
        fun initialize(context: Context): CrashLogger {
            val crashLogger = CrashLogger(context.applicationContext)
            crashLogger.initialize()
            CrashLoggerInstance.setInstance(crashLogger)
            return crashLogger
        }
    }
}
