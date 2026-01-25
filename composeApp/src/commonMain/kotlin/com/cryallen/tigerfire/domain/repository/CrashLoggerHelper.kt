package com.cryallen.tigerfire.domain.repository

import com.cryallen.tigerfire.domain.model.NonFatalError

/**
 * 崩溃日志辅助类
 *
 * 提供便捷的方法来记录错误和设置场景上下文
 */
object CrashLoggerHelper {

    /**
     * 记录非致命错误
     *
     * @param error 错误对象
     */
    fun logError(error: NonFatalError) {
        try {
            CrashLoggerInstance.getInstance().logError(error)
        } catch (e: Exception) {
            // 静默处理，避免日志记录失败导致应用崩溃
        }
    }

    /**
     * 记录视频加载失败错误
     *
     * @param videoPath 视频路径
     * @param reason 失败原因
     * @param scene 当前场景（可选）
     */
    fun logVideoLoadFailed(videoPath: String, reason: String, scene: String? = null) {
        logError(NonFatalError.videoLoadFailed(videoPath, reason, scene))
    }

    /**
     * 记录 Lottie 动画解析失败错误
     *
     * @param animationPath 动画路径
     * @param reason 失败原因
     * @param scene 当前场景（可选）
     */
    fun logLottieParseFailed(animationPath: String, reason: String, scene: String? = null) {
        logError(NonFatalError.lottieParseFailed(animationPath, reason, scene))
    }

    /**
     * 记录内存警告
     *
     * @param availableMemory 可用内存（MB）
     * @param totalMemory 总内存（MB）
     * @param scene 当前场景（可选）
     */
    fun logMemoryWarning(availableMemory: Long, totalMemory: Long, scene: String? = null) {
        logError(NonFatalError.memoryWarning(availableMemory, totalMemory, scene))
    }

    /**
     * 记录资源未找到错误
     *
     * @param resourcePath 资源路径
     * @param scene 当前场景（可选）
     */
    fun logResourceNotFound(resourcePath: String, scene: String? = null) {
        logError(NonFatalError.resourceNotFound(resourcePath, scene))
    }

    /**
     * 记录音频播放失败错误
     *
     * @param audioPath 音频路径
     * @param reason 失败原因
     * @param scene 当前场景（可选）
     */
    fun logAudioPlayFailed(audioPath: String, reason: String, scene: String? = null) {
        logError(NonFatalError.audioPlayFailed(audioPath, reason, scene))
    }

    /**
     * 设置当前场景
     *
     * @param scene 场景名称（如 "FireStation"、"School"、"Forest"）
     */
    fun setCurrentScene(scene: String) {
        try {
            CrashLoggerInstance.getInstance().setCurrentScene(scene)
        } catch (e: Exception) {
            // 静默处理
        }
    }

    /**
     * 设置用户最后的操作
     *
     * @param action 操作描述
     */
    fun setLastAction(action: String) {
        try {
            CrashLoggerInstance.getInstance().setLastAction(action)
        } catch (e: Exception) {
            // 静默处理
        }
    }

    /**
     * 捕获并记录异常
     *
     * 用于 try-catch 块中记录异常
     *
     * @param e 异常对象
     * @param scene 当前场景（可选）
     * @param context 附加上下文信息（可选）
     */
    fun logException(e: Exception, scene: String? = null, context: String? = null) {
        val error = NonFatalError(
            timestamp = System.currentTimeMillis(),
            errorType = com.cryallen.tigerfire.domain.model.ErrorType.UNKNOWN,
            message = "${e.javaClass.simpleName}: ${e.message}",
            details = buildMap {
                put("exceptionClass", e.javaClass.simpleName)
                put("exceptionMessage", e.message ?: "No message")
                context?.let { put("context", it) }
            },
            scene = scene,
            stackTrace = e.stackTraceToString()
        )
        logError(error)
    }

    /**
     * 安全执行代码块，捕获并记录异常
     *
     * @param scene 场景名称
     * @param block 要执行的代码块
     */
    inline fun <T> safeExecute(scene: String? = null, block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            logException(e, scene)
            null
        }
    }

    /**
     * 安全执行代码块，捕获并记录异常，提供默认值
     *
     * @param defaultValue 异常时返回的默认值
     * @param scene 场景名称
     * @param block 要执行的代码块
     */
    inline fun <T> safeExecuteOrDefault(
        defaultValue: T,
        scene: String? = null,
        block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            logException(e, scene)
            defaultValue
        }
    }
}
