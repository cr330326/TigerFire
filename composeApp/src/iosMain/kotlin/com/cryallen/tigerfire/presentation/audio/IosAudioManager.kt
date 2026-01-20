package com.cryallen.tigerfire.presentation.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.cryallen.tigerfire.domain.model.SceneType
import platform.Foundation.*

/**
 * iOS 音频管理器实现
 *
 * 使用 AVFoundation 播放音效和语音
 * 注意：这是简化实现，完整的音频播放需要在 Xcode 中配置
 */
class IosAudioManager : AudioManager {

    companion object {
        private var INSTANCE: IosAudioManager? = null

        fun getInstance(): IosAudioManager {
            return INSTANCE ?: IosAudioManager().also {
                INSTANCE = it
            }
        }

        fun clearInstance() {
            INSTANCE = null
        }
    }

    // 音效资源映射
    private fun getSoundResourceName(type: SoundType, scene: SceneType? = null): String {
        return when (type) {
            SoundType.CLICK -> when (scene) {
                SceneType.FIRE_STATION -> "fire_click"
                SceneType.SCHOOL -> "school_click"
                SceneType.FOREST -> "forest_click"
                null -> "click"
            }
            SoundType.SUCCESS -> "success"
            SoundType.HINT -> "hint"
            SoundType.DRAG -> "drag"
            SoundType.SNAP -> "snap"
            SoundType.BADGE -> "badge"
            SoundType.ALL_COMPLETED -> "all_completed"
            SoundType.ALERT -> "alert"
            SoundType.VOICE -> "voice"
        }
    }

    override fun playClickSound(scene: SceneType?) {
        println("iOS Audio: Playing click sound for $scene")
        // 实际音频播放需要在 Swift/ObjC 中实现
        // 这里使用 NSLog 记录音频播放请求
        NSLog("Play click sound: ${getSoundResourceName(SoundType.CLICK, scene)}")
    }

    override fun playVoice(voicePath: String) {
        println("iOS Audio: Playing voice: $voicePath")
        NSLog("Play voice: $voicePath")
    }

    override fun playSuccessSound() {
        println("iOS Audio: Playing success sound")
        NSLog("Play success sound")
    }

    override fun playHintSound() {
        println("iOS Audio: Playing hint sound")
        NSLog("Play hint sound")
    }

    override fun playDragSound() {
        println("iOS Audio: Playing drag sound")
        NSLog("Play drag sound")
    }

    override fun playSnapSound() {
        println("iOS Audio: Playing snap sound")
        NSLog("Play snap sound")
    }

    override fun playBadgeSound() {
        println("iOS Audio: Playing badge sound")
        NSLog("Play badge sound")
    }

    override fun playAllCompletedSound() {
        println("iOS Audio: Playing all completed sound")
        NSLog("Play all completed sound")
    }

    override fun playAlertSound() {
        println("iOS Audio: Playing alert sound (looping)")
        NSLog("Play alert sound")
    }

    override fun stopAlertSound() {
        println("iOS Audio: Stopping alert sound")
        NSLog("Stop alert sound")
    }

    override fun release() {
        println("iOS Audio: Releasing audio resources")
        NSLog("Release audio resources")
    }
}

/**
 * iOS Audio 扩展函数
 */
@Composable
fun getAudioManager(): AudioManager {
    if (LocalInspectionMode.current) {
        return PreviewAudioManager()
    }
    return IosAudioManager.getInstance()
}

/**
 * 预览模式的 AudioManager 空实现
 */
private class PreviewAudioManager : AudioManager {
    override fun playClickSound(scene: SceneType?) {}
    override fun playVoice(voicePath: String) {}
    override fun playSuccessSound() {}
    override fun playHintSound() {}
    override fun playDragSound() {}
    override fun playSnapSound() {}
    override fun playBadgeSound() {}
    override fun playAllCompletedSound() {}
    override fun playAlertSound() {}
    override fun stopAlertSound() {}
    override fun release() {}
}
