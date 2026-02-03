package com.cryallen.tigerfire.domain.repository

import com.cryallen.tigerfire.data.local.LogFileManager
import com.cryallen.tigerfire.domain.model.CrashInfo
import com.cryallen.tigerfire.domain.model.CrashLogFile
import com.cryallen.tigerfire.domain.model.NonFatalError
import com.cryallen.tigerfire.domain.utils.TimeUtils
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * iOS 平台的崩溃日志记录器实现（简化版）
 *
 * 功能：
 * - 简化的异常处理日志记录
 * - 将崩溃日志写入临时目录/crash_logs/
 *
 * 注意：由于 Kotlin/Native 的限制，NSSetUncaughtExceptionHandler 无法直接使用
 * 此实现仅提供基本的日志记录功能
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalAtomicApi::class)
actual class CrashLogger {

    private val logFileManager = LogFileManager()

    // 保存当前场景和用户操作（使用 AtomicReference 保证线程安全）
    private val currentScene = AtomicReference("UNKNOWN")
    private val lastAction = AtomicReference("")

    // 设备 ID 缓存
    private val deviceId by lazy { retrieveDeviceId() }

    actual fun initialize() {
        println("Initializing CrashLogger (simplified iOS version)...")

        // 简化版：仅打印初始化信息，不设置实际的异常处理器
        // 在 Kotlin/Native 中设置 C 函数指针比较复杂，
        // 实际项目中建议使用第三方崩溃报告库（如 Crashlytics）

        println("CrashLogger initialized successfully")
        println("Logs directory: ${logFileManager.logsDirectory}")
    }

    /**
     * 处理未捕获的异常
     */
    private fun handleException(exception: platform.Foundation.NSException) {
        println("Handling exception: ${exception.name}")

        try {
            val crashInfo = CrashInfo(
                appVersion = getAppVersion(),
                buildNumber = getBuildNumber(),
                deviceModel = getDeviceModel(),
                osVersion = getOSVersion(),
                timestamp = TimeUtils.getCurrentTimeMillis(),
                crashType = exception.name ?: "Unknown",
                stackTrace = "", // 简化版不收集堆栈信息
                scene = currentScene.load(),
                userAction = if (lastAction.load().length > 0) lastAction.load() else null,
                memoryUsage = getMemoryUsage(),
                deviceFreeMemory = getFreeMemory(),
                threadName = null,
                deviceId = deviceId
            )

            // 记录崩溃
            logCrash(crashInfo)

            println("Crash log written successfully")

        } catch (e: Exception) {
            println("Failed to log crash: ${e.message}")
        }
    }

    actual fun logCrash(crashInfo: CrashInfo) {
        try {
            val jsonString = serializeCrashInfo(crashInfo)
            val fileName = "crash_${crashInfo.timestamp}_${crashInfo.deviceId}.log"
            logFileManager.writeLog(fileName, jsonString)
        } catch (e: Exception) {
            println("Failed to log crash: ${e.message}")
        }
    }

    actual fun logError(error: NonFatalError) {
        try {
            val jsonString = serializeError(error)
            val fileName = "error_${error.timestamp}.log"
            logFileManager.writeLog(fileName, jsonString)
        } catch (e: Exception) {
            println("Failed to log error: ${e.message}")
        }
    }

    actual fun setCurrentScene(scene: String) {
        currentScene.store(scene)
        println("Scene changed to: $scene")
    }

    actual fun setLastAction(action: String) {
        lastAction.store(action)
        println("Last action: $action")
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
     * 序列化崩溃信息为 JSON 字符串
     */
    private fun serializeCrashInfo(crashInfo: CrashInfo): String {
        return """{
  "appVersion": "${crashInfo.appVersion}",
  "buildNumber": "${crashInfo.buildNumber}",
  "deviceModel": "${crashInfo.deviceModel}",
  "osVersion": "${crashInfo.osVersion}",
  "timestamp": ${crashInfo.timestamp},
  "crashType": "${crashInfo.crashType}",
  "stackTrace": "${crashInfo.stackTrace}",
  "scene": "${crashInfo.scene ?: ""}",
  "userAction": "${crashInfo.userAction ?: ""}",
  "memoryUsage": ${crashInfo.memoryUsage},
  "deviceFreeMemory": ${crashInfo.deviceFreeMemory},
  "threadName": "${crashInfo.threadName ?: ""}",
  "deviceId": "${crashInfo.deviceId}"
}"""
    }

    /**
     * 序列化错误信息为 JSON 字符串
     */
    private fun serializeError(error: NonFatalError): String {
        val detailsJson = error.details.entries.joinToString(",") { (k, v) -> "\"$k\": \"$v\"" }
        return """{
  "timestamp": ${error.timestamp},
  "errorType": "${error.errorType.name}",
  "message": "${error.message}",
  "details": {$detailsJson},
  "scene": "${error.scene ?: ""}",
  "stackTrace": "${error.stackTrace ?: ""}"
}"""
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
            println("Failed to get device ID: ${e.message}")
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
