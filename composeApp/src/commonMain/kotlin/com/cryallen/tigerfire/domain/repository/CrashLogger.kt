package com.cryallen.tigerfire.domain.repository

import com.cryallen.tigerfire.domain.model.CrashInfo
import com.cryallen.tigerfire.domain.model.CrashLogFile
import com.cryallen.tigerfire.domain.model.NonFatalError

/**
 * 崩溃日志记录器接口
 *
 * 负责捕获应用崩溃和非致命错误，并将日志保存到本地文件系统
 * 使用 expect/actual 模式实现平台特定的崩溃捕获逻辑
 *
 * 各平台实现职责：
 * - Android: 使用 Thread.UncaughtExceptionHandler 捕获异常
 * - iOS: 使用 NSSetUncaughtExceptionHandler 捕获异常
 */
expect class CrashLogger {

    /**
     * 初始化崩溃日志系统
     *
     * 应在 Application.onCreate() (Android) 或 AppDelegate (iOS) 中调用
     * 设置全局异常处理器和内存警告监听
     */
    fun initialize()

    /**
     * 记录崩溃信息
     *
     * @param crashInfo 崩溃信息
     */
    fun logCrash(crashInfo: CrashInfo)

    /**
     * 记录非致命错误
     *
     * @param error 非致命错误
     */
    fun logError(error: NonFatalError)

    /**
     * 设置当前场景
     *
     * 用于在崩溃日志中记录用户当前所在的场景
     *
     * @param scene 场景名称（如 "FireStation"、"School"、"Forest"）
     */
    fun setCurrentScene(scene: String)

    /**
     * 设置用户最后的操作
     *
     * 用于在崩溃日志中记录用户崩溃前最后执行的操作
     *
     * @param action 操作描述（如 "点击设备按钮"、"播放视频"）
     */
    fun setLastAction(action: String)

    /**
     * 获取所有日志文件列表
     *
     * @return 日志文件列表，按时间戳降序排列
     */
    fun getLogFiles(): List<CrashLogFile>

    /**
     * 清理旧日志
     *
     * 删除超过保留数量的旧日志文件（默认保留最新 20 个）
     */
    fun cleanupOldLogs()

    /**
     * 清空所有日志
     */
    fun clearAllLogs()
}

/**
 * CrashLogger 单例管理器
 *
 * 用于在所有平台间共享 CrashLogger 实例
 */
object CrashLoggerInstance {
    @Volatile
    private var instance: CrashLogger? = null

    /**
     * 获取 CrashLogger 单例
     */
    fun getInstance(): CrashLogger {
        return instance ?: throw IllegalStateException("CrashLogger not initialized. Call initialize() first.")
    }

    /**
     * 设置单例实例（供平台实现使用）
     */
    fun setInstance(logger: CrashLogger) {
        instance = logger
    }
}
