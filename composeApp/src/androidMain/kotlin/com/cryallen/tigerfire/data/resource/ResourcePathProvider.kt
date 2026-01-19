package com.cryallen.tigerfire.data.resource

/**
 * Android 平台特定的资源路径提供者实现
 *
 * Android 使用 assets 目录存放资源，路径格式为：file:///android_asset/...
 */
actual class ResourcePathProvider {

    /**
     * 获取 Lottie 动画文件的路径
     *
     * @param name Lottie 文件名（不含扩展名）
     * @return Android assets 路径，如 "file:///android_asset/lottie/anim_truck_enter.json"
     */
    actual fun getLottiePath(name: String): String {
        return "file:///android_asset/lottie/$name.json"
    }

    /**
     * 获取 MP4 视频文件的路径
     *
     * @param name 视频文件名（不含扩展名）
     * @return Android assets 路径，如 "file:///android_asset/videos/firestation_extinguisher.mp4"
     */
    actual fun getVideoPath(name: String): String {
        return "file:///android_asset/videos/$name.mp4"
    }

    /**
     * 获取音频文件的路径
     *
     * @param name 音频文件名（不含扩展名）
     * @return Android assets 路径，如 "file:///android_asset/audio/click_firestation.mp3"
     */
    actual fun getAudioPath(name: String): String {
        return "file:///android_asset/audio/$name.mp3"
    }
}
