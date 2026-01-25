package com.cryallen.tigerfire.domain.model

import kotlinx.serialization.Serializable

/**
 * 错误类型枚举
 *
 * 定义应用中可能出现的各类非致命错误类型
 */
@Serializable
enum class ErrorType {
    /** 视频加载失败 */
    VIDEO_LOAD_FAILED,

    /** Lottie 动画解析失败 */
    LOTTIE_PARSE_FAILED,

    /** 数据库读取错误 */
    DATABASE_READ_ERROR,

    /** 数据库写入错误 */
    DATABASE_WRITE_ERROR,

    /** 内存警告 */
    MEMORY_WARNING,

    /** 资源未找到 */
    RESOURCE_NOT_FOUND,

    /** 网络错误 */
    NETWORK_ERROR,

    /** 音频播放失败 */
    AUDIO_PLAY_FAILED,

    /** 其他未知错误 */
    UNKNOWN
}
