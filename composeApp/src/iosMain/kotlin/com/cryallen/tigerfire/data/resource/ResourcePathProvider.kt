package com.cryallen.tigerfire.data.resource

import platform.Foundation.NSBundle

/**
 * iOS 平台特定的资源路径提供者实现
 *
 * iOS 使用 Bundle.main 获取应用资源，路径格式为：Bundle.main.path(forResource:ofType:)
 */
actual class ResourcePathProvider {

    /**
     * 获取 Lottie 动画文件的路径
     *
     * @param name Lottie 文件名（不含扩展名）
     * @return Bundle 资源路径，如 "/path/to/app.app/lottie/anim_truck_enter.json"
     */
    actual fun getLottiePath(name: String): String {
        return getResourcePath("lottie", name, "json") ?: ""
    }

    /**
     * 获取 MP4 视频文件的路径
     *
     * @param name 视频文件名（不含扩展名）
     * @return Bundle 资源路径，如 "/path/to/app.app/videos/firestation_extinguisher.mp4"
     */
    actual fun getVideoPath(name: String): String {
        return getResourcePath("videos", name, "mp4") ?: ""
    }

    /**
     * 获取音频文件的路径
     *
     * @param name 音频文件名（不含扩展名）
     * @return Bundle 资源路径，如 "/path/to/app.app/audio/click_firestation.mp3"
     */
    actual fun getAudioPath(name: String): String {
        return getResourcePath("audio", name, "mp3") ?: ""
    }

    /**
     * iOS 辅助方法：从 Bundle 获取资源路径
     *
     * @param subpath 子目录（如 "lottie"、"videos"、"audio"）
     * @param name 文件名（不含扩展名）
     * @param ext 文件扩展名（如 "json"、"mp4"、"mp3"）
     * @return 资源完整路径，若不存在则返回 null
     */
    private fun getResourcePath(subpath: String, name: String, ext: String): String? {
        val bundlePath = "$subpath/$name.$ext"
        return NSBundle.mainBundle.pathForResource(bundlePath, ofType: null)
    }
}
