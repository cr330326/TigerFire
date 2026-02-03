package com.cryallen.tigerfire.presentation.audio

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.cryallen.tigerfire.data.resource.ResourcePathProvider
import com.cryallen.tigerfire.domain.model.SceneType

/**
 * iOS 音频管理器实现（简化版 - 占位符实现）
 *
 * 注意：这是一个占位符实现，仅用于编译通过
 * 实际音频播放需要在 Swift 侧实现
 */
class IosAudioManager (
    private val resourcePathProvider: ResourcePathProvider
) : AudioManager {

    companion object {
        private var INSTANCE: IosAudioManager? = null

        fun getInstance(resourcePathProvider: ResourcePathProvider): IosAudioManager {
            return INSTANCE ?: IosAudioManager(resourcePathProvider).also {
                INSTANCE = it
            }
        }

        fun clearInstance() {
            INSTANCE = null
        }
    }

    // ==================== 公开方法实现 ====================

    override fun playClickSound(scene: SceneType?) {
        println("IosAudioManager: playClickSound(scene=$scene)")
        // TODO: 实现实际的音频播放
    }

    override fun playVoice(voicePath: String) {
        println("IosAudioManager: playVoice(path=$voicePath)")
        // TODO: 实现实际的音频播放
    }

    override fun playSuccessSound() {
        println("IosAudioManager: playSuccessSound")
        // TODO: 实现实际的音频播放
    }

    override fun playHintSound() {
        println("IosAudioManager: playHintSound")
        // TODO: 实现实际的音频播放
    }

    override fun playDragSound() {
        println("IosAudioManager: playDragSound")
        // TODO: 实现实际的音频播放
    }

    override fun playSnapSound() {
        println("IosAudioManager: playSnapSound")
        // TODO: 实现实际的音频播放
    }

    override fun playBadgeSound() {
        println("IosAudioManager: playBadgeSound")
        // TODO: 实现实际的音频播放
    }

    override fun playAllCompletedSound() {
        println("IosAudioManager: playAllCompletedSound")
        // TODO: 实现实际的音频播放
    }

    override fun playAlertSound() {
        println("IosAudioManager: playAlertSound")
        // TODO: 实现实际的音频播放
    }

    override fun stopAlertSound() {
        println("IosAudioManager: stopAlertSound")
        // TODO: 实现实际的音频停止
    }

    override fun release() {
        println("IosAudioManager: release")
        // TODO: 实现实际的资源释放
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
    // 需要注入 ResourcePathProvider
    return IosAudioManager.getInstance(ResourcePathProvider())
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
