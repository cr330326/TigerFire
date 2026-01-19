package com.cryallen.tigerfire.data.resource

/**
 * 资源路径提供者
 *
 * 使用 expect/actual 模式，在各平台实现不同的资源路径获取逻辑
 * 用于获取 Lottie 动画、MP4 视频、音频文件的本地路径
 */
expect class ResourcePathProvider {

    /**
     * 获取 Lottie 动画文件的路径
     *
     * @param name Lottie 文件名（不含扩展名）
     * @return 文件路径
     */
    fun getLottiePath(name: String): String

    /**
     * 获取 MP4 视频文件的路径
     *
     * @param name 视频文件名（不含扩展名）
     * @return 文件路径
     */
    fun getVideoPath(name: String): String

    /**
     * 获取音频文件的路径
     *
     * @param name 音频文件名（不含扩展名）
     * @return 文件路径
     */
    fun getAudioPath(name: String): String
}
