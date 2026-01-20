package com.cryallen.tigerfire.presentation.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.cryallen.tigerfire.domain.model.SceneType

/**
 * iOS 音频管理器实现
 *
 * 通过 expect/actual 模式调用 Swift 实现
 * 使用 AVFoundation 框架，支持 AVAudioSession 多音频混音
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

    // ==================== 公开方法实现 ====================

    override fun playClickSound(scene: SceneType?) {
        val sceneValue = when (scene) {
            SceneType.FIRE_STATION -> 0
            SceneType.SCHOOL -> 1
            SceneType.FOREST -> 2
            null -> -1
        }
        IosAudioPlayerHelper.playClickSound(sceneValue)
    }

    override fun playVoice(voicePath: String) {
        IosAudioPlayerHelper.playVoice(voicePath)
    }

    override fun playSuccessSound() {
        IosAudioPlayerHelper.playSuccessSound()
    }

    override fun playHintSound() {
        IosAudioPlayerHelper.playHintSound()
    }

    override fun playDragSound() {
        IosAudioPlayerHelper.playDragSound()
    }

    override fun playSnapSound() {
        IosAudioPlayerHelper.playSnapSound()
    }

    override fun playBadgeSound() {
        IosAudioPlayerHelper.playBadgeSound()
    }

    override fun playAllCompletedSound() {
        IosAudioPlayerHelper.playAllCompletedSound()
    }

    override fun playAlertSound() {
        IosAudioPlayerHelper.playAlertSound()
    }

    override fun stopAlertSound() {
        IosAudioPlayerHelper.stopAlertSound()
    }

    override fun release() {
        IosAudioPlayerHelper.release()
    }
}

/**
 * iOS Audio Player Helper
 *
 * 通过 external 函数调用 Swift 实现
 */
internal object IosAudioPlayerHelper {

    external fun playClickSound(sceneValue: Int)
    external fun playVoice(voicePath: String)
    external fun playSuccessSound()
    external fun playHintSound()
    external fun playDragSound()
    external fun playSnapSound()
    external fun playBadgeSound()
    external fun playAllCompletedSound()
    external fun playAlertSound()
    external fun stopAlertSound()
    external fun release()
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
