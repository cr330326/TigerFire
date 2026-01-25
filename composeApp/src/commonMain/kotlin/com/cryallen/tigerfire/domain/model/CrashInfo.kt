package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * 崩溃信息模型
 *
 * 用于记录应用崩溃时的详细信息，包括设备信息、堆栈跟踪、内存状态等
 *
 * @property appVersion App 版本号
 * @property buildNumber 构建号
 * @property deviceModel 设备型号
 * @property osVersion 操作系统版本
 * @property timestamp 崩溃时间戳（毫秒）
 * @property crashType 崩溃类型（异常类名）
 * @property stackTrace 堆栈跟踪
 * @property scene 当前场景（可选）
 * @property userAction 用户最后的操作（可选）
 * @property memoryUsage 内存占用（MB）
 * @property deviceFreeMemory 设备可用内存（MB）
 * @property threadName 线程名称（可选）
 * @property deviceId 设备唯一标识（哈希值）
 */
@Serializable
data class CrashInfo(
    val appVersion: String,
    val buildNumber: String,
    val deviceModel: String,
    val osVersion: String,
    val timestamp: Long,
    val crashType: String,
    val stackTrace: String,
    val scene: String? = null,
    val userAction: String? = null,
    val memoryUsage: Long,
    val deviceFreeMemory: Long,
    val threadName: String? = null,
    val deviceId: String
) {
    /**
     * 将 CrashInfo 序列化为 JSON 字符串
     */
    fun toJson(): String = Json.encodeToString(this)

    companion object {
        private val json = Json {
            prettyPrint = true
        }

        /**
         * 从 JSON 字符串反序列化 CrashInfo
         */
        fun fromJson(jsonString: String): CrashInfo? = try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
