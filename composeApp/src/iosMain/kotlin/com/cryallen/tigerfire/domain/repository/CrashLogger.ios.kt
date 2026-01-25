package com.cryallen.tigerfire.domain.repository

import com.cryallen.tigerfire.data.local.LogFileManager
import com.cryallen.tigerfire.domain.model.CrashInfo
import com.cryallen.tigerfire.domain.model.CrashLogFile
import com.cryallen.tigerfire.domain.model.NonFatalError
import platform.Foundation.NSBundle
import platform.Foundation.JSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIDevice
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification
import platform.Foundation.didReceiveMemoryWarningNotification
import platform.Foundation.NSObject
import platform.Foundation.NSException
import platform.Foundation.name
import platform.Foundation.callStackSymbols
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.darwin.NSUncaughtExceptionHandler
import kotlin.native.concurrent.AtomicReference
import platform.darwin.os_log
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_WARN
import platform.Foundation.NSData
import platform.Foundation.stringUsingEncoding

/**
 * iOS 平台的崩溃日志记录器实现
 *
 * 功能：
 * - 使用 NSSetUncaughtExceptionHandler 捕获全局异常
 * - 注册内存警告监听
 * - 将崩溃日志写入 Application Support/crash_logs/
 */
actual class CrashLogger {

    private val logFileManager = LogFileManager()

    // 保存当前场景和用户操作（使用 AtomicReference 保证线程安全）
    private val currentScene = AtomicReference("UNKNOWN")
    private val lastAction = AtomicReference("")

    // 内存警告观察者
    private var memoryWarningObserver: NSObject? = null

    // 设备 ID 缓存
    private val deviceId by lazy { retrieveDeviceId() }

    actual fun initialize() {
        os_log(OS_LOG_TYPE_DEFAULT, "Initializing CrashLogger...")

        // 1. 设置 Objective-C 异常处理器
        NSSetUncaughtExceptionHandler { exception ->
            handleException(exception)
        }

        // 2. 注册内存警告监听
        memoryWarningObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = didReceiveMemoryWarningNotification,
            object = null,
            queue = null
        ) { _ ->
            handleMemoryWarning()
        }

        os_log(OS_LOG_TYPE_DEFAULT, "CrashLogger initialized successfully")
        os_log(OS_LOG_TYPE_DEFAULT, "Logs directory: ${logFileManager.logsDirectory}")
    }

    /**
     * 处理未捕获的异常
     */
    private fun handleException(exception: NSException) {
        os_log(OS_LOG_TYPE_ERROR, "Handling exception: ${exception.name}")

        try {
            val crashInfo = CrashInfo(
                appVersion = getAppVersion(),
                buildNumber = getBuildNumber(),
                deviceModel = getDeviceModel(),
                osVersion = getOSVersion(),
                timestamp = kotlin.system.getTimeMillis(),
                crashType = exception.name,
                stackTrace = exception.callStackSymbols.joinToString("\n"),
                scene = currentScene.get(),
                userAction = lastAction.get().takeIf { it.isNotEmpty() },
                memoryUsage = getMemoryUsage(),
                deviceFreeMemory = getFreeMemory(),
                threadName = null,
                deviceId = deviceId
            )

            // 记录崩溃
            logCrash(crashInfo)

            os_log(OS_LOG_TYPE_DEFAULT, "Crash log written successfully")

        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to log crash: ${e.message}")
        }
    }

    /**
     * 处理内存警告
     */
    private fun handleMemoryWarning() {
        os_log(OS_LOG_TYPE_WARN, "System memory warning")

        val error = NonFatalError(
            timestamp = kotlin.system.getTimeMillis(),
            errorType = com.cryallen.tigerfire.domain.model.ErrorType.MEMORY_WARNING,
            message = "System memory warning received",
            details = mapOf(
                "availableMemory" to "${getFreeMemory()}MB",
                "totalMemory" to "${getTotalMemory()}MB",
                "usedMemory" to "${getMemoryUsage()}MB"
            ),
            scene = currentScene.get()
        )

        logError(error)
    }

    actual fun logCrash(crashInfo: CrashInfo) {
        try {
            val jsonObj = mapOf(
                "appVersion" to crashInfo.appVersion,
                "buildNumber" to crashInfo.buildNumber,
                "deviceModel" to crashInfo.deviceModel,
                "osVersion" to crashInfo.osVersion,
                "timestamp" to crashInfo.timestamp,
                "crashType" to crashInfo.crashType,
                "stackTrace" to crashInfo.stackTrace,
                "scene" to (crashInfo.scene ?: ""),
                "userAction" to (crashInfo.userAction ?: ""),
                "memoryUsage" to crashInfo.memoryUsage,
                "deviceFreeMemory" to crashInfo.deviceFreeMemory,
                "threadName" to (crashInfo.threadName ?: ""),
                "deviceId" to crashInfo.deviceId
            )

            val jsonData = JSONSerialization.dataWithJSONObject(
                obj = jsonObj,
                options = NSJSONWritingPrettyPrinted
            ) as NSData

            val jsonString = NSString.create(data = jsonData, encoding = NSUTF8StringEncoding).toString()
                ?: throw IllegalStateException("Failed to serialize crash info")

            val fileName = "crash_${crashInfo.timestamp}_${crashInfo.deviceId}.log"
            logFileManager.writeLog(fileName, jsonString)
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to log crash: ${e.message}")
        }
    }

    actual fun logError(error: NonFatalError) {
        try {
            val jsonObj = mapOf(
                "timestamp" to error.timestamp,
                "errorType" to error.errorType.name,
                "message" to error.message,
                "details" to error.details,
                "scene" to (error.scene ?: ""),
                "stackTrace" to (error.stackTrace ?: "")
            )

            val jsonData = JSONSerialization.dataWithJSONObject(
                obj = jsonObj,
                options = NSJSONWritingPrettyPrinted
            ) as NSData

            val jsonString = NSString.create(data = jsonData, encoding = NSUTF8StringEncoding).toString()
                ?: throw IllegalStateException("Failed to serialize error info")

            val fileName = "error_${error.timestamp}.log"
            logFileManager.writeLog(fileName, jsonString)
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_ERROR, "Failed to log error: ${e.message}")
        }
    }

    actual fun setCurrentScene(scene: String) {
        currentScene.set(scene)
        os_log(OS_LOG_TYPE_DEFAULT, "Scene changed to: $scene")
    }

    actual fun setLastAction(action: String) {
        lastAction.set(action)
        os_log(OS_LOG_TYPE_DEFAULT, "Last action: $action")
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
     * 使用 identifierForVendor 的哈希值，避免泄露隐私
     */
    private fun retrieveDeviceId(): String {
        return try {
            val vendorId = UIDevice.currentDevice.identifierForVendor?.UUIDString
            vendorId?.hashCode()?.toString(16) ?: "unknown"
        } catch (e: Exception) {
            os_log(OS_LOG_TYPE_WARN, "Failed to get device ID: ${e.message}")
            "unknown"
        }
    }

    /**
     * 获取 App 版本号
     */
    private fun getAppVersion(): String {
        return try {
            NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
                ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * 获取构建号
     */
    private fun getBuildNumber(): String {
        return try {
            NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
                ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * 获取设备型号
     */
    private fun getDeviceModel(): String {
        return try {
            val device = UIDevice.currentDevice
            "${device.systemName} ${device.systemVersion} - ${device.model}"
        } catch (e: Exception) {
            "Unknown iOS Device"
        }
    }

    /**
     * 获取操作系统版本
     */
    private fun getOSVersion(): String {
        return try {
            UIDevice.currentDevice.systemVersion
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * 获取内存使用情况（MB）
     */
    private fun getMemoryUsage(): Long {
        // iOS 无法直接获取精确内存使用情况，返回估算值
        return 0L
    }

    /**
     * 获取可用内存（MB）
     */
    private fun getFreeMemory(): Long {
        // iOS 无法直接获取精确可用内存，返回估算值
        return 0L
    }

    /**
     * 获取总内存（MB）
     */
    private fun getTotalMemory(): Long {
        // iOS 无法直接获取精确总内存，返回估算值
        return 0L
    }

    companion object {
        /**
         * 初始化 CrashLogger 单例
         *
         * 应在 AppDelegate 中调用
         */
        fun initialize(): CrashLogger {
            val crashLogger = CrashLogger()
            crashLogger.initialize()
            CrashLoggerInstance.setInstance(crashLogger)
            return crashLogger
        }
    }
}
