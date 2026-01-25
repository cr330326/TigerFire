package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * 非致命错误模型
 *
 * 用于记录应用运行过程中的非致命错误，如资源加载失败、网络错误等
 *
 * @property timestamp 错误发生时间戳（毫秒）
 * @property errorType 错误类型
 * @property message 错误消息
 * @property details 额外的错误详情（键值对）
 * @property scene 发生错误的场景（可选）
 * @property stackTrace 堆栈跟踪（可选）
 */
@Serializable
data class NonFatalError(
    val timestamp: Long,
    val errorType: ErrorType,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val scene: String? = null,
    val stackTrace: String? = null
) {
    /**
     * 将 NonFatalError 序列化为 JSON 字符串
     */
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        private val json = Json {
            prettyPrint = true
        }

        /**
         * 从 JSON 字符串反序列化 NonFatalError
         */
        fun fromJson(jsonString: String): NonFatalError? = try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            null
        }

        /**
         * 创建一个视频加载失败的错误
         */
        fun videoLoadFailed(videoPath: String, reason: String, scene: String? = null): NonFatalError {
            return NonFatalError(
                timestamp = System.currentTimeMillis(),
                errorType = ErrorType.VIDEO_LOAD_FAILED,
                message = "Failed to load video: $reason",
                details = mapOf(
                    "videoPath" to videoPath,
                    "reason" to reason
                ),
                scene = scene
            )
        }

        /**
         * 创建一个 Lottie 动画解析失败的错误
         */
        fun lottieParseFailed(animationPath: String, reason: String, scene: String? = null): NonFatalError {
            return NonFatalError(
                timestamp = System.currentTimeMillis(),
                errorType = ErrorType.LOTTIE_PARSE_FAILED,
                message = "Failed to parse Lottie animation: $reason",
                details = mapOf(
                    "animationPath" to animationPath,
                    "reason" to reason
                ),
                scene = scene
            )
        }

        /**
         * 创建一个内存警告错误
         */
        fun memoryWarning(availableMemory: Long, totalMemory: Long, scene: String? = null): NonFatalError {
            return NonFatalError(
                timestamp = System.currentTimeMillis(),
                errorType = ErrorType.MEMORY_WARNING,
                message = "System memory warning",
                details = mapOf(
                    "availableMemory" to "${availableMemory}MB",
                    "totalMemory" to "${totalMemory}MB"
                ),
                scene = scene
            )
        }

        /**
         * 创建一个资源未找到错误
         */
        fun resourceNotFound(resourcePath: String, scene: String? = null): NonFatalError {
            return NonFatalError(
                timestamp = System.currentTimeMillis(),
                errorType = ErrorType.RESOURCE_NOT_FOUND,
                message = "Resource not found: $resourcePath",
                details = mapOf(
                    "resourcePath" to resourcePath
                ),
                scene = scene
            )
        }

        /**
         * 创建一个音频播放失败错误
         */
        fun audioPlayFailed(audioPath: String, reason: String, scene: String? = null): NonFatalError {
            return NonFatalError(
                timestamp = System.currentTimeMillis(),
                errorType = ErrorType.AUDIO_PLAY_FAILED,
                message = "Failed to play audio: $reason",
                details = mapOf(
                    "audioPath" to audioPath,
                    "reason" to reason
                ),
                scene = scene
            )
        }
    }
}
